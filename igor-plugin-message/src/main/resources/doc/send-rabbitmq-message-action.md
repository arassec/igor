# 'Send RabbitMQ Message' Action

## Description

This action sends a message to a RabbitMQ exchange.

## Parameters

The action can be configured by the following parameters.

Parameter | Description
---|:---|
Message connector | The RabbitMQ connector to use for sending the message.
Exchange | The RabbitMQ exchange to send the messages to.
Message template | A template message that is used as message body. Parameters can be filled by using the template syntax explained below.
Content encoding | Sets the encoding for messages sent by this connector.
Content type | Specifies the content type of the message's content.
Headers | Each line can contain a 'Header:Value'-pair which is used in messages sent by this action.

## Mustache Template Parameters

The message template can contain mustache expressions to fill the message with dynamic values from the processed data
item.

As example, let's assume the action operates on the following data item:

``` json
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

``` json
{
  "file": "README.TXT"
}
```

This can be done by configuring the following message template:

``` json
{
  "file": "{{data.filename}}"
}
```