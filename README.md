[![Travis (.com)](https://img.shields.io/travis/com/arassec/igor)](https://travis-ci.com/arassec/igor) 
[![Codecov](https://img.shields.io/codecov/c/github/arassec/igor)](https://codecov.io/gh/arassec/igor)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=arassec_igor&metric=alert_status)](https://sonarcloud.io/dashboard?id=arassec_igor) 
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=arassec_igor&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=arassec_igor) 
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=arassec_igor&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=arassec_igor)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=arassec_igor&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=arassec_igor)

# Igor 
This repository contains the sources of igor, a tool for managing 'continuous workarounds'. This tool tries to provide a place to organize 
data-centric use-cases and workarounds in a centralized and reliable way. 

# Status
Igor is an application to configure generic ['Extract, Transform, Load'](https://en.wikipedia.org/wiki/Extract,_transform,_load)
processes. The web-gui can be used to configure those processes. The current focus lies on file processing, e.g. 'copy a file 
from one source (FTP/SSH/WEB) to a destination (FTP/SSH/WEB)'.

The software is still in an early stage. There are a handful of use-cases around file processing that can be solved right now. At 
the moment, the main focus of work lies on testing and documentation.

In the future, databases, messaging systems or other interfaces could be added to extract, transform and load the data. 

# Installation
The application uses Spring-Boot 2 and is currently delivered as standalone (fat) JAR. In order to compile igor, at least Java 11
 is required. An embedded h2 database is used as database. It will automatically be generated upon first start in the current 
working directory, in a directory named 'dbdata'. 

In order to use igor, perform the following steps after cloning the repository:

```
./mvnw clean install
java -jar application/target/igor-<VERSION>.jar
```

You can then navigate to the running application in your browser at: http://localhost:8080
