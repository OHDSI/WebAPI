ALTER TABLE ${ohdsiSchema}.ANALYSIS_GENERATION_INFO ADD PRIMARY KEY (JOB_EXECUTION_ID);
ALTER TABLE ${ohdsiSchema}.cc_analysis ADD PRIMARY KEY (cohort_characterization_id, fe_analysis_id);
ALTER TABLE ${ohdsiSchema}.cc_cohort ADD PRIMARY KEY (cohort_characterization_id, cohort_id);
ALTER TABLE ${ohdsiSchema}.cca ADD PRIMARY KEY (cca_id);
ALTER TABLE ${ohdsiSchema}.cca_execution_ext ADD PRIMARY KEY (cca_execution_id);
ALTER TABLE ${ohdsiSchema}.cohort ADD PRIMARY KEY (cohort_definition_id, subject_id);
ALTER TABLE ${ohdsiSchema}.cohort_analysis_list_xref ADD PRIMARY KEY (source_id, cohort_id, analysis_id);
ALTER TABLE ${ohdsiSchema}.cohort_concept_map ADD PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_inclusion ADD PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_inclusion_result ADD PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_inclusion_stats ADD PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.cohort_summary_stats ADD PRIMARY KEY (cohort_definition_id);
ALTER TABLE ${ohdsiSchema}.CONCEPT_SET_ITEM ADD PRIMARY KEY (CONCEPT_SET_ITEM_ID);
ALTER TABLE ${ohdsiSchema}.feas_study_inclusion_stats ADD PRIMARY KEY (study_id);
ALTER TABLE ${ohdsiSchema}.feas_study_index_stats ADD PRIMARY KEY (study_id);
ALTER TABLE ${ohdsiSchema}.feas_study_result ADD PRIMARY KEY (study_id);
ALTER TABLE ${ohdsiSchema}.feasibility_inclusion ADD PRIMARY KEY (name);
ALTER TABLE ${ohdsiSchema}.heracles_analysis ADD PRIMARY KEY (analysis_id);
ALTER TABLE ${ohdsiSchema}.PENELOPE_LAERTES_UNIVERSE ADD PRIMARY KEY (ID);
ALTER TABLE ${ohdsiSchema}.plp ADD PRIMARY KEY (PLP_ID);


ALTER TABLE ${ohdsiSchema}.cohort_features ADD (id NUMBER);
CREATE SEQUENCE cohort_features_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.cohort_features_bir
  BEFORE INSERT ON ${ohdsiSchema}.cohort_features
  FOR EACH ROW
  BEGIN
    SELECT cohort_features_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.cohort_features SET id = cohort_features_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.cohort_features ADD PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.cohort_features_analysis_ref ADD (id NUMBER);
CREATE SEQUENCE cohort_feat_anlys_ref_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.cohort_feat_anlys_ref_bir
  BEFORE INSERT ON ${ohdsiSchema}.cohort_features_analysis_ref
  FOR EACH ROW
  BEGIN
    SELECT cohort_feat_anlys_ref_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.cohort_features_analysis_ref SET id = cohort_feat_anlys_ref_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.cohort_features_analysis_ref ADD PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.cohort_features_dist ADD (id NUMBER);
CREATE SEQUENCE cohort_features_dist_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.cohort_features_dist_bir
  BEFORE INSERT ON ${ohdsiSchema}.cohort_features_dist
  FOR EACH ROW
  BEGIN
    SELECT cohort_features_dist_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.cohort_features_dist SET id = cohort_features_dist_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.cohort_features_dist ADD PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.cohort_features_ref ADD (id NUMBER);
CREATE SEQUENCE cohort_features_ref_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.cohort_features_ref_bir
  BEFORE INSERT ON ${ohdsiSchema}.cohort_features_ref
  FOR EACH ROW
  BEGIN
    SELECT cohort_features_ref_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.cohort_features_ref SET id = cohort_features_ref_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.cohort_features_ref ADD PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.heracles_heel_results ADD (id NUMBER);
CREATE SEQUENCE heracles_heel_results_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.heracles_heel_results_bir
  BEFORE INSERT ON ${ohdsiSchema}.heracles_heel_results
  FOR EACH ROW
  BEGIN
    SELECT heracles_heel_results_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.heracles_heel_results SET id = heracles_heel_results_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.heracles_heel_results ADD PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.heracles_results ADD (id NUMBER);
CREATE SEQUENCE heracles_results_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.heracles_results_bir
  BEFORE INSERT ON ${ohdsiSchema}.heracles_results
  FOR EACH ROW
  BEGIN
    SELECT heracles_results_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.heracles_results SET id = heracles_results_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.heracles_results ADD PRIMARY KEY (id);


ALTER TABLE ${ohdsiSchema}.heracles_results_dist ADD (id NUMBER);
CREATE SEQUENCE heracles_results_dist_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.heracles_results_dist_bir
  BEFORE INSERT ON ${ohdsiSchema}.heracles_results_dist
  FOR EACH ROW
  BEGIN
    SELECT heracles_results_dist_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.heracles_results_dist SET id = heracles_results_dist_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.heracles_results_dist ADD PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.ir_analysis_dist ADD (id NUMBER);
CREATE SEQUENCE ir_analysis_dist_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.ir_analysis_dist_bir
  BEFORE INSERT ON ${ohdsiSchema}.ir_analysis_dist
  FOR EACH ROW
  BEGIN
    SELECT ir_analysis_dist_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.ir_analysis_dist SET id = ir_analysis_dist_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.ir_analysis_dist ADD PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.ir_analysis_result ADD (id NUMBER);
CREATE SEQUENCE ir_analysis_result_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.ir_analysis_result_bir
  BEFORE INSERT ON ${ohdsiSchema}.ir_analysis_result
  FOR EACH ROW
  BEGIN
    SELECT ir_analysis_result_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.ir_analysis_result SET id = ir_analysis_result_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.ir_analysis_result ADD PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.ir_analysis_strata_stats ADD (id NUMBER);
CREATE SEQUENCE ir_anls_strat_stat_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.ir_analysis_strata_stats_bir
  BEFORE INSERT ON ${ohdsiSchema}.ir_analysis_strata_stats
  FOR EACH ROW
  BEGIN
    SELECT ir_anls_strat_stat_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.ir_analysis_strata_stats SET id = ir_anls_strat_stat_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.ir_analysis_strata_stats ADD PRIMARY KEY (id);


ALTER TABLE ${ohdsiSchema}.ir_strata ADD (id NUMBER);
CREATE SEQUENCE ir_strata_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.ir_strata_bir
  BEFORE INSERT ON ${ohdsiSchema}.ir_strata
  FOR EACH ROW
  BEGIN
    SELECT ir_strata_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.ir_strata SET id = ir_strata_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.ir_strata ADD PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.penelope_laertes_uni_pivot ADD (id NUMBER);
CREATE SEQUENCE penelope_lae_uni_p_pk_seq START WITH 1 NOCYCLE;
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.penelope_lae_uni_p_bir
  BEFORE INSERT ON ${ohdsiSchema}.penelope_laertes_uni_pivot
  FOR EACH ROW
  BEGIN
    SELECT penelope_lae_uni_p_pk_seq.nextval INTO :new.id FROM dual;
  END;
/
UPDATE ${ohdsiSchema}.penelope_laertes_uni_pivot SET id = penelope_lae_uni_p_pk_seq.nextval;
ALTER TABLE ${ohdsiSchema}.penelope_laertes_uni_pivot ADD PRIMARY KEY (id);