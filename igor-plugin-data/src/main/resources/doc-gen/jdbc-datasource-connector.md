# JDBC-Datasource Connector

## Description
A connector for JDBC compatible databases. 

The following JDBC drivers are included in igor: 


* H2
* PostgreSQL
* Oracle
* IBM DB2
* Microsoft SQL Server


For additional databases, integrate the igor-spring-boot-starter into a new Spring-Boot project as described in the documentation and add the respective JDBC JAR-file to the classpath.

## Parameters
The component can be configured by the following parameters:

Parameter | Description
:---|:---
URL | The URL to the datasource, e.g. 'jdbc:postgresql://localhost:5432/igor'.
Username | The username to use to connect to the datasource.
Password | The user's password.
Driver | The driver class name, e.g. 'org.postgresql.Driver'
