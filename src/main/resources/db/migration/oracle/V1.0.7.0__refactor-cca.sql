CREATE SEQUENCE ${ohdsiSchema}.CCA_T_C_SEQUENCE START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775807 NOCYCLE;
CREATE TABLE ${ohdsiSchema}.cca_t_c
(
  id number(10) DEFAULT ON NULL ${ohdsiSchema}.CCA_T_C_SEQUENCE.NEXTVAL NOT NULL ENABLE,
  cca_id number(10) NOT NULL,
  target_id number(10) NOT NULL,
  comparator_id number(10) NOT NULL,
  ps_exclusion_id number(10) NOT NULL,
  ps_inclusion_id number(10) NOT NULL,
  CONSTRAINT PK_cca_t_c PRIMARY KEY (id)
);

CREATE SEQUENCE ${ohdsiSchema}.CCA_O_SEQUENCE START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775807 NOCYCLE;
CREATE TABLE ${ohdsiSchema}.cca_o
(
  id number(10) DEFAULT ON NULL ${ohdsiSchema}.CCA_O_SEQUENCE.NEXTVAL NOT NULL ENABLE,
  cca_id number(10) NOT NULL,
  outcome_id number(10) NOT NULL,
  CONSTRAINT PK_cca_o PRIMARY KEY (id)
);

-- Copy over the data from the existing estimation specifications 
INSERT INTO ${ohdsiSchema}.cca_t_c (cca_id, target_id, comparator_id, ps_exclusion_id, ps_inclusion_id)
SELECT cca_id, treatment_id, comparator_id, ps_exclusion_id, ps_inclusion_id
FROM ${ohdsiSchema}.cca
;

INSERT INTO ${ohdsiSchema}.cca_o (cca_id, outcome_id)
SELECT cca_id, outcome_id
FROM ${ohdsiSchema}.cca
;

-- Augment the CCA table to remove the columns that are now stored in a 1:M format
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN treatment_id;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN comparator_id;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN outcome_id;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_exclusion_id;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_inclusion_id;


