# RabbitMQ Connector

## Description
A message-connector that uses RabbitMQ to send messages.

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Host | The host running RabbitMQ.
Port | The port, RabbitMQ is listening on.
Username | The RabbitMQ user's name.
Password | The password of the RabbitMQ user.
Virtual Host | The virtual host to use on the RabbitMQ server.
Heart Beat | RabbitMQ heart beat **in seconds** to use to check the connection to the server.
Connection Timeout | A timeout **in milliseconds** after which a connection to the server will be aborted.
Prefetch Count | Number of messages that are fetched at once without waiting for acknowledgements of the previous messages.
