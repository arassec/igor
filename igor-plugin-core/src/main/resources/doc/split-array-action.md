# 'Split Array' Action

## Description
This action splits a JSON-Array into multiple data items.
Each data item contains one element from the original array at the same position the array had before.

If there is no JSON-Array at the configured position, the data item will be **filtered** by this action.

## Parameters
The action can be configured by the following parameters:

Parameter | Description
---|:---|
Array selector| A Mustache expression selecting a JSON-Array from the data item. The array is split and its content separated into individual data items.

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
Array selector | { { data.input } }

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