CREATE SCHEMA IF NOT EXISTS igor;

CREATE TABLE igor.job (
    id VARCHAR(256) PRIMARY KEY,
    version INTEGER,
    content TEXT
);

CREATE TABLE igor.service (
    id VARCHAR(256) PRIMARY KEY,
    version INTEGER,
    content TEXT
);
