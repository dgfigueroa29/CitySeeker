# CitySeeker AI Coding Agent Guidelines

## 🏗️ Architecture: Clean & Single-Module

The project uses a single-module `:app` structure following **Clean Architecture** principles.

- **Presentation**: `presentation/feature/city/` (Compose + ViewModels + MVI Contracts)
- **Domain**: `domain/usecase/`, `domain/model/`, `domain/repository/` (Interfaces)
- **Data**: `data/source/`, `data/repository/`, `data/network/`, `data/local/` (Room/Retrofit)

## 🔑 Core Patterns

- **MVVM + MVI**: ViewModels use `StateFlow` and "Contract" classes for state management (e.g.,
  `ListContract.kt`).
- **State Hoisting**: Separate `Stateful` wrappers from `Stateless` Composables for easier
  testing/previews.
- **Resource Wrapper**: Use `UiStateModel<T>` (Loading, Success, Error) for flow-based data
  handling.
- **Offline-First**: Data flows through `CityTrie` (RAM) -> `Room` (Local) -> `Retrofit` (Remote).
- **Trie Search**: Prefix search is powered by `CityTrie.kt`. Always insert data into the Trie after
  DB/API fetch.

## 🧪 Testing Strategy

Follow the **Given-When-Then** pattern with **MockK** and **Turbine**.

- **ViewModels**: Test `StateFlow` updates using `turbine.test { ... }`.
- **UseCases**: Mock repositories and verify flow emissions.
- **Trie**: Unit test for case-insensitive prefix matching and sorting.
- **Naming**: Use backticks for descriptive test names:
  `` `should emit success when data is loaded` ``.

## 🛠️ Critical Workflows

- **Build**: `./gradlew assembleDebug`
- **Lint**: `./gradlew detekt ktlintCheck` (Fix with `./gradlew ktlintFormat`)
- **Test**: `./gradlew test` (Unit) or `./gradlew connectedAndroidTest` (UI)
- **Setup**: Requires `mapboxToken={token}` in `local.properties`.

## 📍 Key Files

- `docs/ARCHITECTURE.md`: High-level diagrams and layer responsibilities.
- `docs/CODE_STYLE.md`: Exhaustive naming and pattern conventions.
- `docs/TESTING_STRATEGY.md`: MockK/Turbine templates and coverage targets.
- `di/ApplicationModule.kt`: Centralized Hilt bindings.
