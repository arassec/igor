# 'Sort by timestamp pattern' Action
This action sorts data items based on a timestamp in the input data.

# Parameters
The action can be configured by the following parameters:

Parameter | Description
---|:---|
Input | A JSON-Path expression selecting a property from the data item. The property's value is converted into a timestamp and used for sorting.
Pattern | TODO!
Timestamp format | The format of the property's value containing the timestamp. See [Java DateTimeFormat](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html) (section 'Patterns for Formatting and Parsing') for allowed values.
Sort ascending | If checked, data items are sorted from older to newer timestamps. If unchecked, data items are sorted from newer to older timestamps. 