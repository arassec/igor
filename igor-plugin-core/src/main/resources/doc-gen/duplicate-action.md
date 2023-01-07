# Duplicate Action

## Description
This action duplicates each processed data item by the configured amount.


The index of the duplicated data item is added to the result under the key 'index'. 

## Examples
The configured amount will be the result size of the data items created by the action.


E.g. a configuration of 1 will result in the data item being passed through, a configuration of 2 will result in a duplication of the processed data item, and so on...

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Amount | The amount of duplicate data items that should be created by this action.
