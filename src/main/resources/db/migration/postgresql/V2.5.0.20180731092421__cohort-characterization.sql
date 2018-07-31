CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.cohort_characterizations
(
  id                 BIGSERIAL                PRIMARY KEY,
  name               VARCHAR(255)   NOT NULL,
  created_by         INTEGER                  NOT NULL,
  created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
  updated_by         INTEGER,
  updated_at         TIMESTAMP WITH TIME ZONE,
);

ALTER TABLE ${ohdsiSchema}.cohort_characterizations
  ADD CONSTRAINT fk_cc_ser_user_creator FOREIGN KEY (created_by)
REFERENCES ${ohdsiSchema}.sec_user (id)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE ${ohdsiSchema}.cohort_characterizations
  ADD CONSTRAINT fk_cc_ser_user_updater FOREIGN KEY (updated_by)
REFERENCES ${ohdsiSchema}.sec_user (id)
ON UPDATE NO ACTION ON DELETE NO ACTION;



CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.cohort_characterization_params
(
  id                          BIGSERIAL               PRIMARY KEY,
  cohort_characterization_id  BIGINT                  NOT NULL,
  name                        VARCHAR(255),
  value                       VARCHAR(255)
);

ALTER TABLE ${ohdsiSchema}.cohort_characterization_params
  ADD CONSTRAINT fk_ccp_cohort_characterizations FOREIGN KEY (cohort_characterization_id)
REFERENCES ${ohdsiSchema}.cohort_characterizations (id)
ON UPDATE NO ACTION ON DELETE CASCADE;



CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.fe_analyses
(
  id         BIGSERIAL               PRIMARY KEY,
  type       VARCHAR(255),
  name       VARCHAR(255),
  domain     VARCHAR(255),
  descr      VARCHAR(1000),
  value      VARCHAR(255),
  design     Text,
  is_locked  BOOLEAN
);



CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.cohort_characterization_generations
(
  id                          BIGSERIAL              PRIMARY KEY,
  cohort_characterization_id  BIGINT                 NOT NULL,
  source_id                   BIGINT                 NOT NULL,
  date                        TIMESTAMP              WITH TIME ZONE,
  status                      VARCHAR(255)
);

ALTER TABLE ${ohdsiSchema}.cohort_characterization_generations
  ADD CONSTRAINT fk_ccg_cohort_characterizations FOREIGN KEY (cohort_characterization_id)
REFERENCES ${ohdsiSchema}.cohort_characterizations (id)
ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE ${ohdsiSchema}.cohort_characterization_generations
  ADD CONSTRAINT fk_ccg_source FOREIGN KEY (source_id)
REFERENCES ${ohdsiSchema}.source(source_id)
ON UPDATE NO ACTION ON DELETE NO ACTION;



CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.cohort_characterization_results
(
  id BIGSERIAL PRIMARY KEY,
  type VARCHAR(255),
  cohort_characterization_generation_id BIGINT NOT NULL,
  analysis_id BIGINT,
  covariate_id BIGINT,
  covariate_name VARCHAR(255),
  time_window VARCHAR(255),
  concept_id BIGINT,
  count_value BIGINT,
  avg_value DOUBLE PRECISION,
  stdev_value DOUBLE PRECISION,
  min_value DOUBLE PRECISION,
  p10_value DOUBLE PRECISION,
  p25_value DOUBLE PRECISION,
  median_value DOUBLE PRECISION,
  p75_value DOUBLE PRECISION,
  p90_value DOUBLE PRECISION,
  max_value DOUBLE PRECISION
);

ALTER TABLE ${ohdsiSchema}.cohort_characterization_results
  ADD CONSTRAINT fk_ccr_cohort_char_gen FOREIGN KEY (cohort_characterization_generation_id)
REFERENCES ${ohdsiSchema}.cohort_characterization_generations(id)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE ${ohdsiSchema}.cohort_characterization_results
  ADD CONSTRAINT fk_ccr_fe_analyses FOREIGN KEY (cohort_characterization_generation_id)
