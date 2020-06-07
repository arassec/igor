# Igor Reference Guide

## What is Igor?
Igor is a tool for managing 'continuous workarounds'.
It provides an easy to use, reliable place where you can put all those workarounds, which would 
otherwise be solved by scattered scripts or code fragments across your applications and connectors. 

It is similar to [Node-RED](https://nodered.org/), [Huginn](https://github.com/huginn/huginn) or [Beehive](https://github.com/muesli/beehive), but focuses more on
data processing instead of specific online services or home automation. 

It's written in Java using [Spring Boot](https://spring.io/projects/spring-boot) and provides an easy-to-use web-frontend written in [vue.js](https://vuejs.org/).

## Quick Start

### Docker
``` sh
# use docker to get the latest, stable version:
docker run --name igor -p8080:8080 arassec/igor

# igor is now available in your browser under:
http://localhost:8080
```

### From Source
In order to build igor from source Java 11 needs to be installed.
``` sh
# get the sources from GitHub:
git clone https://github.com/arassec/igor.git

# build igor
mvnw clean install

# run the application
java -jar application/target/igor-0-SNAPSHOT.jar

# igor is now available in your browser under:
http://localhost:8080
```

## Contact

You can contact me via e-mail under andreas.sensen@arassec.com

If you found a bug or have a feature request, don't hesitate to create an issue at [GitHub](https://github.com/arassec/igor/issues).