# Security Audit Checklist

This document provides a comprehensive security review for CitySeeker MVP.

## Security Checklist

### Network Security

- [ ] **Cleartext traffic disabled** (minSdk 31+)
    - ✅ `android:usesCleartextTraffic="false"` in `AndroidManifest.xml`
    - ✅ All endpoints use HTTPS

- [ ] **Network Security Config**
  ```xml
  <!-- res/xml/network_security_config.xml -->
  <network-security-config>
      <base-config cleartextTrafficPermitted="false">
          <trust-anchors>
              <certificates src="system" />
          </trust-anchors>
      </base-config>
  </network-security-config>
  ```

- [ ] **Certificate pinning** (optional for MVP)
  ```xml
  <domain-config>
      <domain includeSubdomains="true">gist.githubusercontent.com</domain>
      <pin-set>
          <pin digest="SHA-256">base64==</pin>
      </pin-set>
  </domain-config>
  ```

---

### Secrets Management

- [ ] **No hardcoded secrets in code**
    - ✅ Mapbox token from `local.properties` → `BuildConfig.MAPBOX_TOKEN`
    - ✅ API URL from `BuildConfig.CITIES_URL`

- [ ] **`.gitignore` configured**
  ```
  # .gitignore
  local.properties
  *.keystore
  *.jks
  google-services.json
  ```

- [ ] **CI/CD secrets configured**
    - GitHub: Secrets → MAPBOX_TOKEN, SENTRY_DSN
    - Bitrise: Secrets → same
    - CircleCI: Environment variables → same

- [ ] **No secrets in git history**
  ```bash
  # Check for leaked secrets
  git log --all --oneline | head -100
  # Use: trufflehog or gitleaks
  gitleaks detect --source .
  ```

---

### ProGuard / R8

- [ ] **Code obfuscation enabled**
  ```kotlin
  // app/build.gradle.kts
  android {
      buildTypes {
          release {
              isMinifyEnabled = true
              isShrinkResources = true
              proguardFiles(
                  getDefaultProguardFile("proguard-android-optimize.txt"),
                  "proguard-rules.pro"
              )
          }
      }
  }
  ```

- [ ] **Keep rules for essential classes**
  ```proguard
  # proguard-rules.pro
  
  # Mapbox
  -keep class com.mapbox.** { *; }
  -dontwarn com.mapbox.**
  
  # Retrofit/Gson
  -keepattributes Signature
  -keepattributes *Annotation*
  -keep class retrofit2.** { *; }
  -keepclasseswithmembers class * {
      @retrofit2.http.* <methods>;
  }
  
  # Gson
  -keep class com.google.gson.** { *; }
  -keep class * implements com.google.gson.TypeAdapterFactory
  -keep class * implements com.google.gson.JsonSerializer
  -keep class * implements com.google.gson.JsonDeserializer
  -keepclassmembers class com.boa.test.city.seeker.data.local.entity.** {
      <fields>;
  }
  
  # Room
  -keep class * extends androidx.room.RoomDatabase
  -keep @androidx.room.Entity class *
  -keep @androidx.room.Dao interface *
  -keep class androidx.room.** { *; }
  
  # Hilt
  -keep class dagger.hilt.** { *; }
  -keep @dagger.hilt.** class *
  -keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
  
  # Coroutines
  -keep class kotlinx.coroutines.** { *; }
  -keepclassmembers class kotlinx.coroutines.** {
      volatile <fields>;
  }
  
  # Timber
  -keep class timber.log.** { *; }
  
  # Sentry
  -keep class io.sentry.** { *; }
  -dontwarn io.sentry.**
  
  # DataStore
  -keep class androidx.datastore.** { *; }
  
  # Kotlin Serialization
  -keepattributes *Annotation*, InnerClasses
  -dontnote kotlinx.serialization.AnnotationsKt
  ```

---

### Data Storage

- [ ] **Room database encryption** (if sensitive data)
    - Current: Not encrypted (only city data, not sensitive)
    - Future: Use SQLCipher if needed

- [ ] **DataStore security**
    - Current: Not encrypted (city IDs only)
    - For sensitive data: Use `EncryptedFile` + `DataStore`

- [ ] **SharedPreferences deprecated**
    - ✅ Using DataStore (not SharedPreferences)

---

### Input Validation

- [ ] **Search query validation**
  ```kotlin
  // CityDataSourceImpl.kt
  override suspend fun searchCities(query: String): List<CityEntity> {
      // Sanitize query
      val sanitized = query.trim()
          .replace(Regex("[<>\"';]"), "")
          .take(100) // Limit length
      
      return if (sanitized.isNotEmpty()) {
          cityDatabase.cityDao().searchCities(sanitized)
      } else {
          cityDatabase.cityDao().getAll()
      }
  }
  ```

