# 'List files' Action

## Description
This action lists all files contained in a directory. 

The action creates new data items for every file listed.
A data item created by this action could look like this:
``` json
{
  "data": {
    "filename": "README.TXT",
    "lastModified": "2020-04-18T00:00:00+02:00",
    "directory": "/"
  },
  "meta": {
    "jobId": "1e91a654-ba8c-4c3a-afd0-932bd27d2888",
    "timestamp": 1587203554775
  }
}
```

## Parameters
The 'List files' action can be configured with the following parameters.

Parameter | Description
---|:---|
Source | A file-connector that provides the directory to list files in.
Directory | Path to the directory that contains the files provided by the file connector. 
File ending | An optional file ending which is used to filter files with the file connector, e.g. 'jpeg'. 
