# MsgGo

![MsgGo](./app/src/main/res/drawable/icon.png)  

A lightweight SMS group sending App on Android.  

![](https://img.shields.io/github/downloads/yztz/MsgGo/total?color=green)

[中文](./README.md) | [EN](./README_EN.md)

## Features
* Beautiful user interface with good interactivity.
* Import based on excel data format.
* Automatically acquires SMS variable names.
* Provides SMS editor, with support for SMS [**Magic Variable**](#Magic-Variable) substitution.
* Real-time feedback on SMS sending status.
* No need to specify a fixed format, supporting specifying phone-number list in-app.
* *One-click import of third-party application data.*  

## Basic Usage
1. Import data.
2. Edit SMS content.
3. Select the number variable.
4. Check the phone number list of recipients.
5. Send SMS.

## Excel Format
column1|column2|column3|...
-|-|-|-
data|data|data|...
data|data|data|...
...|  
  
Note:
1. Compatibility with all kinds of excel formats.
2. ***The sending delay should not be too short, otherwise there may be interception problems.***

## Download
[release](https://github.com/yztz/MsgGo/releases/download/1.5/app.apk)  

## Magic Variable

What Is Magic Variable?

For example, in the following scenario:

> You want to send this text message to multiple people: Hi ${xxx}, blah blah...

The ${xxx} here is the magic variable. In the imported excel, each magic variable corresponds to each column, for example:

name|phone
-|-
John|123
Leno|456

Then there are two magic variables, namely 'name' and 'mobile number'. You can simply click on the little button on the upper left corner of SMS editing interface to choose the magic variables you need, for example: 

> Hi ${name}, your phone number is ${phone}

Afterwards, the variables will be substituted automatically according to the record of each line:

name|phone|SMS content
-|-|-
John|123|Hi John, your phone number is 123
Leno|456|Hi Leno, your phone number is 456


## TODO

* Phone-number list memorization, which avoids re-selecting phone-numbers each time they send SMS.
* A detailed list of SMS sending status, informing the user of who the hell did not receive the SMS.

If this project helps you anyway, would you please give it a little star :) Thank you!

## END

For any problems on usage or bugs, please contact me at 19052129@hdu.edu.cn

**Solemn reminder: This project is for study only, do not use this software to spread illegal harassment content. the users' behavior and purpose have nothing to do with me!**
