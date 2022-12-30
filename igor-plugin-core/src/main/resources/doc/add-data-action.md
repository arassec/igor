# 'Add Data' Action

## Description
The action adds JSON data to the processed data item. 

## Example
The following configuration of the 'Json' parameter: 
```
{
  "data": {
    "alpha": "beta",
    "delta": [
      42,
      23
    ]
  }
}
```


will result in the following data item: 

```
{
  "data": {
    "alpha": "beta",
    "delta": [
      42,
      23
    ]
  },
  "meta": {
    "jobId": "01d11f89-1b89-4fa0-8da4-cdd75229f8b5",
    "timestamp": 1599580925108
  }
}
```

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Json | The JSON to add to the data item. The configured JSON must not be an array!
