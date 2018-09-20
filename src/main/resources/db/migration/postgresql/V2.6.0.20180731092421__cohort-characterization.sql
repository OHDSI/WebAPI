CREATE SEQUENCE ${ohdsiSchema}.cohort_characterization_seq;
CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.cohort_characterization
(
  id                 BIGINT PRIMARY KEY DEFAULT NEXTVAL('cohort_characterization_seq'),
  name               VARCHAR(255) NOT NULL,
  created_by_id      INTEGER,
  created_date       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
  modified_by_id     INTEGER,
  modified_date      TIMESTAMP WITH TIME ZONE,
  hash_code          INTEGER NULL
);

ALTER TABLE ${ohdsiSchema}.cohort_characterization
  ADD CONSTRAINT fk_cc_ser_user_creator FOREIGN KEY (created_by_id)
REFERENCES ${ohdsiSchema}.sec_user (id)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE ${ohdsiSchema}.cohort_characterization
  ADD CONSTRAINT fk_cc_ser_user_updater FOREIGN KEY (modified_by_id)
REFERENCES ${ohdsiSchema}.sec_user (id)
ON UPDATE NO ACTION ON DELETE NO ACTION;



CREATE SEQUENCE ${ohdsiSchema}.cc_param_sequence;
CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.cc_param
(
  id                          BIGINT PRIMARY KEY DEFAULT NEXTVAL('cc_param_sequence'),
  cohort_characterization_id  BIGINT NOT NULL,
  name                        VARCHAR(255),
  value                       VARCHAR(255)
);

ALTER TABLE ${ohdsiSchema}.cc_param
  ADD CONSTRAINT fk_ccp_cc FOREIGN KEY (cohort_characterization_id)
REFERENCES ${ohdsiSchema}.cohort_characterization (id)
ON UPDATE NO ACTION ON DELETE CASCADE;



CREATE SEQUENCE ${ohdsiSchema}.fe_analysis_sequence;
CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.fe_analysis
(
  id         BIGINT PRIMARY KEY DEFAULT NEXTVAL('fe_analysis_sequence'),
  type       VARCHAR(255),
  name       VARCHAR(255),
  domain     VARCHAR(255),
  descr      VARCHAR(1000),
  value      VARCHAR(255),
  design     Text,
  is_locked  BOOLEAN,
  stat_type  VARCHAR(255)
);

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:post', 'Create cohort characterization'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:import:post', 'Import cohort characterization'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:get', 'Get cohort characterizations list'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:*:get', 'Get cohort characterization'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:*:generation:get', 'Get cohort characterization generations'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:generation:*:get', 'Get cohort characterization generation'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:generation:*:delete', 'Delete cohort characterization generation and results'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:generation:*:result:get', 'Get cohort characterization generation results'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:generation:*:design:get', 'Get cohort characterization generation design'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:*:export:get', 'Export cohort characterization'),

  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'feature-analysis:get', 'Get feature analyses list'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'feature-analysis:*:get', 'Get feature analysis'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'feature-analysis:post', 'Create feature analysis');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp."value" IN (
  'cohort-characterization:post',
  'cohort-characterization:import:post',
  'cohort-characterization:get',
  'cohort-characterization:*:get',
  'cohort-characterization:*:generation:get',
  'cohort-characterization:generation:*:get',
  'cohort-characterization:generation:*:delete',
  'cohort-characterization:generation:*:result:get',
  'cohort-characterization:generation:*:design:get',
  'cohort-characterization:*:export:get',

  'feature-analysis:get',
  'feature-analysis:*:get',
  'feature-analysis:post'
)
AND sr.name IN ('Atlas users');


-- SOURCE based permissions

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT nextval('${ohdsiSchema}.sec_permission_id_seq') AS id,
          'source:' || source_key || ':access' AS value,
          'Access to Source with SourceKey = ' || source_key AS description
  FROM ${ohdsiSchema}.source;

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  SELECT sr.id, sp.id
	FROM ${ohdsiSchema}.source
	join ${ohdsiSchema}.sec_permission sp ON sp.value IN ('source:' || source_key || ':access')
  join ${ohdsiSchema}.sec_role sr ON sr.name = 'Source user (' || source_key || ')';

CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.cc_analysis
(
  cohort_characterization_id BIGINT NOT NULL,
  fe_analysis_id BIGINT NOT NULL
);

