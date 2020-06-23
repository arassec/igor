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
