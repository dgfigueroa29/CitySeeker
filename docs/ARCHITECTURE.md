# Architecture Overview

CitySeeker follows **Clean Architecture** with a **single-module** structure (`:app`), using *
*MVVM + MVI** pattern with Jetpack Compose for UI.

## High-Level Architecture

```mermaid
graph TB
    subgraph Presentation["Presentation Layer"]
        MS[MainActivity] --> NS[NavHostController]
        NS --> LS[ListScreen]
        NS --> DS[DetailScreen]
        LS --> LVM[ListViewModel]
        DS --> DVM[DetailViewModel]
        LVM --> LST[ListState]
    end

    subgraph Domain["Domain Layer"]
        SCUC[SearchCityUseCase]
        TFUC[ToggleFavoriteUseCase]
        GCBUC[GetCityByIdUseCase]
        CRI[CityRepository]
        PRI[PreferenceRepository]
        CM[CityModel]
        USM[UiStateModel]
    end

    subgraph Data["Data Layer"]
        CDSI[CityDataSourceImpl]
        PDSI[PreferenceDataSourceImpl]
        CRI2[CityRepositoryImpl]
        PRI2[PreferenceRepositoryImpl]
        CD[CityDatabase]
        CA[CityApi]
        CT[CityTrie]
        DS[DataStore]
    end

    LVM --> SCUC
    LVM --> TFUC
    DVM --> GCBUC
    SCUC --> CRI
    TFUC --> PRI
    GCBUC --> CRI
    CRI --> CRI2
    PRI --> PRI2
    CRI2 --> CDSI
    CRI2 --> PDSI
    CDSI --> CD
    CDSI --> CA
    CDSI --> CT
    PDSI --> DS
```

## Layer Responsibilities

| Layer            | Responsibility                                       | Key Classes                                                                                                   |
|------------------|------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| **Presentation** | UI rendering, user interaction, navigation           | `ListScreen`, `DetailScreen`, `MainScreen`, `ListViewModel`, `DetailViewModel`                                |
| **Domain**       | Business logic, use cases, repository interfaces     | `SearchCityUseCase`, `ToggleFavoriteUseCase`, `GetCityByIdUseCase`, `CityRepository`, `PreferenceRepository`  |
| **Data**         | Data sources, API calls, local storage, data mapping | `CityDataSourceImpl`, `PreferenceDataSourceImpl`, `CityRepositoryImpl`, `CityTrie`, `CityDatabase`, `CityApi` |

## Data Flow: City Search

```mermaid
sequenceDiagram
    participant User
    participant ListScreen
    participant ListViewModel
    participant SearchCityUseCase
    participant CityRepository
    participant CityTrie
    participant CityDatabase

    User->>ListScreen: Types query
    ListScreen->>ListViewModel: refreshQuery(query)
    ListViewModel->>SearchCityUseCase: invoke(textFilter)
    SearchCityUseCase->>CityRepository: searchCities(textFilter)
    CityRepository->>CityTrie: search(prefix)
    alt Trie has results
        CityTrie-->>CityRepository: List<CityModel>
    else Trie empty
        CityRepository->>CityDatabase: query(query)
        CityDatabase-->>CityRepository: List<CityEntity>
    end
    CityRepository-->>SearchCityUseCase: List<CityModel>
    SearchCityUseCase-->>ListViewModel: UiStateModel.Success
    ListViewModel-->>ListScreen: listState.setList(cities)
    ListScreen-->>User: Renders city list
```

## Dependency Injection Graph (Hilt)

```mermaid
graph LR
    subgraph SingletonComponent
        AM[ApplicationModule]
    end

    subgraph Database
        AM --> CD[CityDatabase]
        AM --> DS[DataStore]
    end

    subgraph Network
        AM --> OKH[OkHttpClient]
        OKH --> RT[Retrofit]
        RT --> CA[CityApi]
    end

    subgraph DataSource
        AM --> CDSI[CityDataSourceImpl]
        AM --> PDSI[PreferenceDataSourceImpl]
    end

    subgraph Repository
        AM --> CRI[CityRepositoryImpl]
        AM --> PRI[PreferenceRepositoryImpl]
    end

    subgraph UseCase
        SCUC[SearchCityUseCase] --> CRI
        TFUC[ToggleFavoriteUseCase] --> PRI
        GCBUC[GetCityByIdUseCase] --> CRI
    end

    subgraph ViewModel
        LVM[ListViewModel] --> SCUC
        LVM --> TFUC
        DVM[DetailViewModel] --> GCBUC
    end

    CD --> CDSI
    DS --> PDSI
    CA --> CDSI
```

## Navigation Flow

```mermaid
stateDiagram-v2
    [*] --> MainActivity
    MainActivity --> MainScreen

    state MainScreen {
        [*] --> PortraitLayout
        [*] --> LandscapeLayout

        state PortraitLayout {
            [*] --> ListScreen_P
            ListScreen_P --> MapScreen_P: City clicked
        }

        state LandscapeLayout {
            [*] --> ListScreen_L
            [*] --> DetailScreen_L
            DetailScreen_L --> MapScreen_L: Navigate to map
        }
    }

    MapScreen_P --> ListScreen_P: Back
    MapScreen_L --> LandscapeLayout: Back
```

## Module Structure (Single Module)

