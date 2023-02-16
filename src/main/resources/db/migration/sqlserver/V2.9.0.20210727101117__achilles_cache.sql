CREATE TABLE webapi.achilles_cache_seq (
  id INT IDENTITY(1,1) PRIMARY KEY
);

CREATE TABLE webapi.achilles_cache
(
    id         BIGINT NOT NULL,
    source_id  INT NOT NULL,
    cache_name VARCHAR NOT NULL,
    cache      TEXT,
    CONSTRAINT achilles_cache_pk PRIMARY KEY (id),
    CONSTRAINT achilles_cache_fk FOREIGN KEY (source_id) REFERENCES webapi.source (source_id) ON DELETE CASCADE
);

INSERT INTO webapi.achilles_cache (id, source_id, cache_name, cache)
SELECT (SELECT MAX(id) + 1 FROM webapi.achilles_cache_seq), source_id, cache_name, cache
FROM webapi.achilles_cache;


CREATE UNIQUE NONCLUSTERED INDEX achilles_cache_source_id_idx ON webapi.achilles_cache (source_id, cache_name);