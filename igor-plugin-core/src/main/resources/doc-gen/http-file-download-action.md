# 'HTTP File Download' Action

## Description
This action Downloads a file from an HTTP(S) server and stores it in the target's filesystem.

The action adds data about the downloaded file to the data item under the 'downloadedFile' key.

A data item processed by this action could look like this: 
```
{
  "data": {},
  "meta": {
    "jobId": "69c52202-4bc6-4753-acfe-b24870a75e90",
    "simulation": true,
    "timestamp": 1601302794228
  },
  "downloadedFile": {
    "targetFilename": "downloaded-file.txt",
    "targetDirectory": "/tmp"
  }
}
```

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Http Connector | An 'HTTP Connector' that is configured to execute the request.
Url | The HTTP(S) URL for the request.
Target | The file connector for the target filesystem.
Target Directory | The directory to store the downloaded file in.
Target Filename | The filename of the downloaded file.
Headers | The HTTP headers to use for the request. Headers must be entered as 'key: value'-pairs, with each header in a separate line.
Append Transfer Suffix | If checked, the filename on the target connector will be appended with '.igor' during transfer. This is useful to indicate that the file is currently downloaded.
Append Filetype Suffix | If checked, igor will try to determine the file type and append it to the target filename (e.g. '.html' or '.jpeg').
Username | A username for HTTP Basic Authentication. If set together with a password, a HTTP Authorization header with the encoded password will be added to every request.
Password | A password for HTTP Basic Authentication.
Target Key | The name of the key the action's results will be stored in.
