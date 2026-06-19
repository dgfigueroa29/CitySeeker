# Code Style & Conventions

This document captures the coding conventions extracted from the CitySeeker codebase.

## Naming Conventions

### Files & Classes

| Type                   | Convention                              | Example                                                            |
|------------------------|-----------------------------------------|--------------------------------------------------------------------|
| ViewModel              | `[Feature]ViewModel`                    | `ListViewModel`, `DetailViewModel`                                 |
| Screen (Composable)    | `[Feature]Screen`                       | `ListScreen`, `DetailScreen`, `MainScreen`                         |
| State (MVI)            | `[Feature]State` or `[Feature]Contract` | `ListState`, `DetailContract`                                      |
| Use Case               | `[Verb][Noun]UseCase`                   | `SearchCityUseCase`, `ToggleFavoriteUseCase`, `GetCityByIdUseCase` |
| Repository (interface) | `[Entity]Repository`                    | `CityRepository`, `PreferenceRepository`                           |
| Repository (impl)      | `[Entity]RepositoryImpl`                | `CityRepositoryImpl`, `PreferenceRepositoryImpl`                   |
| DataSource (interface) | `[Entity]DataSource`                    | `CityDataSource`, `PreferenceDataSource`                           |
| DataSource (impl)      | `[Entity]DataSourceImpl`                | `CityDataSourceImpl`, `PreferenceDataSourceImpl`                   |
| Entity (Room)          | `[Entity]Entity`                        | `CityEntity`                                                       |
| Model (Domain)         | `[Entity]Model`                         | `CityModel`                                                        |
| DAO                    | `[Entity]Dao`                           | `CityDao`                                                          |
| Mapper                 | `[Entity]Mapper`                        | `CityMapper`                                                       |
| API Interface          | `[Entity]Api`                           | `CityApi`                                                          |
| DI Module              | `[Scope]Module`                         | `ApplicationModule`                                                |
| Component (UI)         | `[ComponentName]`                       | `SearchBar`, `FilterSwitch`, `LoadingIndicator`                    |
| Extension File         | `[Feature]Extensions` or `Extensions`   | `Extensions.kt`                                                    |

### Functions & Variables

| Type             | Convention               | Example                                          |
|------------------|--------------------------|--------------------------------------------------|
| Public function  | `camelCase`              | `refreshQuery()`, `toggleFavorite()`             |
| Private function | `camelCase`              | `getCities()`, `processFile()`                   |
| Boolean          | `is/has/should` prefix   | `isLoading`, `isConnected`, `isShowingFavorites` |
| Mutable state    | `_underscored` (private) | `_loadingState`, `_cityList`                     |
| Public state     | `no underscore`          | `loadingState`, `cityList`                       |
| Constant         | `UPPER_SNAKE_CASE`       | `LIMIT`, `FILE_CITY`, `DB_NAME`                  |
| Lambda parameter | `it` or descriptive      | `it` for single, `city` for named                |

## Dependency Injection (Hilt)

### Module Structure

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    // All providers are @Singleton
    // Interface binding: bindImpl -> Interface
    // Factory method: provide + Interface name
}
```

### Provider Naming

```kotlin
// Database
@Provides @Singleton
fun providesDatabase(@ApplicationContext context: Context): CityDatabase

// Network
@Provides @Singleton
fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient
@Provides @Singleton
fun provideRetrofit(client: OkHttpClient): Retrofit
@Provides @Singleton
fun provideCityApi(retrofit: Retrofit): CityApi

// DataSource
@Provides @Singleton
fun provideCityDataSource(...): CityDataSource = CityDataSourceImpl(...)

// Repository
@Provides @Singleton
fun provideCityRepository(...): CityRepository = CityRepositoryImpl(...)

// DataStore
@Provides @Singleton
fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences>
```

### ViewModel Injection

```kotlin
@HiltViewModel
class ListViewModel @Inject constructor(
    private val searchCityUseCase: SearchCityUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel()
```

## Coroutine Patterns

### ViewModel Scope

```kotlin
// Always use viewModelScope, never GlobalScope
viewModelScope.launch {
    searchCityUseCase.invoke(query)
        .collect { resource ->
            // Handle state updates
        }
}
```

### Flow Threading

```kotlin
// Use case emits on IO
.flowOn(Dispatchers.IO)

// Repository performs network/DB on IO
suspend fun searchCities(): List<CityModel> = withContext(Dispatchers.IO) {
    // Network/DB operations
}
```

### Coroutine Builders

```kotlin
// Preferred
viewModelScope.launch { ... }
viewModelScope.launch { ... }

// For parallel execution
coroutineScope {
    launch { task1() }
    launch { task2() }
}
```

## Compose Patterns

### State Hoisting

```kotlin
// Stateless component
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
)

// Stateful wrapper
@Composable
fun ListStateful(
    listState: ListState,
    onSearchQueryChanged: (String) -> Unit,
    onShowFavoritesChanged: (Boolean, String) -> Unit,
    onCityClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
)
```

### State Collection

```kotlin
// Collect StateFlow in Composable
val cities = listState.cityList.collectAsState().value
val query by listState.queryState.collectAsState()

