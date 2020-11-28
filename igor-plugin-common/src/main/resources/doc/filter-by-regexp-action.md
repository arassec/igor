# 'Filter by Regular Expression' Action

## Description
This action filters data items by evaluating a regular expression agains a property value of the data item.

If the regular expression matches, the data item is passed to the following action. 
Otherwise the data item is filtered and not passed to the following action.

## Parameters
The action can be configured by the following parameters:

Parameter | Description
---|:---|
Input | A mustache expression selecting a property from the data item. The property's value is used for matching against the regular expression.
Expression | The regular expression.
Drop matching | If checked, data items that **match** the regular expression will be removed from the stream. If unchecked, data items that **do not** match are removed from further processing.
Num threads | The number of threads this action uses to filter data items.

## Regular Expressions
This action uses Java's 'String.matches(String regExp)' method for regular expression matching.
The regular expressions supported by this method are described in [The Java Tutorials](https://docs.oracle.com/javase/tutorial/essential/regex/).