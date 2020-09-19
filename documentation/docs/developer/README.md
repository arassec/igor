# Extending Igor

## Introduction
Igor is highly extensible and can be customized very easily. 
It is based on the popular Spring framework and uses many of its tools and concepts.

Custom types for the following components can easily be added to igor:
- Trigger
- Action
- Connector 

## Requirements 
Igor requires **{{ $themeConfig.igorJavaVersion }}** to be installed.

## Project Configuration
At the moment, the best way to extend igor is by creating your own Spring-Boot project and using the `igor-spring-boot-starter`.

Visit [start.spring.io](https://start.spring.io) and create a new Spring-Boot project.

Then add the following dependency to your project's `pom.xml` file:
<div class="language-text"><pre>
<code>&lt;dependency&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;com.arassec.igor&lt;/groupId&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;igor-spring-boot-starter&lt;/artifactId&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&lt;version&gt;{{ $themeConfig.igorVersion }}&lt;/version&gt;
&lt;/dependency&gt;</code></pre></div>

Now your application should start and igor's web frontend should be available with the standard features of the respective version.

Although possible, it is not recommended embedding igor into a larger application with a their own database.