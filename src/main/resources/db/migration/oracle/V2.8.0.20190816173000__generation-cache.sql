CREATE SEQUENCE ${ohdsiSchema}.generation_cache_sequence;

CREATE TABLE ${ohdsiSchema}.generation_cache (
  id INTEGER,
  type VARCHAR NOT NULL,
  design_hash VARCHAR NOT NULL,
  source_id INTEGER NOT NULL,
  result_identifier INTEGER NOT NULL,
  result_checksum VARCHAR NOT NULL,
  created_date DATE NOT NULL DEFAULT NOW(),
  CONSTRAINT PK_generation_cache PRIMARY KEY (id),
  CONSTRAINT FK_gc_source_id_source
    FOREIGN KEY (source_id)
    REFERENCES ${ohdsiSchema}.source (source_id)
);