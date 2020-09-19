<p align="center">
  <img width="256" src="https://raw.githubusercontent.com/arassec/igor/master/documentation/docs/.vuepress/public/logo.png" alt="igor-logo"/>
</p>

<p align="center">
  <a href="https://travis-ci.com/arassec/igor">
    <img src="https://img.shields.io/travis/com/arassec/igor" alt="build-state"/>
  </a>
  <a href="https://sonarcloud.io/dashboard?id=arassec_igor">
    <img src="https://img.shields.io/sonar/quality_gate/arassec_igor?server=http%3A%2F%2Fsonarcloud.io" alt="quality-state"/>
  </a>
  <a href="https://sonarcloud.io/component_measures?id=arassec_igor&metric=coverage&view=treemap">
    <img src="https://img.shields.io/sonar/coverage/arassec_igor?server=http%3A%2F%2Fsonarcloud.io&color=success" alt="coverage-state"/>
  </a>
</p> 


# What is Igor?
Igor is a tool for managing 'continuous workarounds'.

It provides an easy to use, reliable place where you can put all those workarounds, which would 
otherwise be solved by scattered scripts or code fragments across your applications and connectors. 

It is similar to [Node-RED](https://nodered.org/), [Huginn](https://github.com/huginn/huginn) or [Beehive](https://github.com/muesli/beehive), but focuses more on
data processing instead of specific online services or home automation. 

It's written in Java using [Spring Boot](https://spring.io/projects/spring-boot) and provides an easy-to-use web-frontend written in [vue.js](https://vuejs.org/).

# What can it do?

Continuous workarounds are configured in igor using **jobs**.

A job ist triggered by a **trigger**, e.g. a CRON trigger starting the job at a regular interval or an event that is received by the trigger.
The trigger creates an initial data item to start the job.

The data item is passed to different, configurable **actions**, that modify the data to the user's needs.
Actions can also be used to load data from or store modified data items in external services using **connectors** for these services..

<br/>

![igor overview image](https://raw.githubusercontent.com/arassec/igor/master/documentation/docs/overview.png)

# Quick Start
Install igor using docker:
``` sh
# use docker to get the latest, stable version:
docker run --name igor -p8080:8080 arassec/igor
```
Igor's web interface should now be available at: [http://localhost:8080](http://localhost:8080)

# Reference Documentation

The reference documentation is distributed within the igor application.
The documentation of the latest, stable release is also available online at:

[Igor Reference Documentation](https://arassec.com/igor/)

# License

This project is licensed under the [MIT License](https://github.com/arassec/igor/blob/master/LICENSE)