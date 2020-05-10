# 'Copy file' Action
This action copies a file.

# Parameters
The action can be configured by the following parameters:

Parameter | Description
---|:---|
Source | A file-connector providing access to the file to copy.
Source directory | The directory containing the file to copy. Either a fixed value or a JSON-Path expression selecting a property from the data item. If a JSON-Path expression is used, the property's value will be used as directory name.
Source filename | The name of the file to copy. Either a fixed value or a JSON-Path expression selecting a property from the data item. If a JSON-Path expression is used, the property's value will be used as filename.
Target | A file-connector providing access to the filesystem the file should be copied to.
Target directory | The target directory for the copied file. Either a fixed value or a JSON-Path expression selecting a property from the data item. If a JSON-Path expression is used, the property's value will be used as directory name.
Target filename | The target name of the copied file. Either a fixed value or a JSON-Path expression selecting a property from the data item. If a JSON-Path expression is used, the property's value will be used as filename.
Append transfer suffix | If checked, the filename on the target connector will be appended with '.igor' during transfer. This is useful to indicate that the file is currently copied.
Append filtype suffix | If checked, igor will try to determine the file type and append it to the target filename (e.g. '.html').
Num threads | The number of threads this action uses to process data items.
