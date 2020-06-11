# 'Sort by timestamp pattern' Action

## Description
This action sorts data items based on a timestamp inside a value of the input data.

This might e.g. be used to sort files by a timestamp that is part of their filename, if the timestamp of the last modification is not available. 

## Parameters
The action can be configured by the following parameters:

Parameter | Description
---|:---|
Input | A JSON-Path expression selecting a property from the data item. The property's value is converted into a timestamp and used for sorting.
Pattern | A regular expression matching the timestamp part of the input value.
Timestamp format | The format of the timtestamp part of the property's value. See [Java DateTimeFormat](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html) (section 'Patterns for Formatting and Parsing') for allowed values.
Sort ascending | If checked, data items are sorted from older to newer timestamps. If unchecked, data items are sorted from newer to older timestamps.

## Example
 
An example data item processed by this action might look like this:
``` json
{
  "data": {
    "filename": "alpha_20200113185100_beta.jpeg",
    "directory": "/"
  },
  "meta": {
    "jobId": "1e91a654-ba8c-4c3a-afd0-932bd27d2888",
    "taskId": "b5350687-fbbe-446d-9776-68c6fbbcf982",
    "timestamp": 1587203554775
  }
}
```
With the following configuration, all data items of a job execution could be sorted by the timestamp encoded in the filename:

Parameter | Configuration value
---|:---| 
Input | $.data.filename
Pattern | [0-9]{14}
Timestamp format | yyyyMMddHHmmss