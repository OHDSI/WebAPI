-- Cohort Definition

ALTER TABLE ${ohdsiSchema}.cohort_definition ADD COLUMN created_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN modified_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.cohort_definition d SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = d.created_by AND d.created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.cohort_definition d SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = d.modified_by AND d.modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.cohort_definition DROP COLUMN created_by, DROP COLUMN modified_by;

-- Feasibility Study

ALTER TABLE ${ohdsiSchema}.feasibility_study ADD COLUMN created_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN modified_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.feasibility_study f SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = f.created_by AND f.created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.feasibility_study f SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = f.modified_by AND f.modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.feasibility_study DROP COLUMN created_by, DROP COLUMN modified_by;

-- Incidence Rate Analysis

ALTER TABLE ${ohdsiSchema}.ir_analysis ADD COLUMN created_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN modified_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.ir_analysis i SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = i.created_by AND i.created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.ir_analysis i SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = i.modified_by AND i.modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.ir_analysis DROP COLUMN created_by, DROP COLUMN modified_by;

-- CCA

ALTER TABLE ${ohdsiSchema}.cca ADD COLUMN created_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN modified_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.cca c SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = c.created_by AND c.created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.cca c SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = c.modified_by AND c.modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN created_by, DROP COLUMN modified_by;

-- ConceptSet

ALTER TABLE ${ohdsiSchema}.concept_set ADD COLUMN created_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN modified_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.concept_set c SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = c.created_by AND c.created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.concept_set c SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = c.modified_by AND c.modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.concept_set DROP COLUMN created_by, DROP COLUMN modified_by;

-- Patient Level Prediction

ALTER TABLE ${ohdsiSchema}.plp ADD COLUMN created_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN modified_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.plp p SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = p.created_by AND p.created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.plp p SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = p.modified_by AND p.modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.plp DROP COLUMN created_by, DROP COLUMN modified_by;