-- Transform the base cca table into cca_analysis
CREATE SEQUENCE ${ohdsiSchema}.CCA_ANALYSIS_SEQUENCE START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775807 NOCYCLE;
CREATE TABLE ${ohdsiSchema}.cca_analysis
(
  id number(10) DEFAULT ON NULL ${ohdsiSchema}.CCA_ANALYSIS_SEQUENCE.NEXTVAL NOT NULL ENABLE,
  cca_id number(10) NOT NULL,
  description varchar2(255),
  model_type varchar2(50),
  time_at_risk_start number(10),
  time_at_risk_end number(10),
  add_exposure_days_to_end number(10),
  minimum_washout_period number(10),
  minimum_days_at_risk number(10),
  rm_subjects_in_both_cohorts number(10),
  rm_prior_outcomes number(10),
  ps_adjustment number(10),
  ps_demographics number(10),
  ps_demographics_gender number(10),
  ps_demographics_race number(10),
  ps_demographics_ethnicity number(10),
  ps_demographics_age number(10),
  ps_demographics_year number(10),
  ps_demographics_month number(10),
  ps_trim number(10),
  ps_trim_fraction float,
  ps_match number(10),
  ps_match_max_ratio number(10),
  ps_strat number(10),
  ps_strat_num_strata number(10),
  ps_condition_occ number(10),
  ps_condition_occ_365d number(10),
  ps_condition_occ_30d number(10),
  ps_condition_occ_inpt180d number(10),
  ps_condition_era number(10),
  ps_condition_era_ever number(10),
  ps_condition_era_overlap number(10),
  ps_condition_group number(10),
  ps_condition_group_meddra number(10),
  ps_condition_group_snomed number(10),
  ps_drug_exposure number(10),
  ps_drug_exposure_365d number(10),
  ps_drug_exposure_30d number(10),
  ps_drug_era number(10),
  ps_drug_era_365d number(10),
  ps_drug_era_30d number(10),
  ps_drug_era_overlap number(10),
  ps_drug_era_ever number(10),
  ps_drug_group number(10),
  ps_procedure_occ number(10),
  ps_procedure_occ_365d number(10),
  ps_procedure_occ_30d number(10),
  ps_procedure_group number(10),
  ps_observation number(10),
  ps_observation_365d number(10),
  ps_observation_30d number(10),
  ps_observation_count_365d number(10),
  ps_measurement number(10),
  ps_measurement_365d number(10),
  ps_measurement_30d number(10),
  ps_measurement_count_365d number(10),
  ps_measurement_below number(10),
  ps_measurement_above number(10),
  ps_concept_counts number(10),
  ps_risk_scores number(10),
  ps_risk_scores_charlson number(10),
  ps_risk_scores_dcsi number(10),
  ps_risk_scores_chads2 number(10),
  ps_risk_scores_chads2vasc number(10),
  ps_interaction_year number(10),
  ps_interaction_month number(10),
  om_covariates number(10),
  om_exclusion_id number(10),
  om_inclusion_id number(10),
  om_demographics number(10),
  om_demographics_gender number(10),
  om_demographics_race number(10),
  om_demographics_ethnicity number(10),
  om_demographics_age number(10),
  om_demographics_year number(10),
  om_demographics_month number(10),
  om_trim number(10),
  om_trim_fraction float,
  om_match number(10),
  om_match_max_ratio number(10),
  om_strat number(10),
  om_strat_num_strata number(10),
  om_condition_occ number(10),
  om_condition_occ_365d number(10),
  om_condition_occ_30d number(10),
  om_condition_occ_inpt180d number(10),
  om_condition_era number(10),
  om_condition_era_ever number(10),
  om_condition_era_overlap number(10),
  om_condition_group number(10),
  om_condition_group_meddra number(10),
  om_condition_group_snomed number(10),
  om_drug_exposure number(10),
  om_drug_exposure_365d number(10),
  om_drug_exposure_30d number(10),
  om_drug_era number(10),
  om_drug_era_365d number(10),
  om_drug_era_30d number(10),
  om_drug_era_overlap number(10),
  om_drug_era_ever number(10),
  om_drug_group number(10),
  om_procedure_occ number(10),
  om_procedure_occ_365d number(10),
  om_procedure_occ_30d number(10),
  om_procedure_group number(10),
  om_observation number(10),
  om_observation_365d number(10),
  om_observation_30d number(10),
  om_observation_count_365d number(10),
  om_measurement number(10),
  om_measurement_365d number(10),
  om_measurement_30d number(10),
  om_measurement_count_365d number(10),
  om_measurement_below number(10),
  om_measurement_above number(10),
  om_concept_counts number(10),
  om_risk_scores number(10),
  om_risk_scores_charlson number(10),
  om_risk_scores_dcsi number(10),
  om_risk_scores_chads2 number(10),
  om_risk_scores_chads2vasc number(10),
  om_interaction_year number(10),
  om_interaction_month number(10),
  del_covariates_small_count number(10),
  negative_control_id number(10),
  CONSTRAINT PK_cca_analysis PRIMARY KEY (id)
);

