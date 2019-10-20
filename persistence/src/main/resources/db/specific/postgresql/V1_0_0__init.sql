CREATE TABLE job (
    id SERIAL PRIMARY KEY,
    version INTEGER,
    name VARCHAR(256) NOT NULL UNIQUE,
    content TEXT
);

CREATE TABLE service (
    id SERIAL PRIMARY KEY,
    version INTEGER,
    name VARCHAR(256) NOT NULL UNIQUE,
    content TEXT
);

CREATE TABLE job_execution (
    id SERIAL PRIMARY KEY,
    version INTEGER,
    job_id INTEGER NOT NULL,
    state VARCHAR(64) NOT NULL,
    content TEXT
);

CREATE TABLE persistent_value (
    id SERIAL PRIMARY KEY,
    version INTEGER,
    job_id INTEGER NOT NULL,
    task_id VARCHAR NOT NULL,
    created TIMESTAMP NOT NULL,
    content VARCHAR NOT NULL
);
CREATE UNIQUE INDEX unique_persistent_value ON persistent_value (job_id, task_id, content);

CREATE TABLE job_service_reference (
    job_id INTEGER NOT NULL,
    service_id INTEGER NOT NULL
);
CREATE UNIQUE INDEX unique_job_service_reference ON job_service_reference (job_id, service_id);
