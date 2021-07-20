# 'Read file' Action

## Description
This action reads a file from a file-connector and adds its contents to the processed data item.


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

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Source | A connector providing access to the file to read.
Directory | The directory containing the file to delete. Either a fixed value or a mustache expression selecting a property from the data item. If a mustache expression is used, the property's value will be used as directory name.
Filename | The name of the file to delete. Either a fixed value or a mustache expression selecting a property from the data item. If a mustache expression is used, the property's value will be used as filename.
