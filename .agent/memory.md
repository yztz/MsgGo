# MsgGo Project Memory

## Overview
**MsgGo** is a high-performance, privacy-focused Android application designed for personalized bulk SMS sending. It leverages Excel data files to dynamically substitute variables into message templates, enabling rapid, personalized outreach. The app features a state-of-the-art **Material 3** interface and a guided configuration workflow.

## Core Features
- **Dynamic Excel Import**: Supports `.xls` and `.xlsx` formats via Apache POI. Users can map any column as the phone number source.
- **Variable Placeholder Substitution**: Replaces placeholders `${变量名}` in templates with row-specific data. Variables are displayed as styled chips in the editor.
- **Progressive Setup Flow**: A guided Home screen experience that reveals configuration steps (Data -> Column/Template -> SIM -> Send) sequentially based on completion.
- **Smart History & State Persistence**:
    - Automatically saves and restores message templates and column selections.
    - Uses **Content Signature (MD5/Hash)** to detect if an Excel file has been modified.
    - Features a "光影回廊" (History) section for quick access to recently used files.
- **Messenger Preview**: Allows users to preview the finalized message before sending.
- **Cost Calculation & Rate Management**: Configurable SMS rate with real-time cost estimation.
- **Multi-SIM Support**: Intelligent SIM card detection and selection, including Xiaomi-specific handling.
- **Sensitive Word Filter**: Detects sensitive words in messages before sending using `houbb/sensitive-word` library.
- **Randomized Delay**: Optional random delay between messages to avoid carrier blocks.
- **Duplicate Removal**: Automatically removes duplicate phone numbers before sending.
- **Foreground Service**: Uses a foreground `MessageService` with persistent notifications.
- **Privacy First**: **Zero Internet Permission** required. All processing happens entirely on-device.

## Project Architecture

### Package Structure: `top.yztz.msggo`
- **`.activities`**:
    - `MainActivity`: Main container with Bottom Navigation (Home/Settings) and collapsible toolbar with red panda mascot.
    - `ChooserActivity`: Recipient selection, previewing, sensitive word check, and sending trigger.
    - `EditActivity`: Full-screen message template editor with variable chip rendering and whole-chip deletion.
    - `SendingActivity`: Dedicated sending UI that orchestrates the per-message sending loop, pause/resume, and progress display.
    - `AboutActivity`: App info with Easter egg (tap icon 5x for watermelon panda).
    - `MarkdownActivity`: Displays markdown content (privacy policy, disclaimer).
- **`.adapters`**:
    - `CheckboxAdapter`: RecyclerView adapter for recipient selection checkboxes in ChooserActivity.
    - `ListAdapter` / `DataAdapter`: Displays Excel data rows in horizontal scrollable format.
    - `SendingListAdapter`: (in activities package) Displays sending progress with animated state transitions.
- **`.fragments`**:
    - `HomeFrag`: Primary dashboard with progressive configuration list ("指尖驿站") and history ("光影回廊").
    - `SettingFrag`: Application preferences including delay, randomize delay, SMS rate, language, check for updates, export logs.
- **`.data`**:
    - `DataModel`: Central singleton for data state, Excel parsing, template storage, deduplication.
    - `DataCleaner`: Cache and data cleanup utilities (internal cache, databases, files).
    - `HistoryManager`: Manages JSON-based history of file contexts with `HistoryItem` inner class.
    - `Message`: Data class with phone, content, and `MessageState` (PENDING/WAITING/SUBMITTED/SENT/FAILED).
    - `SettingManager`: SharedPreferences wrapper for app settings.
    - `Settings`: Constants for limits (file size 50MB, row count 200, delay 1-8s).
- **`.services`**:
    - `MessageService`: Lightweight foreground service for sending individual SMS messages. Exposes `Callback` interface with `onMessageSubmitted(index)` and `onMessageConfirmed(index, success)`. Methods: `initSession(total)`, `sendOne(message, index, subId)`, `notifyPaused()`, `notifyResumed()`, `finishSession(completed)`.
    - `SMSSender`: Low-level interface for `SmsManager`, handles SMS submission and broadcast registration.
    - `SMSBroadcastReceiver`: Receives SMS delivery status broadcasts.
- **`.util`**: 
    - `TextParser`: Replaces `${变量}` placeholders with row data. Contains `VARIABLE_PATTERN`.
    - `ExcelReader`: Apache POI wrapper for reading .xls/.xlsx files, enforces row/size limits.
    - `SensitiveWordUtil`: Wrapper for `houbb/sensitive-word` detection library.
    - `FileUtil`: File operations, serialization, cache size formatting, raw resource loading.
    - `HashUtils`: MD5 hashing for content signatures.
    - `LocaleUtils`: Language switching support (auto/en/zh).
    - `XiaomiUtil`: Handles MIUI-specific "Service SMS" permissions.
    - `ToastUtil`: Singleton toast helper to avoid stacking.
    - `CompatUtils`: dp2px conversion utility.
- **`.exception`**:
    - `DataLoadFailed`: Custom exception for Excel loading errors with string resource ID.
- **`.widgets`**:
    - `ObservableScrollView`: Custom ScrollView with touch detection and `ScrollViewListener` callback.

## Key UI/UX Concepts
- **指尖驿站 (Home Dashboard)**: Configuration card with sequential steps.
- **光影回廊 (History)**: Card-based display of recent files.
- **Red Panda Mascot**: Appears in MainActivity header, switches between bamboo/grape on page change with fade animation.
- **Variable Chips**: In EditActivity, variables render as styled chips with background color, stroke, and smaller font.
- **Sending Animation**: `SendingListAdapter` animates message state transitions with color and icon changes.

## Sending Architecture (Refactored)
The sending mechanism uses a **per-message control model**:

1. **`SendingActivity`** is the controller:
   - Maintains `currentIndex`, `confirmedCount`, `isPaused`, `isStopped`
   - Contains `SendingState` (IDLE/SENDING/PAUSED/STOPPED/COMPLETED) and `MessageState` enums
   - Uses `Handler.postDelayed()` to schedule next message send with delay
   - Implements `MessageService.Callback` to receive submission/confirmation events
   
2. **`MessageService`** is a lightweight executor:
   - Runs as foreground service for process priority
   - Sends one message at a time via `sendOne(message, index, subId)`
   - Notifies callback on submission and delivery confirmation
   - Updates notification text for progress/pause states

3. **Flow**:
   - `SendingActivity.onCreate()` -> binds to service -> `service.initSession(total)`
   - `startSending()` -> `sendNextMessage()` -> calculates delay -> `handler.postDelayed(executeCurrentSend)`
   - `executeCurrentSend()` -> `service.sendOne()` -> callback `onMessageSubmitted()`
   - SMS delivery broadcast -> `onMessageConfirmed()` -> update UI -> `checkCompletion()`

## GitHub Workflows
- **release.yml**: Triggered on tags `v*` or `[0-9]*`. Includes version check step that compares git tag with `versionName` in `build.gradle`, fails if mismatch.
- **nightly.yml**: Nightly builds.

## Development Environment
- **Namespace/Application ID**: `top.yztz.msggo`
- **Minimum SDK**: 23 (Android 6.0)
- **Target SDK**: 34 (Android 14)
- **Technology Stack**: Java, Material Components 3, Apache POI (Android optimized), houbb/sensitive-word.
- **App Icon**: `ic_launcher_v2` (updated)
