# 'Send RabbitMQ Message' Action

## Description
This action sends a message to a RabbitMQ exchange. 

## Mustache Template Parameters
The message template can contain mustache expressions to fill the message with dynamic values from the processed data item.


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
  "file": "{{data.filename}}"
}
```

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Message Connector | The RabbitMQ connector to use for sending the message.
Exchange | The RabbitMQ exchange to send the messages to.
Message Template | A template message that is used as message body. Parameters can be filled by using the template syntax explained below.
Content Encoding | Sets the encoding for messages sent by this connector.
Content Type | Specifies the content type of the message's content.
Routing Key | An optional routing key that will be used in messages of this connector.
Headers | Each line can contain a 'Header:Value'-pair which is used in messages sent by this action.
Simulation Safe | Only sends messages during simulated job executions if unchecked.
