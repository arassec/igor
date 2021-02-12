# Developing Igor

## Requirements
If you not only want to extend igor but modify its core, you can do this by following these steps. 

To ease development of igor's frontend, which is written in Vue.js, you should install **{{ $themeConfig.igorNodeVersion }}**
(or a later version) in addition to **{{ $themeConfig.igorJavaVersion }}**.

To execute the end-to-end tests you have to install [Docker](https://www.docker.com/) on your machine.

## Backend
The first step is to checkout igor's source code from GitHub:
``` shell script
$> git clone https://github.com/arassec/igor.git
```

Next build the whole project using java (including the end-to-end tests using the maven profile `e2e`:
``` shell script
$> ./mvnw clean install -Pe2e
```

You can start igor now with the following main class from within your IDE:
```
com.arassec.igor.standalone.IgorStandaloneApplication
```
The backend's REST API will now be available under: `http://localhost:8080/api`

## Frontend
In order to start the frontend you have to change to the `frontend` directory and run:
``` shell script
$> npm install
$> npm run serve
```
The frontend is now available under: `http://localhost:8081/`

## End-To-End Tests
During the build, the [Cypress](https://www.cypress.io/) end-to-end tests are executed by maven if the profile `e2e` is set.

To run the tests locally, execute the following in the `e2e` directory:
``` shell script
$> npm install
$> npm run env:up
```

This will install necessary software and start required services using the `docker-compose.yml` file provided in the directory.

You can then execute the end-to-end tests manually by starting the backend and frontend as described above,
and finally starting the Cypress frontend:

``` shell script
$> npm run cypress:local
```

If you modify igor, you should make sure those tests still pass, by activating the `e2e` profile during the build.
