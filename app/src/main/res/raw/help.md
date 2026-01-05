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