# 'Fixed input' Provider

## Description
This provider creates data items that can be configured by the user.
The user can specify a string which is added to the created data.

A data item created by this provider could look like this:
``` json
{
  "data": {
    "input": "THIS IS CONFIGURED BY THE USER!"
  },
  "meta": {
    "jobId": "1e91a654-ba8c-4c3a-afd0-932bd27d2888",
    "taskId": "b5350687-fbbe-446d-9776-68c6fbbcf982",
    "timestamp": 1587202126484
  }
}
```

## Parameters
The following parameters can be configured for the 'Fixed Input' provider.

Parameter | Description
---|:---|
Input | The input to add to the data item.
Separate lines | If checked, each newline in the configured input is used for a new data item. If unchecked, the configured string will be added to a single data item. 
Simulation limit | The maximum number of data items that should be provided during job simulations. 
