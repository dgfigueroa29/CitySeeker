# Lint Configuration

CitySeeker uses **Detekt** and **ktlint** for static analysis. This document provides the
recommended configuration.

## Current Setup

From `build.gradle.kts`:

```kotlin
plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0" apply true
}
```

**Issue:** No custom `detekt.yml` configuration exists. Using default rules.

---

## Recommended detekt.yml

Create at project root: `config/detekt/detekt.yml`

```yaml
build:
    maxIssues: 20
    weights:
        complexity: 2
        style: 1
        comments: 1
        naming: 1
        empty: 2

complexity:
    LongMethod:
        threshold: 60
        ignoreAnnotated: [ ]
    ComplexCondition:
        threshold: 4
    TooManyFunctions:
        thresholdInFiles: 30
        thresholdInClasses: 30
    ComplexMethod:
        threshold: 15
    LongParameterList:
        functionThreshold: 8
        constructorThreshold: 10
    LargeClass:
        thresholdInFiles: 600
    NestedBlockDepth:
        threshold: 5

style:
    MaxLineLength:
        maxLineLength: 120
        ignoreBackTickIdentifiers: true
    MagicNumber:
        active: true
        ignoreNumbers: "-1,0,1,2,3,4,5,10,100,1000,10000"
        ignoreAnnotated: [ "Preview" ]
    ReturnCount:
        max: 6
        allowedExpressionCount: 2
    ForbiddenComment:
        active: false
    UnusedPrivateMember:
        active: true
    UnnamedPublicClass:
        active: false
    UnnamedPrivateClass:
        active: false

naming:
    FunctionNaming:
        ignoreAnnotated: [ "Composable", "Preview" ]
    TopLevelPropertyNaming:
        constantPattern: "[A-Z][A-Za-z0-9_]*"
    ClassNaming:
        ignoreAnnotated: [ "Entity", "Dao" ]

empty:
    EmptyFunctionBlock:
        ignoreOverridden: true
    EmptyElseBlock:
        active: true
    EmptyWhenBlock:
        active: true
    EmptyCatchBlock:
        active: true
        allowedExceptionNameRegex: "_.*"

exceptions:
    TooGenericExceptionCaught:
        active: true
        exceptionNames:
            - Exception
            - Throwable
        ignoreBlocks:
            - CoroutineScope.launch
            - viewModelScope.launch

coroutines:
    CoroutinesPredictable:
        active: true
    GlobalScopeUsage:
        active: true
    RedundantSuppressAnnotation:
        active: true

formatting:
    active: true
    android: false
    autoCorrect: true

experimental:
    UnusedImportAlias:
        active: true
```

---

## ktlint Configuration

Create at project root: `.editorconfig`

```ini
root = true

[*]
indent_style = space
indent_size = 4
end_of_line = lf
charset = utf-8
trim_trailing_whitespace = true
insert_final_newline = true

[*.{kt,kts}]
max_line_length = 120
ktlint_code_style = android_app
ij_kotlin_keep_line_breaks = true
ij_kotlin_keep_first_column_comment = true
ij_kotlin_keep_control_statement_one_line = true
ij_kotlin_keep_multiple_expressions_on_one_line = false
ij_kotlin_allow_trailing_comma = true
ij_kotlin_allow_trailing_comma_on_call_site = true

[*.{xml,json,yaml,yml}]
indent_size = 2

[*.md]
trim_trailing_whitespace = false

[*.sh]
indent_style = space
indent_size = 2
```

---

## Custom Rules for CitySeeker

### Rule 1: Ban GlobalScope.launch

```yaml
# In detekt.yml
coroutines:
    GlobalScopeUsage:
        active: true
        # Fails on any GlobalScope usage
```

**Suppression (when absolutely necessary):**

```kotlin
@Suppress("GlobalScopeUsage") // Only for app-level crash handlers
```

---

### Rule 2: Enforce UiStateModel in ViewModels

This requires a custom Detekt rule. Create `config/detekt/rules/custom-detekt-rules.jar` or use this
pattern:

```kotlin
// Recommended: Use code review to enforce
// All ViewModels must:
// 1. Return UiStateModel from UseCases
// 2. Never expose raw exceptions to UI
// 3. Use StateFlow<UiStateModel<T>>
```

---

### Rule 3: Limit !! Chains

