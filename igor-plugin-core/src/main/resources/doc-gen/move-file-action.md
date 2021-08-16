# 'Move file' Action

## Description
This action moves/renames a file.

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Source | A file-connector providing access to the file.
Source Directory | The directory containing the file to move. Either a fixed value or a mustache expression selecting a property from the data item. If a mustache expression is used, the property's value will be used as directory name.
Source Filename | The name of the file to move. Either a fixed value or a mustache expression selecting a property from the data item. If a mustache expression is used, the property's value will be used as filename.
Target Directory | The target directory of the moved file. Either a fixed value or a mustache expression selecting a property from the data item. If a mustache expression is used, the property's value will be used as directory name.
Target Filename | The target name of the moved file. Either a fixed value or a mustache expression selecting a property from the data item. If a mustache expression is used, the property's value will be used as filename.
