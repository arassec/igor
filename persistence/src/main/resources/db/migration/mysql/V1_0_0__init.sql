CREATE TABLE job (
    id VARCHAR(256) PRIMARY KEY,
    version INTEGER,
    content JSON
) ENGINE=InnoDB  COMPRESSION="zlib" CHARSET=utf8;

CREATE TABLE service (
    id VARCHAR(256) PRIMARY KEY,
    version INTEGER,
    content JSON
) ENGINE=InnoDB  COMPRESSION="zlib" CHARSET=utf8;
