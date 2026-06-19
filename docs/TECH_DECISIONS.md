# Technical Decisions (ADR)

This document records the key architectural decisions made during the CitySeeker MVP development
using the **Architecture Decision Record (ADR)** format.

---

## ADR-0001: Trie-based Prefix Search for Cities

**Status:** Accepted  
**Date:** 2025  
**Context:** CitySeeker MVP

### Context

The application needs to search through 200k+ cities based on a prefix string. The search must be:

- Case-insensitive
- Responsive (UI updates with every character typed)
- Efficient (optimized for prefix matching, not full-text search)
- Alphabetically sorted (city name first, then country code)

The original approach used a database `LIKE` query, which performed adequately but did not meet
the "as responsive as possible" requirement from the challenge specification.

### Decision

Implement a custom **Trie (prefix tree)** data structure (`CityTrie.kt`) that indexes cities by both
their name and country code.

**Key design choices:**

- Each trie node stores a `MutableSet<CityModel>` of all cities matching the prefix up to that node
- The trie is populated once when data is loaded from the database
- Search is case-insensitive via lowercase normalization
- Results are sorted by `(name, country)` on each search

### Alternatives Considered

1. **SQL LIKE query** — Simple but O(n) scan, not optimal for prefix matching
2. **Room FTS (Full-Text Search)** — Good for full-text but heavier setup, less control over sorting
3. **In-memory sorted list + binary search** — Fast for sorted data but insertion overhead
4. **Elasticsearch/Algolia** — Overkill for local dataset

### Consequences

**Positive:**

- O(m) search time where m = prefix length (very fast)
- Case-insensitive search with no performance penalty
- Results pre-sorted at the trie level
- Minimal latency for UI updates (<1ms for typical queries)

**Negative:**

- Additional memory usage (~50MB for 200k cities with duplicate references per node)
- Custom code to maintain (no library support)
- Trie must be rebuilt if data changes
- CityModel objects are duplicated across multiple trie nodes

**Mitigations:**

- Lazy trie construction on first search
- Batch processing of database results
- Consider refactoring to store city references (IDs) instead of full objects in future iterations

### References

- `app/src/main/java/com/boa/test/city/seeker/data/source/CityTrie.kt`
- `app/src/main/java/com/boa/test/city/seeker/data/source/CityDataSourceImpl.kt:184-200`

---

## ADR-0002: Room + Retrofit over Ktor + Realm

**Status:** Accepted  
**Date:** 2025  
**Context:** CitySeeker MVP

### Context

The initial implementation attempted to use **Ktor Client** for networking and **Realm** for local
storage. However, performance issues were observed during development, particularly with:

- Large JSON file processing (200k+ city records)
- Database query performance for prefix search
- Memory consumption during data loading

### Decision

Replace Ktor with **Retrofit** and Realm with **Room**:

| Aspect        | Previous (Ktor + Realm) | Current (Retrofit + Room) |
|---------------|-------------------------|---------------------------|
| Network       | Ktor Client             | Retrofit 3.0.0            |
| Serialization | Kotlin Serialization    | Gson                      |
| Database      | Realm                   | Room (SQLite)             |
| JSON Parsing  | Manual                  | Gson Streaming API        |
| Compression   | None                    | Gzip (OkHttp interceptor) |

### Alternatives Considered

1. **Keep Ktor + Realm** — Familiar stack but performance issues unresolved
2. **Retrofit + Realm** — Hybrid approach, but Realm overhead persists
3. **Ktor + Room** — Possible but Retrofit has better ecosystem/compression support
4. **Apollo GraphQL** — Not applicable (REST API source)

### Consequences

**Positive:**

- 70% reduction in download size with Gzip compression
- Streaming JSON parsing via `JsonReader` (no full object deserialization)
- Room's `@Insert` with batch support for fast inserts
- Standard Android ecosystem tools with better community support
- Built-in caching via OkHttp `Cache`

**Negative:**

- Room requires more boilerplate (Entity, DAO, Database classes)
- Gson less type-safe than Kotlin Serialization
- Migration from Realm required data layer rewrite

### Implementation Details

```kotlin
// ApplicationModule.kt - OkHttp with Gzip compression
val gzipInterceptor = Interceptor { chain ->
    val request = chain.request().newBuilder()
        .addHeader("Accept-Encoding", "gzip")
        .build()
    chain.proceed(request)
}
builder.addInterceptor(gzipInterceptor) // 70% size reduction
```

