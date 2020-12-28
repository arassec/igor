# 'HTTP Request' Action

## Description
This action issues HTTP requests against web servers.

The action adds the results of the request to the data item under the 'webResponse' key.

A data item processed by this action could look like this:
``` json
{
  "data": {},
  "meta": {
    "jobId": "69c52202-4bc6-4753-acfe-b24870a75e90",
    "simulation": true,
    "timestamp": 1601302794228
  },
  "webResponse": {
    "headers": {
      ":status": [
        "200"
      ],
      "access-control-allow-credentials": [
        "true"
      ],
      "access-control-allow-origin": [
        "*"
      ],
      "content-length": [
        "451"
      ],
      "content-type": [
        "application/json"
      ],
      "date": [
        "Mon, 28 Sep 2020 14:19:52 GMT"
      ],
      "server": [
        "gunicorn/19.9.0"
      ]
    },
    "body": {
      "args": {},
      "json": {
        "jobId": "69c52202-4bc6-4753-acfe-b24870a75e90"
      },
      "url": "https://httpbin.org/post"
    }
  }
}
```
## Parameters
The following parameters can be configured for the 'HTTP Request' action:

Parameter | Description
---|:---|
Http connector | An 'HTTP Connector' that is configured to execute the request.
Url | The HTTP(S) URL to execute the request against.
Method | The HTTP method. Can be one of 'GET', 'HEAD', 'POST', 'PUT', 'DELETE', 'CONNECT', 'OPTIONS', 'TRACE' or 'PATCH'.
Headers | The HTTP headers to use for the request. Headers must be entered as 'key: value'-pairs, with each header in a separate line.
Body | The HTTP body to use for the request.
Username | A username for HTTP Basic Authentication. If set together with a password, a HTTP Authorization header with the encoded password will be added to every request.
Password | A password for HTTP Basic Authentication.
Target key | The name of the key the web request's results will be stored in.
Ignore errors | If checked, igor will ignore the HTTP result code and treat every response as HTTP 200 ('OK').
Simulation safe | If checked, HTTP POST, PUT and DELETE will not be executed by the action during simulated job executions.
Num threads | The number of threads this action uses to execute requests.
