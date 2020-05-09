# Job
Jobs are the core elements of igor. They contain all configurations required to fulfill a specific mission.

A job is triggered by a **Trigger**, which determines when the job should run.

Jobs themselves are split into **Tasks**. Tasks are executed sequentially and perform independent steps to support the job's mission. 
If a task fails, the job execution is aborted and all subsequent tasks are ignored.

# Parameters
The following parameters can be configured for every job. 

Parameter | Description
---|:---|
Active | Active jobs are triggered by the configured trigger, inactive jobs not.
Name | The name of the job.
Description | An optional description of the job.
History Limit | Number of job executions that are kept by igor. If the limit is reached, old successful executions will be removed.
Fault tolerant | If checked, the job will be triggered even if the last job execution failed. A successful execution will mark all previous, failed executions as 'Resolved'. If unchecked, the job will not be triggered if the last job execution failed. 

Depending on the trigger's type, additional parameters might be configured.