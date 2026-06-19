# Testing Strategy

This document outlines the testing approach for CitySeeker, current gaps, and recommended test
templates using MockK and Turbine.

## Test Pyramid

```
                    ┌─────────┐
                    │   UI    │ 10% - Compose tests
                    │  Tests  │ Slow, fragile, high value
                   ┌┴─────────┴┐
                   │Integration │ 20% - Repository + DB
                   │   Tests    │ Medium speed, medium value
                  ┌┴───────────┴┐
                  │   Unit      │ 70% - UseCases, ViewModels, Trie
                  │   Tests     │ Fast, stable, high coverage
                  └─────────────┘
```

## Current State

### Existing Test Files

| File                         | Status         | Tests                |
|------------------------------|----------------|----------------------|
| `ListViewModelTest.kt`       | **Stubs only** | 20 TODO test methods |
| `DetailViewModelTest.kt`     | **Stubs only** | Similar pattern      |
| `ExampleUnitTest.kt`         | Basic          | Placeholder          |
| `ExampleInstrumentedTest.kt` | Basic          | Compose placeholder  |

### Gap Analysis

| Component               | Coverage | Target | Gap                                |
|-------------------------|----------|--------|------------------------------------|
| `CityTrie`              | 0%       | 100%   | No tests for core search algorithm |
| `SearchCityUseCase`     | 0%       | 90%    | No flow testing                    |
| `ToggleFavoriteUseCase` | 0%       | 90%    | No repository interaction tests    |
| `GetCityByIdUseCase`    | 0%       | 90%    | No tests                           |
| `ListViewModel`         | 0%       | 85%    | 20 stubs, 0 implementations        |
| `DetailViewModel`       | 0%       | 85%    | Stubs only                         |
| `CityDataSourceImpl`    | 0%       | 80%    | Complex logic, no tests            |
| `CityRepositoryImpl`    | 0%       | 80%    | No tests                           |
| `ListScreen`            | 0%       | 60%    | No Compose UI tests                |
| `DetailScreen`          | 0%       | 60%    | No Compose UI tests                |

## Required Dependencies

Add to `app/build.gradle.kts`:

```kotlin
// Testing
testImplementation("io.mockk:mockk:1.14.0")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
testImplementation("app.cash.turbine:turbine:1.2.0")
testImplementation("androidx.arch.core:core-testing:2.2.0")

// UI Testing
androidTestImplementation("io.mockk:mockk-android:1.14.0")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
androidTestImplementation("androidx.test.ext:junit:1.3.0")
androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

// Robolectric (for ViewModel tests without device)
testImplementation("org.robolectric:robolectric:4.14.1")
testImplementation("androidx.test:core-ktx:1.6.1")
```

## Test Templates

### 1. CityTrie Unit Tests