INSERT INTO ${ohdsiSchema}.cca_analysis (cca_id, description, model_type, time_at_risk_start, time_at_risk_end, 
            add_exposure_days_to_end, minimum_washout_period, minimum_days_at_risk, 
            rm_subjects_in_both_cohorts, rm_prior_outcomes, ps_adjustment, 
            ps_demographics, ps_demographics_gender, ps_demographics_race, 
            ps_demographics_ethnicity, ps_demographics_age, ps_demographics_year, 
            ps_demographics_month, ps_trim, ps_trim_fraction, ps_match, ps_match_max_ratio, 
            ps_strat, ps_strat_num_strata, ps_condition_occ, ps_condition_occ_365d, 
            ps_condition_occ_30d, ps_condition_occ_inpt180d, ps_condition_era, 
            ps_condition_era_ever, ps_condition_era_overlap, ps_condition_group, 
            ps_condition_group_meddra, ps_condition_group_snomed, ps_drug_exposure, 
            ps_drug_exposure_365d, ps_drug_exposure_30d, ps_drug_era, ps_drug_era_365d, 
            ps_drug_era_30d, ps_drug_era_overlap, ps_drug_era_ever, ps_drug_group, 
            ps_procedure_occ, ps_procedure_occ_365d, ps_procedure_occ_30d, 
            ps_procedure_group, ps_observation, ps_observation_365d, ps_observation_30d, 
            ps_observation_count_365d, ps_measurement, ps_measurement_365d, 
            ps_measurement_30d, ps_measurement_count_365d, ps_measurement_below, 
            ps_measurement_above, ps_concept_counts, ps_risk_scores, ps_risk_scores_charlson, 
            ps_risk_scores_dcsi, ps_risk_scores_chads2, ps_risk_scores_chads2vasc, 
            ps_interaction_year, ps_interaction_month, om_covariates, om_exclusion_id, 
            om_inclusion_id, om_demographics, om_demographics_gender, om_demographics_race, 
            om_demographics_ethnicity, om_demographics_age, om_demographics_year, 
            om_demographics_month, om_trim, om_trim_fraction, om_match, om_match_max_ratio, 
            om_strat, om_strat_num_strata, om_condition_occ, om_condition_occ_365d, 
            om_condition_occ_30d, om_condition_occ_inpt180d, om_condition_era, 
            om_condition_era_ever, om_condition_era_overlap, om_condition_group, 
            om_condition_group_meddra, om_condition_group_snomed, om_drug_exposure, 
            om_drug_exposure_365d, om_drug_exposure_30d, om_drug_era, om_drug_era_365d, 
            om_drug_era_30d, om_drug_era_overlap, om_drug_era_ever, om_drug_group, 
            om_procedure_occ, om_procedure_occ_365d, om_procedure_occ_30d, 
            om_procedure_group, om_observation, om_observation_365d, om_observation_30d, 
            om_observation_count_365d, om_measurement, om_measurement_365d, 
            om_measurement_30d, om_measurement_count_365d, om_measurement_below, 
            om_measurement_above, om_concept_counts, om_risk_scores, om_risk_scores_charlson, 
            om_risk_scores_dcsi, om_risk_scores_chads2, om_risk_scores_chads2vasc, 
            om_interaction_year, om_interaction_month, del_covariates_small_count, 
            negative_control_id)
SELECT cca_id, 'Analysis', model_type, time_at_risk_start, time_at_risk_end, 
       add_exposure_days_to_end, minimum_washout_period, minimum_days_at_risk, 
       rm_subjects_in_both_cohorts, rm_prior_outcomes, ps_adjustment, 
       ps_demographics, ps_demographics_gender, ps_demographics_race, 
       ps_demographics_ethnicity, ps_demographics_age, ps_demographics_year, 
       ps_demographics_month, ps_trim, ps_trim_fraction, ps_match, ps_match_max_ratio, 
       ps_strat, ps_strat_num_strata, ps_condition_occ, ps_condition_occ_365d, 
       ps_condition_occ_30d, ps_condition_occ_inpt180d, ps_condition_era, 
       ps_condition_era_ever, ps_condition_era_overlap, ps_condition_group, 
       ps_condition_group_meddra, ps_condition_group_snomed, ps_drug_exposure, 
       ps_drug_exposure_365d, ps_drug_exposure_30d, ps_drug_era, ps_drug_era_365d, 
       ps_drug_era_30d, ps_drug_era_overlap, ps_drug_era_ever, ps_drug_group, 
       ps_procedure_occ, ps_procedure_occ_365d, ps_procedure_occ_30d, 
       ps_procedure_group, ps_observation, ps_observation_365d, ps_observation_30d, 
       ps_observation_count_365d, ps_measurement, ps_measurement_365d, 
       ps_measurement_30d, ps_measurement_count_365d, ps_measurement_below, 
       ps_measurement_above, ps_concept_counts, ps_risk_scores, ps_risk_scores_charlson, 
       ps_risk_scores_dcsi, ps_risk_scores_chads2, ps_risk_scores_chads2vasc, 
       ps_interaction_year, ps_interaction_month, om_covariates, om_exclusion_id, 
       om_inclusion_id, om_demographics, om_demographics_gender, om_demographics_race, 
       om_demographics_ethnicity, om_demographics_age, om_demographics_year, 
       om_demographics_month, om_trim, om_trim_fraction, om_match, om_match_max_ratio, 
       om_strat, om_strat_num_strata, om_condition_occ, om_condition_occ_365d, 
       om_condition_occ_30d, om_condition_occ_inpt180d, om_condition_era, 
       om_condition_era_ever, om_condition_era_overlap, om_condition_group, 
       om_condition_group_meddra, om_condition_group_snomed, om_drug_exposure, 
       om_drug_exposure_365d, om_drug_exposure_30d, om_drug_era, om_drug_era_365d, 
       om_drug_era_30d, om_drug_era_overlap, om_drug_era_ever, om_drug_group, 
       om_procedure_occ, om_procedure_occ_365d, om_procedure_occ_30d, 
       om_procedure_group, om_observation, om_observation_365d, om_observation_30d, 
       om_observation_count_365d, om_measurement, om_measurement_365d, 
       om_measurement_30d, om_measurement_count_365d, om_measurement_below, 
       om_measurement_above, om_concept_counts, om_risk_scores, om_risk_scores_charlson, 
       om_risk_scores_dcsi, om_risk_scores_chads2, om_risk_scores_chads2vasc, 
       om_interaction_year, om_interaction_month, del_covariates_small_count, 
       negative_control_id
