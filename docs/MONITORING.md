# Monitoring & Observability

This document covers crash reporting, performance monitoring, and observability setup for
CitySeeker.

## Current State

**Timber** is configured for debug logging only. No production crash reporting or performance
monitoring exists.

```kotlin
// CitySeekerApp.kt
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}
```

---

## Recommended Tools

| Tool                     | Purpose                       | Cost                     | Integration Effort |
|--------------------------|-------------------------------|--------------------------|--------------------|
| **Sentry**               | Crash reporting + performance | Free tier (5k events/mo) | 30 min             |
| **Firebase Crashlytics** | Crash reporting               | Free                     | 20 min             |
| **Firebase Performance** | Network + startup metrics     | Free                     | 15 min             |
| **Play Console Vitals**  | ANR, startup, rendering       | Free (auto)              | 0 min              |
| **Timber**               | Debug logging                 | Free                     | Already done       |
| **LeakCanary**           | Memory leak detection         | Free                     | 10 min             |

---

## Sentry Integration

### Step 1: Add Dependencies

```kotlin
// app/build.gradle.kts
dependencies {
    // Sentry
    implementation("io.sentry:sentry-android:7.14.0")
    implementation("io.sentry:sentry-android-okhttp:7.14.0")
    implementation("io.sentry:sentry-android-timber:7.14.0")

    // Gradle plugin for source maps
    // build.gradle.kts (root)
    plugins {
        id("io.sentry.android.gradle") version "4.11.0" apply false
    }
}
```

### Step 2: Initialize Sentry

```kotlin
// CitySeekerApp.kt
class CitySeekerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Sentry
        SentryAndroid.init(this) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            options.tracesSampleRate = 1.0  // 100% in dev, reduce in prod
            options.profilesSampleRate = 1.0
            options.isEnableUserInteractionTracing = true
            options.isEnableAutoActivityLifecycleTracing = true

            // Environment
            options.environment = if (BuildConfig.DEBUG) "development" else "production"
            options.release = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

            // Breadcrumbs
            options.isEnableAutoSessionTracking = true
            options.sessionTrackingIntervalMillis = 30_000
        }

        // Timber integration
        Timber.plant(SentryTimberTree())

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
```

### Step 3: Custom Timber Tree

```kotlin
// monitoring/SentryTimberTree.kt
class SentryTimberTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when (priority) {
            Log.ERROR -> {
                Sentry.captureMessage(message, SentryLevel.ERROR)
                t?.let { Sentry.captureException(it) }
            }
            Log.WARN -> {
                Sentry.captureMessage(message, SentryLevel.WARNING)
            }
            Log.INFO -> {
                Sentry.addBreadcrumb(
                    Breadcrumb().apply {
                        level = SentryLevel.INFO
                        this.message = message
                        this.category = tag
                    }
                )
            }
        }
    }
}
```

### Step 4: OkHttp Integration

```kotlin
// ApplicationModule.kt
@Provides
@Singleton
fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
    val builder = OkHttpClient.Builder()
    
    // Add Sentry interceptor
    builder.addInterceptor(SentryOkHttpInterceptor())
    
    // Existing gzip interceptor
    val gzipInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Accept-Encoding", "gzip")
            .build()
        chain.proceed(request)
    }
    builder.addInterceptor(gzipInterceptor)
    
    // Cache
    val cache = Cache(File(context.cacheDir, "http_cache"), 20 * 1024 * 1024L)
    builder.cache(cache)
    
    return builder.build()
}
```

### Step 5: Performance Traces

```kotlin
// SearchCityUseCase.kt
operator fun invoke(textFilter: String): Flow<UiStateModel<List<CityModel>>> = flow {
    val transaction = Sentry.startTransaction("searchCities", "usecase")
    val span = transaction.startChild("search", "Query: $textFilter")
    
    try {
        emit(UiStateModel.Loading(true))
        val cities = cityRepository.searchCities(textFilter)
        span.status = SpanStatus.OK
        emit(UiStateModel.Success(cities))
    } catch (e: Exception) {
        span.status = SpanStatus.INTERNAL_ERROR
        span.setThrowable(e)
        throw e
    } finally {
        span.finish()
        transaction.finish()
    }
}.flowOn(Dispatchers.IO)
```

### Step 6: Local Properties

```properties
# local.properties
SENTRY_DSN=https://your-dsn@sentry.io/project-id
```

---

## Firebase Crashlytics Integration

### Step 1: Add Dependencies

```kotlin
// app/build.gradle.kts
plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}

dependencies {
    implementation("com.google.firebase:firebase-crashlytics-ktx:19.0.3")
    implementation("com.google.firebase:firebase-analytics-ktx:22.1.0")
}
```

### Step 2: Initialize

```kotlin
// CitySeekerApp.kt
class CitySeekerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Firebase
        FirebaseApp.initializeApp(this)
        
        // Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        }
    }
}
```

### Step 3: Custom Exceptions

