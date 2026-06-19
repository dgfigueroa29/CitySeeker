# Deployment Guide

This document covers build variants, versioning, and release process for CitySeeker.

## Build Variants

### Current Configuration

| Variant   | Build Type | Debuggable | Minify | Signing          |
|-----------|------------|------------|--------|------------------|
| `debug`   | debug      | Yes        | No     | Debug keystore   |
| `release` | release    | No         | Yes    | Release keystore |

### Recommended Configuration

Add `dev` and `qa` variants for development workflow:

```kotlin
// app/build.gradle.kts
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
            buildConfigField("String", "ENVIRONMENT", "\"development\"")
        }

        dev {
            initWith(buildTypes.debug)
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "ENVIRONMENT", "\"development\"")
            buildConfigField("String", "BACK_OFFICE_URL", "\"https://dev-api.cityseeker.com\"")
        }

        qa {
            initWith(buildTypes.release)
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"
            isMinifyEnabled = true
            buildConfigField("String", "ENVIRONMENT", "\"qa\"")
            buildConfigField("String", "BACK_OFFICE_URL", "\"https://qa-api.cityseeker.com\"")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "ENVIRONMENT", "\"production\"")
        }
    }

    // Variant filtering (optional)
    variantFilter { variant ->
        val name = variant.name
        if (name == "release" && com.android.build.api.dsl.BuildType.DEBUG == variant.buildType) {
            setIgnore(true)
        }
    }
}
```

### Variant Output

```
app/
├── build/
│   └── outputs/
│       ├── apk/
│       │   ├── debug/
│       │   │   └── app-debug.apk
│       │   ├── dev/
│       │   │   └── app-dev.apk
│       │   ├── qa/
│       │   │   └── app-qa.apk
│       │   └── release/
│       │       └── app-release.apk
│       └── bundle/
│           └── release/
│               └── app-release.aab
```

---

## Versioning Strategy

### Semantic Versioning

Format: `MAJOR.MINOR.PATCH` (e.g., `1.2.3`)

| Component | Rule                              | Example       |
|-----------|-----------------------------------|---------------|
| MAJOR     | Breaking changes, data migration  | 1.0.0 → 2.0.0 |
| MINOR     | New features, backward compatible | 1.0.0 → 1.1.0 |
| PATCH     | Bug fixes, performance            | 1.0.0 → 1.0.1 |

### Version Code

`versionCode` must increase monotonically for Play Store:

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        versionCode = 1  // Increment for each release
        versionName = "1.0.0"
    }
}
```

### Version Bumping Script

```bash
#!/bin/bash
# scripts/bump_version.sh

BUMP_TYPE=$1  # major, minor, patch

if [ -z "$BUMP_TYPE" ]; then
    echo "Usage: ./scripts/bump_version.sh [major|minor|patch]"
    exit 1
fi

# Read current version
CURRENT_VERSION=$(grep "versionName" app/build.gradle.kts | awk -F '"' '{print $2}')
VERSION_CODE=$(grep "versionCode" app/build.gradle.kts | awk '{print $2}')

# Parse version
MAJOR=$(echo $CURRENT_VERSION | cut -d. -f1)
MINOR=$(echo $CURRENT_VERSION | cut -d. -f2)
PATCH=$(echo $CURRENT_VERSION | cut -d. -f3)

# Bump version
case $BUMP_TYPE in
    major)
        MAJOR=$((MAJOR + 1))
        MINOR=0
        PATCH=0
        ;;
    minor)
        MINOR=$((MINOR + 1))
        PATCH=0
        ;;
    patch)
        PATCH=$((PATCH + 1))
        ;;
    *)
        echo "Invalid bump type"
        exit 1
        ;;
esac

NEW_VERSION="$MAJOR.$MINOR.$PATCH"
NEW_VERSION_CODE=$((VERSION_CODE + 1))

# Update files
sed -i "s/versionCode = $VERSION_CODE/versionCode = $NEW_VERSION_CODE/" app/build.gradle.kts
sed -i "s/versionName = \"$CURRENT_VERSION\"/versionName = \"$NEW_VERSION\"/" app/build.gradle.kts

