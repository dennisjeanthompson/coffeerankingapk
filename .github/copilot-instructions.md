# CoffeeRankingAPK – AI Agent Guide

## Start Here
- Read `README.md` for product goals and feature matrix (owners vs lovers flows).
- Inspect `app/build.gradle` for Compose, Mapbox Navigation/Search, Firebase, and size optimizations (R8, resource shrink, ABI splits).
- Check `gradle.properties` for the Mapbox downloads token and global Gradle flags.
- Review `app/src/main/AndroidManifest.xml` for required permissions, Mapbox access-token metadata, and navigation activity declarations.
- Follow `app/src/main/java/com/example/coffeerankingapk/` structure: `ui/` (Compose screens), `navigation/` (NavHost & turn-by-turn `TurnByTurnNavigationActivity`), `data/mock/` (stub JSON), `di/` (Hilt modules).

## Build & Test
- `./gradlew clean assembleDebug` for day-to-day builds (unshrunken, all ABIs for emulator use).
- `./gradlew clean assembleRelease` builds a shrunk, resource-trimmed APK per ABI (outputs under `app/build/outputs/apk/release/`); inspect `app/build/outputs/mapping/release/` for R8 reports.
- `./gradlew test` runs JVM tests; instrumentation via `./gradlew connectedAndroidTest` when a device/emulator is attached.
- Size diagnostics: `./gradlew app:dependencies --configuration releaseRuntimeClasspath`, `apkanalyzer files sizes <apk>`, `apkanalyzer dex packages <apk>`.

## Key Conventions
- Jetpack Compose is the primary UI; XML layouts exist only where Mapbox UI components require View binding (`TurnByTurnNavigationActivity`).
- Navigation is split between Compose navigation graphs and an imperative Mapbox turn-by-turn activity; keep navigation routes in `ui/navigation/` consistent with `NavigationGraph.kt`.
- Mock repositories serve data; real integrations should replace the sources in `data/mock` while maintaining the same interfaces to keep UI previews working.
- Mapbox public tokens belong in `res/values/strings.xml`; the download token stays in untracked `local.properties`/environment when committing.
- Heavy Mapbox modules (`navigationcore`, `search`, `voice`) are included deliberately—remove only with UX approval and after auditing usages in `MapScreen.kt` and `TurnByTurnNavigationActivity.kt`.

## APK Size Notes
- Release builds use R8 full mode, resource shrinking, and per-ABI splits (ARMv7a + ARM64) to keep artifacts under store limits; ensure any new native dependency supports these ABIs.
- Avoid adding universal/native debug libraries to `implementation`; prefer `debugImplementation` for tooling-only dependencies.
- Large assets belong in the backend or CDN—`res/` currently contains only lightweight icons; keep it that way.
- If adding Mapbox modules, prefer `ndk27` artifacts to match existing dependencies and avoid duplicate native loaders.

## When Updating
- Keep `.github/workflows/` tasks in sync with local scripts; mirror any new Gradle tasks in CI.
- Document new environment variables or tokens in `README.md` “Setup” and mirror here.
- When touching navigation or DI, update corresponding mock data to preserve previews/tests.

Ping the maintainers if instructions drift from reality—this file is meant to stay brief, actionable, and current.