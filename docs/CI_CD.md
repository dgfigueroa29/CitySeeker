# CI/CD Configuration

This document provides CI/CD pipeline configurations for CitySeeker with GitHub Actions, Bitrise,
and CircleCI.

## Pipeline Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        CI/CD PIPELINE                           │
├─────────────────────────────────────────────────────────────────┤
│  Stage 1      Stage 2      Stage 3      Stage 4      Stage 5   │
│  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐  │
│  │ Lint   │→ │ Test   │→ │ Build  │→ │ Bundle │→ │ Deploy │  │
│  │ Check  │  │        │  │ Debug  │  │ Release│  │        │  │
│  └────────┘  └────────┘  └────────┘  └────────┘  └────────┘  │
│  detekt      unit test   APK         AAB          Play Store  │
│  ktlint      coverage    artifact    signed       internal    │
└─────────────────────────────────────────────────────────────────┘
```

---

## GitHub Actions

### Primary Workflow: `.github/workflows/ci.yml`

```yaml
name: CI/CD Pipeline

on:
    push:
        branches: [ main, develop, 'feature/**', 'bugfix/**' ]
    pull_request:
        branches: [ main, develop ]

env:
    JAVA_VERSION: '17'
    GRADLE_VERSION: '8.12'

jobs:
    # ──────────────────────────────────────────────
    # Stage 1: Lint & Static Analysis
    # ──────────────────────────────────────────────
    lint:
        name: Lint Check
        runs-on: ubuntu-latest
        steps:
            -   name: Checkout
                uses: actions/checkout@v4

            -   name: Set up JDK ${{ env.JAVA_VERSION }}
                uses: actions/setup-java@v4
                with:
                    java-version: ${{ env.JAVA_VERSION }}
                    distribution: 'temurin'

            -   name: Setup Gradle
                uses: gradle/actions/setup-gradle@v4

            -   name: Cache Gradle
                uses: actions/cache@v4
                with:
                    path: |
                        ~/.gradle/caches
                        ~/.gradle/wrapper
                    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle.kts') }}
                    restore-keys: |
                        ${{ runner.os }}-gradle-

            -   name: Detekt
                run: ./gradlew detekt

            -   name: ktlint
                run: ./gradlew ktlintCheck

            -   name: Upload Detekt Report
                if: always()
                uses: actions/upload-artifact@v4
                with:
                    name: detekt-report
                    path: build/reports/detekt/

    # ──────────────────────────────────────────────
    # Stage 2: Unit Tests
    # ──────────────────────────────────────────────
    test:
        name: Unit Tests
        runs-on: ubuntu-latest
        needs: lint
        steps:
            -   name: Checkout
                uses: actions/checkout@v4

            -   name: Set up JDK ${{ env.JAVA_VERSION }}
                uses: actions/setup-java@v4
                with:
                    java-version: ${{ env.JAVA_VERSION }}
                    distribution: 'temurin'

            -   name: Setup Gradle
                uses: gradle/actions/setup-gradle@v4

            -   name: Run Tests
                run: ./gradlew testDebugUnitTest

            -   name: Generate Coverage Report
                run: ./gradlew koverXmlReport

            -   name: Upload Test Results
                if: always()
                uses: actions/upload-artifact@v4
                with:
                    name: test-results
                    path: app/build/reports/tests/

            -   name: Upload Coverage Report
                if: always()
                uses: actions/upload-artifact@v4
                with:
                    name: coverage-report
                    path: app/build/reports/kover/xml/

            -   name: Code Coverage Check
                if: github.ref == 'refs/heads/main'
                uses: codecov/codecov-action@v4
                with:
                    file: app/build/reports/kover/xml/coverage.xml
                    token: ${{ secrets.CODECOV_TOKEN }}
                    fail_ci_if_error: false

    # ──────────────────────────────────────────────
    # Stage 3: Build Debug APK
    # ──────────────────────────────────────────────
    build-debug:
        name: Build Debug APK
        runs-on: ubuntu-latest
        needs: test
        steps:
            -   name: Checkout
                uses: actions/checkout@v4

            -   name: Set up JDK ${{ env.JAVA_VERSION }}
                uses: actions/setup-java@v4
                with:
                    java-version: ${{ env.JAVA_VERSION }}
                    distribution: 'temurin'

            -   name: Setup Gradle
                uses: gradle/actions/setup-gradle@v4

            -   name: Build Debug APK
                env:
                    MAPBOX_TOKEN: ${{ secrets.MAPBOX_TOKEN }}
                run: ./gradlew assembleDebug

            -   name: Upload Debug APK
                uses: actions/upload-artifact@v4
                with:
                    name: debug-apk
                    path: app/build/outputs/apk/debug/

    # ──────────────────────────────────────────────
    # Stage 4: Build Release Bundle
    # ──────────────────────────────────────────────
    build-release:
        name: Build Release Bundle
        runs-on: ubuntu-latest
        needs: test
        if: github.ref == 'refs/heads/main' || startsWith(github.ref, 'refs/tags/v')
        steps:
            -   name: Checkout
                uses: actions/checkout@v4

            -   name: Set up JDK ${{ env.JAVA_VERSION }}
                uses: actions/setup-java@v4
                with:
                    java-version: ${{ env.JAVA_VERSION }}
                    distribution: 'temurin'

            -   name: Setup Gradle
                uses: gradle/actions/setup-gradle@v4

            -   name: Decode Keystore
                if: startsWith(github.ref, 'refs/tags/v')
                run: echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > release.keystore

            -   name: Build Release AAB
                if: startsWith(github.ref, 'refs/tags/v')
                env:
                    MAPBOX_TOKEN: ${{ secrets.MAPBOX_TOKEN }}
                    KEYSTORE_PATH: release.keystore
                    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
                    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
                    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
                run: ./gradlew bundleRelease

            -   name: Build Release APK (unsigned)
                if: github.ref == 'refs/heads/main'
                env:
                    MAPBOX_TOKEN: ${{ secrets.MAPBOX_TOKEN }}
                run: ./gradlew assembleRelease

            -   name: Upload Release Artifact
                uses: actions/upload-artifact@v4
                with:
                    name: release-bundle
                    path: |
                        app/build/outputs/bundle/release/
                        app/build/outputs/apk/release/

    # ──────────────────────────────────────────────
    # Stage 5: Deploy to Play Store
    # ──────────────────────────────────────────────
    deploy:
        name: Deploy to Play Store
        runs-on: ubuntu-latest
        needs: build-release
        if: startsWith(github.ref, 'refs/tags/v')
        steps:
            -   name: Checkout
                uses: actions/checkout@v4

            -   name: Set up JDK ${{ env.JAVA_VERSION }}
                uses: actions/setup-java@v4
                with:
                    java-version: ${{ env.JAVA_VERSION }}
                    distribution: 'temurin'

            -   name: Setup Gradle
                uses: gradle/actions/setup-gradle@v4

            -   name: Download Release Bundle
                uses: actions/download-artifact@v4
                with:
                    name: release-bundle

            -   name: Upload to Play Store (Internal)
                uses: r0adkll/upload-google-play@v1
                with:
                    serviceAccountJsonPlainText: ${{ secrets.PLAY_SERVICE_ACCOUNT }}
                    packageName: com.boa.test.city.seeker
                    releaseFiles: app-release.aab
                    track: internal
                    status: completed

            -   name: Create GitHub Release
                uses: softprops/action-gh-release@v2
                with:
                    files: |
                        app/build/outputs/bundle/release/app-release.aab
                    generate_release_notes: true
