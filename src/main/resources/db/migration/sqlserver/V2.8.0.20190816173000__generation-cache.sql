CREATE SEQUENCE ${ohdsiSchema}.generation_cache_sequence;

CREATE TABLE ${ohdsiSchema}.generation_cache (
  id INTEGER DEFAULT NEXT VALUE FOR ${ohdsiSchema}.generation_cache_sequence,
  type VARCHAR NOT NULL,
  design_hash VARCHAR NOT NULL,
  source_id INTEGER NOT NULL,
  result_identifier INTEGER NOT NULL,
  result_checksum VARCHAR NOT NULL,
  created_date DATE NOT NULL,
  CONSTRAINT PK_generation_cache PRIMARY KEY (id)
);

ALTER TABLE ${ohdsiSchema}.generation_cache
  ADD CONSTRAINT FK_gc_source_id_source FOREIGN KEY (source_id)
REFERENCES ${ohdsiSchema}.source (source_id)
ON UPDATE NO ACTION ON DELETE NO ACTION;