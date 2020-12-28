# Web-Hook Trigger

## Description
The web-hook trigger is an event based trigger that runs a job after an HTTP request has been made to igor's web-hook interface.

Igor's web-hook interface can be called with HTTP-GET or HTTP-POST requests.
The call's parameters will be added to the job's data items.

The interface is reachable under the following URL:

```
http(s)://<HOST:PORT>/webhook/<JOB-ID>
```

## Parameters
The following parameters can be configured for the web-hook trigger:

Parameter | Description
---|:---|
Simulation data | A list of "key=value" pairs that is used during simulated job runs as trigger input data. Each line must contain one "key=value" pair.

## Examples

Let's assume igor is running on localhost and reachable under port 8080.
A job with ID `01d11f89-1b89-4fa0-8da4-cdd75229f8b5`, and the web-hook trigger attached to it, can be triggered by calling:

```
http://localhost:8080/webhook/01d11f89-1b89-4fa0-8da4-cdd75229f8b5
```

This can be done by either using HTTP-GET or HTTP-POST.

If the request contains parameters, they are added to each data item which is processed by the job.

E.g. the following request:

```
http://localhost:8080/webhook/01d11f89-1b89-4fa0-8da4-cdd75229f8b5?alpha=42
```

will result in the following data items:

```
{
  "data": {
    "alpha": "42"
  },
  "meta": {
    "jobId": "01d11f89-1b89-4fa0-8da4-cdd75229f8b5",
    "timestamp": 1599580925108
  }
}
```
