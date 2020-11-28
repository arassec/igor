# 'Split Array' Action

## Description
This action splits a JSON-Array into multiple data items.
Each data item contains one element from the original array at the same position the array had before.

If there is no JSON-Array at the configured position, the data item will be **filtered** by this action.

## Parameters
The action can be configured by the following parameters:

Parameter | Description
---|:---|
Json path query| A JSON-Path expression selecting a JSON-Array from the data item. Must contain either a JSON-Path expression or a mustache expression which evaluates to a JSON-Path expression in the data item. The array is split and its content separated into individual data items.
Num threads | The number of threads this action uses to process data items.

## Example
 
An example data item processed by this action might look like this:
``` json
{
  "data": {
    "input": [
      "a",
      "b",
      "c"
    ]
  }
}
```
With the following configuration:

Parameter | Configuration value
---|:---| 
Input | $.data.input

these three data items are created by the action:
``` json
{
  "data": {
    "input": "a"
  }
}
```
``` json
{
  "data": {
    "input": "b"
  }
}
```
``` json
{
  "data": {
    "input": "c"
  }
}
```