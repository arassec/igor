# Log Action

## Description
This action logs every processed data item to igor's log with DEBUG level.

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Level | The loglevel to use for logging the message. Must be one of 'error', 'warn', 'info', 'debug' or 'trace'.
Message | The message to log. Supports mustache templates to log parts of or the complete data item being processed.