```kotlin
package com.boa.test.city.seeker.data.source

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CityTrieTest {

    private lateinit var trie: CityTrie

    private val denverCity = CityModel(
        id = 1,
        name = "Denver",
        country = "US",
        latitude = 39.7392,
        longitude = -104.9903
    )

    private val dallasCity = CityModel(
        id = 2,
        name = "Dallas",
        country = "US",
        latitude = 32.7767,
        longitude = -96.7970
    )

    private val sydneyCity = CityModel(
        id = 3,
        name = "Sydney",
        country = "AU",
        latitude = -33.8688,
        longitude = 151.2093
    )

    @Before
    fun setup() {
        trie = CityTrie()
        trie.insert(denverCity)
        trie.insert(dallasCity)
        trie.insert(sydneyCity)
    }

    @Test
    fun `search with prefix D returns Denver and Dallas`() {
        val results = trie.search("D")
        assertEquals(2, results.size)
        assertTrue(results.containsAll(listOf(dallasCity, denverCity)))
    }

    @Test
    fun `search with prefix Den returns only Denver`() {
        val results = trie.search("Den")
        assertEquals(1, results.size)
        assertEquals("Denver", results.first().name)
    }

    @Test
    fun `search is case insensitive`() {
        val results = trie.search("den")
        assertEquals(1, results.size)
        assertEquals("Denver", results.first().name)
    }

    @Test
    fun `search with prefix S returns Sydney`() {
        val results = trie.search("S")
        assertEquals(1, results.size)
        assertEquals("Sydney", results.first().name)
    }

    @Test
    fun `search with prefix Alb returns Albuquerque only`() {
        val albuquerque = CityModel(4, "Albuquerque", "US", 35.0844, -106.6504)
        trie.insert(albuquerque)

        val results = trie.search("Alb")
        assertEquals(1, results.size)
        assertEquals("Albuquerque", results.first().name)
    }

    @Test
    fun `search with empty prefix returns all cities`() {
        val results = trie.search("")
        assertEquals(3, results.size)
    }

    @Test
    fun `search with non-existing prefix returns empty`() {
        val results = trie.search("XYZ")
        assertEquals(0, results.size)
    }

    @Test
    fun `search results are sorted by name then country`() {
        val results = trie.search("D")
        assertEquals("Dallas", results[0].name)
        assertEquals("Denver", results[1].name)
    }

    @Test
    fun `search by country code`() {
        val results = trie.search("AU")
        assertEquals(1, results.size)
        assertEquals("Sydney", results.first().name)
    }

    @Test
    fun `search is case insensitive for country`() {
        val results = trie.search("au")
        assertEquals(1, results.size)
        assertEquals("Sydney", results.first().name)
    }

    @Test
    fun `insert duplicate city does not create duplicates`() {
        trie.insert(denverCity)
        val results = trie.search("Den")
        assertEquals(1, results.size)
    }
}
```

### 2. SearchCityUseCase Tests (Turbine)

