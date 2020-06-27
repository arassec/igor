# Installation

There are currently two ways to install igor: as docker image and compiling from source.

::: tip
After installation, igor's web interface will be available at: [http://localhost:8080/](http://localhost:8080/)
:::

## Docker
The latest, stable version of igor is available as docker image under the :latest tag.
``` shell script
# use docker to get the latest, stable version:
$> docker run --name igor -p8080:8080 arassec/igor
```

## Spring-Boot Executable Jar
Igor uses Spring-Boot and during the build process an executable JAR ist created. 
You can simply download it and run it from the console.
**Java 11** is required in order to start igor.
``` shell script
# download the executable jar from GitHub
$> curl https://github.com/arassec/igor/archive/igor-0.1.0.jar

# Start the jar with java
$> java -jar igor-0.1.0.jar 
```

## From Source
In order to build igor from source Java 11 needs to be installed.
The application is compiled as Spring-Boot fat jar, containing all its dependencies.
``` shell script
# get the sources from GitHub:
$> git clone https://github.com/arassec/igor.git

# build igor
$> cd igor && ./mvnw clean install

# run the application
$> java -jar application/target/igor-0.0.0-SNAPSHOT.jar
```