// Side effect with LaunchedEffect
LaunchedEffect(searchQuery) {
    delay(200) // Debounce
    onSearchQueryChanged(searchQuery)
}
```

### Recomposition Optimization

```kotlin
// Use remember for expensive computations
val filteredCities = remember(cities, query) {
    cities.filter { it.name.contains(query, ignoreCase = true) }
}

// Use derivedStateOf for derived state
val showButton by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 0 }
}
```

### Animation Patterns

```kotlin
// AnimatedVisibility
AnimatedVisibility(
    visible = isVisible,
    enter = scaleIn() + fadeIn(),
    exit = scaleOut() + fadeOut()
) {
    // Content
}
```

## Error Handling

### Sealed Class Pattern

```kotlin
sealed class UiStateModel<out T> {
    data class Loading(val isLoading: Boolean) : UiStateModel<Nothing>()
    data class Success<T>(val data: T) : UiStateModel<T>()
    data class Error(val message: String) : UiStateModel<Nothing>()
}
```

### Flow Error Handling

```kotlin
// Use case with catch operator
operator fun invoke(textFilter: String): Flow<UiStateModel<List<CityModel>>> = flow {
    emit(UiStateModel.Loading(true))
    val cities = cityRepository.searchCities(textFilter)
    emit(UiStateModel.Success(cities))
}.catch {
    Timber.e("Error: ${it.stackTraceToString()}")
    emit(UiStateModel.Error(it.message ?: "Unknown error"))
}.flowOn(Dispatchers.IO)
```

### Exception Logging

```kotlin
// Always log with Timber before returning error
try {
    // Operation
} catch (e: Exception) {
    Timber.e("Error description: ${e.stackTraceToString()}")
    // Return fallback or error state
}
```

## Repository Pattern

### Interface

```kotlin
interface CityRepository {
    suspend fun searchCities(
        query: String,
        withOnlyFavorites: Boolean
    ): List<CityModel>
}
```

### Implementation

```kotlin
class CityRepositoryImpl @Inject constructor(
    private val dataSource: CityDataSource,
    private val preferenceDataSource: PreferenceDataSource
) : CityRepository {
    override suspend fun searchCities(
        query: String,
        withOnlyFavorites: Boolean
    ): List<CityModel> {
        return dataSource.searchCities(query)
            .map { it.toDomain() }
            .let { cities ->
                if (withOnlyFavorites) {
                    val favorites = preferenceDataSource.getSetString()
                    cities.filter { favorites.contains(it.id.toString()) }
                } else {
                    cities
                }
            }
    }
}
```

## Data Mapping

### Base Mapper

```kotlin
// domain/base/BaseMapper.kt
interface BaseMapper<Domain, Entity> {
    fun toDomain(entity: Entity): Domain
    fun toEntity(domain: Domain): Entity
}

// Extension for list mapping
fun <Domain, Entity> BaseMapper<Domain, Entity>.mapAll(
    entities: List<Entity>
): List<Domain> = entities.map { toDomain(it) }
```

### Entity to Model

```kotlin
// data/mapper/CityMapper.kt
class CityMapper : BaseMapper<CityModel, CityEntity> {
    override fun toDomain(entity: CityEntity) = CityModel(
        id = entity.id,
        name = entity.name,
        country = entity.country,
        latitude = entity.latitude,
        longitude = entity.longitude
    )
}
```

## Testing Conventions

### Test Class Structure

```kotlin
class ListViewModelTest {
    // System Under Test
    private lateinit var viewModel: ListViewModel

    // Mocks
    @MockK
    private lateinit var searchCityUseCase: SearchCityUseCase

    @MockK
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = ListViewModel(searchCityUseCase, toggleFavoriteUseCase)
    }

    @Test
    fun `test description`() = runTest {
        // Given
        // When
        // Then
    }
}
```

### Turbine Flow Testing

```kotlin
@Test
fun `should emit loading then success`() = runTest {
        // Given
        val query = "Denver"
        coEvery { searchCityUseCase(any(), any()) } returns flow {
            emit(UiStateModel.Loading(true))
            emit(UiStateModel.Success(listOf(cityModel)))
        }

        // When
        viewModel.refreshQuery(query)

        // Then
        viewModel.listState.cityList.test {
            awaitItem() // Loading
            val result = awaitItem()
            assertTrue(result.isNotEmpty())
        }
    }
```

## Suppression Annotations

| Annotation                                | When to Use                               |
|-------------------------------------------|-------------------------------------------|
| `@Suppress("NestedBlockDepth")`           | Deep nesting in parsing logic             |
| `@Suppress("CyclomaticComplexMethod")`    | Complex but necessary branching           |
| `@Suppress("TooGenericExceptionCaught")`  | Catching all exceptions in error handlers |
| `@Suppress("unused")`                     | Public API not yet used                   |
| `@Suppress("ConstructorParameterNaming")` | MutableStateFlow underscore convention    |
| `@Suppress("UnusedPrivateMember")`        | Preview functions                         |
| `@Suppress("DEPRECATION")`                | Using deprecated APIs with migration plan |
| `@Suppress("UnstableApiUsage")`           | Compose compiler options                  |

## File Organization

```
// File header
package com.boa.test.city.seeker...

// Imports (grouped, alphabetical)
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.boa.test.city.seeker.domain.model.*
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

// Class/Function
class MyClass {
    // Properties
    // Companion object
    // Init block
    // Public functions
    // Private functions
}
```