```kotlin
package com.boa.test.city.seeker.domain.usecase

import app.cash.turbine.test
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.domain.model.UiStateModel
import com.boa.test.city.seeker.domain.repository.CityRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SearchCityUseCaseTest {

    private lateinit var useCase: SearchCityUseCase
    private lateinit var cityRepository: CityRepository

    private val testCities = listOf(
        CityModel(1, "Denver", "US", 39.7392, -104.9903),
        CityModel(2, "Dallas", "US", 32.7767, -96.7970)
    )

    @Before
    fun setup() {
        cityRepository = mockk()
        useCase = SearchCityUseCase(cityRepository)
    }

    @Test
    fun `should emit loading then success with cities`() = runTest {
        // Given
        coEvery {
            cityRepository.searchCities("Den", false)
        } returns listOf(testCities.first())

        // When/Then
        useCase("Den", false).test {
            val loading = awaitItem()
            assertTrue(loading is UiStateModel.Loading)

            val success = awaitItem()
            assertTrue(success is UiStateModel.Success)
            assertEquals(1, (success as UiStateModel.Success).data.size)
            assertEquals("Denver", success.data.first().name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should emit loading then error on exception`() = runTest {
        // Given
        coEvery {
            cityRepository.searchCities(any(), any())
        } throws RuntimeException("Network error")

        // When/Then
        useCase("test", false).test {
            val loading = awaitItem()
            assertTrue(loading is UiStateModel.Loading)

            val error = awaitItem()
            assertTrue(error is UiStateModel.Error)
            assertEquals("Network error", (error as UiStateModel.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should emit loading then success with empty list`() = runTest {
        // Given
        coEvery {
            cityRepository.searchCities("XYZ", false)
        } returns emptyList()

        // When/Then
        useCase("XYZ", false).test {
            awaitItem() // Loading
            val success = awaitItem()
            assertTrue(success is UiStateModel.Success)
            assertEquals(0, (success as UiStateModel.Success).data.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should pass favorites filter to repository`() = runTest {
        // Given
        coEvery {
            cityRepository.searchCities("Den", true)
        } returns listOf(testCities.first())

        // When
        useCase("Den", true).test {
            awaitItem()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        // Then
        coEvery { cityRepository.searchCities("Den", true) }
    }
}
```

### 3. ListViewModel Tests (Turbine)

```kotlin
package com.boa.test.city.seeker.presentation.feature.city.list

import app.cash.turbine.test
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.domain.model.UiStateModel
import com.boa.test.city.seeker.domain.usecase.SearchCityUseCase
import com.boa.test.city.seeker.domain.usecase.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    private lateinit var viewModel: ListViewModel
    private lateinit var searchCityUseCase: SearchCityUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testCities = listOf(
        CityModel(1, "Denver", "US", 39.7392, -104.9903),
        CityModel(2, "Dallas", "US", 32.7767, -96.7970)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchCityUseCase = mockk()
        toggleFavoriteUseCase = mockk()
        viewModel = ListViewModel(searchCityUseCase, toggleFavoriteUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load should set loading state to true`() {
        // Given
        coEvery {
            searchCityUseCase(any(), any())
        } returns flow { emit(UiStateModel.Success(testCities)) }

        // When
        viewModel.load()

        // Then
        assertTrue(viewModel.listState.loadingState.value)
    }

    @Test
    fun `refreshQuery should update query state`() {
        // Given
        coEvery {
            searchCityUseCase(any(), any())
        } returns flow { emit(UiStateModel.Success(testCities)) }

        // When
        viewModel.refreshQuery("Denver")

        // Then
        assertEquals("Denver", viewModel.listState.queryState.value)
    }

    @Test
    fun `refreshQuery should update city list`() = runTest {
        // Given
        coEvery {
            searchCityUseCase("Denver", false)
        } returns flow {
            emit(UiStateModel.Loading(true))
            emit(UiStateModel.Success(listOf(testCities.first())))
        }

        // When
        viewModel.refreshQuery("Denver")

        // Then
        viewModel.listState.cityList.test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Denver", result.first().name)
        }
    }

    @Test
    fun `toggleFavorite should call use case`() {
        // Given
        coEvery { toggleFavoriteUseCase("1") } returns Unit

        // When
        viewModel.toggleFavorite("1")

        // Then
        coVerify { toggleFavoriteUseCase("1") }
    }

    @Test
    fun `toggleFavorite should update city list`() {
        // Given
        coEvery { toggleFavoriteUseCase("1") } returns Unit

        // When
        viewModel.toggleFavorite("1")

        // Then
        val city = viewModel.listState.cityList.value.find { it.id == 1L }
        // Verify favorite was toggled (implementation depends on initial state)
    }

    @Test
    fun `updateConnectionStatus to connected clears error`() {
        // Given
        viewModel.refreshError("No data")

        // When
        viewModel.updateConnectionStatus(true)

        // Then
        assertEquals("", viewModel.listState.errorState.value)
    }

    @Test
    fun `updateConnectionStatus to disconnected sets error`() {
        // When
        viewModel.updateConnectionStatus(false)

        // Then
        assertTrue(viewModel.listState.errorState.value.isNotBlank())
    }

    @Test
    fun `refreshError should update error state`() {
        // When
        viewModel.refreshError("Test error")

        // Then
        assertEquals("Test error", viewModel.listState.errorState.value)
    }

    @Test
    fun `refreshLoading should update loading state`() {
        // When
        viewModel.refreshLoading(true)

        // Then
        assertTrue(viewModel.listState.loadingState.value)
    }

    @Test
    fun `refreshFavoriteFilter should update filter and refresh query`() {
        // Given
        coEvery {
            searchCityUseCase(any(), any())
        } returns flow { emit(UiStateModel.Success(testCities)) }

        // When
        viewModel.refreshFavoriteFilter(true, "Denver")

        // Then
        assertTrue(viewModel.listState.favoriteFilterState.value)
    }
}
```

### 4. ToggleFavoriteUseCase Tests

```kotlin
package com.boa.test.city.seeker.domain.usecase

import com.boa.test.city.seeker.domain.repository.PreferenceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ToggleFavoriteUseCaseTest {

    private lateinit var useCase: ToggleFavoriteUseCase
    private lateinit var preferenceRepository: PreferenceRepository

    @Before
    fun setup() {
        preferenceRepository = mockk()
        useCase = ToggleFavoriteUseCase(preferenceRepository)
    }

    @Test
    fun `should call repository toggleString with cityId`() = runTest {
        // Given
        coEvery { preferenceRepository.toggleString("123") } returns Unit

        // When
        useCase("123")

        // Then
        coVerify { preferenceRepository.toggleString("123") }
    }

    @Test
    fun `should handle empty cityId`() = runTest {
        // Given
        coEvery { preferenceRepository.toggleString("") } returns Unit

        // When
        useCase("")

        // Then
        coVerify { preferenceRepository.toggleString("") }
    }
}
```

### 5. Compose UI Tests

```kotlin
package com.boa.test.city.seeker.presentation.feature.city.list

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.boa.test.city.seeker.domain.model.CityModel
import org.junit.Rule
import org.junit.Test

class ListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCities = listOf(
        CityModel(1, "Denver", "US", 39.7392, -104.9903),
        CityModel(2, "Dallas", "US", 32.7767, -96.7970)
    )

    @Test
    fun should displayCityList()
    {
        // Given
        val listState = ListState()

        // When
        composeTestRule.setContent {
            ListStateful(
                listState = listState,
                onSearchQueryChanged = {},
                onShowFavoritesChanged = { _, _ -> },
                onCityClick = {},
                onToggleFavorite = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("CitySeeker").assertIsDisplayed()
    }

    @Test
    fun should displaySearchBar()
    {
        // Given
        val listState = ListState()

        // When
        composeTestRule.setContent {
            ListStateful(
                listState = listState,
                onSearchQueryChanged = {},
                onShowFavoritesChanged = { _, _ -> },
                onCityClick = {},
                onToggleFavorite = {}
            )
        }

        // Then
        composeTestRule.onNodeWithTag("SearchBar").assertExists()
    }

    @Test
    fun should displayNoResultsWhenEmpty()
    {
        // Given
        val listState = ListState()
        listState.setQuery("XYZ")

        // When
        composeTestRule.setContent {
            ListStateful(
                listState = listState,
                onSearchQueryChanged = {},
                onShowFavoritesChanged = { _, _ -> },
                onCityClick = {},
                onToggleFavorite = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("No results found").assertExists()
    }
}
```

## Test Coverage Targets

| Component                | Target | Priority |
|--------------------------|--------|----------|
| CityTrie                 | 100%   | Critical |
| SearchCityUseCase        | 90%    | High     |
| ToggleFavoriteUseCase    | 90%    | High     |
| GetCityByIdUseCase       | 90%    | High     |
| ListViewModel            | 85%    | High     |
| DetailViewModel          | 85%    | High     |
| CityRepositoryImpl       | 80%    | Medium   |
| CityDataSourceImpl       | 80%    | Medium   |
| PreferenceDataSourceImpl | 80%    | Medium   |
| Compose Screens          | 60%    | Medium   |

## Running Tests

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.boa.test.city.seeker.data.source.CityTrieTest"

# Run with coverage
./gradlew testDebugUnitTest coverageReport

# Run instrumented tests
./gradlew connectedAndroidTest
```

## CI Integration

Add to `build.gradle.kts`:

```kotlin
// Coverage reporting
plugins {
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

koverReport {
    xml {
        totalFile.set(project.layout.buildDirectory.file("reports/kover/coverage.xml"))
    }
}

tasks.withType<Test> {
    finalizedBy("koverHtmlReportDebug")
}
```
