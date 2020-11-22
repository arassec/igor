---
pageClass: docpage
---

# Custom Actions

## Introduction
Actions manipulate the data items supplied to them.
There is one method a custom action must implement:

``` java
/**
 * Executes the action.
 *
 * @param data         The data the action will work with.
 * @param jobExecution The job's execution log.
 *
 * @return A list of data items that should further be processed, 
 * or {@code null}, if there is no further data to process.
 */
List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution);
```
The method gets the original data item as input and returns a list of result data items for the next action to process.

## Example Code
A custom action should extend `BaseAction` to get sensible defaults for the methods to override.

The code of our custom action looks like this:

``` java
/**
 * A custom action that adds a message to the data items.
 */
@IgorComponent
public class CustomAction extends BaseAction {

    /**
     * A parameter switching the message on or off.
     */
    @IgorParam(defaultValue = "true")
    private boolean addMessage;

    /**
     * Creates a new component instance.
     */
    public CustomAction() {
        super("Demo-Actions", "Custom-Action");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, 
                                             JobExecution jobExecution) {
        if (addMessage) {
            data.put("message", "A custom action's message");
        }
        return List.of(data);
    }

}
```

This action will be available in igor under the Category- and Type-ID we set in the constructor.

Category|Type
---|---
Demo-Actions|Custom-Action

The processed data item of our action looks like this, if the checkbox is selected by the user:

``` json
{
  "data": {},
  "meta": {
    "jobId": "2400f526-b5b2-4d7e-b1f7-12e8cb886944",
    "simulation": true,
    "simulationLimit": 25,
    "timestamp": 1605884485974
  },
  "message": "A custom action's message"
}
```
