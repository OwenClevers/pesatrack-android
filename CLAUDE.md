# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build file interference — important

Android Studio's AGP Upgrade Assistant repeatedly re-injects
`coreKtxVersion = "1.19.0"` into `gradle/libs.versions.toml` and
`implementation(libs.core.ktx)` into `android/app/build.gradle.kts`.
That version requires compileSdk 37 and AGP 9.1.0, and breaks the build.
If either appears, delete it — the correct entry is `androidx-core-ktx`
on `coreKtx = "1.10.1"`. This has recurred four times. Always check
`git status` for unexpected changes to build files before building.

## Design rules

No emoji anywhere in the UI — stroke icons only (Material Symbols
Outlined). Sentence case for all labels and buttons.

## Project overview

PesaTrack is a native Android personal-finance tracker (Kotlin + Jetpack Compose), targeting Kenyan users ("Track every shilling", KSh currency formatting, M-Pesa SMS as a planned transaction source). The app currently lives entirely under `android/`; `backend/`, `database/`, `design/`, `infrastructure/`, and `scripts/` at the repo root are placeholders for future work and are currently empty.

`docs/pesatrack-design-spec.html` is the canonical UI design spec (colors, typography, component specs) — check it before making visual/theme changes.

## Commands

All commands run from the `android/` directory.

```bash
./gradlew assembleDebug          # build debug APK
./gradlew build                  # full build (compile + lint + test)
./gradlew test                   # unit tests (JVM, app/src/test)
./gradlew testDebugUnitTest      # unit tests for debug variant only
./gradlew connectedAndroidTest   # instrumented tests (app/src/androidTest), needs device/emulator
./gradlew lint                   # Android lint
```

Run a single test class/method with `--tests`:

```bash
./gradlew test --tests "com.pesatrack.app.SomeTest"
./gradlew test --tests "com.pesatrack.app.SomeTest.someMethod"
```

There is no CI workflow configured yet (`.github/` has no workflow files).

## Architecture

Standard clean-architecture layering under `android/app/src/main/java/com/pesatrack/app/`:

- **`domain/`** — plain Kotlin models (`Transaction`, `TransactionType`, `TransactionSource`, `Category`) and repository interfaces (`TransactionRepository`). No Android/Room dependencies.
- **`data/`** — Room implementation: `database/entity` (Room entities), `database/dao`, `database/converters` (type converters, e.g. `LocalDateTime` <-> String), `database/AppDatabase`, `mapper/` (`Entity <-> domain model` extension functions, e.g. `TransactionEntity.toDomain()` / `Transaction.toEntity()`), and `repository/` (repository interface implementations that map DAO flows to domain models).
- **`presentation/`** — one package per screen (e.g. `dashboard/`, `splash/`), each with a `*Screen.kt` (Composable), and where needed a `*ViewModel.kt` + `*UiState.kt`. ViewModels expose a single `StateFlow<UiState>` built via `.stateIn(viewModelScope, WhileSubscribed(5_000), ...)`.
- **`navigation/`** — `Screen.kt` (sealed class of routes) and `AppNavigation.kt` (single `NavHost`).
- **`ui/theme/`** — Compose theme: `Color.kt`/`PesaTrackColors.kt` (custom semantic colors exposed via `LocalPesaTrackColors` CompositionLocal, e.g. `expense`, `income`, `accent`), `Theme.kt` (`PesaTrackTheme` wrapping Material3 `lightColorScheme`), `Type.kt`, and `components/` for shared themed composables (e.g. `CategoryVisuals.kt` maps a `Category` to an icon + color pair, `TransactionRow.kt`).
- **`di/AppModule.kt`** — manual dependency injection (no Hilt/Koin). A plain `object` with double-checked-locking singleton providers (`provideDatabase(context)`, `provideTransactionRepository(context)`). ViewModels are wired via a `ViewModelProvider.Factory` nested in the ViewModel class (see `DashboardViewModel.Factory`), constructed at the call site with dependencies from `AppModule`.
- **`core/`** — small app-wide utilities: `Constants.kt`, `Formatters.kt` (e.g. `formatKsh`).

When adding a new feature, follow this same shape: domain model + repository interface → Room entity/DAO/mapper/repository impl → provider in `AppModule` → ViewModel/UiState → Composable screen → route in `Screen`/`AppNavigation`.

### Data layer notes

- Room schema is exported to `android/app/schemas` (`room.schemaLocation` set in `app/build.gradle.kts`) and committed — bump the `@Database(version = ...)` and add a migration when changing entities, don't just edit the entity in place.
- `Category` is a hardcoded enum with fixed IDs (`domain/model/Category.kt`), not a DB-backed table.

### Toolchain

The Gradle/AGP/compileSdk versions in `android/gradle/libs.versions.toml` and `android/app/build.gradle.kts` are intentionally pinned (see the `because(...)` constraint comments in `app/build.gradle.kts`) — compileSdk 36 / AGP 8.10.1, since newer `androidx.core` versions require compileSdk 37 / AGP 9.1.0. Don't bump these casually; if a dependency forces a newer `androidx.core`, either pin it back down the same way or upgrade the whole toolchain deliberately.

All Kotlin-related plugin entries in `libs.versions.toml` must use `version.ref = "kotlin"`, never a hardcoded literal — a hardcoded `2.4.0` on `kotlin-android` silently forced every Kotlin artifact (including the compose compiler plugin) up to 2.4.0 too and broke KSP compatibility (`ksp-2.1.21-2.0.1 is too old for kotlin-2.4.0`).

### Compose Material3

The project is on Compose Material3 `1.4.0` (via `composeBom = "2026.02.01"`). Two API gotchas hit while building the Add Transaction screen's category dropdown:

- `ExposedDropdownMenu` is not top-level — it's a member function of `ExposedDropdownMenuBoxScope`, so it's only callable inside an `ExposedDropdownMenuBox { ... }` content lambda and needs no separate import.
- Anchor typing uses `ExposedDropdownMenuAnchorType` (e.g. `Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)`), not `ExposedDropdownMenuDefaults` — the latter has no anchor-type constants in this version.
