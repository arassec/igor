CREATE TABLE job (
    id VARCHAR(128) PRIMARY KEY,
    version INTEGER,
    name VARCHAR(256) NOT NULL UNIQUE,
    content TEXT
);

CREATE TABLE connector (
    id VARCHAR(128) PRIMARY KEY,
    version INTEGER,
    name VARCHAR(256) NOT NULL UNIQUE,
    content TEXT
);

CREATE TABLE job_execution (
    id SERIAL PRIMARY KEY,
    version INTEGER,
    job_id VARCHAR(128) NOT NULL,
    state VARCHAR(64) NOT NULL,
    content TEXT
);

CREATE TABLE persistent_value (
    id SERIAL PRIMARY KEY,
    version INTEGER,
    job_id VARCHAR(128) NOT NULL,
    created TIMESTAMP NOT NULL,
    content VARCHAR NOT NULL
);
CREATE UNIQUE INDEX unique_persistent_value ON persistent_value (job_id, content);

CREATE TABLE job_connector_reference (
    job_id VARCHAR(128) NOT NULL,
    connector_id VARCHAR(128) NOT NULL
);
CREATE UNIQUE INDEX unique_job_connector_reference ON job_connector_reference (job_id, connector_id);
