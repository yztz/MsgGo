# MsgGo Project Memory

## Overview
MsgGo is a lightweight Android application designed for bulk SMS sending with dynamic variable substitution. It features a modern **Material 3** interface and an optimized architecture for reliable background processing.

## Core Features
- **Excel Data Import**: Supports `.xls` and `.xlsx` formats using Apache POI. No fixed format required.
- **Magic Variable Substitution**: Enables personalized messages by replacing placeholders (e.g., `${name}`) with data from specific Excel columns.
- **Multi-SIM Support**: Allows users to select which SIM card to use for sending via a native Material selection dialog.
- **Real-time Feedback**: Provides detailed logs and progress status via a `BottomSheetDialog` and persistent notifications.
- **Material Design 3**: Modern, cohesive user interface using Material Components (Cards, Dialogs, BottomSheet, Dynamic Colors).
- **Foreground Service**: Ensures reliable message delivery even when the app is in the background. Supports cancellation via notification actions.
- **Privacy & Security**: The app **does not require internet permission**, ensuring data remains local.

## Project Architecture
The project follows a standard Android multi-component architecture, separating data management, background processing, and user interface.

### Package Structure: `top.yzzblog.messagehelper`
- **`.activities`**: UI Controllers for the main application flows.
- **`.data`**: Data handling, Excel parsing, and local persistence.
- **`.services`**:
    - `LoadService`: Handles file loading (LiveData based).
    - `MessageService`: Foreground service for sending SMS (LiveData based).
    - `SendingMonitor`: Singleton holding `LiveData` for sending status/logs.
- **`.util`**: Shared helper utilities (File, Toast, Text parsing).
- **`.fragments`**: Modular UI components used within activities.

### Core Components & Logic Flow

#### 1. Data Layer (`.data`)
- **`DataLoader`**: The central data hub. Manages `DataModel`, `SpManager` (SharedPreferences), and coordinates Excel loading.
- **`DataModel`**: A serializable container holding a list of `HashMap<String, String>` representing Excel rows.

#### 2. Main Workflow Logic
- **Initialization**: `MainActivity` serves as the entry point.
- **Importing**: Users import files. `LoadService` parses file -> `LiveData` updates UI -> `DataModel` populated.
- **Editing**: `EditActivity` or `SettingFrag` for template editing.
- **Sim Selection**: `HomeFrag` uses `MaterialAlertDialog` for SIM choice.
- **Preparation**: `ChooserActivity` selects recipients and generates finalized messages.
- **Sending**: `ChooserActivity` starts `MessageService` and shows `BottomSheetDialog`. UI observes `SendingMonitor`.

#### 3. Message Sending (`.services`)
- **`MessageService`**:
    - Starts as a Foreground Service with a "Cancel" action notification.
    - Loops through messages, calling `SMSSender.sendMessage()`.
    - Updates `SendingMonitor` with progress (requests sent).
    - Registers an internal `BroadcastReceiver` to listen for delivery results (`SENT_SMS_ACTION`) and updates `SendingMonitor` with logs (success/fail).
- **`SendingMonitor`**: Singleton class exposing `LiveData` for `SendingState`, `logs`, `progress`, and `total`. Decouples Service from UI.
- **`SMSSender`**: Wrapper for `SmsManager`.

## Usage Workflow
1. **Import**: `MainActivity` -> `LoadService`.
2. **Compose**: `EditActivity`.
3. **Configure**: `HomeFrag` (SIM Selection).
4. **Review & Send**: `ChooserActivity` -> Start `MessageService` -> Observe `SendingMonitor`.
5. **Monitor**: View progress in App (BottomSheet) or Notification drawer.

## Key Technical Decisions
- **LiveData Architecture**: Replaced legacy `BroadcastReceiver` patterns with `LiveData` (via `SendingMonitor` and `LoadService`) to make UI updates lifecycle-aware and robust against configuration changes.
- **Material 3**: usage of `BottomSheetDialog`, `MaterialAlertDialogBuilder`, and `LinearProgressIndicator` aligns with modern Android guidelines.
