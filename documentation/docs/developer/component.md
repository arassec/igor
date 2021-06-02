# General Concepts

## Category- and Type-ID
All igor components are organized in categories and have a unique type.
To support this concept, an igor component must implement the following two methods:
``` java
String getCategoryId();

String getTypeId();
```
Both properties are strings and can be freely assigned. 
The category and type of a component are selected in the user interface with dropdown-boxes.

If you want to add your custom component to an existing category, you should set the corresponding category ID or better yet, inherit from the appropriate base class.

|Component Type|Category|Category-ID|Base Class to Extend|
|---|---|---|---|
|Trigger|Util|"util"|BaseUtilTrigger.class|
|Trigger|Web|"web"|-|
|Action|Util|"util"|BaseUtilAction.class|
|Action|File|"file"|BaseFileAction.class|
|Action|Message|"message"|BaseMessageAction.class|
|Action|Persistence|"persistence"|BasePersistenceAction.class|
|Connector|File|"file"|BaseFileConnector.class|
|Connector|Message|"message"|BaseMessageConnector.class|

If you give your component a type ID that is already taken, igor will throw an exception during startup.

## Interfaces
In order to write your own igor component, you have to implement the following interfaces with your class.
Base classes exist that ease development of custom components of the given type.

|Component Type|Interface to Implement|Base Class to Extend|
|---|---|---|
|Trigger|Trigger.class|BaseTrigger.class|
|Action|Action.class|BaseAction.class|
|Connector|Connector.class|BaseConnector.class|

Additionally, you have to use the following annotations in your class.

## Annotations
Igor provides the following annotations that must be used in components.

|Annotation|Type|Description|
|---|---|---|
|@IgorComponent|Class Annotation|Marks a class as component that will be used by igor.|
|@IgorParam|Property Annotation|Marks a property of a class as parameter, that should be configurable by the user.|
|@IgorSimulationSafe|Method Annotation|Marks a method of a Connector as safe for simulated job executions.

### @IgorComponent
This annotation is basically a "wrapper" for Spring's `@Scope` annotation. 
Igor components are Spring beans. 
This annotation makes sure, they are in scope `prototype`.

::: warning
The **prototype** scope is important, since many instances of the same component can be created simultaneously, and will be configured in different ways.
:::

A singleton component might be useful in some cases and is possible in general.
Be aware though, that properties annotated with @IgorParam will be overwritten any time the singleton instance is used during a job, and thus might have side effects on running jobs.

An example use of this annotation might look like this:
``` java
@IgorComponent
public class CustomConnector implements Connector {
   ...
}
```

### @IgorParam
This annotation marks a property of a component as configurable by the user.
All properties of a component annotated with @IgorParam will be shown in the application's parameter editor.
A default value can be assigned directly to the property.

In order to control the appearance, the annotation has the following parameters itself:

|Parameter|Default Value|Description|
|---|---|---|
|sortIndex|0|Indicates the position of the parameter in the editor in relation to the other properties. Properties will be ordered ascending according to this value.|
|secured|false|If set to `true`, the parameter editor will create a password input field and the contents of the property will be hidden by default. Additionally, if a security provider is active, the properties contents will be secured before persisting them.
|advanced|false|If set to 'true', the parameter will be hidden by default and only visible, if the user opens the advanced parameter configuration.
|subtype|NONE|Can be used to further specify the type of the parameter. The parameter editor will adopt the input accordingly. Possible values are 'MULTI_LINE' for parameters that can contain multiple lines of input and 'CRON', if the parameter represents a cron expression.

::: tip
Igor supports [bean validation](https://beanvalidation.org/). 
You should use it to validate the input made by the user.
:::

An example use of this annotation might look like this:
``` java
@Positive
@IgorParam(advanced = true)
private int port = 42;
```

### @IgorSimulationSafe
This annotation marks connector methods as safe for execution during simulated job runs.

Only methods marked with `@IgorSimulationSafe` will be executed during simulated job runs. 
For all other methods of a connector, a proxy will be generated which uses default values.

::: warning
Only idempotent operations should be annotated with this annotation.
:::

An example use of this annotation might look like this:
``` java
@IgorSimulationSafe
List<Entry> listEntries(String parameter);
```
 