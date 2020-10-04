# 'Filter Persisted Value' Action

## Description
This action filters data items based on values from igor's datastore.

Filtered data items are not passed to the following action.

## Parameters
The action can be configured by the following parameters:

Parameter | Description
---|:---|
Input | A mustache expression selecting a property from the data item. The property's value is checked against all persisted values from igor's datastore. If the value is already persisted, the data item is filtered.
Num threads | The number of threads this action uses to process data items.
