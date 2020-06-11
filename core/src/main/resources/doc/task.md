# Task

## Description
Jobs are split into **Tasks**. 
Tasks are executed sequentially and perform independent steps to support the job's mission. 
If a task fails, the job execution is aborted and all subsequent tasks are ignored.

The input data for each task is provided by a **Provider**. 
Each data items is passed to the first action of the task, and then handed over to the following action and so on.
Think about the "Java Stream API" to get an idea of the process. 
The data items are the initial source of the stream's data.

## Parameters
The following parameters can be configured for every task. 

Parameter | Description
---|:---|
Active | Active tasks retrieve data from their provider. Inactive tasks are skipped during job executions.
Name | The name of the task.
Description | An optional description of the task.

Depending on the provider's type, additional parameters might be configured.