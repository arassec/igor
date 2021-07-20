# Skip Action

## Description
This action skips the first 'n' data items. 

## Event-Triggered Jobs
**This action is not available in event-triggered jobs!** The counter that skips the first 'n' data items is set on job start and applies to the complete job execution. Each skipped data item increases the counter, which is never reset until the next job execution. Since event triggered jobs don't stop their execution, the counter will never be reset. Thus, this action would skip the first 'n' data items of an event-triggered job, and forward **all** following data items afterwards until the job were restarted!

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Number | The configured number of items to skip.
