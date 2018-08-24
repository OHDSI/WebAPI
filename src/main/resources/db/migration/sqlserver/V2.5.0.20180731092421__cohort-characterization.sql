CREATE SEQUENCE ${ohdsiSchema}.cohort_characterizations_sequence;
CREATE TABLE ${ohdsiSchema}.cohort_characterizations
(
  id                 BIGINT                PRIMARY KEY DEFAULT NEXT VALUE FOR 'cohort_characterizations_sequence',
  name               VARCHAR(255)   NOT NULL,
  created_by         INTEGER                  NOT NULL,
  created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
  updated_by         INTEGER,
  updated_at         TIMESTAMP WITH TIME ZONE,
  hash_code          INTEGER                  NULL
);

ALTER TABLE ${ohdsiSchema}.cohort_characterizations
  ADD CONSTRAINT fk_cc_ser_user_creator FOREIGN KEY (created_by)
REFERENCES ${ohdsiSchema}.sec_user (id)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE ${ohdsiSchema}.cohort_characterizations
  ADD CONSTRAINT fk_cc_ser_user_updater FOREIGN KEY (updated_by)
REFERENCES ${ohdsiSchema}.sec_user (id)
ON UPDATE NO ACTION ON DELETE NO ACTION;



CREATE SEQUENCE ${ohdsiSchema}.cc_params_sequence;
CREATE TABLE ${ohdsiSchema}.cc_params
(
  id                          BIGINT               PRIMARY KEY DEFAULT NEXT VALUE FOR 'cc_params_sequence',
  cohort_characterization_id  BIGINT                  NOT NULL,
  name                        VARCHAR(255),
  value                       VARCHAR(255)
);

ALTER TABLE ${ohdsiSchema}.cc_params
  ADD CONSTRAINT fk_ccp_cc FOREIGN KEY (cohort_characterization_id)
REFERENCES ${ohdsiSchema}.cohort_characterizations (id)
ON UPDATE NO ACTION ON DELETE CASCADE;



CREATE SEQUENCE ${ohdsiSchema}.fe_analyses_sequence;
CREATE TABLE ${ohdsiSchema}.fe_analyses
(
  id         BIGINT               PRIMARY KEY DEFAULT NEXT VALUE FOR 'fe_analyses_sequence',
  type       VARCHAR(255),
  name       VARCHAR(255),
  domain     VARCHAR(255),
  descr      VARCHAR(1000),
  value      VARCHAR(255),
  design     Text,
  is_locked  BOOLEAN,
  stat_type  VARCHAR(255)
);


INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohortcharacterization:POST', 'Create cohort characterization');
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp."value" = 'cohortcharacterization:post' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohortcharacterization:*:GET', 'Get cohort characterization');
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp."value" = 'cohortcharacterization:*:get' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohortcharacterization:*:UPDATE', 'Update cohort characterization');
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp."value" = 'cohortcharacterization:*:update' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'cohortcharacterization:*:DELETE', 'Delete cohort characterization');
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp."value" = 'cohortcharacterization:*:delete' AND sr.name IN ('admin');



CREATE TABLE ${ohdsiSchema}.cc_analyses
(
  cohort_characterization_id BIGINT NOT NULL,
  fe_analysis_id BIGINT NOT NULL
);

ALTER TABLE ${ohdsiSchema}.cc_analyses
  ADD CONSTRAINT fk_c_char_a_fe_analyses FOREIGN KEY (fe_analysis_id)
REFERENCES ${ohdsiSchema}.fe_analyses(id)
ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE ${ohdsiSchema}.cc_analyses
  ADD CONSTRAINT fk_c_char_a_cc FOREIGN KEY (cohort_characterization_id)
REFERENCES ${ohdsiSchema}.cohort_characterizations(id)
ON UPDATE NO ACTION ON DELETE CASCADE;



CREATE SEQUENCE ${ohdsiSchema}.fe_analysis_criteria_sequence;
CREATE TABLE ${ohdsiSchema}.fe_analysis_criteria
(
  id BIGINT PRIMARY KEY DEFAULT NEXT VALUE FOR 'fe_analysis_criteria_sequence',
  name VARCHAR(255),
  expression Text,
  fe_analysis_id BIGINT
);

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria
  ADD CONSTRAINT fk_fec_fe_analyses FOREIGN KEY (fe_analysis_id)
REFERENCES ${ohdsiSchema}.fe_analyses(id)
ON UPDATE NO ACTION ON DELETE CASCADE;



CREATE TABLE ${ohdsiSchema}.cc_cohorts
(
  cohort_characterization_id BIGINT NOT NULL,
  cohort_id BIGINT NOT NULL
);

ALTER TABLE ${ohdsiSchema}.cc_cohorts
  ADD CONSTRAINT fk_c_char_c_fe_analyses FOREIGN KEY (cohort_id)
