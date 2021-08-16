# General Concepts

## Category- and Type-ID
All igor components are organized in categories and have a unique type.
To support this concept, an igor component must provide a category and type ID within the component annotation (see <router-link to="/developer/component.html#igorcomponent">@IgorComponent</router-link>).

The category and type of a component are selected in the user interface with dropdown-boxes.

If you want to add your custom component to an existing category, you should set it accordingly:

|Category|Category-ID Definition|String representation|
|---|---|---|
|Util|CoreCategory.UTIL|"util"|
|Web|CoreCategory.WEB|"web"|
|File|CoreCategory.FILE|"file|
|Message|CoreCategory.MESSAGE|"message"|
|File|CoreCategory.TEST|"test"|
|Persistence|CoreCategory.PERSISTENCE|"persistence"|

If you give your component a type ID that is already taken, igor will throw an exception during startup.

## Interfaces
In order to write your own igor component, you have to implement the following interfaces with your class.
Base classes exist to ease the development of custom components of the given type.

The interfaces and base classes are all found in the package (or sub-packages of): `com.arassec.igor.core.model`

|Component Type|Interface to Implement|Base Class to Extend|
|---|---|---|
|Trigger|Trigger|BaseTrigger|
|Action|Action|BaseAction|
|Connector|Connector|BaseConnector|

Additionally, you have to use the following annotations in your class.

## Annotations
Igor provides the following annotations that must be used in components.

|Annotation|Type|Description|
|---|---|---|
|@IgorComponent|Class Annotation|Marks a class as component that will be used by igor.|
|@IgorParam|Property Annotation|Marks a property of a class as parameter, that should be configurable by the user.|
|@IgorSimulationSafe|Method Annotation|Marks a method of a Connector as safe for simulated job executions.

### @IgorComponent
This annotation marks a class as igor component.

Within the annotation the component's category and type ID must be specified.

An example use of this annotation might look like this:
``` java
@IgorComponent(categoryId = "custom-category", typeId = "custom-type")
public class CustomConnector extends BaseConnector {
   ...
}
```

Only classes annotated with `@IgorComponent` will be considered as components, thus enabling inheritance.

E.g. the following class will **not** be considered by igor, although implementing a component interface, and can thus be used as base class for multiple custom components:
``` java
public abstract class CustomBaseConnector implements Connector {
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

::: tip
Only idempotent operations should be annotated with this annotation.
:::

An example use of this annotation might look like this:
``` java
@IgorSimulationSafe
List<Entry> listEntries(String parameter);
```

## Method Hooks
All igor components implement the following methods.
They are each called once during job execution and can be extended by custom components to perform initialization or shutdown tasks:

``` java
/**
 * Initializes the component before job executions.
 *
 * @param jobExecution Contains the state of the job execution.
 */
default void initialize(JobExecution jobExecution) {
}

/**
 * Shuts the component down at the end of the job execution.
 *
 * @param jobExecution Contains the state of the job execution.
 */
default void shutdown(JobExecution jobExecution) {
}
```
