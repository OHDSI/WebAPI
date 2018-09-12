-- Cohort Definition

ALTER TABLE ${ohdsiSchema}.cohort_definition ADD created_by_id NUMBER(10) NULL CONSTRAINT FK_cd_su_cid REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.cohort_definition ADD modified_by_id NUMBER(10) NULL CONSTRAINT FK_cd_su_mid REFERENCES ${ohdsiSchema}.sec_user(id);


UPDATE ${ohdsiSchema}.cohort_definition d SET created_by_id =
  (SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = d.created_by)
  WHERE d.created_by IS NOT NULL AND EXISTS(SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = d.created_by);

UPDATE ${ohdsiSchema}.cohort_definition d SET modified_by_id =
  (SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = d.modified_by)
  WHERE d.modified_by IS NOT NULL AND EXISTS(SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = d.modified_by);

ALTER TABLE ${ohdsiSchema}.cohort_definition DROP(created_by, modified_by);

-- Feasibility Study

ALTER TABLE ${ohdsiSchema}.feasibility_study ADD created_by_id NUMBER(10) NULL CONSTRAINT FK_fs_su_cid REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.feasibility_study ADD modified_by_id NUMBER(10) NULL CONSTRAINT FK_fs_su_mid REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.feasibility_study f SET created_by_id =
  (SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = f.created_by)
  WHERE f.created_by IS NOT NULL AND EXISTS(SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = f.created_by);

UPDATE ${ohdsiSchema}.feasibility_study f SET modified_by_id =
  (SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = f.modified_by)
  WHERE f.modified_by IS NOT NULL AND EXISTS(SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = f.modified_by);

ALTER TABLE ${ohdsiSchema}.feasibility_study DROP(created_by, modified_by);

-- Incidence Rate Analysis

ALTER TABLE ${ohdsiSchema}.ir_analysis ADD created_by_id NUMBER(10) NULL CONSTRAINT FK_ira_su_cid REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.ir_analysis ADD modified_by_id NUMBER(10) NULL CONSTRAINT FK_ira_su_mid REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.ir_analysis i SET created_by_id =
  (SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = i.created_by)
  WHERE i.created_by IS NOT NULL AND EXISTS(SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = i.created_by);

UPDATE ${ohdsiSchema}.ir_analysis i SET modified_by_id =
  (SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = i.modified_by)
  WHERE i.modified_by IS NOT NULL AND EXISTS(SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = i.modified_by);

ALTER TABLE ${ohdsiSchema}.ir_analysis DROP(created_by, modified_by);

-- CCA

ALTER TABLE ${ohdsiSchema}.cca ADD created_by_id NUMBER(10) NULL CONSTRAINT FK_cca_su_cid REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.cca ADD modified_by_id NUMBER(10) NULL CONSTRAINT FK_cca_su_mid REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.cca c SET created_by_id =
  (SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = c.created_by)
  WHERE c.created_by IS NOT NULL AND EXISTS(SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = c.created_by);

UPDATE ${ohdsiSchema}.cca c SET modified_by_id =
  (SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = c.modified_by)
  WHERE c.modified_by IS NOT NULL AND EXISTS(SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = c.modified_by);

ALTER TABLE ${ohdsiSchema}.cca DROP(created_by, modified_by);

-- ConceptSet

ALTER TABLE ${ohdsiSchema}.concept_set ADD created_by_id NUMBER(10) NULL CONSTRAINT FK_cs_su_cid REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.concept_set ADD modified_by_id NUMBER(10) NULL CONSTRAINT FK_cs_su_mid REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.concept_set c SET created_by_id =
  (SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = c.created_by)
  WHERE c.created_by IS NOT NULL AND EXISTS(SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = c.created_by);

UPDATE ${ohdsiSchema}.concept_set c SET modified_by_id =
  (SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = c.modified_by)
  WHERE c.modified_by IS NOT NULL AND EXISTS(SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = c.modified_by);

ALTER TABLE ${ohdsiSchema}.concept_set DROP(created_by, modified_by);

-- Patient Level Prediction

ALTER TABLE ${ohdsiSchema}.plp ADD created_by_id NUMBER(10) NULL CONSTRAINT FK_plp_su_cid REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.plp ADD modified_by_id NUMBER(10) NULL CONSTRAINT FK_plp_su_mid REFERENCES ${ohdsiSchema}.sec_user(id);

UPDATE ${ohdsiSchema}.plp p SET created_by_id =
  (SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = p.created_by)
  WHERE p.created_by IS NOT NULL AND EXISTS(SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = p.created_by);

UPDATE ${ohdsiSchema}.plp p SET modified_by_id =
  (SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = p.modified_by)
  WHERE p.modified_by IS NOT NULL AND EXISTS(SELECT u.id FROM ${ohdsiSchema}.sec_user u WHERE u.login = p.modified_by);

ALTER TABLE ${ohdsiSchema}.plp DROP(created_by, modified_by);