```kotlin
// CityDataSourceImpl.kt - Streaming JSON parsing
reader.beginArray()
val batch = mutableListOf<CityEntity>()
while (reader.hasNext()) {
    val city = parseCity(reader)
    if (city.name.isNotBlank()) batch.add(city)
    if (batch.size >= LIMIT) {
        insertBatch(batch)
        batch.clear()
    }
}
```

### References

- `app/src/main/java/com/boa/test/city/seeker/di/ApplicationModule.kt:64-89`
- `app/src/main/java/com/boa/test/city/seeker/data/source/CityDataSourceImpl.kt:229-260`

---

## ADR-0003: Offline-First with Raw Resource Fallback

**Status:** Accepted  
**Date:** 2025  
**Context:** CitySeeker MVP

### Context

The application must work offline after the initial data download. The challenge requires:

- Data available on first launch (even without network)
- Persistent local copy after download
- Graceful degradation when network is unavailable

### Decision

Implement a **three-tier fallback strategy**:

1. **Primary:** Room database (fastest query)
2. **Secondary:** Cached JSON file in app cache directory
3. **Tertiary:** Raw resource file (`R.raw.cities`) bundled with APK

**Data flow:**

```
App Launch → Check DB → If empty → Check cache file → If missing → Download from API
                                                    ↓ If download fails
                                                    Load from raw/cities.json
```

### Alternatives Considered

1. **Network-only with error handling** — Simple but no offline support
2. **Room-only with sync** — Requires initial network, no fallback
3. **Cache-only (no DB)** — Simpler but no query optimization
4. **Pre-populated Room DB** — Larger APK, but instant startup

### Consequences

**Positive:**

- Zero-network startup (raw resource provides initial data)
- Fast queries after initial load (Room indexed columns)
- Cache file persists between app launches
- Graceful degradation with user-friendly offline indicator

**Negative:**

- Raw resource adds ~5MB to APK size
- Cache file + DB duplication (same data in two formats)
- Stale data risk (no automatic refresh mechanism)
- Manual cache invalidation logic required

### Implementation Details

```kotlin
// CityDataSourceImpl.kt - Fallback chain
return try {
    if (needDownload && cities.isEmpty()) {
        downloadCities(tempFile, cities)
    } else {
        cities.ifEmpty {
            processFile(tempFile)
            cityDatabase.cityDao().getAll().take(LIMIT)
        }
    }
} catch (e: Exception) {
    Timber.e("Error downloading cities: ${e.stackTraceToString()}")
    cities
}
```

**Raw resource fallback:**

```kotlin
val inputStream = context.resources.openRawResource(R.raw.cities)
inputStream.use { input ->
    FileOutputStream(tempFile).use { output ->
        input.copyTo(output)
    }
}
```

### References

- `app/src/main/java/com/boa/test/city/seeker/data/source/CityDataSourceImpl.kt:49-76`
- `app/src/main/java/com/boa/test/city/seeker/data/source/CityDataSourceImpl.kt:93-128`
- `app/src/main/res/raw/cities.json`

---

## ADR-0004: MVI with StateFlow (not Compose State)

**Status:** Accepted  
**Date:** 2025  
**Context:** CitySeeker MVP

### Context

The list screen manages complex UI state including:

- Loading indicator
- Error/offline messages
- Search query
- Favorite filter toggle
- City list data
- Scroll position

The state must be:

- Observable by Compose UI
- Testable with coroutines tests
- Granular (avoid unnecessary recomposition)
- Consistent across configuration changes

### Decision

Use **MVI pattern with StateFlow** for state management:

```kotlin
data class ListState(
    private val _loadingState: MutableStateFlow<Boolean> = MutableStateFlow(true),
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow(),
    private val _favoriteFilterState: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val favoriteFilterState: StateFlow<Boolean> = _favoriteFilterState.asStateFlow(),
    private val _textFilterState: MutableStateFlow<String> = MutableStateFlow(""),
    val textFilterState: StateFlow<String> = _textFilterState.asStateFlow(),
    private val _errorState: MutableStateFlow<String> = MutableStateFlow(""),
    val errorState: StateFlow<String> = _errorState.asStateFlow(),
    private val _cityList: MutableStateFlow<List<CityModel>> = MutableStateFlow(emptyList()),
    val cityList: StateFlow<List<CityModel>> = _cityList.asStateFlow(),
    private val _queryState: MutableStateFlow<String> = MutableStateFlow(""),
    val queryState: StateFlow<String> = _queryState.asStateFlow()
)
```

### Alternatives Considered

1. **Compose `mutableStateOf`** — Simpler but not lifecycle-aware
2. **LiveData** — Deprecated, less powerful operators
3. **MutableState<T>** — No coroutine integration
4. **SharedFlow** — Emitter-driven, not state-driven

