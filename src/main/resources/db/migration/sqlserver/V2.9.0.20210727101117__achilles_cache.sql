CREATE SEQUENCE ${ohdsiSchema}.achilles_cache_seq START WITH 1;

CREATE TABLE ${ohdsiSchema}.achilles_cache
(
    id         bigint  NOT NULL DEFAULT NEXT VALUE FOR ${ohdsiSchema}.achilles_cache_seq,
    source_id  INT    NOT NULL,
    cache_name varchar NOT NULL,
    cache      text,
    CONSTRAINT achilles_cache_pk PRIMARY KEY (id),
    CONSTRAINT achilles_cache_fk FOREIGN KEY (source_id) REFERENCES ${ohdsiSchema}.source (source_id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX achilles_cache_source_id_idx ON ${ohdsiSchema}.achilles_cache (source_id, cache_name);