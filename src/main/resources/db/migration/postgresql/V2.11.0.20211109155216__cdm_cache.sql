CREATE SEQUENCE ${ohdsiSchema}.cdm_cache_seq;

CREATE TABLE ${ohdsiSchema}.cdm_cache
(
    id                      int8 NOT NULL DEFAULT nextval('${ohdsiSchema}.cdm_cache_seq'),
    concept_id              int4 NOT NULL,
    source_id               int4 NOT NULL,
    record_count            int8 NULL,
    descendant_record_count int8 NULL,
    person_count            int8 NULL,
    descendant_person_count int8 NULL,
    CONSTRAINT cdm_cache_pk PRIMARY KEY (id),
    CONSTRAINT cdm_cache_un UNIQUE (concept_id, source_id),
    CONSTRAINT cdm_cache_fk FOREIGN KEY (source_id) REFERENCES ${ohdsiSchema}.source (source_id) ON DELETE CASCADE
);
CREATE INDEX cdm_cache_concept_id_idx ON ${ohdsiSchema}.cdm_cache USING btree (concept_id, source_id);
