# Configuration

Igor is a Spring-Boot application. 
Thus, it can be configured like any other Spring-Boot application, be it with JVM parameters, an application.properties file in the root directory or any other method supported by Spring.

## Database Configuration
For its own database, igor supports **H2** and **PostgreSQL** databases. 
Igor uses an embedded H2 database as default. It will be stored in a `dbdata` directory in the root folder. 
For production use, PostgreSQL is strongly advised. 
You can configure the Database to use by configuring it with standard Spring mechanisms.

### Example configuration with application parameters:
``` shell script
$> java -jar igor.jar \
      --spring.datasource.url=jdbc:postgresql://localhost:5432/igor \
      --spring.datasource.username=igor \
      --spring.datasource.password=igor \
      --spring.datasource.driver-class-name=org.postgresql.Driver
```

### Example configuration with application.properties:
``` properties
spring.datasource.url=jdbc:postgresql://localhost:5432/igor
spring.datasource.username=igor
spring.datasource.password=igor
spring.datasource.driver-class-name=org.postgresql.Driver
```

## Job Queue Size
By default, igor runs up to 5 jobs in parallel. 
Depending on the hardware igor runs on, more jobs can be configured. 
This can be done with the parameter `igor.core.job-queue-size` 

### Example configuration with application parameter:
``` shell script
$> java -jar igor.jar \
      --igor.core.job-queue-size=25
```

### Example configuration with application.properties:
``` properties
igor.core.job-queue-size=25
```

## Encrypting Configuration values
Igor might use sensitive configuration values for its work, for example passwords to login to online services.
::: danger
By default, sensitive configuration values will be stored in cleartext in igor's database!
:::
In order to save those configuration values encrypted in igor's database, the parameter `igor.persistence.local-security-token` can be used.
If provided, igor uses the configured value for symmetric encryption of sensitive values like passwords.

### Example configuration with application parameter:
``` shell script
$> java -jar igor.jar \
      --igor.persistence.local-security-token=f8c7fb78-3336-11e7-a919-92ebcb67fe33
```

### Example configuration with application.properties:
``` properties
igor.persistence.local-security-token=f8c7fb78-3336-11e7-a919-92ebcb67fe33
```