```

---

## Bitrise Configuration

### `bitrise.yml`

```yaml
---
format_version: "11"
default_step_lib_source: https://github.com/bitrise-io/bitrise-steplib.git

project_type: android

app:
    envs:
        -   GRADLEW_PATH: "./gradlew"
        -   GRADLE_BUILD_FILE_PATH: "build.gradle.kts"
        -   MODULE: "app"
        -   VARIANT: "release"

trigger_map:
    -   push_branch: main
        workflow: deploy
    -   push_branch: develop
        workflow: test
    -   pull_request_source_branch: "*"
        workflow: test
    -   tag: "v*"
        workflow: deploy

workflows:
    # ──────────────────────────────────────────────
    # Test Workflow
    # ──────────────────────────────────────────────
    test:
        description: "Run lint and tests"
        steps:
            -   activate-ssh-key@4:
                    run_if: '{{getenv "SSH_RSA_PRIVATE_KEY" | ne ""}}'
            -   git-clone@8: { }
            -   cache-pull@2: { }

            -   install-missing-android-tools@3:
                    title: Install Android SDK
                    inputs:
                        -   gradlew_path: $GRADLEW_PATH

            -   android-lint@0:
                    title: Detekt
                    inputs:
                        -   project_location: .
                        -   module: ""
                        -   variant: ""
                        -   arguments: "detekt"

            -   android-lint@0:
                    title: ktlint
                    inputs:
                        -   project_location: .
                        -   module: ""
                        -   variant: ""
                        -   arguments: "ktlintCheck"

            -   android-unit-test@1:
                    title: Unit Tests
                    inputs:
                        -   project_location: .
                        -   module: "app"
                        -   variant: "debug"

            -   cache-push@2: { }

    # ──────────────────────────────────────────────
    # Deploy Workflow
    # ──────────────────────────────────────────────
    deploy:
        description: "Build and deploy to Play Store"
        steps:
            -   activate-ssh-key@4:
                    run_if: '{{getenv "SSH_RSA_PRIVATE_KEY" | ne ""}}'
            -   git-clone@8: { }
            -   cache-pull@2: { }

            -   install-missing-android-tools@3:
                    title: Install Android SDK

            -   android-build@0:
                    title: Build Release AAB
                    inputs:
                        -   project_location: .
                        -   module: "app"
                        -   variant: "release"

            -   deploy-to-google-play@1:
                    title: Deploy to Play Store
                    inputs:
                        -   service_account_json_key_path: $BITRISEIO_SERVICE_ACCOUNT_JSON_KEY_URL
                        -   package_name: com.boa.test.city.seeker
                        -   apk_path: app/build/outputs/bundle/release/app-release.aab
                        -   track: internal
                        -   status: completed

            -   cache-push@2: { }
