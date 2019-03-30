CREATE SCHEMA IF NOT EXISTS igor;

CREATE TABLE igor.job (
    id INTEGER PRIMARY KEY auto_increment,
    version INTEGER,
    name VARCHAR(256) NOT NULL UNIQUE,
    content TEXT
);
CREATE SEQUENCE JOB_ID_SEQUENCE START WITH 1 INCREMENT BY 1;

CREATE TABLE igor.service (
    id INTEGER PRIMARY KEY auto_increment,
    version INTEGER,
    name VARCHAR(256) NOT NULL UNIQUE,
    content TEXT
);
CREATE SEQUENCE SERVICE_ID_SEQUENCE START WITH 1 INCREMENT BY 1;

CREATE TABLE igor.job_execution (
    id INTEGER PRIMARY KEY auto_increment,
    version INTEGER,
    job_id INTEGER NOT NULL,
    state VARCHAR(64) NOT NULL,
    content TEXT
);
CREATE SEQUENCE JOB_EXECUTION_ID_SEQUENCE START WITH 1 INCREMENT BY 1;

CREATE TABLE igor.persistent_value (
    id INTEGER PRIMARY KEY auto_increment,
    version INTEGER,
    job_id INTEGER NOT NULL,
    task_id VARCHAR NOT NULL,
    created TIMESTAMP NOT NULL,
    content VARCHAR NOT NULL
);
CREATE SEQUENCE PERSISTENT_VALUE_ID_SEQUENCE START WITH 1 INCREMENT BY 1;
CREATE UNIQUE INDEX igor.unique_persistent_value ON igor.persistent_value (job_id, task_id, content);

CREATE TABLE igor.job_service_reference (
    job_id INTEGER NOT NULL,
    service_id INTEGER NOT NULL
);
CREATE UNIQUE INDEX igor.unique_job_service_reference ON igor.job_service_reference (job_id, service_id);
