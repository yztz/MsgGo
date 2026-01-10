**Last Updated: January 2, 2026**

MsgGo (hereinafter referred to as "this application") understands the importance of privacy to you and is committed to protecting your personal data. This Privacy Policy is intended to explain how this application processes data, the reasons we request permissions, and our efforts to protect your privacy.

### 1. Core Principle: Zero Network Access, Local Processing
MsgGo is designed to be a completely private and secure tool. This application **does not request external network access permissions**. This means:
- All of your data (including Excel file content, phone numbers, SMS templates, etc.) will **never** be uploaded to any server or third-party cloud.
- All logic processing (variable substitution, SMS generation, transmission control) is completed entirely locally on your device.

### 2. How We Use Your Data Through Permissions
To achieve the core function of bulk SMS sending, we need to request the following permissions:

- **Send SMS (SEND_SMS)**: Core function. Used to execute the bulk SMS sending tasks initiated by you.
- **Read Phone State (READ_PHONE_STATE)**: Used to identify SIM card information in multi-SIM devices (such as slot index and carrier name) so that you can select a specific SIM card to send messages.
- **Read/Write External Storage (READ_EXTERNAL_STORAGE)**: Used to read the Excel data files you select and authorize, and to save files when you actively export debug logs.
- **Foreground Service (FOREGROUND_SERVICE)** and **Notification Permission (POST_NOTIFICATIONS)**: To ensure that the application is not interrupted by the system during the process of sending a large number of messages (even when switched to the background), and to provide real-time feedback on sending progress.

### 3. Data Storage and Lifecycle
- **Excel Data**: We copy your input data to a cache directory for access. After clearing the cache, the data expires immediately.
- **Local History**: To enhance your user experience, this application saves your recently opened file paths, SMS templates, and number column selections locally. This data is stored in the application's private directory; you can completely remove these records at any time by clicking "Clear Cache" in the "Settings".
- **Debug Logs**: Debug logs only record the application's operational logic. Logs can only be accessed externally if you **manually click** "Export Debug Logs" and actively share them with the developer.

### 4. Third-Party Services
MsgGo **does not contain** any third-party analysis tools, trackers, advertising SDKs, or SDK monitoring plugins. Due to the lack of network permissions, any form of third-party data collection is physically disabled within this application.

### 5. Your Rights
- **Withdraw Permissions**: You can withdraw granted permissions at any time through system settings, but this may result in the unavailability of related functions.
- **Delete Data**: You can delete all locally saved task configuration caches via "Settings - Clear Cache".

### 6. Contact Us
Since this application is an open-source tool and does not have network communication capabilities, if you have any questions about the Privacy Policy, please contact us via the source code link in the settings interface.