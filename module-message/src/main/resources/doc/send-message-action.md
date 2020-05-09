# Send Message Action
This action sends a message using the configured service.

# Parameters
The action can be configured by the following parameters.

Parameter | Description
---|:---|
Message service | The service to use for sending the message.
Message template | A template message that is used as message body. Parameters can be filled by using the template syntax explained below.
Num threads | The number of threads this action uses to send messages.

# Message Template Parameters
The message template can be filled with values from the action's data by using the following parameter syntax:
```
##<JSON-Path-Expression>##
```
As example, let's assume the action operates on the following data item: 
```
{
  "data": {
    "filename": "README.TXT",
    "lastModified": "2020-04-18T00:00:00+02:00",
    "directory": "/"
  },
  "meta": {
    "jobId": "1e91a654-ba8c-4c3a-afd0-932bd27d2888",
    "taskId": "b5350687-fbbe-446d-9776-68c6fbbcf982",
    "timestamp": 1587203554775
  }
}
```
The action should send a message containing the filename with the key 'file':
```
{
  "file": "README.TXT"
}
```
This can be done by configuring the following message template:
```
{
  "file": "##$.data.filename##"
}
```