# 'Query Data' Action

## Description
Queries a database and adds the result to the processed data item. 

## Example
The following SQL query: 
```
SELECT subject AS sub, body AS bod FROM mail_log;
```

might result in the following data items: 
```
{
  "queryResult": [
    {
      "sub": "Igor Data Test 1",
      "bod": "Test body number 1."
    },
    {
      "sub": "Igor Data Test 2",
      "bod": "Test body number 2."
    }
  ]
}
```

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Datasource | The datasource to query the data from.
SQL Query | The SQL statement to use.
Target Key | The target key to store the query result in. Can be a dot-separated path into the JSON object.
Transaction Key | The key into the data item to get an optional transaction ID. Can be a dot-separated path into the JSON object.