```

### Bitrise Secrets

Set in Bitrise → Workflow → Environment Variables:

```
MAPBOX_TOKEN=<your-mapbox-token>
SENTRY_DSN=<your-sentry-dsn>
KEYSTORE_BASE64=<base64-encoded-keystore>
KEYSTORE_PASSWORD=<keystore-password>
KEY_ALIAS=<key-alias>
KEY_PASSWORD=<key-password>
SERVICE_ACCOUNT_JSON_KEY_URL=<bitrise-file-key-url>
```

---

## CircleCI Configuration

### `.circleci/config.yml`

```yaml
version: 2.1

orbs:
    android: circleci/android@2.5.0
    gradle: circleci/gradle@6.0.0

executors:
    android-executor:
        docker:
            -   image: cimg/android:2024.12
        resource_class: large
        environment:
            GRADLE_OPTS: -Dorg.gradle.daemon=false -Dorg.gradle.workers.max=2
            TERM: dumb

jobs:
    # ──────────────────────────────────────────────
    # Lint Check
    # ──────────────────────────────────────────────
    lint:
        executor: android-executor
        steps:
            - checkout
            -   restore_cache:
                    keys:
                        - gradle-{{ checksum "build.gradle.kts" }}
                        - gradle-
            -   run:
                    name: Detekt
                    command: ./gradlew detekt
            -   run:
                    name: ktlint
                    command: ./gradlew ktlintCheck
            -   save_cache:
                    key: gradle-{{ checksum "build.gradle.kts" }}
                    paths:
                        - ~/.gradle/caches
                        - ~/.gradle/wrapper
            -   store_artifacts:
                    path: build/reports/detekt
                    destination: detekt

    # ──────────────────────────────────────────────
    # Unit Tests
    # ──────────────────────────────────────────────
    test:
        executor: android-executor
        steps:
            - checkout
            -   restore_cache:
                    keys:
                        - gradle-{{ checksum "build.gradle.kts" }}
                        - gradle-
            -   run:
                    name: Unit Tests
                    command: ./gradlew testDebugUnitTest
            -   run:
                    name: Coverage Report
                    command: ./gradlew koverXmlReport
            -   save_cache:
                    key: gradle-{{ checksum "build.gradle.kts" }}
                    paths:
                        - ~/.gradle/caches
                        - ~/.gradle/wrapper
            -   store_test_results:
                    path: app/build/reports/tests
            -   store_artifacts:
                    path: app/build/reports/kover/xml
                    destination: coverage

    # ──────────────────────────────────────────────
    # Build Debug
    # ──────────────────────────────────────────────
    build-debug:
        executor: android-executor
        steps:
            - checkout
            -   restore_cache:
                    keys:
                        - gradle-{{ checksum "build.gradle.kts" }}
                        - gradle-
            -   run:
                    name: Build Debug APK
                    command: ./gradlew assembleDebug
                    no_output_timeout: 15m
            -   save_cache:
                    key: gradle-{{ checksum "build.gradle.kts" }}
                    paths:
                        - ~/.gradle/caches
                        - ~/.gradle/wrapper
            -   store_artifacts:
                    path: app/build/outputs/apk/debug
                    destination: debug-apk

    # ──────────────────────────────────────────────
    # Build Release
    # ──────────────────────────────────────────────
    build-release:
        executor: android-executor
        steps:
            - checkout
            -   restore_cache:
                    keys:
                        - gradle-{{ checksum "build.gradle.kts" }}
                        - gradle-
            -   run:
                    name: Decode Keystore
                    command: echo $KEYSTORE_BASE64 | base64 -d > release.keystore
            -   run:
                    name: Build Release AAB
                    command: ./gradlew bundleRelease
                    no_output_timeout: 15m
            -   save_cache:
                    key: gradle-{{ checksum "build.gradle.kts" }}
                    paths:
                        - ~/.gradle/caches
                        - ~/.gradle/wrapper
            -   store_artifacts:
                    path: app/build/outputs/bundle/release
                    destination: release-bundle

    # ──────────────────────────────────────────────
    # Deploy to Play Store
    # ──────────────────────────────────────────────
    deploy:
        executor: android-executor
        steps:
            - checkout
            -   run:
                    name: Install Play CLI
                    command: |
                        curl -o google-play-cli https://github.com/nicolo-ribaudo/play-cli/releases/download/v1.0.0/play-cli-linux-amd64
                        chmod +x google-play-cli
            -   run:
                    name: Deploy to Play Store
                    command: |
                        ./google-play-cli upload \
                          --package com.boa.test.city.seeker \
                          --track internal \
                          --aab app/build/outputs/bundle/release/app-release.aab \
                          --service-account $PLAY_SERVICE_ACCOUNT

