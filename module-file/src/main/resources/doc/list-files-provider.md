# 'List files' Provider

## Description
This provider lists all files contained in a directory. 

A data item created by this provider could look like this:
``` json
{
  "data": {
    "filename": "README.TXT",
    "lastModified": "2020-04-18T00:00:00+02:00",
    "directory": "/"
  },
  "meta": {
    "jobId": "1e91a654-ba8c-4c3a-afd0-932bd27d2888",
    "taskId": "b5350687-fbbe-446d-9776-68c6fbbcf982",
    "timestamp": 1587203554775
  }
}
```

## Parameters
The 'List files' provider can be configured with the following parameters.

Parameter | Description
---|:---|
Source | A file-connector that provides the directory to list files in.
Directory | Path to the directory that contains the files provided by the file connector. 
File ending | An optional file ending which is used to filter files on the file server, e.g. 'jpeg'. 
Simulation limit | The maximum number of data items that should be provided during job simulations. 