```
app/
├── common/
│   └── Constants.kt
├── di/
│   └── ApplicationModule.kt
├── data/
│   ├── local/
│   │   ├── CityDatabase.kt
│   │   ├── dao/
│   │   │   └── CityDao.kt
│   │   └── entity/
│   │       └── CityEntity.kt
│   ├── mapper/
│   │   └── CityMapper.kt
│   ├── network/
│   │   └── CityApi.kt
│   ├── repository/
│   │   ├── CityRepositoryImpl.kt
│   │   └── PreferenceRepositoryImpl.kt
│   └── source/
│       ├── CityDataSource.kt
│       ├── CityDataSourceImpl.kt
│       ├── CityPagingSource.kt
│       ├── CityTrie.kt
│       ├── PreferenceDataSource.kt
│       └── PreferenceDataSourceImpl.kt
├── domain/
│   ├── base/
│   │   └── BaseMapper.kt
│   ├── model/
│   │   ├── CityModel.kt
│   │   └── UiStateModel.kt
│   ├── repository/
│   │   ├── CityRepository.kt
│   │   └── PreferenceRepository.kt
│   ├── usecase/
│   │   ├── GetCityByIdUseCase.kt
│   │   ├── SearchCityUseCase.kt
│   │   └── ToggleFavoriteUseCase.kt
│   └── util/
│       └── Extensions.kt
├── presentation/
│   ├── CitySeekerApp.kt
│   ├── MainActivity.kt
│   ├── component/
│   │   ├── ConnectivityStatus.kt
│   │   ├── FilterSwitch.kt
│   │   ├── LoadingIndicator.kt
│   │   ├── OfflineIndicator.kt
│   │   ├── SearchBar.kt
│   │   └── Utils.kt
│   ├── feature/
│   │   ├── city/
│   │   │   ├── CityItem.kt
│   │   │   ├── detail/
│   │   │   │   ├── DetailContract.kt
│   │   │   │   ├── DetailScreen.kt
│   │   │   │   └── DetailViewModel.kt
│   │   │   └── list/
│   │   │       ├── ListContract.kt
│   │   │       ├── ListScreen.kt
│   │   │       └── ListViewModel.kt
│   │   └── main/
│   │       └── MainScreen.kt
│   ├── navigation/
│   │   ├── NavigationGraph.kt
│   │   └── Screen.kt
│   ├── sensor/
│   │   └── ConnectivityReceiver.kt
│   └── ui/
│       ├── PreviewUtils.kt
│       └── theme/
│           ├── Color.kt
│           ├── Theme.kt
│           └── Type.kt
```

## Technology Stack

| Layer       | Technology                   | Version                    |
|-------------|------------------------------|----------------------------|
| Language    | Kotlin                       | 2.2.0                      |
| UI          | Jetpack Compose + Material 3 | 2025.07.00 (BOM)           |
| DI          | Hilt                         | 2.57                       |
| Database    | Room                         | 2.7.2                      |
| Network     | Retrofit + Gson + OkHttp     | 3.0.0 / 2.13.1 / 5.1.0     |
| Async       | Kotlin Coroutines + Flow     | 1.10.2                     |
| Navigation  | Jetpack Navigation Compose   | 2.9.3                      |
| Persistence | DataStore                    | 1.1.7                      |
| Maps        | Mapbox                       | 11.11.0                    |
| Animation   | Lottie                       | 6.6.7                      |
| Paging      | AndroidX Paging              | 3.3.6                      |
| Logging     | Timber                       | 5.0.1                      |
| Lint        | Detekt + ktlint              | 1.23.8 / 13.0.0            |
| Build       | Gradle + KSP                 | 8.12.0 (AGP) / 2.2.0-2.0.2 |

## Key Design Patterns

| Pattern                | Implementation                              | Location                                       |
|------------------------|---------------------------------------------|------------------------------------------------|
| **Clean Architecture** | Domain/Data/Presentation separation         | `app/src/main/java/com/boa/test/city/seeker/`  |
| **MVVM + MVI**         | ViewModel + StateFlow + sealed class states | `ListViewModel` + `ListState` + `UiStateModel` |
| **Repository**         | Abstraction over data sources               | `CityRepository` → `CityRepositoryImpl`        |
| **Use Case**           | Single responsibility business logic        | `SearchCityUseCase`, `ToggleFavoriteUseCase`   |
| **Trie**               | Efficient prefix search                     | `CityTrie.kt`                                  |
| **Observer**           | Reactive UI updates via StateFlow           | `ListState` with `MutableStateFlow`            |
| **DI**                 | Dependency injection via Hilt               | `ApplicationModule.kt`                         |
| **Adapter/Converter**  | Data mapping between layers                 | `CityMapper.kt`                                |

## Offline Strategy

```mermaid
graph TD
    A[App Launch] --> B{Database has data?}
    B -->|Yes| C[Load from Room]
    B -->|No| D{Cache file exists?}
    D -->|Yes| E[Process cache file into Room]
    D -->|No| F{Network available?}
    F -->|Yes| G[Download from API]
    F -->|No| H[Load from raw/cities.json fallback]
    G --> I[Save to cache file]
    I --> J[Process into Room]
    E --> K[Load from Room]
    H --> L[Process into Room]
    L --> K
    J --> K
    C --> K
```
