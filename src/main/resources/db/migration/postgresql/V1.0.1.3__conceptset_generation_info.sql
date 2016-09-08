ALTER TABLE ${ohdsiSchema}.concept_set ADD CONSTRAINT PK_concept_set PRIMARY KEY (concept_set_id);

CREATE TABLE ${ohdsiSchema}.concept_set_generation_info(
  concept_set_id INTEGER NOT NULL,
  source_id INTEGER NOT NULL,
  generation_type INTEGER NOT NULL,
  start_time TIMESTAMP NOT NULL,
  execution_duration INTEGER NULL,
  status INTEGER NOT NULL,
  is_valid BOOLEAN NOT NULL,
  CONSTRAINT PK_concept_set_generation_info PRIMARY KEY (concept_set_id, source_id),
  CONSTRAINT FK_concept_set_generation_info_concept_set FOREIGN KEY(concept_set_id)
    REFERENCES ${ohdsiSchema}.concept_set (concept_set_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);