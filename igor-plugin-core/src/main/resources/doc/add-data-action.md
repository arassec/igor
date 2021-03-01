# 'Add Data' Action

## Description

The action adds JSON data to the processed data item.

## Parameters

The following parameters can be configured for the action:

Parameter | Description
---|:---|
Json | The JSON to add to the data item. The configured JSON must not be an array!

## Examples

The following configuration:

- Json: `{ "data": { "alpha": "beta", "delta": [42, 23] } }`

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
