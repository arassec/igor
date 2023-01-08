# 'Split String' Action

## Description
This action uses the split() method of Java strings to split an input string into a JSON array of sub-strings. 

## Example
The following input: 
```
a.simple.example.string
```

together with the regular expression: 
```
\.
```

will result in the following data item: 
```
{
  "data":{
    "splitted":[
      "a",
      "simple",
      "example",
      "string"
    ]
  },
  "meta":{
    "jobId":"cd6e0b06-cdfd-4fc7-809d-644bf1f793f6",
    "timestamp":1673204387303
  }
}
```

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
Input | The input string to split.
Regex | A (Java) regular expression that is used to split the input string.
Target Key | Key into the data item where the resulting array is put. Can be a dot-separated path.
