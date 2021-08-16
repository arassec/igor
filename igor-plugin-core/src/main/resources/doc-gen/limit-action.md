# Limit Action

## Description
This action limits the data stream to the first 'n' data items. 

## Event-Triggered Jobs
**This action is not available in event-triggered jobs!** 


The limit counter is set on job start and applies to the complete job execution. Each processed data item increases the counter, which is never reset until the next job execution. Since event triggered jobs don't stop their execution, the counter would never be reset.



Thus, this action would let the first 'n' data items of an event-triggered job pass, and block **all** following data items until the job were restarted!

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Number | The number of data items to limit the stream to.
