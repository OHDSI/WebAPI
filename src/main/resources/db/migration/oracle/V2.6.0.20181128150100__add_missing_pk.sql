CREATE TABLE ${ohdsiSchema}.cc_cohort_tmp
AS
SELECT DISTINCT cohort_characterization_id, cohort_id
FROM ${ohdsiSchema}.cc_cohort;

DELETE FROM ${ohdsiSchema}.cc_cohort;

INSERT INTO ${ohdsiSchema}.cc_cohort (cohort_characterization_id, cohort_id)
SELECT cohort_characterization_id, cohort_id 
FROM ${ohdsiSchema}.cc_cohort_tmp;

TRUNCATE TABLE ${ohdsiSchema}.cc_cohort_tmp;
DROP TABLE ${ohdsiSchema}.cc_cohort_tmp;

ALTER TABLE ${ohdsiSchema}.ANALYSIS_GENERATION_INFO ADD CONSTRAINT pk_an_gen_info PRIMARY KEY (JOB_EXECUTION_ID);
ALTER TABLE ${ohdsiSchema}.cc_analysis ADD CONSTRAINT pk_cc_analysis PRIMARY KEY (cohort_characterization_id, fe_analysis_id);
ALTER TABLE ${ohdsiSchema}.cc_cohort ADD CONSTRAINT pk_cc_cohort PRIMARY KEY (cohort_characterization_id, cohort_id);
ALTER TABLE ${ohdsiSchema}.cca ADD CONSTRAINT pk_cca PRIMARY KEY (cca_id);
ALTER TABLE ${ohdsiSchema}.cca_execution_ext ADD CONSTRAINT pk_cca_exec_ext PRIMARY KEY (cca_execution_id);
ALTER TABLE ${ohdsiSchema}.cohort ADD CONSTRAINT pk_cohort PRIMARY KEY (cohort_definition_id, subject_id);
ALTER TABLE ${ohdsiSchema}.cohort_analysis_list_xref ADD CONSTRAINT pk_cohort_an_list_xref PRIMARY KEY (source_id, cohort_id, analysis_id);
ALTER TABLE ${ohdsiSchema}.cohort_concept_map ADD CONSTRAINT pk_cohort_concept_map PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_inclusion ADD CONSTRAINT pk_cohort_incl PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_inclusion_result ADD CONSTRAINT pk_cohort_incl_res PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_inclusion_stats ADD CONSTRAINT pk_cohort_incl_stat PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_summary_stats ADD CONSTRAINT pk_cohort_summary_stat PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.CONCEPT_SET_ITEM ADD CONSTRAINT pk_concept_set_item PRIMARY KEY (CONCEPT_SET_ITEM_ID);
ALTER TABLE ${ohdsiSchema}.feas_study_inclusion_stats ADD CONSTRAINT pk_feas_st_incl_stats PRIMARY KEY (study_id);
ALTER TABLE ${ohdsiSchema}.feas_study_index_stats ADD CONSTRAINT pk_feas_st_indx_stats PRIMARY KEY (study_id);
ALTER TABLE ${ohdsiSchema}.feas_study_result ADD CONSTRAINT pk_feas_study_result PRIMARY KEY (study_id);
ALTER TABLE ${ohdsiSchema}.feasibility_inclusion ADD CONSTRAINT pk_feas_inclusion PRIMARY KEY (study_id, sequence);
ALTER TABLE ${ohdsiSchema}.heracles_analysis ADD CONSTRAINT pk_heracles_analysis PRIMARY KEY (analysis_id);
ALTER TABLE ${ohdsiSchema}.PENELOPE_LAERTES_UNIVERSE ADD CONSTRAINT pk_penelope_lae_uni PRIMARY KEY (ID);
ALTER TABLE ${ohdsiSchema}.plp ADD CONSTRAINT pk_plp PRIMARY KEY (PLP_ID);


ALTER TABLE ${ohdsiSchema}.cohort_features ADD (id NUMBER(19));
CREATE SEQUENCE cohort_features_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.cohort_features_bir
  BEFORE INSERT ON ${ohdsiSchema}.cohort_features
  FOR EACH ROW
  BEGIN
    SELECT cohort_features_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.cohort_features SET id = cohort_features_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.cohort_features ADD CONSTRAINT pk_cohort_features PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.cohort_features_analysis_ref ADD (id NUMBER(19));
CREATE SEQUENCE cohort_feat_anlys_ref_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.cohort_feat_anlys_ref_bir
  BEFORE INSERT ON ${ohdsiSchema}.cohort_features_analysis_ref
  FOR EACH ROW
  BEGIN
    SELECT cohort_feat_anlys_ref_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.cohort_features_analysis_ref SET id = cohort_feat_anlys_ref_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.cohort_features_analysis_ref ADD CONSTRAINT pk_coh_features_an_ref PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.cohort_features_dist ADD (id NUMBER(19));
