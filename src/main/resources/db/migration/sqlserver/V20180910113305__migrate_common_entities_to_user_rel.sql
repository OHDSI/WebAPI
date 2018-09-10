-- Cohort Definition

ALTER TABLE ${ohdsiSchema}.cohort_definition ADD created_by_id INT REFERENCES ${ohdsiSchema}.sec_user(id),
  modified_by_id INT REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.cohort_definition SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = created_by AND created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.cohort_definition SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = modified_by AND modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.cohort_definition DROP COLUMN created_by, modified_by;

-- Feasibility Study

ALTER TABLE ${ohdsiSchema}.feasibility_study ADD created_by_id INT REFERENCES ${ohdsiSchema}.sec_user(id),
  modified_by_id INT REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.feasibility_study SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = created_by AND created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.feasibility_study SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = modified_by AND modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.feasibility_study DROP COLUMN created_by, modified_by;

-- Incidence Rate Analysis

ALTER TABLE ${ohdsiSchema}.ir_analysis ADD created_by_id INT REFERENCES ${ohdsiSchema}.sec_user(id),
  modified_by_id INT REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.ir_analysis SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = created_by AND created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.ir_analysis SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = modified_by AND modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.ir_analysis DROP COLUMN created_by, modified_by;

-- CCA

ALTER TABLE ${ohdsiSchema}.cca ADD created_by_id INT REFERENCES ${ohdsiSchema}.sec_user(id),
  modified_by_id INT REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.cca SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = created_by AND created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.cca SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = modified_by AND modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN created_by, modified_by;

-- ConceptSet

ALTER TABLE ${ohdsiSchema}.concept_set ADD created_by_id INT REFERENCES ${ohdsiSchema}.sec_user(id),
  modified_by_id INT REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.concept_set SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = created_by AND created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.concept_set SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = modified_by AND modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.concept_set DROP COLUMN created_by, modified_by;

-- Patient Level Prediction

ALTER TABLE ${ohdsiSchema}.plp ADD created_by_id INT REFERENCES ${ohdsiSchema}.sec_user(id),
  modified_by_id INT REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.plp SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = created_by AND created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.plp SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = modified_by AND modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.plp DROP COLUMN created_by, modified_by;
