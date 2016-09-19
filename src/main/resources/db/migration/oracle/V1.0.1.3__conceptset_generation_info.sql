-- DROP any existing objects from previous runs
-- for Oracle only, this section is commented out for reference in case they need to be run manually
-- the other DBMS create scripts can dynamically drop objects instead
-- 
-- ALTER TABLE concept_set drop CONSTRAINT PK_concept_set;
-- DROP TABLE concept_set_generation_info;


ALTER TABLE ${ohdsiSchema}.concept_set ADD CONSTRAINT PK_concept_set PRIMARY KEY (concept_set_id);

CREATE TABLE ${ohdsiSchema}.concept_set_generation_info(
  concept_set_id INTEGER NOT NULL,
  source_id INTEGER NOT NULL,
  generation_type INTEGER NOT NULL,
  start_time TIMESTAMP NOT NULL,
  execution_duration INTEGER NULL,
  status INTEGER NOT NULL,
  is_valid INTEGER NOT NULL,
  CONSTRAINT PK_cs_generation_info PRIMARY KEY (concept_set_id, source_id),
  CONSTRAINT FK_generation_info_concept_set FOREIGN KEY(concept_set_id)
    REFERENCES ${ohdsiSchema}.concept_set (concept_set_id)
    ON DELETE CASCADE
);