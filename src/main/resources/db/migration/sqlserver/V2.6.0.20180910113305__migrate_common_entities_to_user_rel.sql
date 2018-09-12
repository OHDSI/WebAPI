-- Cohort Definition

ALTER TABLE ${ohdsiSchema}.cohort_definition ADD created_by_id INT NULL, CONSTRAINT FK_cd_su_cid FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.cohort_definition ADD modified_by_id INT NULL, CONSTRAINT FK_cd_su_mid FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

GO

UPDATE ${ohdsiSchema}.cohort_definition SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = created_by AND created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.cohort_definition SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = modified_by AND modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.cohort_definition DROP COLUMN created_by, modified_by;

GO

-- Feasibility Study

ALTER TABLE ${ohdsiSchema}.feasibility_study ADD created_by_id INT NULL, CONSTRAINT FK_fs_su_cid FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.feasibility_study ADD modified_by_id INT NULL, CONSTRAINT FK_fs_su_mid FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

GO

UPDATE ${ohdsiSchema}.feasibility_study SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = created_by AND created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.feasibility_study SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = modified_by AND modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.feasibility_study DROP COLUMN created_by, modified_by;

GO

-- Incidence Rate Analysis

ALTER TABLE ${ohdsiSchema}.ir_analysis ADD created_by_id INT NULL, CONSTRAINT FK_ira_su_cid FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.ir_analysis ADD modified_by_id INT NULL, CONSTRAINT FK_ira_su_mid FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

GO

UPDATE ${ohdsiSchema}.ir_analysis SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = created_by AND created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.ir_analysis SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = modified_by AND modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.ir_analysis DROP COLUMN created_by, modified_by;

GO

-- CCA

ALTER TABLE ${ohdsiSchema}.cca ADD created_by_id INT NULL, CONSTRAINT FK_cca_su_cid FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.cca ADD modified_by_id INT NULL, CONSTRAINT FK_cca_su_mid FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

GO

UPDATE ${ohdsiSchema}.cca SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = created_by AND created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.cca SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = modified_by AND modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN created_by, modified_by;

GO

-- ConceptSet

ALTER TABLE ${ohdsiSchema}.concept_set ADD created_by_id INT NULL, CONSTRAINT FK_cs_su_cid FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.concept_set ADD modified_by_id INT NULL, CONSTRAINT FK_cs_su_mid FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

GO

UPDATE ${ohdsiSchema}.concept_set SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = created_by AND created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.concept_set SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = modified_by AND modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.concept_set DROP COLUMN created_by, modified_by;

GO

-- Patient Level Prediction

ALTER TABLE ${ohdsiSchema}.plp ADD created_by_id INT NULL, CONSTRAINT FK_plp_su_cid FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.plp ADD modified_by_id INT NULL, CONSTRAINT FK_plp_su_mid FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

GO

UPDATE ${ohdsiSchema}.plp SET created_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = created_by AND created_by IS NOT NULL;

UPDATE ${ohdsiSchema}.plp SET modified_by_id = u.id
  FROM ${ohdsiSchema}.sec_user u WHERE u.login = modified_by AND modified_by IS NOT NULL;

ALTER TABLE ${ohdsiSchema}.plp DROP COLUMN created_by, modified_by;

GO
