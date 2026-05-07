# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**MsgGo** is a privacy-focused Android app for bulk SMS sending with `${variable}` template substitution driven by imported Excel files. No internet permission is used. Written in Java 17, targets Android API 26–35.

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease

# Install debug APK to connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "top.yztz.msggo.SensitiveWordUtilTest"
```

Output APKs are named `MsgGo-{versionName}-{abi}.apk`. ProGuard/obfuscation is disabled.

## Architecture

### Data Flow

1. User imports an `.xls`/`.xlsx` file → `ExcelReader` (POI) parses it into a 2D list
2. `DataModel` (singleton) holds the parsed data, selected column index, and message template
3. Column headers become available `${variables}` in the template via `TextParser`
4. `ChooserActivity` previews recipients; `SendingActivity` orchestrates delivery
5. `MessageService` (foreground service) drives actual SMS sending via `SMSSender`
6. `SMSBroadcastReceiver` handles delivery receipts and updates UI via callback

### Key Singleton: `DataModel`

`DataModel` is the central state store passed between activities. It holds:
- Parsed Excel rows
- Selected phone number column index
- Message template string
- File history metadata

File identity uses MD5 hash (`HashUtils`) to detect changes and restore relevant history via `HistoryManager` (JSON files in app cache).

### UI Flow (Progressive Reveal)

`MainActivity` hosts two fragments via `ViewPager2`:
- **HomeFrag**: sequentially reveals steps — Import → Select Column → Edit Template → Send
- **SettingFrag**: app preferences via `SettingManager` (SharedPreferences)

`EditActivity` → `ChooserActivity` → `SendingActivity` is the send pipeline.

### SMS Sending

`MessageService` is a foreground service that exposes a callback interface (`MessageServiceCallback`) to `SendingActivity`. `SMSSender` wraps `SmsManager` with multi-SIM support via `SubscriptionManager`. Randomized delays between sends are used to reduce carrier blocking.

### Notable Utilities

- **`TextParser`**: `${columnName}` substitution engine; resolves variable names against Excel column headers
- **`SensitiveWordUtil`**: wraps `houbb/sensitive-word` for Chinese spam/profanity filtering before send
- **`XiaomiUtil`**: detects MIUI/HyperOS and redirects to vendor-specific SMS permission settings
- **`ExcelReader`**: enforces file size and row count limits before parsing

## Release Process

Releases are triggered by pushing a git tag matching `v*` or a numeric pattern. The CI workflow (`release.yml`) verifies that the tag matches the `versionName` in `build.gradle`, signs the APK using repository secrets, and publishes a GitHub Release. Nightly builds use a manual `workflow_dispatch` trigger.

When bumping the version, update both `versionCode` and `versionName` in `app/build.gradle`. Current version is **3.9** (versionCode: 26000309).

## Localization

String resources exist in both `values/` (English) and `values-zh-rCN/` (Simplified Chinese). All user-visible strings must have entries in both files.