```yaml
style:
  UnnecessaryLet:
    active: true
  UnnecessaryApply:
    active: true
  SafeCast:
    active: true
  ForbiddenMethodCall:
    active: true
    methods:
      - kotlin.collections.first
      - kotlin.collections.last
      - kotlin.collections.single
      # Ban !! indirectly via code review
```

**Manual Check:**

```kotlin
// Bad: multiple !!
val result = response?.body()?.string()!!

// Good: safe calls
val result = response?.body()?.string() ?: return@withContext emptyList()
```

---

### Rule 4: Enforce @OptIn for ExperimentalCoroutinesApi

```yaml
style:
  RequiredAnnotation:
    active: true
    annotations:
      - kotlinx.coroutines.ExperimentalCoroutinesApi
      - kotlinx.coroutines.FlowPreview
```

---

### Rule 5: Compose Naming Conventions

```yaml
naming:
  FunctionNaming:
    active: true
    ignoreAnnotated:
      - Composable
      - Preview
    ignorePattern: "^(Composable|Preview|_)"
  
  TopLevelPropertyNaming:
    active: true
    constantPattern: "[A-Z][A-Za-z0-9_]*"
```

---

## Detekt Baseline

Generate baseline to track progress:

```bash
# Generate baseline
./gradlew detektBaseline

# Output: config/detekt/baseline.yml
```

Use baseline to suppress existing issues:

```yaml
# config/detekt/baseline.yml
issues:
    - 'LongMethod:com.boa.test.city.seeker.data.source.CityDataSourceImpl.getAllCities'
    - 'ComplexMethod:com.boa.test.city.seeker.data.source.CityDataSourceImpl.processFile'
```

---

## CI Integration

Add to `.github/workflows/ci.yml`:

```yaml
jobs:
    lint:
        runs-on: ubuntu-latest
        steps:
            -   uses: actions/checkout@v4
            -   name: Set up JDK 17
                uses: actions/setup-java@v4
                with:
                    java-version: '17'
                    distribution: 'temurin'

            -   name: Detekt
                run: ./gradlew detekt

            -   name: ktlint
                run: ./gradlew ktlintCheck

            -   name: Detekt Report
                if: failure()
                uses: actions/upload-artifact@v4
                with:
                    name: detekt-report
                    path: build/reports/detekt/
```

---

## VS Code / IntelliJ Integration

### Detekt Plugin

```
Settings → Plugins → Detekt → Install
```

### ktlint Plugin

```
Settings → Plugins → ktlint → Install
```

### Auto-format on Save

```
Settings → Tools → Actions on Save → Reformat Code → Enable
```

---

## Suppression Guidelines

| Annotation                               | When to Use                      | Limit        |
|------------------------------------------|----------------------------------|--------------|
| `@Suppress("MagicNumber")`               | Business constants, API codes    | Per-file     |
| `@Suppress("LongMethod")`                | Complex parsing, Compose screens | Per-function |
| `@Suppress("ComplexCondition")`          | Necessary business logic         | Per-function |
| `@Suppress("NestedBlockDepth")`          | JSON parsing, complex flows      | Per-function |
| `@Suppress("TooGenericExceptionCaught")` | Top-level error handlers         | Per-file     |
| `@Suppress("UnusedPrivateMember")`       | Preview functions, future use    | Per-member   |

**Rule:** Maximum 5 `@Suppress` per file. More requires refactoring.

---

## Running Lint

```bash
# Detekt only
./gradlew detekt

# ktlint only
./gradlew ktlintCheck

# Auto-fix ktlint
./gradlew ktlintFormat

# Full lint suite
./gradlew detekt ktlintCheck

# With baseline
./gradlew detekt --baseline config/detekt/baseline.yml
```

---

## Current Issues Summary

Based on codebase analysis:

| Issue                                              | Count | Severity | Fix                        |
|----------------------------------------------------|-------|----------|----------------------------|
| Missing `@OptIn(ExperimentalCoroutinesApi::class)` | 3     | Medium   | Add annotation             |
| `@Suppress("DEPRECATION")` usage                   | 2     | Low      | Review alternatives        |
| Long parameter lists                               | 2     | Medium   | Extract builder class      |
| Magic numbers                                      | 5     | Low      | Extract constants          |
| Empty catch blocks                                 | 3     | High     | Add logging/error handling |

**Total issues:** ~15  
**Auto-fixable:** ~8  
**Manual fix required:** ~7
