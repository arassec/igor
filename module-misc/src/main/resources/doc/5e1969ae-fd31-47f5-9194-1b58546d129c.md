# 'Empty input' Provider
This provider creates data items that only contain igor's meta data.

A data item created by this provider looks similar to this:
```
{
  "meta": {
    "jobId": "1e91a654-ba8c-4c3a-afd0-932bd27d2888",
    "taskId": "b5350687-fbbe-446d-9776-68c6fbbcf982",
    "timestamp": 1587201865488
  }
}
```

# Parameters
The following parameters can be configured for the 'Empty Input' provider.

Parameter | Description
---|:---|
Amount | The amount of data items that should be created by the provider.
Simulation limit | The maximum number of data items that should be provided during job simulations. 
