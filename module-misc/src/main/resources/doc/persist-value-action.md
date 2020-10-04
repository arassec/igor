# 'Persist Value' Action

## Description
This action persists values in igor's datastore.

## Parameters
The action can be configured by the following parameters:

Parameter | Description
---|:---|
Input | A mustache expression selecting a property from the data item. The property's value is persisted in igor's datastore.
Num values to keep | The number of persisted values to keep. If the number is exceeded, old values are removed from the datastore.
Num threads | The number of threads this action uses to process data items.
