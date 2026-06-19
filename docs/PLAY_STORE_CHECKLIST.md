# Play Store Release Checklist

## Pre-Release Preparation

### 1. Build Configuration

- [ ] Update `versionCode` and `versionName` in `build.gradle.kts`
- [ ] Verify `minSdk` and `targetSdk` are correct
- [ ] Test on multiple device sizes (phone, tablet)
- [ ] Test on both light and dark themes
- [ ] Verify landscape and portrait orientations

### 2. App Signing

- [ ] Generate upload key with `keytool`
- [ ] Configure signing in `build.gradle.kts`
- [ ] Store keystore securely (not in version control)
- [ ] Test signed APK/AAB locally

### 3. Assets & Metadata

- [ ] App icon (512x512 PNG, high-res)
- [ ] Feature graphic (1024x500 PNG)
- [ ] Screenshots (min 2, max 8 per device category)
- [ ] Short description (80 characters max)
- [ ] Full description (4000 characters max)
- [ ] Privacy policy URL
- [ ] Terms of service URL (if applicable)

### 4. Content Rating

- [ ] Complete IARC content rating questionnaire
- [ ] Verify age rating is appropriate
- [ ] Select content descriptors

### 5. Target Audience

- [ ] Select target age groups
- [ ] Verify audience is appropriate for app content
- [ ] Set up parental controls if needed

### 6. Data Safety

- [ ] Declare data collection practices
- [ ] Specify data usage purposes
- [ ] Confirm data sharing practices
- [ ] Link to privacy policy

## Build & Testing

### 7. Build Release AAB

```bash
# Generate signed AAB
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
```

### 8. Test Release Build

- [ ] Install and test on physical device
- [ ] Verify all features work correctly
- [ ] Check for crashes or ANRs
- [ ] Test offline functionality
- [ ] Verify Mapbox integration
- [ ] Test search and filtering
- [ ] Verify favorites persistence
- [ ] Check animations and transitions
- [ ] Test screen rotation
- [ ] Verify accessibility features

### 9. Performance Testing

- [ ] Run Macrobenchmark tests
- [ ] Check startup time (cold, warm)
- [ ] Verify scroll performance
- [ ] Test search responsiveness
- [ ] Monitor memory usage
- [ ] Check battery impact

## Upload & Publishing

### 10. Google Play Console Setup

- [ ] Create new app in Play Console
- [ ] Select default language
- [ ] Set app type (app/game)
- [ ] Choose free or paid model

### 11. Store Listing

- [ ] Upload app icon
- [ ] Upload feature graphic
- [ ] Add screenshots for each device category
- [ ] Write short description
- [ ] Write full description
- [ ] Add developer contact info

### 12. Content Declaration

- [ ] Complete content rating
- [ ] Set target audience
- [ ] Complete data safety section
- [ ] Add privacy policy link

### 13. App Access

- [ ] Set app availability (all countries or specific)
- [ ] Configure pricing (if paid)
- [ ] Set up in-app products (if applicable)

### 14. Review & Submit

- [ ] Review all sections for completeness
- [ ] Verify no policy violations
- [ ] Submit for review
- [ ] Monitor review status

## Post-Release

### 15. Monitoring

- [ ] Monitor crash reports in Sentry
- [ ] Check user reviews and ratings
- [ ] Respond to user feedback
- [ ] Monitor performance metrics

### 16. Updates

- [ ] Plan regular updates
- [ ] Address user feedback
- [ ] Fix bugs promptly
- [ ] Add new features based on demand

## Key Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release AAB (signed)
./gradlew bundleRelease

# Build release APK (for testing)
./gradlew assembleRelease

# Run all tests
./gradlew test

# Run detekt
./gradlew detekt

# Run benchmarks
./gradlew :benchmark:connectedBenchmarkAndroidTest
```

## Signing Configuration

### Generate Upload Key

```bash
keytool -genkeypair -v -storetype PKCS12 -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

### Configure in build.gradle.kts

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("upload-keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
}
```

## Environment Variables

```bash
# Required for release builds
export KEYSTORE_PATH="path/to/upload-keystore.jks"
export KEYSTORE_PASSWORD="your-keystore-password"
export KEY_ALIAS="upload"
export KEY_PASSWORD="your-key-password"
export MAPBOX_TOKEN="your-mapbox-token"
export SENTRY_DSN="your-sentry-dsn"
```
