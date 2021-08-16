---
pageClass: docpage
---

# Custom Triggers

## Introduction
Triggers start an igor job. 
At the moment, there are three kinds of triggers available in igor:
- Triggers for manual execution
- Triggers that schedule a job based on a CRON expression
- Event based triggers

## Methods
There should usually be no need to create a custom trigger.
However, the following method might be useful and can be implemented by custom triggers:

``` java
/**
 * Creates the initial data item that is used as input for following actions.
 *
 * @return The initial data item.
 */
Map<String, Object> createDataItem();
```

Additionally, custom event based triggers might be useful to support event sources that igor doesn't support natively.
In this case the custom trigger can contain a (custom) connector that is used to retrieve the events.

## Custom Manual Trigger
A custom trigger for manual execution should extend `BaseTrigger` to get sensible defaults for the methods to override.

The code of our custom manual trigger looks like this:

``` java
/**
 * A custom trigger for manual job execution.
 */
@IgorComponent(categoryId = "Demo-Triggers", typeId = "Custom-Manual-Trigger")
public class CustomManualTrigger extends BaseTrigger {

    /**
     * A sample parameter.
     */
    @IgorParam
    private String demoParam;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> createDataItem() {
        Map<String, Object> dataItem = super.createDataItem();
        dataItem.put("demoParam", demoParam);
        return dataItem;
    }

}
```

This trigger will be available in igor under the Category- and Type-ID we set in the `@IgorComponent` annotation.

Category|Type
---|---
Demo-Triggers|Custom-Manual-Trigger

The demo parameter will be added to the initial data item:

``` json
{
  "demoParam": "user input",
  "data": {},
  "meta": {
    "jobId": "2400f526-b5b2-4d7e-b1f7-12e8cb886944",
    "simulation": true,
    "simulationLimit": 25,
    "timestamp": 1599587099204
  }
}
```

## Custom Scheduled Trigger
A custom scheduled trigger must implement the `ScheduledTrigger` interface.

The code of our custom scheduled trigger looks like this:

``` java
/**
 * A custom trigger for scheduled job execution.
 */
@IgorComponent(categoryId = "Demo-Triggers", typeId = "Custom-Scheduled-Trigger")
public class CustomScheduledTrigger extends BaseTrigger implements ScheduledTrigger {

    /**
     * The CRON expression that is used to trigger the job.
     */
    @NotEmpty
    @IgorParam(subtype = ParameterSubtype.CRON)
    protected String cronExpression;

    /**
     * A sample parameter.
     */
    @IgorParam
    private String demoParam;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> createDataItem() {
        Map<String, Object> dataItem = super.createDataItem();
        dataItem.put("demoParam", demoParam);
        return dataItem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCronExpression() {
        return cronExpression;
    }

}
```

This trigger will be available under the same category as the previous one:

Category|Type
---|---
Demo-Triggers|Custom-Scheduled-Trigger

According to our code, the configured parameter will be added to the initial data item created by the trigger:

``` json
{
  "demoParam": "user input"
  "data": {},
  "meta": {
    "jobId": "2400f526-b5b2-4d7e-b1f7-12e8cb886944",
    "simulation": true,
    "simulationLimit": 25,
    "timestamp": 1599837870589
  }
}
```

## Custom Event Trigger
A custom event trigger should extend `BaseEventTrigger` to get sensible defaults for the methods to override.

The code of our custom event trigger looks like this:

``` java
/**
 * A custom trigger for jobs that are triggered by events.
 */
@IgorComponent(categoryId = "Demo-Triggers", typeId = "Custom-Event-Trigger")
public class CustomEventTrigger extends BaseEventTrigger {

    /**
     * Spring's publisher for events. This is used to start processing the
     * event's data.
     */
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * A sample parameter.
     */
    @IgorParam
    private String demoParam;

    /**
     * The job's ID.
     */
    private String jobId;

    /**
     * Creates a new instance.
     *
     * @param applicationEventPublisher The event published injected by Spring.
     */
    public CustomEventTrigger(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(JobExecution jobExecution) {
        super.initialize(jobExecution);
        this.jobId = jobExecution.getJobId();

        // Simulates an event, fifteen seconds after the trigger is initialized...
        var timer = new Timer();
        TimerTask fakeEvent = new TimerTask() {
            @Override
            public void run() {
                fakeEvent();
            }
        };
        timer.schedule(fakeEvent, 15000);
    }

    /**
     * Usually, an event would be e.g. a message that is received, and the code to
     * start processing the event's data could be integrated in a custom connector.
     */
    public void fakeEvent() {
        Map<String, Object> dataItem = new HashMap<>();
        dataItem.put("currentTimeMillis", System.currentTimeMillis());
        // The EventType must match the result provided by getSupportedEventType()!
        applicationEventPublisher.publishEvent(
                new JobTriggerEvent(jobId, dataItem, EventType.UNKNOWN));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> createDataItem() {
        Map<String, Object> dataItem = super.createDataItem();
        dataItem.put("demoParam", demoParam);
        return dataItem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventType getSupportedEventType() {
        return EventType.UNKNOWN;
    }

}
```

Again, the trigger will be available under the category as the previous two.

Category|Type
---|---
Demo-Triggers|Custom-Event-Trigger

According to our code, the data items processed will contain the current time in milliseconds since 1970 as "event data".
Additionally, the configured parameter value of our trigger will be added to the initial data item, too:

``` json
{
  "demoParam": "user input",
  "currentTimeMillis": 1629041492762,
  "data": {},
  "meta": {
    "jobId": "2400f526-b5b2-4d7e-b1f7-12e8cb886944",
    "simulation": false,
    "timestamp": 1629041492762
  }
}
```
