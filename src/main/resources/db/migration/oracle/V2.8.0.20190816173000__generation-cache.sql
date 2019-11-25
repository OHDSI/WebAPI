CREATE SEQUENCE ${ohdsiSchema}.generation_cache_sequence;

CREATE TABLE ${ohdsiSchema}.generation_cache (
  id INTEGER,
  type VARCHAR(255) NOT NULL,
  design_hash VARCHAR(255) NOT NULL,
  source_id INTEGER NOT NULL,
  result_identifier INTEGER NOT NULL,
  result_checksum VARCHAR(255), -- can be null in case of empty result set
  created_date DATE NOT NULL,
  CONSTRAINT PK_generation_cache PRIMARY KEY (id),
  CONSTRAINT FK_gc_source_id_source
    FOREIGN KEY (source_id)
    REFERENCES ${ohdsiSchema}.source (source_id)
);

ALTER TABLE ${ohdsiSchema}.generation_cache ADD CONSTRAINT uq_gc_hash UNIQUE (type, design_hash, source_id);
ALTER TABLE ${ohdsiSchema}.generation_cache ADD CONSTRAINT uq_gc_result UNIQUE (type, source_id, result_identifier);
