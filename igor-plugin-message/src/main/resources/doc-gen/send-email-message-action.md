# 'Send E-Mail' Action

## Description
This action sends an E-Mail. 

## Mustache Template Parameters
All of the action's parameters support mustache templates as input. 

As example, let's assume the action operates on the following data item: 

```
{
  "contact": {
    "mail": "igor@arassec.com",
    "name": "Igor",
    ...
  },
  "meta": {
    "jobId": "1e91a654-ba8c-4c3a-afd0-932bd27d2888",
    "timestamp": 1587203554775
  }
}
```


The action's parameters could be configured in the following way 

To: 

```
{{contact.mail}}
```


Body: 

```
Dear {{ contact.name }},\n...
```


to send an E-Mail based on the data item's input: 

```
To: igor@arassec.com

Dear igor,
...
```

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Email Connector | The E-Mail SMTP connector to use for sending the mail.
From | The sender's E-Mail address.
To | A comma separated list of recipients.
Subject | The E-Mails subject.
Content Type | The E-Mail body's content type.
Body | The E-Mail's body.
Attachments | List of newline separated paths to files that should be sent as E-Mail attachments.
