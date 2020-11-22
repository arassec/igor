# Job

## Description
Jobs are the core elements of igor. They contain all configurations required to fulfill a specific... job.

A **Trigger** determines when the job should run. It activates job execution e.g. based on a daily schedule or on received events.
The trigger creates an initial data item, which is passed to the job's first action.

Each data item is then handed over to the following action and so on.
Think about the "Java Stream API" to get an idea of the process. 

**Actions** implement the job's logic and operate on the data items of the data stream.

## Parameters
The following parameters can be configured for every job. 

Parameter | Description
---|:---|
Active | Active jobs are triggered by the configured trigger, inactive jobs not.
Name | The name of the job.
Description | An optional description of the job.
History Limit | Number of job executions that are kept by igor. If the limit is reached, old successful executions will be removed.
Simulation Limit | Maximum number of data items that is processed by actions during a job simulation.
Fault tolerant | If checked, the job will be triggered even if the last job execution failed. A successful execution will mark all previous, failed executions as 'Resolved'. If unchecked, the job will not be triggered if the last job execution failed. 

Additional configuration parameters might be available, depending on the trigger's type.