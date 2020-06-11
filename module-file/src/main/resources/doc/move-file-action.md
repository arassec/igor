# 'Move file' Action

## Description
This action moves/renames a file.

## Parameters
The action can be configured by the following parameters:

Parameter | Description
---|:---|
Source | A file-connector providing access to the file.
Source directory | The directory containing the file to move. Either a fixed value or a JSON-Path expression selecting a property from the data item. If a JSON-Path expression is used, the property's value will be used as directory name.
Source filename | The name of the file to move. Either a fixed value or a JSON-Path expression selecting a property from the data item. If a JSON-Path expression is used, the property's value will be used as filename.
Target directory | The target directory of the moved file. Either a fixed value or a JSON-Path expression selecting a property from the data item. If a JSON-Path expression is used, the property's value will be used as directory name.
Target filename | The target name of the moved file. Either a fixed value or a JSON-Path expression selecting a property from the data item. If a JSON-Path expression is used, the property's value will be used as filename.
Num threads | The number of threads this action uses to process data items.
