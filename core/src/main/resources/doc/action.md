# Action

## Description
Jobs consist of a list of **actions**, which are called sequentially in the configured order.
If an action fails, further execution is aborted.

An action operates on each data item of a job's data stream and performs modifications on it. The result is handed
over to the following action, which itself can modify the data.

Actions are similar to methods of a "Java Stream API" stream.
 
## Parameters
The following parameters can be configured for every action.

Parameter | Description
---|:---|
Active | Active tasks retrieve data from their provider. Inactive tasks are skipped during job executions.
Name | The name of the task.

Depending on the actual type, the action might require more parameters to be configured.

## Mustache Template Parameters
Most actions support mustache expressions in configuration parameters. 
Those expressions are evaluated against the data item the action operates on.

For example, an action operating on the following data item: 
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
can contain the mustache expression
```
{{data.filename}}
```
in a parameter configuration. 
During execution, the actual filename from the JSON data is used as parameter value.