```kotlin
// monitoring/CrashlyticsExtensions.kt
fun Throwable.logToCrashlytics() {
    FirebaseCrashlytics.getInstance().apply {
        recordException(this)
        log("Error: ${this.message}")
    }
}

fun String.logToCrashlytics() {
    FirebaseCrashlytics.getInstance().log(this)
}

fun setCrashlyticsUser(userId: String) {
    FirebaseCrashlytics.getInstance().setUserId(userId)
}
```

---

## Firebase Performance

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.google.firebase:firebase-perf-ktx:21.0.0")
}
```

### Custom Traces

```kotlin
// CityDataSourceImpl.kt
suspend fun getAllCities(): List<CityEntity> {
    val trace = FirebasePerformance.getInstance().newTrace("getAllCities")
    trace.start()
    trace.putAttribute("source", "api")
    
    return try {
        val cities = cityDatabase.cityDao().getAll().take(LIMIT)
        trace.putMetric("city_count", cities.size.toLong())
        cities
    } finally {
        trace.stop()
    }
}
```

---

## LeakCanary

```kotlin
// app/build.gradle.kts
debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
```

No code changes needed — automatic leak detection in debug builds.

---

## Metrics Dashboard

### Key Metrics to Track

| Category    | Metric        | Target  | Alert Threshold |
|-------------|---------------|---------|-----------------|
| **Startup** | Cold start    | < 2s    | > 3s            |
| **Startup** | Warm start    | < 1s    | > 1.5s          |
| **Search**  | Latency (p50) | < 50ms  | > 100ms         |
| **Search**  | Latency (p95) | < 100ms | > 200ms         |
| **Scroll**  | FPS           | > 55    | < 45            |
| **Network** | Failure rate  | < 1%    | > 5%            |
| **Network** | Response time | < 500ms | > 2s            |
| **Memory**  | Peak usage    | < 150MB | > 200MB         |
| **Crashes** | Crash rate    | < 0.5%  | > 1%            |
| **ANR**     | ANR rate      | < 0.1%  | > 0.5%          |

### Custom Metrics (Timber + Sentry)

```kotlin
// monitoring/Metrics.kt
object Metrics {
    fun trackSearch(query: String, resultCount: Int, durationMs: Long) {
        Sentry.withScope { scope ->
            scope.setExtra("query", query)
            scope.setExtra("result_count", resultCount)
            scope.setExtra("duration_ms", durationMs)
            Sentry.captureMessage("search_executed", SentryLevel.INFO)
        }
        
        Timber.i("Search: query=$query, results=$resultCount, duration=${durationMs}ms")
    }
    
    fun trackFavoriteToggled(cityId: String, isFavorite: Boolean) {
        Sentry.addBreadcrumb(
            Breadcrumb().apply {
                message = "Favorite toggled"
                type = "user"
                setData("city_id", cityId)
                setData("is_favorite", isFavorite)
            }
        )
    }
    
    fun trackError(error: Throwable, context: String) {
        Sentry.withScope { scope ->
            scope.setExtra("context", context)
            Sentry.captureException(error)
        }
    }
}
```

---

## Alert Rules

### Sentry Alerts

```yaml
# Sentry Alert Rules (via UI or API)
Alerts:
  - Name: "High Error Rate"
    Condition: "Error count > 50 in 1 hour"
    Action: "Email + Slack"
    
  - Name: "Spike in Crashes"
    Condition: "Crash count > 20 in 10 minutes"
    Action: "Email + Slack + PagerDuty"
    
  - Name: "Performance Degradation"
    Condition: "Transaction duration p95 > 2s"
    Action: "Email"
```

### Firebase Console Alerts

```
Firebase Console → Performance → Alerts
  - Startup time > 3s
  - Network failure > 5%
  
Firebase Console → Crashlytics → Alerts
  - Crash rate > 1%
  - ANR rate > 0.5%
```

---

## Debug Tools

### Timber Debug Output

```kotlin
// Debug-only verbose logging
if (BuildConfig.DEBUG) {
    Timber.plant(object : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String {
            return "(${element.fileName}:${element.lineNumber})"
        }
    })
}
```

### StrictMode (Debug)

```kotlin
// CitySeekerApp.kt
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

## Integration Checklist

- [ ] Add Sentry DSN to `local.properties`
- [ ] Configure Sentry Gradle plugin
- [ ] Initialize Sentry in `CitySeekerApp`
- [ ] Add SentryTimberTree
- [ ] Add OkHttp interceptor
- [ ] Add Firebase `google-services.json`
- [ ] Configure Crashlytics
- [ ] Add LeakCanary
- [ ] Create Sentry alerts
- [ ] Set up Firebase Performance alerts
- [ ] Test crash reporting in debug
- [ ] Verify metrics in Sentry dashboard
- [ ] Document team notification process

---

## Testing Monitoring

```bash
# Force test crash (debug only)
Sentry.captureMessage("Test crash", SentryLevel.FATAL)

# Verify Crashlytics
FirebaseCrashlytics.getInstance().recordException(RuntimeException("Test"))

# Check logs
adb logcat | grep -E "Sentry|Firebase|Timber"
```
