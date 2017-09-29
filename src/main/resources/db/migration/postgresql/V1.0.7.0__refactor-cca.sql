CREATE SEQUENCE ${ohdsiSchema}.CCA_T_C_SEQUENCE MAXVALUE 9223372036854775807 NO CYCLE;
CREATE TABLE ${ohdsiSchema}.cca_t_c
(
  id integer NOT NULL DEFAULT NEXTVAL('${ohdsiSchema}.CCA_T_C_SEQUENCE'),
  cca_id integer NOT NULL,
  target_id integer NOT NULL,
  comparator_id integer NOT NULL,
  ps_exclusion_id integer NOT NULL,
  ps_inclusion_id integer NOT NULL,
  CONSTRAINT PK_cca_t_c PRIMARY KEY (id)
 );

CREATE SEQUENCE ${ohdsiSchema}.CCA_O_SEQUENCE MAXVALUE 9223372036854775807 NO CYCLE;
CREATE TABLE ${ohdsiSchema}.cca_o
(
  id integer NOT NULL DEFAULT NEXTVAL('${ohdsiSchema}.CCA_O_SEQUENCE'),
  cca_id integer NOT NULL,
  outcome_id integer NOT NULL,
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
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS treatment_id;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS comparator_id;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS outcome_id;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_exclusion_id;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_inclusion_id;


-- Transform the base cca table into cca_analysis
CREATE SEQUENCE ${ohdsiSchema}.CCA_ANALYSIS_SEQUENCE MAXVALUE 9223372036854775807 NO CYCLE;
CREATE TABLE ${ohdsiSchema}.cca_analysis
(
  id integer NOT NULL DEFAULT NEXTVAL('${ohdsiSchema}.CCA_ANALYSIS_SEQUENCE'),
  cca_id integer NOT NULL,
  description character varying(255),
  model_type character varying(50),
  time_at_risk_start integer,
  time_at_risk_end integer,
  add_exposure_days_to_end integer,
  minimum_washout_period integer,
  minimum_days_at_risk integer,
  rm_subjects_in_both_cohorts integer,
  rm_prior_outcomes integer,
  ps_adjustment integer,
  ps_demographics integer,
  ps_demographics_gender integer,
  ps_demographics_race integer,
  ps_demographics_ethnicity integer,
  ps_demographics_age integer,
  ps_demographics_year integer,
  ps_demographics_month integer,
  ps_trim integer,
  ps_trim_fraction double precision,
  ps_match integer,
  ps_match_max_ratio integer,
  ps_strat integer,
  ps_strat_num_strata integer,
  ps_condition_occ integer,
  ps_condition_occ_365d integer,
  ps_condition_occ_30d integer,
  ps_condition_occ_inpt180d integer,
  ps_condition_era integer,
  ps_condition_era_ever integer,
  ps_condition_era_overlap integer,
  ps_condition_group integer,
  ps_condition_group_meddra integer,
  ps_condition_group_snomed integer,
  ps_drug_exposure integer,
  ps_drug_exposure_365d integer,
  ps_drug_exposure_30d integer,
  ps_drug_era integer,
  ps_drug_era_365d integer,
  ps_drug_era_30d integer,
  ps_drug_era_overlap integer,
  ps_drug_era_ever integer,
  ps_drug_group integer,
  ps_procedure_occ integer,
  ps_procedure_occ_365d integer,
  ps_procedure_occ_30d integer,
  ps_procedure_group integer,
  ps_observation integer,
  ps_observation_365d integer,
  ps_observation_30d integer,
  ps_observation_count_365d integer,
  ps_measurement integer,
  ps_measurement_365d integer,
  ps_measurement_30d integer,
  ps_measurement_count_365d integer,
  ps_measurement_below integer,
  ps_measurement_above integer,
  ps_concept_counts integer,
  ps_risk_scores integer,
  ps_risk_scores_charlson integer,
  ps_risk_scores_dcsi integer,
  ps_risk_scores_chads2 integer,
  ps_risk_scores_chads2vasc integer,
  ps_interaction_year integer,
  ps_interaction_month integer,
  om_covariates integer,
  om_exclusion_id integer,
  om_inclusion_id integer,
  om_demographics integer,
  om_demographics_gender integer,
  om_demographics_race integer,
  om_demographics_ethnicity integer,
  om_demographics_age integer,
  om_demographics_year integer,
  om_demographics_month integer,
  om_trim integer,
  om_trim_fraction double precision,
  om_match integer,
  om_match_max_ratio integer,
  om_strat integer,
  om_strat_num_strata integer,
  om_condition_occ integer,
  om_condition_occ_365d integer,
  om_condition_occ_30d integer,
  om_condition_occ_inpt180d integer,
  om_condition_era integer,
  om_condition_era_ever integer,
  om_condition_era_overlap integer,
  om_condition_group integer,
  om_condition_group_meddra integer,
  om_condition_group_snomed integer,
  om_drug_exposure integer,
  om_drug_exposure_365d integer,
  om_drug_exposure_30d integer,
  om_drug_era integer,
  om_drug_era_365d integer,
  om_drug_era_30d integer,
  om_drug_era_overlap integer,
  om_drug_era_ever integer,
  om_drug_group integer,
  om_procedure_occ integer,
  om_procedure_occ_365d integer,
  om_procedure_occ_30d integer,
  om_procedure_group integer,
  om_observation integer,
  om_observation_365d integer,
  om_observation_30d integer,
  om_observation_count_365d integer,
  om_measurement integer,
  om_measurement_365d integer,
  om_measurement_30d integer,
  om_measurement_count_365d integer,
  om_measurement_below integer,
  om_measurement_above integer,
  om_concept_counts integer,
  om_risk_scores integer,
  om_risk_scores_charlson integer,
  om_risk_scores_dcsi integer,
  om_risk_scores_chads2 integer,
  om_risk_scores_chads2vasc integer,
  om_interaction_year integer,
  om_interaction_month integer,
  del_covariates_small_count integer,
  negative_control_id integer,
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
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS model_type;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS time_at_risk_start;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS time_at_risk_end;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS add_exposure_days_to_end;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS minimum_washout_period;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS minimum_days_at_risk;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS rm_subjects_in_both_cohorts;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS rm_prior_outcomes;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_adjustment;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_demographics;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_demographics_gender;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_demographics_race;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_demographics_ethnicity;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_demographics_age;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_demographics_year;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_demographics_month;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_trim;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_trim_fraction;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_match;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_match_max_ratio;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_strat;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_strat_num_strata;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_condition_occ;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_condition_occ_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_condition_occ_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_condition_occ_inpt180d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_condition_era;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_condition_era_ever;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_condition_era_overlap;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_condition_group;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_condition_group_meddra;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_condition_group_snomed;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_drug_exposure;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_drug_exposure_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_drug_exposure_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_drug_era;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_drug_era_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_drug_era_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_drug_era_overlap;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_drug_era_ever;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_drug_group;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_procedure_occ;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_procedure_occ_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_procedure_occ_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_procedure_group;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_observation;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_observation_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_observation_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_observation_count_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_measurement;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_measurement_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_measurement_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_measurement_count_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_measurement_below;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_measurement_above;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_concept_counts;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_risk_scores;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_risk_scores_charlson;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_risk_scores_dcsi;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_risk_scores_chads2;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_risk_scores_chads2vasc;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_interaction_year;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS ps_interaction_month;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_covariates;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_exclusion_id;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_inclusion_id;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_demographics;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_demographics_gender;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_demographics_race;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_demographics_ethnicity;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_demographics_age;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_demographics_year;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_demographics_month;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_trim;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_trim_fraction;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_match;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_match_max_ratio;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_strat;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_strat_num_strata;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_condition_occ;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_condition_occ_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_condition_occ_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_condition_occ_inpt180d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_condition_era;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_condition_era_ever;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_condition_era_overlap;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_condition_group;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_condition_group_meddra;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_condition_group_snomed;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_drug_exposure;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_drug_exposure_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_drug_exposure_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_drug_era;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_drug_era_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_drug_era_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_drug_era_overlap;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_drug_era_ever;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_drug_group;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_procedure_occ;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_procedure_occ_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_procedure_occ_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_procedure_group;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_observation;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_observation_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_observation_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_observation_count_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_measurement;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_measurement_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_measurement_30d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_measurement_count_365d;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_measurement_below;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_measurement_above;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_concept_counts;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_risk_scores;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_risk_scores_charlson;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_risk_scores_dcsi;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_risk_scores_chads2;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_risk_scores_chads2vasc;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_interaction_year;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS om_interaction_month;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS del_covariates_small_count;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS negative_control_id;
ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN IF EXISTS sec_user_id;


-- Update the columns that store user info & date
ALTER TABLE ${ohdsiSchema}.cca ADD COLUMN created_by character varying(255);
ALTER TABLE ${ohdsiSchema}.cca ADD COLUMN modified_by character varying(255);
ALTER TABLE ${ohdsiSchema}.cca RENAME COLUMN created TO created_date;
ALTER TABLE ${ohdsiSchema}.cca RENAME COLUMN modified TO modified_date;
