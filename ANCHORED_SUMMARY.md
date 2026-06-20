# CitySeeker — Anchored Summary

## Completed Features (from EXTRAS.md)

### AR City Exploration (DONE ✓)
- `ArCityScreen.kt` + `ArCityViewModel.kt` — Compose UI with Mapbox satellite style
- `ArMapView` composable — orbital camera animation, city info overlay
- `NavigationGraph` entry `Screen.AR` with city ID param
- DetailScreen button with connectivity gating
- Strings in EN (`en/strings.xml`) + ES (`es/strings.xml`)

### Personalized City Routes (DONE ✓)
- `RouteScreen.kt` + `RouteViewModel.kt` + `RouteContract.kt`
- `RouteCityCard` composable with gradient overlay, difficulty/category badges
- `GetRecommendationsUseCase` — algorithmic scoring (popularity, rating, distance)
- MainScreen button with connectivity gating
- Strings in EN + ES

---

## Improvement Phases

### Phase 2 — Modernization & Technical Debt (DONE ✓)
- **2.1 Edge-to-edge**: `enableEdgeToEdge()` confirmed in `MainActivity.onCreate()`, `targetSdk=37` mandatory for API 37+
- **2.2 Predictive back**: `enableOnBackInvokedCallback=true` by default on targetSdk 33+, no action needed
- **2.3 Photo Picker**: App doesn't request photo/gallery access — no migration needed

### Phase 3 — Performance & Build (DONE ✓)
- **3.1 Baseline Profiles**: `baseline-prof.txt` created with all app classes (`Lcom/boa/test/city/seeker/...;`), `profileinstaller` lib already included
- **3.2 Compose Compiler**: Strong skipping enabled by default in Kotlin 2.0+ Compose compiler plugin — no action needed
- **3.3 R8 Full Mode**: `android.enableR8.fullMode=true` in `gradle.properties`, proguard rules cleaned (removed unnecessary broad keeps for okhttp/dagger/timber/datastore)
- **3.4 Room Auto-Migration**: `Migrations.kt` with `MIGRATION_2_3` (creates `journal_entries` table + indices), `autoMigrations` annotation added, `fallbackToDestructiveMigration` replaced in both Hilt module and singleton builder

### Phase 4 — Tooling & Libraries (DONE ✓)
- **4.1 Hilt Navigation Compose**: 1.4.0 not released — staying at 1.3.0
- **4.2 Compose Testing v2**: Already using `createComposeRule()` from `junit4.v2` — no action needed

### Phase 5 — UI/Platform (Partial)
- **5.3 Mapbox loadStyleUri**: Current supported API in Mapbox v11 — no migration needed
- **5.1 Material 3 Adaptive**: Pending review
- **5.4 Trackpad/mouse**: Pending verification

---

## Library Versions (all latest stable as of Jun 2026)
- AGP 9.2.1, Kotlin 2.4.0, Compose BOM 2026.06.00
- Hilt 2.59.2, Room 2.8.4, Navigation Compose 2.9.8
- Mapbox 11.24.3, Coil 2.7.0, Lottie 6.7.1
- Retrofit 3.0.0, OkHttp 5.4.0, KSP 2.3.9
- Sentry 8.44.0, Firebase BOM 34.15.0
- Testing: MockK 1.14.11, Turbine 1.2.1, Robolectric 4.16.1, Roborazzi 1.64.0
