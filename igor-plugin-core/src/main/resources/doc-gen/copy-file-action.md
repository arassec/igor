# 'Copy file' Action

## Description
This action copies a file.

Details about the action's parameters are added to processed data items under the 'copiedFile' key.

A data item processed by this action could look like this: 
```
{
  "data": {
    "filename": "KeyGenerator.png",
    "lastModified": "2007-03-19T20:52:58+01:00",
    "directory": "/pub/example/"
  },
  "meta": {
    "jobId": "e0b925ed-104f-45b1-81b7-5d79ea46a633",
    "simulation": true,
    "timestamp": 1601302694149
  },
  "copiedFile": {
    "sourceDirectory": "/pub/example/",
    "targetFilename": "KeyGenerator.png",
    "targetDirectory": "/volume1/data/test/",
    "sourceFilename": "KeyGenerator.png"
  }
}
```

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Source | A file-connector providing access to the file to copy.
Source Directory | The directory containing the file to copy. Either a fixed value or a mustache expression selecting a property from the data item. If a mustache expression is used, the property's value will be used as directory name.
Source Filename | The name of the file to copy. Either a fixed value or a mustache expression selecting a property from the data item. If a mustache expression is used, the property's value will be used as filename.
Target | A file-connector providing access to the filesystem the file should be copied to.
Target Directory | The target directory for the copied file. Either a fixed value or a mustache expression selecting a property from the data item. If a mustache expression is used, the property's value will be used as directory name.
Target Filename | The target name of the copied file. Either a fixed value or a mustache expression selecting a property from the data item. If a mustache expression is used, the property's value will be used as filename.
Append Transfer Suffix | Enables an ".igor" file suffix during file transfer. The suffix will be removed after the file has been copied completely.
Append Filetype Suffix | If checked, igor will try to determine the file type and append it to the target filename (e.g. '.html' or '.jpeg').
