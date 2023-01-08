# 'XML to JSON' Action

## Description
This action converts an XML string into a JSON object at the same location. 

## Example
The following XML string: 
```
<?xml version="1.0" encoding="UTF-8"?>
<element>
    <status>400</status>
    <message>This is an example.</message>
    <error>A</error>
    <error>B</error>
    <error>C</error>
</element>
```

will result in the following data item: 
```
{
  "data": {
    "convertedXml": {
      "status":"400",
      "message":"This is an example.",
      "error":[
        "A",
        "B",
        "C"
      ]
    }
  },
  "meta": {
    "jobId": "01d11f89-1b89-4fa0-8da4-cdd75229f8b5",
    "timestamp": 1599580925108
  }
}
```

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Xml | The XML string to convert. Can be a mustache template to read the value from an attribute of the processed data item.
Target Key | Key into the data item where the converted JSON is put. Can be a dot-separated path.