CREATE SEQUENCE cohort_features_dist_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.cohort_features_dist_bir
  BEFORE INSERT ON ${ohdsiSchema}.cohort_features_dist
  FOR EACH ROW
  BEGIN
    SELECT cohort_features_dist_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.cohort_features_dist SET id = cohort_features_dist_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.cohort_features_dist ADD CONSTRAINT pk_coh_features_dist PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.cohort_features_ref ADD (id NUMBER(19));
CREATE SEQUENCE cohort_features_ref_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.cohort_features_ref_bir
  BEFORE INSERT ON ${ohdsiSchema}.cohort_features_ref
  FOR EACH ROW
  BEGIN
    SELECT cohort_features_ref_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.cohort_features_ref SET id = cohort_features_ref_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.cohort_features_ref ADD CONSTRAINT pk_coh_features_ref PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.heracles_heel_results ADD (id NUMBER(19));
CREATE SEQUENCE heracles_heel_results_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.heracles_heel_results_bir
  BEFORE INSERT ON ${ohdsiSchema}.heracles_heel_results
  FOR EACH ROW
  BEGIN
    SELECT heracles_heel_results_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.heracles_heel_results SET id = heracles_heel_results_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.heracles_heel_results ADD CONSTRAINT pk_heracles_heel_res PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.heracles_results ADD (id NUMBER(19));
CREATE SEQUENCE heracles_results_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.heracles_results_bir
  BEFORE INSERT ON ${ohdsiSchema}.heracles_results
  FOR EACH ROW
  BEGIN
    SELECT heracles_results_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.heracles_results SET id = heracles_results_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.heracles_results ADD CONSTRAINT pk_heracles_res PRIMARY KEY (id);


ALTER TABLE ${ohdsiSchema}.heracles_results_dist ADD (id NUMBER(19));
CREATE SEQUENCE heracles_results_dist_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.heracles_results_dist_bir
  BEFORE INSERT ON ${ohdsiSchema}.heracles_results_dist
  FOR EACH ROW
  BEGIN
    SELECT heracles_results_dist_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.heracles_results_dist SET id = heracles_results_dist_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.heracles_results_dist ADD CONSTRAINT pk_heracles_res_dist PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.ir_analysis_dist ADD (id NUMBER(19));
CREATE SEQUENCE ir_analysis_dist_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.ir_analysis_dist_bir
  BEFORE INSERT ON ${ohdsiSchema}.ir_analysis_dist
  FOR EACH ROW
  BEGIN
    SELECT ir_analysis_dist_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.ir_analysis_dist SET id = ir_analysis_dist_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.ir_analysis_dist ADD CONSTRAINT pk_ir_analysis_dist PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.ir_analysis_result ADD (id NUMBER(19));
CREATE SEQUENCE ir_analysis_result_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.ir_analysis_result_bir
  BEFORE INSERT ON ${ohdsiSchema}.ir_analysis_result
  FOR EACH ROW
  BEGIN
    SELECT ir_analysis_result_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.ir_analysis_result SET id = ir_analysis_result_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.ir_analysis_result ADD CONSTRAINT pk_ir_analysis_res PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.ir_analysis_strata_stats ADD (id NUMBER(19));
CREATE SEQUENCE ir_anls_strat_stat_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.ir_analysis_strata_stats_bir
  BEFORE INSERT ON ${ohdsiSchema}.ir_analysis_strata_stats
  FOR EACH ROW
  BEGIN
    SELECT ir_anls_strat_stat_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.ir_analysis_strata_stats SET id = ir_anls_strat_stat_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.ir_analysis_strata_stats ADD CONSTRAINT pk_ir_an_strata_stats PRIMARY KEY (id);


ALTER TABLE ${ohdsiSchema}.ir_strata ADD (id NUMBER(19));
CREATE SEQUENCE ir_strata_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.ir_strata_bir
  BEFORE INSERT ON ${ohdsiSchema}.ir_strata
  FOR EACH ROW
  BEGIN
    SELECT ir_strata_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.ir_strata SET id = ir_strata_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.ir_strata ADD CONSTRAINT pk_ir_strata PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.penelope_laertes_uni_pivot ADD (id NUMBER(19));
CREATE SEQUENCE penelope_lae_uni_p_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.penelope_lae_uni_p_bir
  BEFORE INSERT ON ${ohdsiSchema}.penelope_laertes_uni_pivot
  FOR EACH ROW
  BEGIN
    SELECT penelope_lae_uni_p_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.penelope_laertes_uni_pivot SET id = penelope_lae_uni_p_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.penelope_laertes_uni_pivot ADD CONSTRAINT pk_penelope_lae_uni_piv PRIMARY KEY (id);