REFERENCES ${ohdsiSchema}.cohort_characterization_generations(id)
ON UPDATE NO ACTION ON DELETE NO ACTION;



INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortcharacterization:POST', 'Create cohort characterization');
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp."value" = 'cohortcharacterization:post' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortcharacterization:*:GET', 'Get cohort characterization');
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp."value" = 'cohortcharacterization:*:get' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortcharacterization:*:UPDATE', 'Update cohort characterization');
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp."value" = 'cohortcharacterization:*:update' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohortcharacterization:*:DELETE', 'Delete cohort characterization');
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp."value" = 'cohortcharacterization:*:delete' AND sr.name IN ('admin');



CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.cohort_characterizations_analyses
(
  cohort_characterization_id BIGINT NOT NULL,
  fe_analysis_id BIGINT NOT NULL
);

ALTER TABLE ${ohdsiSchema}.cohort_characterizations_analyses
  ADD CONSTRAINT fk_c_char_a_fe_analyses FOREIGN KEY (fe_analysis_id)
REFERENCES ${ohdsiSchema}.fe_analyses(id)
ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE ${ohdsiSchema}.cohort_characterizations_analyses
  ADD CONSTRAINT fk_c_char_a_cc FOREIGN KEY (cohort_characterization_id)
REFERENCES ${ohdsiSchema}.cohort_characterizations(id)
ON UPDATE NO ACTION ON DELETE CASCADE;



CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.fe_analysis_criteria
(
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255),
  expression Text,
  fe_analysis_id BIGINT
);

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria
  ADD CONSTRAINT fk_fec_fe_analyses FOREIGN KEY (fe_analysis_id)
REFERENCES ${ohdsiSchema}.fe_analyses(id)
ON UPDATE NO ACTION ON DELETE CASCADE;



CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.cohort_characterizations_cohorts
(
  cohort_characterization_id BIGINT NOT NULL,
  cohort_id BIGINT NOT NULL
);

ALTER TABLE ${ohdsiSchema}.cohort_characterizations_cohorts
  ADD CONSTRAINT fk_c_char_c_fe_analyses FOREIGN KEY (cohort_id)
REFERENCES ${ohdsiSchema}.cohort_definition(id)
ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE ${ohdsiSchema}.cohort_characterizations_cohorts
  ADD CONSTRAINT fk_c_char_c_cc FOREIGN KEY (cohort_characterization_id)
REFERENCES ${ohdsiSchema}.cohort_characterizations(id)
ON UPDATE NO ACTION ON DELETE CASCADE;


ALTER TABLE ${ohdsiSchema}.cohort_definition_details ADD hash_code int null;


INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Measurement Range Group', NULL, 'Covariates indicating whether measurements are below, within, or above normal range in the short term window.', null, 'MeasurementRangeGroupShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era Start', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table starting in the long term window.', null, 'ConditionGroupEraStartLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era Start', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table starting in the medium term window.', null, 'DrugGroupEraStartMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with any part of the short term window.', null, 'ConditionEraShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the long term window.', null, 'DrugGroupEraLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with the end of the risk window.', null, 'ConditionGroupEraOverlapping', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the short term window.', null, 'DrugGroupEraShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the medium term window.', null, 'DrugGroupEraMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era Start', 'CONDITION', 'One covariate per condition in the condition_era table starting in the long term window.', null, 'ConditionEraStartLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with any time prior to index.', null, 'ConditionEraAnyTimePrior', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era Start', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table starting in the medium term window.', null, 'ConditionGroupEraStartMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Exposure', 'DRUG', 'One covariate per drug in the drug_exposure table starting in the long term window.', null, 'DrugExposureLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Measurement Range Group', NULL, 'Covariates indicating whether measurements are below, within, or above normal range in the long term window.', null, 'MeasurementRangeGroupLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Measurement Range Group', NULL, 'Covariates indicating whether measurements are below, within, or above normal range in the medium term window.', null, 'MeasurementRangeGroupMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any time prior to index.', null, 'DrugGroupEraAnyTimePrior', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with any part of the medium term window.', null, 'ConditionEraMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with the end of the risk window.', null, 'ConditionEraOverlapping', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era Start', 'CONDITION', 'One covariate per condition in the condition_era table starting in the short term window.', null, 'ConditionEraStartShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era Start', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table starting in the short term window.', null, 'DrugGroupEraStartShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the short term window.', null, 'ConditionGroupEraShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era Start', 'CONDITION', 'One covariate per condition in the condition_era table starting in the medium term window.', null, 'ConditionEraStartMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Occurrence', 'PROCEDURE', 'One covariate per procedure in the procedure_occurrence table in the medium term window.', null, 'ProcedureOccurrenceMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with any part of the long term window.', null, 'ConditionEraLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era Start', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table starting in the long term window.', null, 'DrugGroupEraStartLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with the end of the risk window.', null, 'DrugGroupEraOverlapping', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Measurement Range Group', NULL, 'Covariates indicating whether measurements are below, within, or above normal range any time prior to index.', null, 'MeasurementRangeGroupAnyTimePrior', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with any time prior to index.', null, 'ConditionGroupEraAnyTimePrior', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Exposure', 'DRUG', 'One covariate per drug in the drug_exposure table starting any time prior to index.', null, 'DrugExposureAnyTimePrior', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era Start', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table starting in the short term window.', null, 'ConditionGroupEraStartShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the long term window.', null, 'ConditionGroupEraLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Exposure', 'DRUG', 'One covariate per drug in the drug_exposure table starting in the short term window.', null, 'DrugExposureShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Group Era', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the medium term window.', null, 'ConditionGroupEraMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Exposure', 'DRUG', 'One covariate per drug in the drug_exposure table starting in the medium term window.', null, 'DrugExposureMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Observation', 'OBSERVATION', 'One covariate per observation in the observation table in the short term window.', null, 'ObservationShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era Start', 'DRUG', 'One covariate per drug in the drug_era table starting in the long term window.', null, 'DrugEraStartLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Dcsi', 'CONDITION', 'The Diabetes Comorbidity Severity Index (DCSI) using all conditions prior to the window end.', null, 'Dcsi', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era Start', 'DRUG', 'One covariate per drug in the drug_era table starting in the long short window.', null, 'DrugEraStartShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Ingredient Count', 'DRUG', 'The number of distinct ingredients observed in the medium term window.', null, 'DistinctIngredientCountMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Measurement', 'MEASUREMENT', 'One covariate per measurement in the measurement table any time prior to index.', null, 'MeasurementAnyTimePrior', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Measurement', 'MEASUREMENT', 'One covariate per measurement in the measurement table in the medium term window.', null, 'MeasurementMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Condition Count', 'CONDITION', 'The number of distinct condition concepts observed in the long term window.', null, 'DistinctConditionCountLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Measurement Value', NULL, 'One covariate containing the value per measurement-unit combination in the long term window.', null, 'MeasurementValueLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era', 'DRUG', 'One covariate per drug in the drug_era table overlapping with any part of the short window.', null, 'DrugEraShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era', 'DRUG', 'One covariate per drug in the drug_era table overlapping with the end of the risk window.', null, 'DrugEraOverlapping', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Observation', 'OBSERVATION', 'One covariate per observation in the observation table any time prior to index.', null, 'ObservationAnyTimePrior', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Ingredient Count', 'DRUG', 'The number of distinct ingredients observed in the long term window.', null, 'DistinctIngredientCountLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Procedure Count', 'PROCEDURE', 'The number of distinct procedures observed in the short term window.', null, 'DistinctProcedureCountShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Condition Count', 'CONDITION', 'The number of distinct condition concepts observed in the short term window.', null, 'DistinctConditionCountShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Charlson Index', 'CONDITION', 'The Charlson comorbidity index (Romano adaptation) using all conditions prior to the window end.', null, 'CharlsonIndex', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Measurement', 'MEASUREMENT', 'One covariate per measurement in the measurement table in the short term window.', null, 'MeasurementShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Procedure Count', 'PROCEDURE', 'The number of distinct procedures observed in the medium term window.', null, 'DistinctProcedureCountMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Exposure', 'DEVICE', 'One covariate per device in the device exposure table starting any time prior to index.', null, 'DeviceExposureAnyTimePrior', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Observation', 'OBSERVATION', 'One covariate per observation in the observation table in the long term window.', null, 'ObservationLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Condition Count', 'CONDITION', 'The number of distinct condition concepts observed in the medium term window.', null, 'DistinctConditionCountMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Occurrence', 'PROCEDURE', 'One covariate per procedure in the procedure_occurrence table in the short term window.', null, 'ProcedureOccurrenceShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Observation', 'OBSERVATION', 'One covariate per observation in the observation table in the medium term window.', null, 'ObservationMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Exposure', 'DEVICE', 'One covariate per device in the device exposure table starting in the long term window.', null, 'DeviceExposureLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Measurement Value', NULL, 'One covariate containing the value per measurement-unit combination in the short term window.', null, 'MeasurementValueShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Exposure', 'DEVICE', 'One covariate per device in the device exposure table starting in the medium term window.', null, 'DeviceExposureMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Measurement', 'MEASUREMENT', 'One covariate per measurement in the measurement table in the long term window.', null, 'MeasurementLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Measurement Value', NULL, 'One covariate containing the value per measurement-unit combination in the medium term window.', null, 'MeasurementValueMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era Start', 'DRUG', 'One covariate per drug in the drug_era table starting in the medium term window.', null, 'DrugEraStartMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Measurement Value', NULL, 'One covariate containing the value per measurement-unit combination any time prior to index.', null, 'MeasurementValueAnyTimePrior', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Ingredient Count', 'DRUG', 'The number of distinct ingredients observed in the short term window.', null, 'DistinctIngredientCountShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Exposure', 'DEVICE', 'One covariate per device in the device exposure table starting in the short term window.', null, 'DeviceExposureShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Procedure Count', 'PROCEDURE', 'The number of distinct procedures observed in the long term window.', null, 'DistinctProcedureCountLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Occurrence', 'CONDITION', 'One covariate per condition in the condition_occurrence table starting in the long term window.', null, 'ConditionOccurrenceLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Index Month', 'DEMOGRAPHICS', 'Month of the index date.', null, 'DemographicsIndexMonth', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Occurrence', 'CONDITION', 'One covariate per condition in the condition_occurrence table starting any time prior to index.', null, 'ConditionOccurrenceAnyTimePrior', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Ethnicity', 'DEMOGRAPHICS', 'Ethnicity of the subject.', null, 'DemographicsEthnicity', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Age Group', 'DEMOGRAPHICS', 'Age of the subject on the index date (in 5 year age groups)', null, 'DemographicsAgeGroup', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Race', 'DEMOGRAPHICS', 'Race of the subject.', null, 'DemographicsRace', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Prior Observation Time', 'DEMOGRAPHICS', 'Number of continuous days of observation time preceding the index date.', null, 'DemographicsPriorObservationTime', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Gender', 'DEMOGRAPHICS', 'Gender of the subject.', null, 'DemographicsGender', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Index Year Month', 'DEMOGRAPHICS', 'Both calendar year and month of the index date in a single variable.', null, 'DemographicsIndexYearMonth', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Occurrence', 'CONDITION', 'One covariate per condition in the condition_occurrence table starting in the medium term window.', null, 'ConditionOccurrenceMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Age', 'DEMOGRAPHICS', 'Age of the subject on the index date (in years).', null, 'DemographicsAge', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Chads 2', 'CONDITION', 'The CHADS2 score using all conditions prior to the window end.', null, 'Chads2', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Time In Cohort', 'DEMOGRAPHICS', 'Number of days of observation time during cohort period.', null, 'DemographicsTimeInCohort', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Index Year', 'DEMOGRAPHICS', 'Year of the index date.', null, 'DemographicsIndexYear', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Post Observation Time', 'DEMOGRAPHICS', 'Number of continuous days of observation time following the index date.', null, 'DemographicsPostObservationTime', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Chads 2 Vasc', 'CONDITION', 'The CHADS2VASc score using all conditions prior to the window end.', null, 'Chads2Vasc', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Occurrence Primary Inpatient', 'CONDITION', 'One covariate per condition observed as a primary diagnosis in an inpatient setting in the condition_occurrence table starting in the long term window.', null, 'ConditionOccurrencePrimaryInpatientLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Occurrence', 'PROCEDURE', 'One covariate per procedure in the procedure_occurrence table in the long term window.', null, 'ProcedureOccurrenceLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Occurrence Primary Inpatient', 'CONDITION', 'One covariate per condition observed as a primary diagnosis in an inpatient setting in the condition_occurrence table starting any time prior to index.', null, 'ConditionOccurrencePrimaryInpatientAnyTimePrior', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era', 'DRUG', 'One covariate per drug in the drug_era table overlapping with any part of the long term window.', null, 'DrugEraLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Occurrence', 'PROCEDURE', 'One covariate per procedure in the procedure_occurrence table any time prior to index.', null, 'ProcedureOccurrenceAnyTimePrior', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era', 'DRUG', 'One covariate per drug in the drug_era table overlapping with any part of the medium term window.', null, 'DrugEraMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Era', 'DRUG', 'One covariate per drug in the drug_era table overlapping with any time prior to index.', null, 'DrugEraAnyTimePrior', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Occurrence', 'CONDITION', 'One covariate per condition in the condition_occurrence table starting in the short term window.', null, 'ConditionOccurrenceShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Measurement Count', 'MEASUREMENT', 'The number of distinct measurements observed in the short term window.', null, 'DistinctMeasurementCountShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Concept Count', 'VISIT', 'The number of visits observed in the short term window, stratified by visit concept ID.', null, 'VisitConceptCountShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Observation Count', 'OBSERVATION', 'The number of distinct observations observed in the medium term window.', null, 'DistinctObservationCountMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Observation Count', 'OBSERVATION', 'The number of distinct observations observed in the long term window.', null, 'DistinctObservationCountLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Measurement Count', 'MEASUREMENT', 'The number of distinct measurements observed in the long term window.', null, 'DistinctMeasurementCountLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Measurement Count', 'MEASUREMENT', 'The number of distinct measurements observed in the medium term window.', null, 'DistinctMeasurementCountMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Distinct Observation Count', 'OBSERVATION', 'The number of distinct observations observed in the short term window.', null, 'DistinctObservationCountShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Concept Count', 'VISIT', 'The number of visits observed in the long term window, stratified by visit concept ID.', null, 'VisitConceptCountLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Concept Count', 'VISIT', 'The number of visits observed in the medium term window, stratified by visit concept ID.', null, 'VisitConceptCountMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Count', 'VISIT', 'The number of visits observed in the medium term window.', null, 'VisitCountMediumTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Count', 'VISIT', 'The number of visits observed in the short term window.', null, 'VisitCountShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Occurrence Primary Inpatient', 'CONDITION', 'One covariate per condition observed  as a primary diagnosis in an inpatient setting in the condition_occurrence table starting in the short term window.', null, 'ConditionOccurrencePrimaryInpatientShortTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Count', 'VISIT', 'The number of visits observed in the long term window.', null, 'VisitCountLongTerm', true);
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked) VALUES ('PRESET', 'Occurrence Primary Inpatient', 'CONDITION', 'One covariate per condition observed  as a primary diagnosis in an inpatient setting in the condition_occurrence table starting in the medium term window.', null, 'ConditionOccurrencePrimaryInpatientMediumTerm', true);

-- TODO indexes