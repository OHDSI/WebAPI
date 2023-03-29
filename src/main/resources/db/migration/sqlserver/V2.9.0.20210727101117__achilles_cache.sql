CREATE SEQUENCE webapi.achilles_cache_seq START WITH 1;

CREATE TABLE webapi.achilles_cache
(
    id         bigint  NOT NULL DEFAULT NEXT VALUE FOR webapi.achilles_cache_seq,
    source_id  INT    NOT NULL,
    cache_name varchar NOT NULL,
    cache      text,
    CONSTRAINT achilles_cache_pk PRIMARY KEY (id),
    CONSTRAINT achilles_cache_fk FOREIGN KEY (source_id) REFERENCES webapi."source" (source_id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX achilles_cache_source_id_idx ON webapi.achilles_cache (source_id, cache_name);