echo "Version bumped: $CURRENT_VERSION → $NEW_VERSION (code: $NEW_VERSION_CODE)"
```

---

## Signing Configuration

### Debug Signing (Automatic)

Android Studio auto-generates debug keystore at `~/.android/debug.keystore`.

### Release Signing

```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### Generate Keystore

```bash
keytool -genkeypair \
    -alias cityseeker \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -keystore release.keystore \
    -storepass YOUR_PASSWORD \
    -keypass YOUR_KEY_PASSWORD \
    -dname "CN=CitySeeker, O=YourOrg, L=City, S=State, C=US"
```

### Keystore Security

```
# .gitignore
*.keystore
*.jks
release.keystore
```

---

## Play Store Deployment

### Step 1: Build Release AAB

```bash
# Clean build
./gradlew clean

# Build release bundle
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
```

### Step 2: Sign Bundle

```bash
# Use bundletool to sign
java -jar bundletool.jar sign \
    --ks=release.keystore \
    --ks-pass=pass:YOUR_PASSWORD \
    --ks-key-alias=cityseeker \
    --key-pass=pass:YOUR_KEY_PASSWORD \
    --out=app-release-signed.aab \
    --bundle=app/build/outputs/bundle/release/app-release.aab
```

### Step 3: Generate APKs for Testing

```bash
# Generate universal APK for testing
java -jar bundletool.jar build-apks \
    --bundle=app-release-signed.aab \
    --output=app-release.apks \
    --ks=release.keystore \
    --ks-pass=pass:YOUR_PASSWORD \
    --ks-key-alias=cityseeker \
    --key-pass=pass:YOUR_KEY_PASSWORD

# Install on device
adb install --r app-release.apks
```

### Step 4: Upload to Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Select app → Production → Create new release
3. Upload `app-release.aab`
4. Add release notes
5. Roll out to internal track first

### Rollout Stages

```
Internal Testing (1-2 days)
    ↓
Closed Testing (Alpha) (3-5 days)
    ↓
Open Testing (Beta) (7 days)
    ↓
Production (Phased rollout)
    ├── 10% traffic (1 day)
    ├── 50% traffic (2 days)
    ├── 100% traffic
```

---

## CI/CD Deployment

### GitHub Actions Deployment

```yaml
# .github/workflows/deploy.yml
name: Deploy to Play Store

on:
    push:
        tags:
            - 'v*'

jobs:
    deploy:
        runs-on: ubuntu-latest
        steps:
            -   uses: actions/checkout@v4

            -   name: Set up JDK 17
                uses: actions/setup-java@v4
                with:
                    java-version: '17'
                    distribution: 'temurin'

            -   name: Decode Keystore
                run: echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > release.keystore

            -   name: Build Release
                env:
                    KEYSTORE_PATH: release.keystore
                    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
                    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
                    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
                run: ./gradlew bundleRelease

            -   name: Upload to Play Store
                uses: r0adkll/upload-google-play@v1
                with:
                    serviceAccountJsonPlainText: ${{ secrets.PLAY_SERVICE_ACCOUNT }}
                    packageName: com.boa.test.city.seeker
                    releaseFiles: app/build/outputs/bundle/release/app-release.aab
                    track: internal
                    status: completed
```

### Fastlane (Alternative)

```ruby
# fastlane/Fastfile
default_platform(:android)

platform :android do
  desc "Build and upload to Play Store"
  lane :deploy do
    # Build
    gradle(
      task: "bundle",
      build_type: "Release"
    )
    
    # Upload
    upload_to_play_store(
      track: "internal",
      aab: "app/build/outputs/bundle/release/app-release.aab"
    )
  end
  
  desc "Build and upload to internal testing"
  lane :beta do
    gradle(
      task: "bundle",
      build_type: "Release"
    )
    
    upload_to_play_store(
      track: "internal",
      aab: "app/build/outputs/bundle/release/app-release.aab",
      rollout: "10%"
    )
  end
end
```

