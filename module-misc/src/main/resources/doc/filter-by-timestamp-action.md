# 'Filter by Timestamp' Action

## Description
This action filters data items by comparing a timestamp from the input against a configured time span.

Filtered data items are not passed to following actions.

## Parameters
The action can be configured by the following parameters:

Parameter | Description
---|:---|
Input | A JSON-Path expression selecting a property from the data item. The property's value is converted into a timestamp and afterwards used for filtering.
Older than | If checked, data items with timestamps older than the configured time span are filtered. If unchecked, data items with timestamps younger than the configured time span are filtered.
Amount | The amount of the time span.
Time unit | The unit of the time span. Allowed values are e.g. 'MINUTES', DAYS', 'WEEKS'. See [Java ChronoUnit](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/temporal/ChronoUnit.html) for all allowed values.
Timestamp format | The format of the property's value containing the timestamp. See [Java DateTimeFormat](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html) (section 'Patterns for Formatting and Parsing') for allowed values. The special values 'millis' and 'seconds' are additionally supported (see below).
Num threads | The number of threads this action uses to filter data items.

## Filtering Epoch Timestamps
If the timestamp is given as Epoch timestamp, you can use the following special valus as 'Timestamp format' parameter:

Timestamp format value | Description
---|:---|
epoch-millis | Assumes the timestamp value is given as Epoch timestamp in milliseconds.
epoch-seconds | Assumes the timestamp value is given as Epoch timestamp in seconds.