[![Travis (.com)](https://img.shields.io/travis/com/arassec/igor)](https://travis-ci.com/arassec/igor) 
[![Sonar Quality Gate](https://img.shields.io/sonar/quality_gate/arassec_igor?server=http%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/dashboard?id=arassec_igor)
[![Sonar Coverage](https://img.shields.io/sonar/coverage/arassec_igor?server=http%3A%2F%2Fsonarcloud.io&color=success)](https://sonarcloud.io/component_measures?id=arassec_igor&metric=coverage&view=treemap)

> “I'm an Igor, thur. We don't athk quethtionth."  
> 
> *Terry Pratchett, Making Money* 

<img style="float: right;" height="240" width="256" src="https://raw.githubusercontent.com/arassec/igor/master/documentation/docs/.vuepress/public/logo.png" alt="igor-logo"/>

Igor is a tool for managing 'continuous workarounds'.

It provides an easy to use, reliable place where you can put all those workarounds, which would 
otherwise be solved by scattered scripts or code fragments across your applications and connectors. 

# Status

Igor gives the user a toolbox consisting of connectors and actions, which can be flexibly combined to solve bigger tasks.

It is similar to [Node-RED](https://nodered.org/), [Huginn](https://github.com/huginn/huginn) or [Beehive](https://github.com/muesli/beehive), but focuses more on
data processing instead of specific online services or home automation. 
It's written in Java using [Spring Boot](https://spring.io/projects/spring-boot) and provides an easy-to-use web-frontend written in [vue.js](https://vuejs.org/).

There are a handful of use-cases around file processing that can be solved right now, 
e.g. 'Regularly copy new files from an FTP-Server to a destination server via SCP'.

In the future, databases, messaging systems or other interfaces could be added to extract, transform and load the data. 

# Reference Guide

[Current Development Version](https://arassec.github.io/igor/current)

# License

This project is licensed under the [MIT License](https://github.com/arassec/igor/blob/master/LICENSE)