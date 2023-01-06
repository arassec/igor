---
pageClass: docpage
---

# Documentation and I18N

## Documenting Components
Igor provides an online help for every component that is documented.
The help is opened by clicking the ![help button](../user/images/common/help-button.png) button in the parameter editor.

Component documentation must be in **Markdown** format and placed in the following directory in the classpath:
```
doc/
```
The file containing a component's documentation must be name like the component's **type ID**, e.g.
```
doc/custom-component.md
```
where `custom-component` ist the component's type ID.

I18N of component documentation is supported by placing translated documentation files in subdirectories that are named like the respective locale.

E.g. a translated documentation in german for the custom component must be placed in a classpath directory like:
```
doc/de_DE/custom-component.md
```

If no translated documentation for the current user's locale is found, igor will fall back to the standard documentation (if it exists).

## I18N
Igor supports translation of categories, types and parameters of components.
In order to use the translation you have to place properties files in the following directory in the classpath:
```
i18n/
```

E.g. the translation for your custom components could be placed in the file:
```
i18n/custom-labels.properties
```

Translations must be placed in subdirectories that are named like the respective locale.

E.g. a german translation for your custom components must be placed in:
```
i18n/de_DE/custom-labels.properties
```
All properties files from the classpath residing in an `i18n/` directory will be used as message source for translations.

### I18N Example
For a custom component...
``` java
/**
 * A custom action.
 */
@IgorComponent(categoryId = "custom-actions-category", typeId = "custom-action-type")
public class CustomAction extends BaseAction {

    /**
     * A sample parameter.
     */
    @IgorParam
    private boolean addMessage = true;

    ...
}
```

...the properties file might look like this:
``` properties
# Category-ID label
custom-actions-category=Custom Actions

# Type-ID label
custom-action-type=Special Action

# Igor parameters of components:
custom-action-type.addMessage=Really add message?
```

The labels defined will be used in igor's UI and in generated component documentation.

## Generating Documentation
Since keeping code and documentation in sync is difficult, you can use the **igor-maven-plugin** to generate documentation of your custom components.

::: danger Java 17 Features
Igor uses [JavaParser](https://javaparser.org/) to parse component source files and generate documentation from them.

The parser supports Java only up to language level **15** at the moment.
Since igor requires Java **17**, you might use langueage features in components that lead to parser errors during documentation generation, e.g.:

```
[ERROR] Could not parse file AddDataAction.java (generated documentation 
might be incomplete): (line 112,col 21) Use of patterns with instanceof is not 
supported.
```

In these cases you should provide manual documentation for the respective component as described above.
:::

In order to use the plugin you have to enable it your project's `pom.xml` file:
<div class="language-text"><pre>
<code>&lt;plugin&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;com.arassec.igor&lt;/groupId&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;igor-maven-plugin&lt;/artifactId&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&lt;version&gt;{{ $themeConfig.igorVersion }}&lt;/version&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&lt;executions&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;execution&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;goals&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;goal&gt;DocGen&lt;/goal&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/goals&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/execution&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&lt;/executions&gt;
&lt;/plugin&gt;
</code></pre></div>

The plugin will scan all igor components under `src/main/java` and generate component documentation from the component's JavaDoc comments in the directory:
```
src/main/resources/doc-gen
```

The precedence to determine which documentation file to use, if multiple for the same type ID exist, is:
* First try manually created documentation for the user's current locale, e.g. `doc/de_DE/custom-component-type.md`
* If that doesn't exist try default, manually created documentation under `doc/`, e.g. `doc/custom-component-type.md`
* If there is still no documentation found, try generated documentation under `doc-gen`, e.g. `doc-gen/custom-component-type.md`

The following HTML elements are converted to their Markdown equivalents during generation:
|HTML Element|Generation Result|
|---|---|
|&lt;h2&gt;heading2&lt;/h2&gt; ... &lt;h6&gt;heading6&lt;/h6&gt;|# heading2 ... ###### heading6|
|&lt;br&gt;|\n\n|
|&lt;strong&gt;text&lt;/strong&gt;|&#42;&#42;text&#42;&#42;|
|&lt;b&gt;text&lt;/b&gt;|&#42;&#42;text&#42;&#42;|
|&lt;i&gt;text&lt;i&gt;|&#42;text&#42;|
|&lt;a href="https://www.arassec.com"&gt;arassec&lt;/a&gt;|&#91;arassec&#93;(https://www.arassec.com)|
|HTML tables|Markdown tables|
|&lt;pre&gt;text&lt;/pre&gt;|&#96;text&#96;|
|&lt;pre&gt;&lt;code&gt;text&lt;/code&gt;&lt;/pre&gt;|\n&#96;&#96;&#96;text&#96;&#96;&#96;\n|
|&lt;p&gt;text&lt;/p&gt;|\n\ntext\n|
|&lt;ul&gt;&lt;li&gt;first&lt;/li&gt;&lt;li&gt;second&lt;/li&gt;&lt;/ul&gt;|* first\n* second\n\n

Due to changes in JavaDoc-Linting HTML &lt;h1&gt; headings are not supported anymore!

### Code-Gen Example
E.g. for the following component...
``` java
/**
 * <h1>Custom Action</h1>
 * An action providing <strong>really</strong> usefull functionality.
 */
@IgorComponent(categoryId = "custom-actions-category", typeId = "custom-action-type")
public class CustomAction extends BaseAction {

    /**
     * A sample parameter. For details see <a href="https://intra.net">here</a>.
     */
    @IgorParam
    private boolean addMessage = true;

    ...
}
```

... the following documentation would be generated:
``` markdown
# Custom Action

An action providing **really** usefull functionality.

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
addMessage | A sample parameter. For details see [here](https://intra.net)
```
