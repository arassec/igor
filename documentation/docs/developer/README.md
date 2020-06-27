# Extending Igor

## Introduction
Igor is highly extensible and can be customized very easily. 
It is based on the popular Spring framework and uses many of its tools and concepts.

Custom types for the following components can easily be added to igor:
- Trigger
- Provider
- Action
- Connector 

## Requirements 
Igor requires **Java 11** to be installed.

## Project Configuration
The best way to extend igor is by creating your own Spring-Boot project and using the `igor-spring-boot-starter`.

Visit [start.spring.io](https://start.spring.io) and create a new Spring-Boot project.

Then add the following dependency to your project's `pom.xml` file:
``` xml
<dependency>
    <groupId>com.arassec.igor</groupId>
    <artifactId>igor-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

Now your application should start and igor's web frontend should be available with the standard features of the respective version.