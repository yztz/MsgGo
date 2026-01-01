# MsgGo Project Memory

## Overview
**MsgGo** is a high-performance, privacy-focused Android application designed for personalized bulk SMS sending. It leverages Excel data files to dynamically substitute variables into message templates, enabling rapid, personalized outreach. The app features a state-of-the-art **Material 3** interface and a guided configuration workflow.

## Core Features
- **Dynamic Excel Import**: Supports `.xls` and `.xlsx` formats via Apache POI. Users can map any column as the phone number source.
- **Variable Placeholder Substitution**: Intuitively replaces placeholders (e.g., `{姓名}`) in templates with row-specific data.
- **Progressive Setup Flow**: A guided Home screen experience that reveals configuration steps (Data -> Column/Template -> SIM -> Send) sequentially based on completion.
- **Smart History & State Persistence**:
    - Automatically saves and restores message templates and column selections.
    - Uses **Content Signature (MD5/Hash)** to detect if an Excel file has been modified, prompting a reset of selections if the schema changed.
    - Features a "光影回廊" (History) section for quick access to recently used files.
- **Messenger Preview**: Allows users to preview the finalized message for any recipient before sending by clicking on the data row.
- **Cost Calculation & Rate Management**:
    - Configurable SMS rate (资费) via a dialog-based input in Settings.
    - Real-time cost estimation in the selection screen.
    - Confirmation dialog summarizing recipient count and total estimated cost.
- **Multi-SIM Support**: Intelligent SIM card detection and selection, including specific permission handling for Xiaomi (MIUI) devices.
- **Foreground Reliability**: Uses a foreground `MessageService` with persistent notifications and real-time status updates via `SendingMonitor`.
- **Privacy First**: **Zero Internet Permission** required. All data processing and message preparation happen entirely on-device.

## Project Architecture

### Package Structure: `top.yztz.msggo`
- **`.activities`**:
    - `MainActivity`: Main container with Bottom Navigation (Home/Settings).
    - `ChooserActivity`: Recipient selection, previewing, and sending trigger.
    - `EditActivity`: Dedicated full-screen message template editor.
- **`.fragments`**:
    - `HomeFrag`: The primary dashboard with the progressive configuration list ("指尖驿站") and history ("光影回廊").
    - `SettingFrag`: Application preferences and features management ("功能").
- **`.data`**:
    - `DataLoader`: Central singleton for data state, shared preferences, and Excel parsing coordination.
    - `HistoryManager`: Manages the local JSON-based history of `DataContext` objects.
    - `DataModel`: Encapsulates the Excel grid data and column mapping.
    - `DataContext`: metadata container for file history (path, template, column, signature, timestamp).
- **`.services`**:
    - `LoadService`: Asynchronous file reading using `LiveData` for status reporting.
    - `MessageService`: Foreground execution of SMS sending logic.
    - `SendingMonitor`: Centralized `LiveData` hub for sending progress, success/fail logs, and states.
    - `SMSSender`: Low-level interface for `SmsManager` and SIM subscription retrieval.
- **`.util`**: 
    - `TextParser`: Replaces placeholders with row data.
    - `XiaomiUtil`: Handles MIUI-specific "Service SMS" and "Phone Info" permissions.
    - `HashUtils`: Computes file content signatures for smart history matching.

## Key UI/UX Concepts
- **指尖驿站 (Home Dashboard)**: A unified configuration card where each setup step is a row with clear visual cues (Chevrons). Completed steps change subtitle status; incomplete steps may be hidden until prerequisites are met.
- **光影回廊 (History)**: Card-based display of recently opened files with timestamp and template snippets.
- **Dialog-Based Interaction**: Replaced inline inputs (like SMS rate) with Material Dialogs for a cleaner, more deliberate user experience.
- **Visual Consistency**: Consistent use of Material 3 components, corner radii (16dp), and primary-container highlights for the final "Send" action.

## Technical Implementation Details

### Progressive Flow Logic
`HomeFrag.updateStatus()` manages visibility:
1. **Data** is always visible.
2. **Column & Template** appear only after `Data` is loaded.
3. **SIM Selection** appears after a `Template` is defined.
4. **Send Action** (the blue highlighted row) appears only when all prerequisites are met.

### Smart History Matching
When a file is loaded:
1. `HashUtils` generates a signature of the first few rows/columns.
2. `HistoryManager` looks for a matching signature in the history.
3. If found, it automatically applies the saved **Phone Column** and **Message Template**.
4. If the path matches but the signature differs, it assumes the file was updated and resets the column/template to ensure data integrity.

### Message Service & Monitoring
- `MessageService` is decoupled from the UI via `SendingMonitor`.
- `SendingMonitor` holds `logs` (List of send status) and `progress` (Percentage).
- `ChooserActivity` displays a `BottomSheetDialog` that observes these `LiveData` streams to show real-time "Success" or "Failure" status for each recipient.

## Development Environment
- **Namespace/Application ID**: `top.yztz.msggo`
- **Minimum SDK**: 23 (Android 6.0)
- **Target SDK**: 34 (Android 14)
- **Technology Stack**: Java, Material Components 3, Apache POI (Android optimized).