ALTER TABLE ${ohdsiSchema}.cc_analysis
  ADD CONSTRAINT fk_c_char_a_fe_analysis FOREIGN KEY (fe_analysis_id)
REFERENCES ${ohdsiSchema}.fe_analysis(id)
ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE ${ohdsiSchema}.cc_analysis
  ADD CONSTRAINT fk_c_char_a_cc FOREIGN KEY (cohort_characterization_id)
REFERENCES ${ohdsiSchema}.cohort_characterization(id)
ON UPDATE NO ACTION ON DELETE CASCADE;



CREATE SEQUENCE ${ohdsiSchema}.fe_analysis_criteria_sequence;
CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.fe_analysis_criteria
(
  id BIGINT PRIMARY KEY DEFAULT NEXTVAL('fe_analysis_criteria_sequence'),
  name VARCHAR(255),
  expression Text,
  fe_analysis_id BIGINT
);

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria
  ADD CONSTRAINT fk_fec_fe_analysis FOREIGN KEY (fe_analysis_id)
REFERENCES ${ohdsiSchema}.fe_analysis(id)
ON UPDATE NO ACTION ON DELETE CASCADE;



CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.cc_cohort
(
  cohort_characterization_id BIGINT NOT NULL,
  cohort_id INT NOT NULL
);

ALTER TABLE ${ohdsiSchema}.cc_cohort
  ADD CONSTRAINT fk_c_char_c_fe_analysis FOREIGN KEY (cohort_id)
REFERENCES ${ohdsiSchema}.cohort_definition(id)
ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE ${ohdsiSchema}.cc_cohort
  ADD CONSTRAINT fk_c_char_c_cc FOREIGN KEY (cohort_characterization_id)
REFERENCES ${ohdsiSchema}.cohort_characterization(id)
ON UPDATE NO ACTION ON DELETE CASCADE;


ALTER TABLE ${ohdsiSchema}.cohort_definition_details ADD hash_code int null;

INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Range Group Short Term', NULL, 'Covariates indicating whether measurements are below, within, or above normal range in the short term window.', null, 'MeasurementRangeGroupShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Group Era Start Long Term', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table starting in the long term window.', null, 'ConditionGroupEraStartLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Group Era Start Medium Term', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table starting in the medium term window.', null, 'DrugGroupEraStartMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Era Short Term', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with any part of the short term window.', null, 'ConditionEraShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Group Era Long Term', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the long term window.', null, 'DrugGroupEraLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Group Era Overlapping', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with the end of the risk window.', null, 'ConditionGroupEraOverlapping', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Group Era Short Term', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the short term window.', null, 'DrugGroupEraShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Group Era Medium Term', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the medium term window.', null, 'DrugGroupEraMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Era Start Long Term', 'CONDITION', 'One covariate per condition in the condition_era table starting in the long term window.', null, 'ConditionEraStartLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Era Any Time Prior', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with any time prior to index.', null, 'ConditionEraAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Group Era Start Medium Term', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table starting in the medium term window.', null, 'ConditionGroupEraStartMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Exposure Long Term', 'DRUG', 'One covariate per drug in the drug_exposure table starting in the long term window.', null, 'DrugExposureLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Range Group Long Term', NULL, 'Covariates indicating whether measurements are below, within, or above normal range in the long term window.', null, 'MeasurementRangeGroupLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Range Group Medium Term', NULL, 'Covariates indicating whether measurements are below, within, or above normal range in the medium term window.', null, 'MeasurementRangeGroupMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Group Era Any Time Prior', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any time prior to index.', null, 'DrugGroupEraAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Era Medium Term', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with any part of the medium term window.', null, 'ConditionEraMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Era Overlapping', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with the end of the risk window.', null, 'ConditionEraOverlapping', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Era Start Short Term', 'CONDITION', 'One covariate per condition in the condition_era table starting in the short term window.', null, 'ConditionEraStartShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Group Era Start Short Term', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table starting in the short term window.', null, 'DrugGroupEraStartShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Group Era Short Term', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the short term window.', null, 'ConditionGroupEraShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Era Start Medium Term', 'CONDITION', 'One covariate per condition in the condition_era table starting in the medium term window.', null, 'ConditionEraStartMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Procedure Occurrence Medium Term', 'PROCEDURE', 'One covariate per procedure in the procedure_occurrence table in the medium term window.', null, 'ProcedureOccurrenceMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Era Long Term', 'CONDITION', 'One covariate per condition in the condition_era table overlapping with any part of the long term window.', null, 'ConditionEraLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Group Era Start Long Term', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table starting in the long term window.', null, 'DrugGroupEraStartLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Group Era Overlapping', 'DRUG', 'One covariate per drug rolled up to ATC groups in the drug_era table overlapping with the end of the risk window.', null, 'DrugGroupEraOverlapping', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Range Group Any Time Prior', NULL, 'Covariates indicating whether measurements are below, within, or above normal range any time prior to index.', null, 'MeasurementRangeGroupAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Group Era Any Time Prior', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with any time prior to index.', null, 'ConditionGroupEraAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Exposure Any Time Prior', 'DRUG', 'One covariate per drug in the drug_exposure table starting any time prior to index.', null, 'DrugExposureAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Group Era Start Short Term', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table starting in the short term window.', null, 'ConditionGroupEraStartShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Group Era Long Term', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the long term window.', null, 'ConditionGroupEraLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Exposure Short Term', 'DRUG', 'One covariate per drug in the drug_exposure table starting in the short term window.', null, 'DrugExposureShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Group Era Medium Term', 'CONDITION', 'One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the medium term window.', null, 'ConditionGroupEraMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Exposure Medium Term', 'DRUG', 'One covariate per drug in the drug_exposure table starting in the medium term window.', null, 'DrugExposureMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Observation Short Term', 'OBSERVATION', 'One covariate per observation in the observation table in the short term window.', null, 'ObservationShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Era Start Long Term', 'DRUG', 'One covariate per drug in the drug_era table starting in the long term window.', null, 'DrugEraStartLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Dcsi', 'CONDITION', 'The Diabetes Comorbidity Severity Index (DCSI) using all conditions prior to the window end.', null, 'Dcsi', true, 'DISTRIBUTION');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Era Start Short Term', 'DRUG', 'One covariate per drug in the drug_era table starting in the long short window.', null, 'DrugEraStartShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Ingredient Count Medium Term', 'DRUG', 'The number of distinct ingredients observed in the medium term window.', null, 'DistinctIngredientCountMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Any Time Prior', 'MEASUREMENT', 'One covariate per measurement in the measurement table any time prior to index.', null, 'MeasurementAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Medium Term', 'MEASUREMENT', 'One covariate per measurement in the measurement table in the medium term window.', null, 'MeasurementMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Condition Count Long Term', 'CONDITION', 'The number of distinct condition concepts observed in the long term window.', null, 'DistinctConditionCountLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Value Long Term', NULL, 'One covariate containing the value per measurement-unit combination in the long term window.', null, 'MeasurementValueLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Era Short Term', 'DRUG', 'One covariate per drug in the drug_era table overlapping with any part of the short window.', null, 'DrugEraShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Era Overlapping', 'DRUG', 'One covariate per drug in the drug_era table overlapping with the end of the risk window.', null, 'DrugEraOverlapping', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Observation Any Time Prior', 'OBSERVATION', 'One covariate per observation in the observation table any time prior to index.', null, 'ObservationAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Ingredient Count Long Term', 'DRUG', 'The number of distinct ingredients observed in the long term window.', null, 'DistinctIngredientCountLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Procedure Count Short Term', 'PROCEDURE', 'The number of distinct procedures observed in the short term window.', null, 'DistinctProcedureCountShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Condition Count Short Term', 'CONDITION', 'The number of distinct condition concepts observed in the short term window.', null, 'DistinctConditionCountShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Charlson Index', 'CONDITION', 'The Charlson comorbidity index (Romano adaptation) using all conditions prior to the window end.', null, 'CharlsonIndex', true, 'DISTRIBUTION');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Short Term', 'MEASUREMENT', 'One covariate per measurement in the measurement table in the short term window.', null, 'MeasurementShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Procedure Count Medium Term', 'PROCEDURE', 'The number of distinct procedures observed in the medium term window.', null, 'DistinctProcedureCountMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Device Exposure Any Time Prior', 'DEVICE', 'One covariate per device in the device exposure table starting any time prior to index.', null, 'DeviceExposureAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Observation Long Term', 'OBSERVATION', 'One covariate per observation in the observation table in the long term window.', null, 'ObservationLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Condition Count Medium Term', 'CONDITION', 'The number of distinct condition concepts observed in the medium term window.', null, 'DistinctConditionCountMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Procedure Occurrence Short Term', 'PROCEDURE', 'One covariate per procedure in the procedure_occurrence table in the short term window.', null, 'ProcedureOccurrenceShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Observation Medium Term', 'OBSERVATION', 'One covariate per observation in the observation table in the medium term window.', null, 'ObservationMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Device Exposure Long Term', 'DEVICE', 'One covariate per device in the device exposure table starting in the long term window.', null, 'DeviceExposureLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Value Short Term', NULL, 'One covariate containing the value per measurement-unit combination in the short term window.', null, 'MeasurementValueShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Device Exposure Medium Term', 'DEVICE', 'One covariate per device in the device exposure table starting in the medium term window.', null, 'DeviceExposureMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Long Term', 'MEASUREMENT', 'One covariate per measurement in the measurement table in the long term window.', null, 'MeasurementLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Value Medium Term', NULL, 'One covariate containing the value per measurement-unit combination in the medium term window.', null, 'MeasurementValueMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Era Start Medium Term', 'DRUG', 'One covariate per drug in the drug_era table starting in the medium term window.', null, 'DrugEraStartMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Measurement Value Any Time Prior', NULL, 'One covariate containing the value per measurement-unit combination any time prior to index.', null, 'MeasurementValueAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Ingredient Count Short Term', 'DRUG', 'The number of distinct ingredients observed in the short term window.', null, 'DistinctIngredientCountShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Device Exposure Short Term', 'DEVICE', 'One covariate per device in the device exposure table starting in the short term window.', null, 'DeviceExposureShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Procedure Count Long Term', 'PROCEDURE', 'The number of distinct procedures observed in the long term window.', null, 'DistinctProcedureCountLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Occurrence Long Term', 'CONDITION', 'One covariate per condition in the condition_occurrence table starting in the long term window.', null, 'ConditionOccurrenceLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Demographics Index Month', 'DEMOGRAPHICS', 'Month of the index date.', null, 'DemographicsIndexMonth', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Occurrence Any Time Prior', 'CONDITION', 'One covariate per condition in the condition_occurrence table starting any time prior to index.', null, 'ConditionOccurrenceAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Demographics Ethnicity', 'DEMOGRAPHICS', 'Ethnicity of the subject.', null, 'DemographicsEthnicity', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Demographics Age Group', 'DEMOGRAPHICS', 'Age of the subject on the index date (in 5 year age groups)', null, 'DemographicsAgeGroup', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Demographics Race', 'DEMOGRAPHICS', 'Race of the subject.', null, 'DemographicsRace', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Demographics Prior Observation Time', 'DEMOGRAPHICS', 'Number of continuous days of observation time preceding the index date.', null, 'DemographicsPriorObservationTime', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Demographics Gender', 'DEMOGRAPHICS', 'Gender of the subject.', null, 'DemographicsGender', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Demographics Index Year Month', 'DEMOGRAPHICS', 'Both calendar year and month of the index date in a single variable.', null, 'DemographicsIndexYearMonth', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Occurrence Medium Term', 'CONDITION', 'One covariate per condition in the condition_occurrence table starting in the medium term window.', null, 'ConditionOccurrenceMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Demographics Age', 'DEMOGRAPHICS', 'Age of the subject on the index date (in years).', null, 'DemographicsAge', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Chads 2', 'CONDITION', 'The CHADS2 score using all conditions prior to the window end.', null, 'Chads2', true, 'DISTRIBUTION');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Demographics Time In Cohort', 'DEMOGRAPHICS', 'Number of days of observation time during cohort period.', null, 'DemographicsTimeInCohort', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Demographics Index Year', 'DEMOGRAPHICS', 'Year of the index date.', null, 'DemographicsIndexYear', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Demographics Post Observation Time', 'DEMOGRAPHICS', 'Number of continuous days of observation time following the index date.', null, 'DemographicsPostObservationTime', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Chads 2 Vasc', 'CONDITION', 'The CHADS2VASc score using all conditions prior to the window end.', null, 'Chads2Vasc', true, 'DISTRIBUTION');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Occurrence Primary Inpatient Long Term', 'CONDITION', 'One covariate per condition observed as a primary diagnosis in an inpatient setting in the condition_occurrence table starting in the long term window.', null, 'ConditionOccurrencePrimaryInpatientLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Procedure Occurrence Long Term', 'PROCEDURE', 'One covariate per procedure in the procedure_occurrence table in the long term window.', null, 'ProcedureOccurrenceLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Occurrence Primary Inpatient Any Time Prior', 'CONDITION', 'One covariate per condition observed as a primary diagnosis in an inpatient setting in the condition_occurrence table starting any time prior to index.', null, 'ConditionOccurrencePrimaryInpatientAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Era Long Term', 'DRUG', 'One covariate per drug in the drug_era table overlapping with any part of the long term window.', null, 'DrugEraLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Procedure Occurrence Any Time Prior', 'PROCEDURE', 'One covariate per procedure in the procedure_occurrence table any time prior to index.', null, 'ProcedureOccurrenceAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Era Medium Term', 'DRUG', 'One covariate per drug in the drug_era table overlapping with any part of the medium term window.', null, 'DrugEraMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Drug Era Any Time Prior', 'DRUG', 'One covariate per drug in the drug_era table overlapping with any time prior to index.', null, 'DrugEraAnyTimePrior', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Condition Occurrence Short Term', 'CONDITION', 'One covariate per condition in the condition_occurrence table starting in the short term window.', null, 'ConditionOccurrenceShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Measurement Count Short Term', 'MEASUREMENT', 'The number of distinct measurements observed in the short term window.', null, 'DistinctMeasurementCountShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Visit Concept Count Short Term', 'VISIT', 'The number of visits observed in the short term window, stratified by visit concept ID.', null, 'VisitConceptCountShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Observation Count Medium Term', 'OBSERVATION', 'The number of distinct observations observed in the medium term window.', null, 'DistinctObservationCountMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Observation Count Long Term', 'OBSERVATION', 'The number of distinct observations observed in the long term window.', null, 'DistinctObservationCountLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Measurement Count Long Term', 'MEASUREMENT', 'The number of distinct measurements observed in the long term window.', null, 'DistinctMeasurementCountLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Measurement Count Medium Term', 'MEASUREMENT', 'The number of distinct measurements observed in the medium term window.', null, 'DistinctMeasurementCountMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Distinct Observation Count Short Term', 'OBSERVATION', 'The number of distinct observations observed in the short term window.', null, 'DistinctObservationCountShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Visit Concept Count Long Term', 'VISIT', 'The number of visits observed in the long term window, stratified by visit concept ID.', null, 'VisitConceptCountLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Visit Concept Count Medium Term', 'VISIT', 'The number of visits observed in the medium term window, stratified by visit concept ID.', null, 'VisitConceptCountMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Visit Count Medium Term', 'VISIT', 'The number of visits observed in the medium term window.', null, 'VisitCountMediumTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Visit Count Short Term', 'VISIT', 'The number of visits observed in the short term window.', null, 'VisitCountShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Occurrence Primary Inpatient Short Term', 'CONDITION', 'One covariate per condition observed  as a primary diagnosis in an inpatient setting in the condition_occurrence table starting in the short term window.', null, 'ConditionOccurrencePrimaryInpatientShortTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Visit Count Long Term', 'VISIT', 'The number of visits observed in the long term window.', null, 'VisitCountLongTerm', true, 'PREVALENCE');
INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type) VALUES ('PRESET', 'Occurrence Primary Inpatient Medium Term', 'CONDITION', 'One covariate per condition observed  as a primary diagnosis in an inpatient setting in the condition_occurrence table starting in the medium term window.', null, 'ConditionOccurrencePrimaryInpatientMediumTerm', true, 'PREVALENCE');

CREATE TABLE ${ohdsiSchema}.analysis_generation_info (
  job_execution_id INTEGER NOT NULL,
  design VARCHAR NOT NULL,
  hash_code VARCHAR NOT NULL,
  created_by_id INTEGER
);

ALTER TABLE ${ohdsiSchema}.analysis_generation_info
  ADD CONSTRAINT fk_cgi_sec_user FOREIGN KEY (created_by_id)
REFERENCES ${ohdsiSchema}.sec_user(id)
ON UPDATE NO ACTION ON DELETE NO ACTION;