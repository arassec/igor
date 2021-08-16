# 'Split Array' Action

## Description
This action splits a JSON-Array into multiple data items. Each data item contains one element from the original array at the same position the array had before.


If there is no JSON-Array at the configured position, the data item will be **filtered** by this action. 

## Example

An example data item processed by this action might look like this: 

```
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


Parameter | Configuration value | 
:---|:---
Array Selector | { { data.input } } | 


these three data items are created by the action: 

```
{
  "data": {
    "input": "a"
  }
}
```

```
{
  "data": {
    "input": "b"
  }
}
```

```
{
  "data": {
    "input": "c"
  }
}
```

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Array Selector | A Mustache expression selecting a JSON-Array from the data item. The array is split and its content separated into individual data items.
