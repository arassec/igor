# 'Delete file' Action
This action deletes a file from a service.

# Parameters
The action can be configured by the following parameters:

Parameter | Description
---|:---|
Service | A service to delete the file from.
Directory | The directory containing the file to delete. Either a fixed value or a JSON-Path expression selecting a property from the data item. If a JSON-Path expression is used, the property's value will be used as directory name.
Filename | The name of the file to delete. Either a fixed value or a JSON-Path expression selecting a property from the data item. If a JSON-Path expression is used, the property's value will be used as filename.
Num threads | The number of threads this action uses to process data items.
