# Pause Action

## Description
This action pauses execution by the configured amount of time.
Execution pauses **for every data item** which is processed by the action.

## Parameters
The action can be configured by the following parameters:

Parameter | Description
---|:---|
Milliseconds | The number of milliseconds to pause for each data item.
Variance | A number smaller than 'Milliseconds'. If set, the actual pause time will be random and between 'Milliseconds - Variance' and 'Milliseconds + Variance'. Set to 0 to disable variable pause times.
