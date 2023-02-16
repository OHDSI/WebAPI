
CREATE TABLE ${ohdsiSchema}.achilles_cache_seq (
  id INT IDENTITY(1,1) PRIMARY KEY
);

CREATE TABLE ${ohdsiSchema}.achilles_cache
(
    id         BIGINT NOT NULL,
    source_id  INT NOT NULL,
    cache_name VARCHAR NOT NULL,
    cache      TEXT,
    CONSTRAINT achilles_cache_pk PRIMARY KEY (id),
    CONSTRAINT achilles_cache_fk FOREIGN KEY (source_id) REFERENCES ${ohdsiSchema}.source (source_id) ON DELETE CASCADE
);

INSERT INTO ${ohdsiSchema}.achilles_cache (id, source_id, cache_name, cache)
SELECT (SELECT MAX(id) + 1 FROM ${ohdsiSchema}.achilles_cache_seq), source_id, cache_name, cache
FROM ${ohdsiSchema}.achilles_cache;


CREATE UNIQUE NONCLUSTERED INDEX achilles_cache_source_id_idx ON ${ohdsiSchema}.achilles_cache (source_id, cache_name);