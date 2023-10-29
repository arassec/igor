# <img src="https://raw.githubusercontent.com/arassec/igor/main/documentation/docs/.vuepress/public/logo.png" width="256"> [![Build Status](https://github.com/arassec/igor/workflows/Build/badge.svg?branch=main)](https://github.com/arassec/igor/actions?query=workflow%3ABuild)  [![Quality Gate](https://img.shields.io/sonar/quality_gate/arassec_igor?server=http%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/dashboard?id=arassec_igor) [![Code Coverage](https://img.shields.io/sonar/coverage/arassec_igor?server=http%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/component_measures?id=arassec_igor&metric=coverage&view=treemap) [![Maven Build Status](https://maven-badges.herokuapp.com/maven-central/com.arassec.igor/application/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.arassec.igor/application)

## About

Igor is a tool for managing 'continuous workarounds'.

It provides an easy to use, reliable place where you can put all those workarounds, which would
otherwise be solved by scattered scripts or code fragments across your applications and services.

It is similar to [Node-RED](https://nodered.org/), [Huginn](https://github.com/huginn/huginn)
or [Beehive](https://github.com/muesli/beehive),
i.e. it provides a "low-code" environment for developers.

It's written in Java using [Spring Boot](https://spring.io/projects/spring-boot) and provides an easy-to-use
web-frontend written in [vue.js](https://vuejs.org/).

## What can it do?

Continuous workarounds are configured in igor using **jobs**.

A job ist triggered by a **trigger**, e.g. a CRON trigger starting the job at a regular interval or an event that is
received by the trigger.
The trigger creates an initial data item to start the job.

The data item is passed to different, configurable **actions**, that modify the data to the user's needs.
Actions can also be used to load data from, or store modified data items in, external services using **connectors** for
these services..

<br/>

![igor overview image](https://raw.githubusercontent.com/arassec/igor/main/documentation/docs/overview.png)

## Quick Start

Install igor using docker:

``` sh
# use docker to get the latest, stable version:
docker run --name igor -p8080:8080 arassec/igor
```

Igor's web interface should now be available at: [http://localhost:8080](http://localhost:8080)

## Connectors

There are currently builtin connectors for the following protocols and services:

* **File Handling**
    * FTP
    * FTPS
    * Local
    * SCP
    * SFTP
* **Messaging**
    * RabbitMQ
    * E-Mail
* **Web**
    * HTTP(S)
* **Data**
    * H2
    * PostgreSQL
    * Oracle
    * IBM DB2
    * Microsoft SQL Server

You can easily add new connectors by creating a Spring-Boot application and using the `igor-spring-boot-starter` as
described in the documentation.

## Reference Documentation

The reference documentation is distributed within the igor application.
The documentation of the latest, **stable** release is also available online at:

[Igor Reference Documentation](https://arassec.github.io/igor/)

## License

This project is licensed under the [MIT License](https://github.com/arassec/igor/blob/main/LICENSE)