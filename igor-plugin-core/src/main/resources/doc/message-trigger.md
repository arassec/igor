# Message Trigger

## Description
The message trigger is an event based trigger that processes an incoming message as data item as soon as it is received.

Every message connector can be used as source for incoming events, as long as it is configured to receive such events.

## Parameters
The following parameters can be configured for the message trigger:

Parameter | Description
---|:---|
Message connector | A message connector that provides the incoming messages.
Meta data | Optional JSON that will be added to every data item created by the trigger under the 'meta' key.
Data | Optional JSON that will be added to every data item created by the trigger under the 'data' key.

## Limitations and Caveats
Not all actions are available for event-triggered jobs. 
E.g. sorting by timestamp requires all data items, that should be sorted, to be known to the action.
Since event-triggered jobs process a continuous stream of incoming events, there is no fixed number of data items to sort.

During simulated job executions the trigger will receive (and probably consume) messages provided by the message connector.
This means, that e.g. RabbitMQ messages will be retrieved **and acknowledged** during simulated job executions!
