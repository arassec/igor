# 'Delete file' Action

## Description
This action deletes a file.

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Source | A file-connector to delete the file from.
Directory | The directory containing the file to delete. Either a fixed value or a mustache expression selecting a property from the data item. If a mustache expression is used, the property's value will be used as directory name.
Filename | The name of the file to delete. Either a fixed value or a mustache expression selecting a property from the data item. If a mustache expression is used, the property's value will be used as filename.
