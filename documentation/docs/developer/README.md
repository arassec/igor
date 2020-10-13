# Extending Igor

## Introduction
Igor is highly extensible and can be customized very easily. 
It is based on the popular Spring framework and uses many of its tools and concepts.

Custom types for the following components can easily be added to igor:
- Trigger
- Action
- Connector 

## Project Configuration
At the moment, the best way to extend igor is by creating your own Spring-Boot project and using the `igor-spring-boot-starter`.

Igor requires at least **{{ $themeConfig.igorJavaVersion }}** to be installed.

Visit [start.spring.io](https://start.spring.io) and create a new Spring-Boot project.

Then add the following dependency to your project's `pom.xml` file:
<div class="language-text"><pre>
<code>&lt;dependency&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;com.arassec.igor&lt;/groupId&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;igor-spring-boot-starter&lt;/artifactId&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&lt;version&gt;{{ $themeConfig.igorVersion }}&lt;/version&gt;
&lt;/dependency&gt;</code></pre></div>

Now your application should start and igor's web frontend should be available with the standard features of the respective version.

Although possible, it is not recommended embedding igor into a larger application with their own database.

## Building Igor

### Requirements
If you not only want to extend igor but modify its core, you can do this by following these steps. 

To ease development of igor's frontend, which is written in Vue.js, you should install **{{ $themeConfig.igorNodeVersion }}**
(or a later version) in addition to **{{ $themeConfig.igorJavaVersion }}**.

### Backend
The first step is to checkout igor's source code from GitHub:
``` shell script
$> git clone https://github.com/arassec/igor.git
```

Next build the whole project using java:
``` shell script
$> cd igor && ./mvnw clean install
```

You can start igor now with the following main class from within your IDE:
```
com.arassec.igor.application.IgorApplication
```
The backend's REST API will now be available under: `http://localhost:8080/api`

### Frontend
In order to start the frontend you have to change to the `frontend` directory and run `npm`:
``` shell script
$> cd frontend && npm run serve
```
The frontend is now available under: `http://localhost:8081/`

### End-To-End Tests
During a normal build, the end-to-end tests are executed by maven using Cypress via testcontainers.

You can execute them manually by starting the backend and frontend as described above and then performing the following steps:

The end-to-end tests are located in the application module under `src/test/e2e`.

First install Cypress via `npm`
``` shell script
$> cd ../application/src/test/e2e && npm install
```

After Cypress is installed successfully, you can start the GUI with the following command (executed from within the e2e directory):
``` shell script
$> npm run cypress:open
```

If you modify igor, you should make sure those tests still pass.