---

## Release Checklist

### Pre-Release

- [ ] Version bump (versionCode + versionName)
- [ ] All tests passing (`./gradlew test`)
- [ ] Lint clean (`./gradlew detekt ktlintCheck`)
- [ ] ProGuard rules verified
- [ ] No debug logs in release
- [ ] API endpoints correct (production URLs)
- [ ] Mapbox token for production
- [ ] SENTRY_DSN for production

### Build

- [ ] Clean build (`./gradlew clean`)
- [ ] Build release AAB (`./gradlew bundleRelease`)
- [ ] Verify APK size (< 10MB)
- [ ] Test on physical device
- [ ] Test on API 31, 33, 36

### Deploy

- [ ] Upload to internal track
- [ ] Test internal release (1 day)
- [ ] Promote to closed testing (alpha)
- [ ] Test closed testing (3-5 days)
- [ ] Promote to open testing (beta)
- [ ] Monitor crash rate (< 1%)
- [ ] Promote to production
- [ ] Phased rollout (10% → 50% → 100%)

### Post-Release

- [ ] Monitor Play Console Vitals
- [ ] Check Sentry for errors
- [ ] Monitor user reviews
- [ ] Tag release in git (`git tag v1.0.0`)
- [ ] Update release notes

---

## Rollback Procedure

### If Crash Rate > 1%

1. **Pause rollout** in Play Console
2. **Identify issue** from Sentry/Play Console
3. **Fix and release** hotfix version
4. **Resume rollout** or **rollback** to previous version

### Rollback via Play Console

1. Go to Production → Releases
2. Select previous stable version
3. Click "Rollback"
4. Confirm rollback
5. Monitor crash rate

### Emergency Rollback

```bash
# If automated, use Fastlane
fastlane android rollback

# Or via Play Console API
# Reference: https://developers.google.com/android-publisher/rollback
```

---

## Environment Configuration

### Build Config Fields

| Field             | debug         | dev           | qa      | release      |
|-------------------|---------------|---------------|---------|--------------|
| `ENVIRONMENT`     | "development" | "development" | "qa"    | "production" |
| `CITIES_URL`      | gist          | gist          | gist    | gist         |
| `MAPBOX_TOKEN`    | from local    | from local    | from CI | from CI      |
| `SENTRY_DSN`      | from local    | from local    | from CI | from CI      |
| `BACK_OFFICE_URL` | localhost     | dev           | staging | production   |

### CI/CD Secrets

| Secret               | GitHub Actions | Bitrise | CircleCI |
|----------------------|----------------|---------|----------|
| MAPBOX_TOKEN         | ✓              | ✓       | ✓        |
| KEYSTORE_BASE64      | ✓              | ✓       | ✓        |
| KEYSTORE_PASSWORD    | ✓              | ✓       | ✓        |
| KEY_ALIAS            | ✓              | ✓       | ✓        |
| KEY_PASSWORD         | ✓              | ✓       | ✓        |
| PLAY_SERVICE_ACCOUNT | ✓              | ✓       | ✓        |
| SENTRY_DSN           | ✓              | ✓       | ✓        |

---

## Monitoring After Release

| Metric         | Source       | Alert Threshold |
|----------------|--------------|-----------------|
| Crash rate     | Play Console | > 1%            |
| ANR rate       | Play Console | > 0.5%          |
| Startup time   | Play Console | > 3s            |
| Network errors | Sentry       | > 5%            |
| User reviews   | Play Console | < 3.5 stars     |

---

## Quick Reference Commands

```bash
# Version bump
./scripts/bump_version.sh patch  # 1.0.0 → 1.0.1

# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease

# Build AAB
./gradlew bundleRelease

# Run tests
./gradlew test

# Lint check
./gradlew detekt ktlintCheck

# Generate APKs from AAB
java -jar bundletool.jar build-apks \
    --bundle=app-release.aab \
    --output=app.apks

# Install on device
adb install -r app-release.apk
```
