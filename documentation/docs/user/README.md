# Installation

All possible ways of installing igor are listed below.

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
**{{ $themeConfig.igorJavaVersion }}** is required in order to start igor.

<div class="language-text"><pre>
<code># download the executable jar from GitHub
$> wget https://github.com/arassec/igor/releases/download/{{ $themeConfig.igorVersion }}/igor-standalone-{{ $themeConfig.igorVersion }}.jar
# Start the jar with java
$> java -jar igor-standalone-{{ $themeConfig.igorVersion }}.jar 
</code></pre></div>

## From Source
In order to build igor from source, **{{ $themeConfig.igorJavaVersion }}** needs to be installed.
The application is compiled as Spring-Boot fat jar, containing all its dependencies.
``` shell script
# get the sources from GitHub:
$> git clone https://github.com/arassec/igor.git

# build igor
$> cd igor && ./mvnw clean install

# run the application
$> java -jar igor-standalone/target/igor-0.0.0-SNAPSHOT.jar
```
