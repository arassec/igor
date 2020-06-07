[![Travis (.com)](https://img.shields.io/travis/com/arassec/igor)](https://travis-ci.com/arassec/igor) 
[![Sonar Quality Gate](https://img.shields.io/sonar/quality_gate/arassec_igor?server=http%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/dashboard?id=arassec_igor)
[![Sonar Coverage](https://img.shields.io/sonar/coverage/arassec_igor?server=http%3A%2F%2Fsonarcloud.io&color=success)](https://sonarcloud.io/component_measures?id=arassec_igor&metric=coverage&view=treemap)

# Igor 

> “I'm an Igor, thur. We don't athk quethtionth."  
> 
> *Terry Pratchett, Making Money* 

Igor is a tool for managing 'continuous workarounds'.
It provides an easy to use, reliable place where you can put all those workarounds, which would 
otherwise be solved by scattered scripts or code fragments across your applications and connectors. 

Keep your applications clean and let igor do the dirty work.

# Status

Igor gives the user a toolbox consisting of connectors and actions, which can be flexibly combined to solve bigger tasks.

It is similar to [Node-RED](https://nodered.org/), [Huginn](https://github.com/huginn/huginn) or [Beehive](https://github.com/muesli/beehive), but focuses more on
data processing instead of specific online connectors or home automation. 
It's written in Java using [Spring Boot](https://spring.io/projects/spring-boot) and provides an easy-to-use web-frontend written in [vue.js](https://vuejs.org/).

There are a handful of use-cases around file processing that can be solved right now, 
e.g. 'Regularly copy new files from an FTP-Server to a destination server via SCP'.

In the future, databases, messaging systems or other interfaces could be added to extract, transform and load the data. 

# Installation
The application is currently delivered as standalone (fat) JAR. 
In order to compile igor, at least Java 11 is required. 
An embedded h2 database is used as database by default. 
It will automatically be generated upon first start in the current  working directory, in a directory named 'dbdata'. 

In order to use igor, perform the following steps after cloning the repository:

```
./mvnw clean install
java -jar application/target/igor-<VERSION>.jar
```

You can then navigate to the running application in your browser at: http://localhost:8080

# License

This project is licensed under the [MIT License](https://github.com/arassec/igor/blob/master/LICENSE)