workflows:
    # ──────────────────────────────────────────────
    # PR / Push to develop
    # ──────────────────────────────────────────────
    test:
        jobs:
            - lint
            -   test:
                    requires:
                        - lint
            -   build-debug:
                    requires:
                        - test

    # ──────────────────────────────────────────────
    # Tag push (release)
    # ──────────────────────────────────────────────
    release:
        jobs:
            - lint
            -   test:
                    requires:
                        - lint
            -   build-release:
                    requires:
                        - test
                    filters:
                        branches:
                            ignore: /.*/
                        tags:
                            only: /^v.*/
            -   deploy:
                    requires:
                        - build-release
                    filters:
                        branches:
                            ignore: /.*/
                        tags:
                            only: /^v.*/
```

### CircleCI Environment Variables

Set in CircleCI → Project → Settings → Environment Variables:

```
MAPBOX_TOKEN=<your-mapbox-token>
SENTRY_DSN=<your-sentry-dsn>
KEYSTORE_BASE64=<base64-encoded-keystore>
KEYSTORE_PASSWORD=<keystore-password>
KEY_ALIAS=<key-alias>
KEY_PASSWORD=<key-password>
PLAY_SERVICE_ACCOUNT=<service-account-json>
```

---

## Secrets Matrix

| Secret                 | Description                      | GitHub Actions                 | Bitrise  | CircleCI |
|------------------------|----------------------------------|--------------------------------|----------|----------|
| `MAPBOX_TOKEN`         | Mapbox public token              | `secrets.MAPBOX_TOKEN`         | Env Var  | Env Var  |
| `SENTRY_DSN`           | Sentry DSN                       | `secrets.SENTRY_DSN`           | Env Var  | Env Var  |
| `KEYSTORE_BASE64`      | Base64 encoded keystore          | `secrets.KEYSTORE_BASE64`      | File key | Env Var  |
| `KEYSTORE_PASSWORD`    | Keystore password                | `secrets.KEYSTORE_PASSWORD`    | Env Var  | Env Var  |
| `KEY_ALIAS`            | Key alias                        | `secrets.KEY_ALIAS`            | Env Var  | Env Var  |
| `KEY_PASSWORD`         | Key password                     | `secrets.KEY_PASSWORD`         | Env Var  | Env Var  |
| `PLAY_SERVICE_ACCOUNT` | Google Play service account JSON | `secrets.PLAY_SERVICE_ACCOUNT` | File key | Env Var  |
| `CODECOV_TOKEN`        | Codecov token (optional)         | `secrets.CODECOV_TOKEN`        | N/A      | N/A      |

---

## Coverage Thresholds

Configure in `build.gradle.kts`:

```kotlin
// Add Kover plugin
plugins {
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

// Configure coverage thresholds
koverReport {
    xml {
        totalFile.set(project.layout.buildDirectory.file("reports/kover/coverage.xml"))
    }
}
```

### Minimum Coverage Targets

| Metric               | Target | Fail CI if below |
|----------------------|--------|------------------|
| Line coverage        | 80%    | Yes              |
| Branch coverage      | 70%    | Yes              |
| Instruction coverage | 80%    | Yes              |

---

## Dependency Update Automation

### Renovate (Recommended)

Create `renovate.json`:

```json
{
    "$schema": "https://docs.renovatebot.com/renovate-schema.json",
    "extends": [
        "config:base"
    ],
    "packageRules": [
        {
            "matchUpdateTypes": [
                "patch"
            ],
            "automerge": true
        },
        {
            "matchUpdateTypes": [
                "minor"
            ],
            "automerge": true
        },
        {
            "matchUpdateTypes": [
                "major"
            ],
            "automerge": false
        }
    ]
}
```

### Dependabot (GitHub Native)

Create `.github/dependabot.yml`:

```yaml
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

## Quick Reference Commands

```bash
# Local lint check
./gradlew detekt ktlintCheck

# Auto-fix ktlint
./gradlew ktlintFormat

# Run tests
./gradlew testDebugUnitTest

# Generate coverage
./gradlew koverXmlReport

# Build debug
./gradlew assembleDebug

# Build release
./gradlew bundleRelease

# Tag release
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

---

## CI/CD Pipeline Status Badges

Add to `README.md`:

```markdown
## CI/CD Status

### GitHub Actions
![CI](https://github.com/your-org/CitySeeker/actions/workflows/ci.yml/badge.svg)

### Bitrise
[![Build Status](https://app.bitrise.io/app/YOUR_APP_SLUG/status.svg?token=YOUR_TOKEN&branch=main)](https://app.bitrise.io/app/YOUR_APP_SLUG)

### CircleCI
[![CircleCI](https://circleci.com/gh/your-org/CitySeeker.svg?style=svg)](https://circleci.com/gh/your-org/CitySeeker)
```
