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
|Trigger|Util|"util-triggers"|BaseUtilTrigger.class|
|Provider|Util|"util-providers"|BaseUtilProvider.class|
|Provider|File|"file-providers"|BaseFileProvider.class|
|Action|Util|"util-actions"|BaseUtilAction.class|
|Action|File|"file-actions"|BaseFileAction.class|
|Action|Message|"message-actions"|BaseMessageAction.class|
|Action|Persistence|"persistence-actions"|BasePersistenceAction.class|
|Connector|File|"file-connectors"|BaseFileConnector.class|
|Connector|Message|"message-connectors"|BaseMessageConnector.class|

If you give your component a type ID that is already taken, igor will throw an exception during startup.

## Interfaces
In order to write your own igor component, you have to implement the following interfaces with your class.
Base classes exist that ease development of custom components of the given type.

|Component Type|Interface to Implement|Base Class to Extend|
|---|---|---|
|Trigger|Trigger.class|BaseTrigger.class|
|Provider|Provider.class|BaseProvider.class|
|Action|Action.class|BaseAction.class|
|Connector|Connector.class|BaseConnector.class|

Additionally, you have to use the following annotations in your class.

## Annotations
Igor provides the following annotations that must be used in components.

|Annotation|Type|Description|
|---|---|---|
|@IgorComponent|Class Annotation|Marks a class as component that will automatically be used by igor.|
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

In order to control the appearance, the annotation has the following parameters itself:

|Parameter|Default Value|Description|
|---|---|---|
|value|0|The standard annotation parameter, indicates the sort order of the parameter in the editor. Properties will be ordered descending according to this value.|
|secured|false|If set to `true`, the parameter editor will create a password input field and the contents of the property will be hidden by default. Additionally, if a security provider is active, the properties contents will be secured before persisting them.
|advanced|false|If set to 'true', the parameter will be hidden by default and only visible, if the user opens the advanced parameter configuration.
|defaultValue|""|An optional default value that will be used in the UI.
|subtype|NONE|Can be used to further specify the type of the parameter. The parameter editor will adopt the input accordingly. Possible values are 'MULTI_LINE' for parameters that can contain multiple lines of input and 'CRON', if the parameter represents a cron expression.

Igor supports [bean validation](https://beanvalidation.org/) which should be used to validate the input made by the user.

An example use of this annotation might look like this:
``` java
@Positive
@IgorParam(advanced = true, defaultValue = "42")
private int port;
```

### @IgorSimulationSafe
This annotation marks connector's method as safe for execution during simulated job runs.

Only methods marked with `@IgorSimulationSafe` will be executed during simulated job runs. 
For all other methods of a connector, a proxy will be generated which uses default values.

::: warning
Only idempotent operations should be annotated with this annoatation.
:::

An example use of this annotation might look like this:
``` java
@IgorSimulationSafe
List<Entry> listEntries(String parameter);
```
 