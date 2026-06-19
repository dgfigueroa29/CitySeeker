# Performance Optimization

This document identifies performance bottlenecks in CitySeeker and provides optimization
recommendations.

## Current Bottlenecks

### 1. Trie Memory Overhead

**Issue:** `CityTrie` stores duplicate `CityModel` objects across multiple nodes.

```
City "Denver, US":
  Root → D → e → n → v → e → r (6 nodes, 6 CityModel refs)
  Root → U → S (2 nodes, 2 CityModel refs)
```

**Impact:** ~50MB memory usage for 200k cities

**Optimization:**

```kotlin
// Current: stores full objects
private data class Node(
    val children: MutableMap<Char, Node> = mutableMapOf(),
    var cities: MutableSet<CityModel> = mutableSetOf()
)

// Optimized: store city IDs, resolve on search
private data class Node(
    val children: MutableMap<Char, Node> = mutableMapOf(),
    var cityIds: MutableSet<Long> = mutableSetOf()
)

// Resolve on search
fun search(prefix: String): List<CityModel> {
    val ids = getNodeIds(prefix)
    return ids.mapNotNull { cityMap[it] }
}
```

**Savings:** ~60% memory reduction (store IDs instead of full objects)

---

### 2. Paging Not Utilized

**Issue:** `CityPagingSource` exists but is not used in `ListScreen`. Full list rendered at once.

```kotlin
// Current: renders all cities
LazyColumn {
    items(cities.size) { index ->
        CityItem(cities[index])
    }
}
```

**Impact:**

- Slow initial render with 200k items
- High memory consumption
- Scroll jank on low-end devices

**Optimization:**

```kotlin
// Recommended: use Paging 3
@Composable
fun ListStateful(
    pagingItems: LazyPagingItems<CityModel>,
    // ...
) {
    LazyColumn {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.id }
        ) { index ->
            val city = pagingItems[index]
            city?.let { CityItem(it) }
        }
    }
}
```

**Savings:**

- 90% reduction in initial memory
- Smoother scroll performance
- Lazy loading of off-screen items

---

### 3. Recomposition Overhead

**Issue:** `ListStateful` reads `listState.cityList.collectAsState().value` which triggers full
recomposition on any list change.

```kotlin
// Current: full recomposition
val cities = listState.cityList.collectAsState().value
```

**Impact:**

- Entire list recomposes when any city changes
- Search typing causes unnecessary recompositions

**Optimization:**

```kotlin
// Use stable keys for items
items(
    count = cities.size,
    key = { index -> cities[index].id }
) { index ->
    val city = cities[index]
    CityItem(
        city = city,
        modifier = Modifier.composable(key(city.id)) {
            // Only recompose if this specific city changes
        }
    )
}

// Or use derivedStateOf
val displayedCities by remember {
    derivedStateOf {
        cities.filter { it.name.contains(query, ignoreCase = true) }
    }
}
```

---

### 4. Trie Built on Main Thread

**Issue:** `CityTrie` is populated during `mapCities()` which may run on main thread in some paths.

```kotlin
// CityDataSourceImpl.kt
override suspend fun mapCities(query: String, trie: CityTrie): List<CityModel> {
    // Trie built here
    var cities = trie.search(query)
        .map { it.copy(isFavorite = favorites.contains(it.id.toString())) }
    // ...
}
```

**Impact:** UI freeze during trie population

**Optimization:**

```kotlin
// Ensure trie is built on background thread
override suspend fun mapCities(query: String, trie: CityTrie): List<CityModel> {
    return withContext(Dispatchers.Default) {
        val favorites = preferenceDataSource.getSetString()
        trie.search(query)
            .map { it.copy(isFavorite = favorites.contains(it.id.toString())) }
    }
}
```

---

### 5. Database Query Performance

**Issue:** `searchCities()` uses `LIKE` query which is O(n) scan.

```kotlin
// CityDao.kt
@Query("SELECT * FROM cities WHERE name LIKE '%' || :query || '%'")
suspend fun searchCities(query: String): List<CityEntity>
```

**Impact:** Slow for large datasets without index

**Optimization:**

```kotlin
// Add FTS (Full-Text Search) support
@Fts4
@Entity(tableName = "cities_fts")
data class CityFts(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "country") val country: String
)

// Or use prefix query (more efficient)
@Query("SELECT * FROM cities WHERE name LIKE :query || '%' OR country LIKE :query || '%'")
suspend fun searchCitiesByPrefix(query: String): List<CityEntity>
```

---

### 6. Image Loading (Mapbox)

**Issue:** Mapbox SDK loads map tiles on every screen visit without caching configuration.

**Impact:** Network usage, battery drain

**Optimization:**

```kotlin
// Configure Mapbox cache
MapboxMapOptions {
    textureView(true)
    setCamera(CameraOptions {
        zoom(10.0)
    })
    setResourceOptions(ResourceOptions.Builder()
        .accessToken(BuildConfig.MAPBOX_TOKEN)
        .cacheSize(50_000_000L) // 50MB cache
        .build())
}
```

---

### 7. Startup Time

**Issue:** Cold start includes:

1. Room database initialization
2. JSON file processing
3. Trie construction
4. Network request

**Impact:** 3-5 second cold start on low-end devices

**Optimization:**

```kotlin
// 1. Use Baseline Profiles
// Generate with: ./gradlew :app:generateBaselineProfile

// 2. Lazy initialization
class CityDataSourceImpl @Inject constructor(
    // Lazy load database
    private val cityDatabase: Lazy<CityDatabase>,
    // ...
) {
    private val dao by lazy { cityDatabase.get().cityDao() }
}

// 3. Splash screen (already implemented)
// AndroidX SplashScreen API

// 4. Pre-warm cache
override fun onCreate() {
    super.onCreate()
    // Pre-load data in background
    lifecycleScope.launch {
        cityDataSource.preloadData()
    }
}
```

---

### 8. Gzip Compression

**Current:** OkHttp interceptor adds `Accept-Encoding: gzip`

**Issue:** Server may not support gzip, causing double parsing

**Optimization:**

```kotlin
// Verify server supports gzip
val gzipInterceptor = Interceptor { chain ->
    val originalRequest = chain.request()
    val compressedRequest = originalRequest.newBuilder()
        .header("Accept-Encoding", "gzip")
        .build()
    val response = chain.proceed(compressedRequest)
    
    // Only decompress if server actually compressed
    if (response.header("Content-Encoding") == "gzip") {
        response.newBuilder()
            .body(GzipDecompressingResponseBody(response.body!!))
            .build()
    } else {
        response
    }
}
```

---

## Performance Profiling Commands

### Android Studio Profiler

```bash
# Generate baseline profile
./gradlew :app:generateBaselineProfile

# Run with profiling
./gradlew :app:assembleDebug
# Then: Run → Profile 'app' → CPU/Memory/Energy
```

### Macrobenchmark

```kotlin
// app/src/androidTest/java/.../StartupBenchmark.kt
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupCompilationNone() = startup(CompilationMode.None())

    @Test
    fun startupCompilationBaselineProfiles() = startup(
        CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require)
    )

    private fun startup(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = "com.boa.test.city.seeker",
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = 10
        ) {
            pressHome()
            startActivityAndWait()
        }
    }
}
```

### LeakCanary (Memory Leaks)

```kotlin
// app/build.gradle.kts
debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
```

### StrictMode

```kotlin
// CitySeekerApp.kt (debug only)
if (BuildConfig.DEBUG) {
    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
    )
    StrictMode.setVmPolicy(
        StrictMode.VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
    )
}
```

---

## Performance Checklist

| Area          | Check                         | Status | Priority |
|---------------|-------------------------------|--------|----------|
| **Memory**    | Trie stores IDs not objects   | ❌      | High     |
| **Memory**    | LeakCanary configured         | ❌      | Medium   |
| **Memory**    | No memory leaks in ViewModels | ❌      | High     |
| **Rendering** | LazyColumn uses keys          | ✅      | High     |
| **Rendering** | Stable composables            | ⚠️     | Medium   |
| **Network**   | Gzip compression              | ✅      | High     |
| **Network**   | OkHttp cache                  | ✅      | Medium   |
| **Network**   | Timeout configured            | ❌      | Medium   |
| **Startup**   | Baseline profiles             | ❌      | High     |
| **Startup**   | Lazy initialization           | ⚠️     | Medium   |
| **Startup**   | Splash screen                 | ✅      | High     |
| **Search**    | Trie optimization             | ⚠️     | High     |
| **Search**    | FTS for fallback              | ❌      | Low      |
| **Scrolling** | Paging implemented            | ❌      | High     |
| **Scrolling** | Item keys                     | ✅      | High     |
| **Battery**   | Background work minimized     | ⚠️     | Medium   |
| **Battery**   | Network batching              | ❌      | Low      |

**Legend:** ✅ Done | ⚠️ Partial | ❌ Not done

---

## Benchmarking

### Key Metrics to Track

| Metric               | Target   | Tool                      |
|----------------------|----------|---------------------------|
| Cold start           | < 2s     | Macrobenchmark            |
| Warm start           | < 1s     | Macrobenchmark            |
| Search latency (p50) | < 50ms   | Custom Timber trace       |
| Search latency (p95) | < 100ms  | Custom Timber trace       |
| Scroll FPS           | > 55 fps | GPU Profiler              |
| Memory (peak)        | < 150MB  | Android Profiler          |
| APK size (release)   | < 10MB   | `./gradlew bundleRelease` |
| Startup ANR rate     | < 0.1%   | Play Console Vitals       |

### Custom Tracing

```kotlin
// Add to SearchCityUseCase
import android.os.Trace

operator fun invoke(textFilter: String): Flow<UiStateModel<List<CityModel>>> = flow {
    Trace.beginSection("SearchCityUseCase")
    try {
        emit(UiStateModel.Loading(true))
        Trace.beginSection("searchCities")
        val cities = cityRepository.searchCities(textFilter)
        Trace.endSection()
        emit(UiStateModel.Success(cities))
    } finally {
        Trace.endSection()
    }
}.flowOn(Dispatchers.IO)
```
