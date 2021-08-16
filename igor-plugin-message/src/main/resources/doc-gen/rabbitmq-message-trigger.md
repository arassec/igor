# RabbitMQ Message Trigger

## Description
A trigger that fires on incoming messages on a RabbitMQ server.


This message trigger is an event based trigger that processes an incoming message as data item as soon as it is received. 

## Limitations and Caveats
Not all actions are available for event-triggered jobs. E.g. sorting by timestamp requires all data items, that should be sorted, to be known to the action. Since event-triggered jobs process a continuous stream of incoming events, there is no fixed number of data items to sort. 

During simulated job executions the trigger will receive (and probably consume) messages provided by the message connector. Thus RabbitMQ messages will be retrieved **and acknowledged** during simulated job executions!

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Rabbit Mq Connector | A RabbitMQ message connector that provides the incoming messages.
Queue Name | The RabbitMQ queue containing the messages.
