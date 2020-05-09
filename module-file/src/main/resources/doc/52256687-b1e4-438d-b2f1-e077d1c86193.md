# 'Read file' Action
This action reads a file from a service and adds its content to the processed data item.

The file's contents will be available in the data item under the key 'fileContents'.

A data item processed by this action could look like this:
```
{
  "data": {
    ...
  },
  "meta": {
    ...
  }
  "fileContents": "THIS IS THE CONTENT READ FROM THE FILE!"
}
```

# Parameters
The action can be configured by the following parameters:

Parameter | Description
---|:---|
Service | A service to delete the file from.
Directory | The directory containing the file to delete. Either a fixed value or a JSON-Path expression selecting a property from the data item. If a JSON-Path expression is used, the property's value will be used as directory name.
Filename | The name of the file to delete. Either a fixed value or a JSON-Path expression selecting a property from the data item. If a JSON-Path expression is used, the property's value will be used as filename.
Num threads | The number of threads this action uses to process data items.