FROM ${ohdsiSchema}.cca
;

-- Augment the CCA table to remove the columns that are now stored in a 1:M format
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN model_type;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN time_at_risk_start;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN time_at_risk_end;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN add_exposure_days_to_end;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN minimum_washout_period;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN minimum_days_at_risk;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN rm_subjects_in_both_cohorts;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN rm_prior_outcomes;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_adjustment;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_demographics;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_demographics_gender;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_demographics_race;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_demographics_ethnicity;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_demographics_age;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_demographics_year;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_demographics_month;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_trim;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_trim_fraction;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_match;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_match_max_ratio;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_strat;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_strat_num_strata;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_condition_occ;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_condition_occ_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_condition_occ_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_condition_occ_inpt180d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_condition_era;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_condition_era_ever;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_condition_era_overlap;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_condition_group;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_condition_group_meddra;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_condition_group_snomed;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_drug_exposure;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_drug_exposure_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_drug_exposure_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_drug_era;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_drug_era_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_drug_era_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_drug_era_overlap;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_drug_era_ever;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_drug_group;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_procedure_occ;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_procedure_occ_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_procedure_occ_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_procedure_group;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_observation;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_observation_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_observation_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_observation_count_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_measurement;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_measurement_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_measurement_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_measurement_count_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_measurement_below;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_measurement_above;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_concept_counts;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_risk_scores;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_risk_scores_charlson;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_risk_scores_dcsi;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_risk_scores_chads2;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_risk_scores_chads2vasc;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_interaction_year;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN ps_interaction_month;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_covariates;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_exclusion_id;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_inclusion_id;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_demographics;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_demographics_gender;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_demographics_race;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_demographics_ethnicity;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_demographics_age;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_demographics_year;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_demographics_month;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_trim;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_trim_fraction;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_match;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_match_max_ratio;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_strat;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_strat_num_strata;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_condition_occ;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_condition_occ_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_condition_occ_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_condition_occ_inpt180d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_condition_era;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_condition_era_ever;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_condition_era_overlap;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_condition_group;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_condition_group_meddra;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_condition_group_snomed;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_drug_exposure;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_drug_exposure_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_drug_exposure_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_drug_era;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_drug_era_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_drug_era_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_drug_era_overlap;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_drug_era_ever;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_drug_group;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_procedure_occ;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_procedure_occ_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_procedure_occ_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_procedure_group;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_observation;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_observation_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_observation_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_observation_count_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_measurement;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_measurement_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_measurement_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_measurement_count_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_measurement_below;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_measurement_above;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_concept_counts;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_risk_scores;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_risk_scores_charlson;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_risk_scores_dcsi;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_risk_scores_chads2;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_risk_scores_chads2vasc;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_interaction_year;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN om_interaction_month;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN del_covariates_small_count;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN negative_control_id;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN sec_user_id;


-- Update the columns that store user info & date
ALTER TABLE ${ohdsiSchema}.cca ADD COLUMN created_by varchar2(255);
ALTER TABLE ${ohdsiSchema}.cca ADD COLUMN modified_by varchar2(255);
ALTER TABLE ${ohdsiSchema}.cca RENAME COLUMN created TO created_date;
ALTER TABLE ${ohdsiSchema}.cca RENAME COLUMN modified TO modified_date;
