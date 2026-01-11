## Basic Usage

1. Import data
2. Select the recipient phone number column
3. Edit SMS content
4. Select SIM card
5. Start sending

Tip: MsgGo allows you to import data by sharing or sending Excel files from other apps!

## Excel Format Requirements

| Column Name | Column Name | Column Name | ... |
|-------------|-------------|-------------|-----|
| Data        | Data        | Data        | ... |
| Data        | Data        | Data        | ... |
| ...         |             |             |     |

Notes:
1. Compatible with .xls/.xlsx formats
2. Excel rows number limit: 200, size limit: 50MB
3. Do not set the sending delay too short, or you may encounter carrier blocking issues.
4. Please be aware that carriers usually impose sending limits. For example, some carriers limit users to 200 messages/hour and 1,000 messages/day. Exceeding these limits may result in restricted sending/receiving capabilities.

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