- [ ] **City ID validation**
  ```kotlin
  override suspend fun getCityById(id: Long): CityEntity? {
      if (id <= 0) return null
      return cityDatabase.cityDao().getCityById(id)
  }
  ```

---

### Dependency Scanning

- [ ] **OWASP Dependency Check**
  ```kotlin
  // build.gradle.kts (root)
  plugins {
      id("org.owasp.dependencycheck") version "10.0.2"
  }
  
  dependencyCheck {
      failBuildOnCVSS = 7.0
      suppressionFile = "config/dependency-check-suppressions.xml"
      analyzers {
          nodeEnabled = false
      }
  }
  ```

- [ ] **Run scan**
  ```bash
  ./gradlew dependencyCheckAnalyze
  ```

- [ ] **GitHub Dependabot**
  ```yaml
  # .github/dependabot.yml
  version: 2
  updates:
    - package-ecosystem: "gradle"
      directory: "/"
      schedule:
        interval: "weekly"
      open-pull-requests-limit: 10
      labels:
        - "dependencies"
    - package-ecosystem: "github-actions"
      directory: "/"
      schedule:
        interval: "weekly"
  ```

---

### Authentication & API

- [ ] **API endpoint security**
    - Current: Public gist API (no auth needed)
    - Future: Implement OAuth2/JWT for private APIs

- [ ] **Token storage**
    - Use EncryptedSharedPreferences for auth tokens
    - Never store tokens in plain text

- [ ] **HTTPS enforcement**
  ```kotlin
  // ApplicationModule.kt
  val httpsInterceptor = Interceptor { chain ->
      val request = chain.request()
      if (!request.url.toString().startsWith("https")) {
          throw SecurityException("Cleartext HTTP not allowed")
      }
      chain.proceed(request)
  }
  ```

---

### Content Security

- [ ] **No sensitive data in logs**
  ```kotlin
  // DebugTree logs only in DEBUG
  if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
  }
  ```

- [ ] **Data masking**
  ```kotlin
  // Log user actions without PII
  Timber.d("Search executed: %s chars", query.length)
  // NOT: Timber.d("Search executed: %s", query)
  ```

- [ ] **Screenshot protection** (optional)
  ```kotlin
  // MainActivity.kt
  window.setFlags(
      WindowManager.LayoutParams.FLAG_SECURE,
      WindowManager.LayoutParams.FLAG_SECURE
  )
  ```

---

### Build Security

- [ ] **Release signing**
    - Use upload key (not deploy key)
    - Store keystore password in CI secrets
    - Never commit `.keystore` or `.jks` files

- [ ] **Debug builds**
    - Use separate debug keystore
    - Enable `isDebuggable = false` for release

- [ ] **Build variants**
    - No debug code in release builds
    - Use `BuildConfig.DEBUG` checks

---

## Vulnerability Assessment

| Category         | Risk                | Status | Action                   |
|------------------|---------------------|--------|--------------------------|
| **Network**      | Man-in-the-middle   | Low    | HTTPS enforced           |
| **Data**         | Data leakage        | Low    | No sensitive data stored |
| **Code**         | Reverse engineering | Medium | R8 obfuscation enabled   |
| **Dependencies** | CVEs                | Medium | Run OWASP scan           |
| **Secrets**      | Token leakage       | Low    | .gitignore configured    |
| **Input**        | Injection           | Low    | Query sanitization       |
| **API**          | Unauthorized access | Low    | Public API (no auth)     |

**Overall Risk Level: LOW** (for MVP with public API)

---

## Pre-Launch Checklist

```bash
# 1. Scan for secrets
gitleaks detect --source .

# 2. Run dependency check
./gradlew dependencyCheckAnalyze

# 3. Build release
./gradlew assembleRelease

# 4. Verify obfuscation
# Check mapping.txt in build/outputs/

# 5. Test on physical device
# Verify no debug logs in release
adb logcat | grep "Timber"

# 6. Upload to Play Console
# Security scan will run automatically
```

---

## Future Enhancements (Post-MVP)

| Enhancement                       | Priority | Effort  |
|-----------------------------------|----------|---------|
| App encryption (Jetpack Security) | High     | 1 day   |
| Certificate pinning               | Medium   | 0.5 day |
| OAuth2 authentication             | Low      | 3 days  |
| Biometric authentication          | Low      | 2 days  |
| Data encryption at rest           | Medium   | 1 day   |
| Secure key storage (KeyStore)     | High     | 0.5 day |
