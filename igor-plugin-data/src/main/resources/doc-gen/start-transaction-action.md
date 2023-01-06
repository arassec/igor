# 'Start Transaction' Action

## Description
Starts a database transaction for **each processed** data item. 

## Attention
Remember to commit the transaction with the 'Commit Transaction' action, or else it will be rolled back at the end of the job execution!

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Datasource | The datasource to start the transaction in.
Transaction Key | The key into the data item to store the transaction ID in. Can be a dot-separated path into the JSON object.
