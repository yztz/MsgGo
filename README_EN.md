# MsgGo

<img src="./fastlane/metadata/android/en-US/images/icon.png" width="30%">

A lightweight bulk SMS application for the Android platform.

![](https://img.shields.io/github/downloads/yztz/MsgGo/total?color=green)

[中文](./README.md) | [EN](./README_EN.md)

## Updates

Happy New Year! Entering our fifth year! The New Year Special Edition is now live—please download and enjoy!

---

It has been nearly 4 years since this project was launched. This update brings a brand-new look and feel (**Material 3**) and significantly improved interactions. Everyone is welcome to download and try it out!

## Screenshots

![](./fastlane/metadata/android/en-US/images/phoneScreenshots/1.png)
![](./fastlane/metadata/android/en-US/images/phoneScreenshots/2.png)
![](./fastlane/metadata/android/en-US/images/phoneScreenshots/3.png)
![](./fastlane/metadata/android/en-US/images/phoneScreenshots/4.png)

## Important Notes

Please be aware that carriers usually impose sending limits. For example, some carriers limit users to 200 messages/hour and 1,000 messages/day. Exceeding these limits may result in restricted sending/receiving capabilities.

## Features

* [New] History logs
* [New] SMS cost estimation
* [New] SMS preview before sending
* [New] HyperOS support
* [New] Multi-language support
* Dual-SIM selection support
* No internet permission required
* Material Design 3
* Import based on Excel data format
* Automatic retrieval of SMS variable names
* Built-in SMS editor with support for [**Magic Variables**](#magic-variables)
* Real-time feedback on message sending status
* No fixed template required; specify the phone number column within the app
* *One-click data sharing/import from third-party apps*

## Basic Usage
1. Import data
2. Edit SMS content
3. Select variable names
4. Choose the recipient phone number column
5. Send

## Excel Format Requirements
| Column Name | Column Name | Column Name | ... |
|-------------|-------------|-------------|-----|
| Data        | Data        | Data        | ... |
| Data        | Data        | Data        | ... |
| ...         |             |             |     |

Notes:
1. (Untested) Compatible with all Excel formats.
2. ***Do not set the sending delay too short, or you may encounter carrier blocking issues.***

## Download
[release](https://github.com/yztz/MsgGo/releases/latest)

## Magic Variables

What are Magic Variables?

Consider the following scenario:

> You want to send a message to multiple people: Dear ${xxx}, hello, balabala...

Here, `${xxx}` is our Magic Variable. In the imported Excel file, each magic variable corresponds to a column. For example:

| Name | Phone Number |
|------|--------------|
| John | 123          |
| Mike | 456          |

In this case, there are two magic variables: `Name` and `Phone Number`. In the SMS editor, simply click the small button in the bottom left corner to select the variable you need, such as:

> Hello ${Name}, your phone number is ${Phone Number}

The software will automatically replace the variables based on each row of data:

| Name | Phone Number | Resulting SMS Content                |
|------|--------------|--------------------------------------|
| John | 123          | Hello John, your phone number is 123 |
| Mike | 456          | Hello Mike, your phone number is 456 |

If this app helps you, please give it a star! :)

## END

If you encounter any issues or bugs while using the software, feel free to submit an issue.

**Solemn Reminder: This project is for educational purposes only. Do not use this software to spread illegal or harassing content. The actions and intentions of the users are independent of the developer!**