### Consequences

**Positive:**

- Immutable public API (StateFlow) prevents accidental state mutation
- Granular state updates (only changed fields trigger recomposition)
- `collectAsState()` integration with Compose
- Testable with Turbine library
- Survives configuration changes via ViewModel

**Negative:**

- More boilerplate than `mutableStateOf`
- `@Suppress("ConstructorParameterNaming")` required for underscore convention
- QueryState bug: `queryState` exposes `_errorState` instead of `_queryState` (line 46 in
  ListContract.kt)

### Known Issue

```kotlin
// ListContract.kt:46 - Bug: queryState exposes errorState
val queryState: StateFlow<String> = _errorState.asStateFlow()
// Should be:
val queryState: StateFlow<String> = _queryState.asStateFlow()
```

### References

- `app/src/main/java/com/boa/test/city/seeker/presentation/feature/city/list/ListContract.kt`
- `app/src/main/java/com/boa/test/city/seeker/presentation/feature/city/list/ListViewModel.kt`
- `app/src/main/java/com/boa/test/city/seeker/domain/model/UiStateModel.kt`

---

## ADR-0005: DataStore for Favorites Persistence

**Status:** Accepted  
**Date:** 2025  
**Context:** CitySeeker MVP

### Context

User favorites must persist between app launches. The data is:

- Simple key-value pairs (city ID → favorited)
- Small volume (< 1000 entries typically)
- No relational queries needed
- Must survive app reinstall (user preference, not app data)

### Decision

Use **Preferences DataStore** for favorites storage:

```kotlin
// PreferenceRepository interface
interface PreferenceRepository {
    suspend fun toggleString(value: String)
    suspend fun getSetString(): Set<String>
}

// DataStore configuration
PreferenceDataStoreFactory.create(
    produceFile = {
        File(context.filesDir, "CitySeeker_pref.preferences_pb")
    }
)
```

**Storage format:**

```
Key: "favorites"
Value: Set<String> of city IDs (e.g., {"3844419", "5128581", "5368361"})
```

### Alternatives Considered

1. **Room database** — Overkill for simple key-value, requires schema migration
2. **SharedPreferences** — Synchronous API, deprecated for new projects
3. **SQLite directly** — Too low-level for simple storage
4. **Firebase Remote Config** — Requires network, not local storage
5. **Proto DataStore** — Type-safe but requires protobuf setup

### Consequences

**Positive:**

- Async API (coroutine-based)
- No schema migration needed
- Atomic read/write operations
- Survives app reinstall (files directory)
- Minimal setup (no DAO, no Entity)

**Negative:**

- No encryption by default (city IDs are not sensitive)
- Limited query capabilities (no "get all favorites with city details")
- File-based storage (not ideal for large datasets)
- Toggle logic requires reading full set, modifying, writing back

### Implementation Details

```kotlin
// PreferenceDataSourceImpl.kt
override suspend fun toggleString(value: String) {
    dataStore.edit { preferences ->
        val key = stringSetPreferencesKey("favorites")
        val current = preferences[key] ?: emptySet()
        preferences[key] = if (value in current) {
            current - value
        } else {
            current + value
        }
    }
}

override suspend fun getSetString(): Set<String> {
    return dataStore.data.map { preferences ->
        val key = stringSetPreferencesKey("favorites")
        preferences[key] ?: emptySet()
    }.first()
}
```

### References

- `app/src/main/java/com/boa/test/city/seeker/data/source/PreferenceDataSourceImpl.kt`
- `app/src/main/java/com/boa/test/city/seeker/data/source/PreferenceDataSource.kt`
- `app/src/main/java/com/boa/test/city/seeker/data/repository/PreferenceRepositoryImpl.kt`
- `app/src/main/java/com/boa/test/city/seeker/di/ApplicationModule.kt:190-214`

---

## Summary

| ADR  | Decision               | Rationale                                  | Trade-off                            |
|------|------------------------|--------------------------------------------|--------------------------------------|
| 0001 | Trie-based search      | O(m) prefix matching, responsive UI        | Memory overhead for node duplication |
| 0002 | Room + Retrofit        | Performance, Gzip compression, streaming   | More boilerplate than Realm          |
| 0003 | Offline-first fallback | Zero-network startup, graceful degradation | APK size increase, stale data risk   |
| 0004 | MVI + StateFlow        | Testable, granular recomposition           | More boilerplate than Compose State  |
| 0005 | DataStore favorites    | Async API, no migration needed             | No encryption, limited queries       |
