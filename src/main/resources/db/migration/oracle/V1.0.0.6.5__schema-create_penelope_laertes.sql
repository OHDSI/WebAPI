CREATE TABLE ${ohdsiSchema}.penelope_laertes_universe 
(
  id NUMBER(19, 0),
  condition_concept_id NUMBER(19, 0),
  condition_concept_name VARCHAR2(255),
  ingredient_concept_id NUMBER(19, 0),
  ingredient_concept_name VARCHAR2(255),
  evidence_type VARCHAR2(255),
  supports CHAR(1),
  statistic_value FLOAT,
  evidence_linkouts VARCHAR2(4000)
);

CREATE TABLE ${ohdsiSchema}.penelope_laertes_uni_pivot(
  condition_concept_id NUMBER(19,0),
  condition_concept_name VARCHAR2(255),
  ingredient_concept_id NUMBER(19,0),
  ingredient_concept_name VARCHAR2(255),
  medline_ct NUMBER(19,0),
  medline_case NUMBER(19,0),
  medline_other NUMBER(19,0),
  semmeddb_ct_t NUMBER(19,0),
  semmeddb_case_t NUMBER(19,0),
  semmeddb_other_t NUMBER(19,0),
  semmeddb_ct_f NUMBER(19,0),
  semmeddb_case_f NUMBER(19,0),
  semmeddb_other_f NUMBER(19,0),
  eu_spc NUMBER(19,0),
  spl_adr NUMBER(19,0),
  aers NUMBER(19,0),
  aers_prr FLOAT,
  aers_prr_original FLOAT
);