REFERENCES ${ohdsiSchema}.cohort_definition(id)
ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE ${ohdsiSchema}.cc_cohorts
  ADD CONSTRAINT fk_c_char_c_cc FOREIGN KEY (cohort_characterization_id)
REFERENCES ${ohdsiSchema}.cohort_characterizations(id)
ON UPDATE NO ACTION ON DELETE CASCADE;


ALTER TABLE ${ohdsiSchema}.cohort_definition_details ADD hash_code int null;

INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Range Group', NULL, 'Covariates indicating whether measurements are below, within, or above normal range in the short term window.', null, 'MeasurementRangeGroupShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era Start', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table starting in the long term window.', null, 'ConditionGroupEraStartLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era Start', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table starting in the medium term window.', null, 'DrugGroupEraStartMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with any part of the short term window.', null, 'ConditionEraShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the long term window.', null, 'DrugGroupEraLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with the end of the risk window.', null, 'ConditionGroupEraOverlapping', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the short term window.', null, 'DrugGroupEraShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the medium term window.', null, 'DrugGroupEraMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era Start', 'CONDITION', 'One covariate per condition in the condition_era table starting in the long term window.', null, 'ConditionEraStartLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with any time prior to index.', null, 'ConditionEraAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era Start', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table starting in the medium term window.', null, 'ConditionGroupEraStartMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Exposure', 'DRUG', 'One covariate per drug in the drug_exposure table starting in the long term window.', null, 'DrugExposureLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Range Group', NULL, 'Covariates indicating whether measurements are below, within, or above normal range in the long term window.', null, 'MeasurementRangeGroupLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Range Group', NULL, 'Covariates indicating whether measurements are below, within, or above normal range in the medium term window.', null, 'MeasurementRangeGroupMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any time prior to index.', null, 'DrugGroupEraAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with any part of the medium term window.', null, 'ConditionEraMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with the end of the risk window.', null, 'ConditionEraOverlapping', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era Start', 'CONDITION', 'One covariate per condition in the condition_era table starting in the short term window.', null, 'ConditionEraStartShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era Start', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table starting in the short term window.', null, 'DrugGroupEraStartShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the short term window.', null, 'ConditionGroupEraShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era Start', 'CONDITION', 'One covariate per condition in the condition_era table starting in the medium term window.', null, 'ConditionEraStartMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Occurrence', 'PROCEDURE', 'One covariate per procedure in the procedure_occurrence table in the medium term window.', null, 'ProcedureOccurrenceMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with any part of the long term window.', null, 'ConditionEraLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era Start', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table starting in the long term window.', null, 'DrugGroupEraStartLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with the end of the risk window.', null, 'DrugGroupEraOverlapping', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Range Group', NULL, 'Covariates indicating whether measurements are below, within, or above normal range any time prior to index.', null, 'MeasurementRangeGroupAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with any time prior to index.', null, 'ConditionGroupEraAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Exposure', 'DRUG', 'One covariate per drug in the drug_exposure table starting any time prior to index.', null, 'DrugExposureAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era Start', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table starting in the short term window.', null, 'ConditionGroupEraStartShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the long term window.', null, 'ConditionGroupEraLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Exposure', 'DRUG', 'One covariate per drug in the drug_exposure table starting in the short term window.', null, 'DrugExposureShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Group Era', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the medium term window.', null, 'ConditionGroupEraMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Exposure', 'DRUG', 'One covariate per drug in the drug_exposure table starting in the medium term window.', null, 'DrugExposureMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Observation', 'OBSERVATION', 'One covariate per observation in the observation table in the short term window.', null, 'ObservationShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era Start', 'DRUG', 'One covariate per drug in the drug_era table starting in the long term window.', null, 'DrugEraStartLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Dcsi', 'CONDITION', 'The Diabetes Comorbidity Severity Index (DCSI) using all conditions prior to the window end.', null, 'Dcsi', true, 'DISTRIBUTION');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era Start', 'DRUG', 'One covariate per drug in the drug_era table starting in the long short window.', null, 'DrugEraStartShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Ingredient Count', 'DRUG', 'The number of distinct ingredients observed in the medium term window.', null, 'DistinctIngredientCountMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement', 'MEASUREMENT', 'One covariate per measurement in the measurement table any time prior to index.', null, 'MeasurementAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement', 'MEASUREMENT', 'One covariate per measurement in the measurement table in the medium term window.', null, 'MeasurementMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Condition Count', 'CONDITION', 'The number of distinct condition concepts observed in the long term window.', null, 'DistinctConditionCountLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Value', NULL, 'One covariate containing the value per measurement-unit combination in the long term window.', null, 'MeasurementValueLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era', 'DRUG', 'One covariate per drug in the drug_era table overlapping with any part of the short window.', null, 'DrugEraShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era', 'DRUG', 'One covariate per drug in the drug_era table overlapping with the end of the risk window.', null, 'DrugEraOverlapping', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Observation', 'OBSERVATION', 'One covariate per observation in the observation table any time prior to index.', null, 'ObservationAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Ingredient Count', 'DRUG', 'The number of distinct ingredients observed in the long term window.', null, 'DistinctIngredientCountLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Procedure Count', 'PROCEDURE', 'The number of distinct procedures observed in the short term window.', null, 'DistinctProcedureCountShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Condition Count', 'CONDITION', 'The number of distinct condition concepts observed in the short term window.', null, 'DistinctConditionCountShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Charlson Index', 'CONDITION', 'The Charlson comorbidity index (Romano adaptation) using all conditions prior to the window end.', null, 'CharlsonIndex', true, 'DISTRIBUTION');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement', 'MEASUREMENT', 'One covariate per measurement in the measurement table in the short term window.', null, 'MeasurementShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Procedure Count', 'PROCEDURE', 'The number of distinct procedures observed in the medium term window.', null, 'DistinctProcedureCountMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Exposure', 'DEVICE', 'One covariate per device in the device exposure table starting any time prior to index.', null, 'DeviceExposureAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Observation', 'OBSERVATION', 'One covariate per observation in the observation table in the long term window.', null, 'ObservationLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Condition Count', 'CONDITION', 'The number of distinct condition concepts observed in the medium term window.', null, 'DistinctConditionCountMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Occurrence', 'PROCEDURE', 'One covariate per procedure in the procedure_occurrence table in the short term window.', null, 'ProcedureOccurrenceShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Observation', 'OBSERVATION', 'One covariate per observation in the observation table in the medium term window.', null, 'ObservationMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Exposure', 'DEVICE', 'One covariate per device in the device exposure table starting in the long term window.', null, 'DeviceExposureLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Value', NULL, 'One covariate containing the value per measurement-unit combination in the short term window.', null, 'MeasurementValueShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Exposure', 'DEVICE', 'One covariate per device in the device exposure table starting in the medium term window.', null, 'DeviceExposureMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement', 'MEASUREMENT', 'One covariate per measurement in the measurement table in the long term window.', null, 'MeasurementLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Value', NULL, 'One covariate containing the value per measurement-unit combination in the medium term window.', null, 'MeasurementValueMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era Start', 'DRUG', 'One covariate per drug in the drug_era table starting in the medium term window.', null, 'DrugEraStartMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Value', NULL, 'One covariate containing the value per measurement-unit combination any time prior to index.', null, 'MeasurementValueAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Ingredient Count', 'DRUG', 'The number of distinct ingredients observed in the short term window.', null, 'DistinctIngredientCountShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Exposure', 'DEVICE', 'One covariate per device in the device exposure table starting in the short term window.', null, 'DeviceExposureShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Procedure Count', 'PROCEDURE', 'The number of distinct procedures observed in the long term window.', null, 'DistinctProcedureCountLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Occurrence', 'CONDITION', 'One covariate per condition in the condition_occurrence table starting in the long term window.', null, 'ConditionOccurrenceLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Index Month', 'DEMOGRAPHICS', 'Month of the index date.', null, 'DemographicsIndexMonth', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Occurrence', 'CONDITION', 'One covariate per condition in the condition_occurrence table starting any time prior to index.', null, 'ConditionOccurrenceAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Ethnicity', 'DEMOGRAPHICS', 'Ethnicity of the subject.', null, 'DemographicsEthnicity', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Age Group', 'DEMOGRAPHICS', 'Age of the subject on the index date (in 5 year age groups)', null, 'DemographicsAgeGroup', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Race', 'DEMOGRAPHICS', 'Race of the subject.', null, 'DemographicsRace', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Prior Observation Time', 'DEMOGRAPHICS', 'Number of continuous days of observation time preceding the index date.', null, 'DemographicsPriorObservationTime', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Gender', 'DEMOGRAPHICS', 'Gender of the subject.', null, 'DemographicsGender', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Index Year Month', 'DEMOGRAPHICS', 'Both calendar year and month of the index date in a single variable.', null, 'DemographicsIndexYearMonth', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Occurrence', 'CONDITION', 'One covariate per condition in the condition_occurrence table starting in the medium term window.', null, 'ConditionOccurrenceMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Age', 'DEMOGRAPHICS', 'Age of the subject on the index date (in years).', null, 'DemographicsAge', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Chads 2', 'CONDITION', 'The CHADS2 score using all conditions prior to the window end.', null, 'Chads2', true, 'DISTRIBUTION');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Time In Cohort', 'DEMOGRAPHICS', 'Number of days of observation time during cohort period.', null, 'DemographicsTimeInCohort', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Index Year', 'DEMOGRAPHICS', 'Year of the index date.', null, 'DemographicsIndexYear', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Post Observation Time', 'DEMOGRAPHICS', 'Number of continuous days of observation time following the index date.', null, 'DemographicsPostObservationTime', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Chads 2 Vasc', 'CONDITION', 'The CHADS2VASc score using all conditions prior to the window end.', null, 'Chads2Vasc', true, 'DISTRIBUTION');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Occurrence Primary Inpatient', 'CONDITION', 'One covariate per condition observed as a primary diagnosis in an inpatient setting in the condition_occurrence table starting in the long term window.', null, 'ConditionOccurrencePrimaryInpatientLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Occurrence', 'PROCEDURE', 'One covariate per procedure in the procedure_occurrence table in the long term window.', null, 'ProcedureOccurrenceLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Occurrence Primary Inpatient', 'CONDITION', 'One covariate per condition observed as a primary diagnosis in an inpatient setting in the condition_occurrence table starting any time prior to index.', null, 'ConditionOccurrencePrimaryInpatientAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era', 'DRUG', 'One covariate per drug in the drug_era table overlapping with any part of the long term window.', null, 'DrugEraLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Occurrence', 'PROCEDURE', 'One covariate per procedure in the procedure_occurrence table any time prior to index.', null, 'ProcedureOccurrenceAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era', 'DRUG', 'One covariate per drug in the drug_era table overlapping with any part of the medium term window.', null, 'DrugEraMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Era', 'DRUG', 'One covariate per drug in the drug_era table overlapping with any time prior to index.', null, 'DrugEraAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Occurrence', 'CONDITION', 'One covariate per condition in the condition_occurrence table starting in the short term window.', null, 'ConditionOccurrenceShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Measurement Count', 'MEASUREMENT', 'The number of distinct measurements observed in the short term window.', null, 'DistinctMeasurementCountShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Concept Count', 'VISIT', 'The number of visits observed in the short term window, stratified by visit concept ID.', null, 'VisitConceptCountShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Observation Count', 'OBSERVATION', 'The number of distinct observations observed in the medium term window.', null, 'DistinctObservationCountMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Observation Count', 'OBSERVATION', 'The number of distinct observations observed in the long term window.', null, 'DistinctObservationCountLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Measurement Count', 'MEASUREMENT', 'The number of distinct measurements observed in the long term window.', null, 'DistinctMeasurementCountLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Measurement Count', 'MEASUREMENT', 'The number of distinct measurements observed in the medium term window.', null, 'DistinctMeasurementCountMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Observation Count', 'OBSERVATION', 'The number of distinct observations observed in the short term window.', null, 'DistinctObservationCountShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Concept Count', 'VISIT', 'The number of visits observed in the long term window, stratified by visit concept ID.', null, 'VisitConceptCountLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Concept Count', 'VISIT', 'The number of visits observed in the medium term window, stratified by visit concept ID.', null, 'VisitConceptCountMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Count', 'VISIT', 'The number of visits observed in the medium term window.', null, 'VisitCountMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Count', 'VISIT', 'The number of visits observed in the short term window.', null, 'VisitCountShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Occurrence Primary Inpatient', 'CONDITION', 'One covariate per condition observed  as a primary diagnosis in an inpatient setting in the condition_occurrence table starting in the short term window.', null, 'ConditionOccurrencePrimaryInpatientShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Count', 'VISIT', 'The number of visits observed in the long term window.', null, 'VisitCountLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analyses (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Occurrence Primary Inpatient', 'CONDITION', 'One covariate per condition observed  as a primary diagnosis in an inpatient setting in the condition_occurrence table starting in the medium term window.', null, 'ConditionOccurrencePrimaryInpatientMediumTerm', true, 'PREVALENCE');

CREATE OR REPLACE VIEW ${ohdsiSchema}.cc_generations as
  (SELECT job.job_execution_id,
          MAX(job.create_time)                                                                     date,
          MAX(job.status)                                                                          status,
          MAX(CASE WHEN params.key_name = 'hash_code' THEN params.string_val END)                  hash_code,
          MAX(CASE WHEN params.key_name = 'cohort_characterization_id' THEN params.string_val END) cohort_characterization_id,
          MAX(CASE WHEN params.key_name = 'source_id' THEN params.string_val END)                  source_id
   FROM ${ohdsiSchema}.batch_job_execution job
          JOIN ${ohdsiSchema}.batch_job_execution_params params ON job.job_execution_id = params.job_execution_id
                                                                     AND (params.key_name = 'hash_code' OR
                                                                          params.key_name = 'cohort_characterization_id' OR
                                                                          params.key_name = 'source_id')
   GROUP BY job.job_execution_id);
