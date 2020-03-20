CREATE SEQUENCE ${ohdsiSchema}.fe_aggregate_sequence START WITH 1;

CREATE TABLE ${ohdsiSchema}.fe_analysis_aggregate(
  id INTEGER NOT NULL CONSTRAINT df_fe_analysis_aggregate DEFAULT NEXT VALUE FOR ${ohdsiSchema}.fe_aggregate_sequence,
  name VARCHAR(MAX) NOT NULL,
  domain VARCHAR(64),
  agg_function VARCHAR(MAX),
  expression VARCHAR(MAX),
  agg_query VARCHAR(MAX),
  is_default BIT DEFAULT 0,
  CONSTRAINT pk_fe_aggregate PRIMARY KEY(id)
);

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD fe_aggregate_id INTEGER;

INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(name, domain, agg_function, expression, agg_query, is_default) VALUES
  ('Events count', null, 'COUNT', '*', null, 1),
  ('Distinct start dates', null, 'COUNT', 'DISTINCT op.observation_period_start_date', 'join observation_period op on op.person_id = v.person_id and op.observation_period_start_date >= E.start_date and op.observation_period_end_date <= E.end_date', 0),
  ('Duration', null, null, 'CASE WHEN op.observation_period_start_date = op.observation_period_end_date THEN 1 ELSE DATEDIFF(day, op.observation_period_start_date, op.observation_period_end_date) END', 'join observation_period op on op.person_id = v.person_id and op.observation_period_start_date >= E.start_date and op.observation_period_end_date <= E.end_date', 0),
  ('Duration (max)', null, 'MAX', 'CASE WHEN op.observation_period_start_date = op.observation_period_end_date THEN 1 ELSE DATEDIFF(day, op.observation_period_start_date, op.observation_period_end_date) END', 'join observation_period op on op.person_id = v.person_id and op.observation_period_start_date >= E.start_date and op.observation_period_end_date <= E.end_date', 0),
  ('Duration (min)', null, 'MIN', 'CASE WHEN op.observation_period_start_date = op.observation_period_end_date THEN 1 ELSE DATEDIFF(day, op.observation_period_start_date, op.observation_period_end_date) END', 'join observation_period op on op.person_id = v.person_id and op.observation_period_start_date >= E.start_date and op.observation_period_end_date <= E.end_date', 0),
  ('Duration (average)', null, 'AVG', 'CASE WHEN op.observation_period_start_date = op.observation_period_end_date THEN 1 ELSE DATEDIFF(day, op.observation_period_start_date, op.observation_period_end_date) END', 'join observation_period op on op.person_id = v.person_id and op.observation_period_start_date >= E.start_date and op.observation_period_end_date <= E.end_date', 0),
  ('Value as number', 'MEASUREMENT', null, 'm.value_as_number', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', 0),
  ('Value as number (max)', 'MEASUREMENT', 'MAX', 'm.value_as_number', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', 0),
  ('Value as number (min)', 'MEASUREMENT', 'MIN', 'm.value_as_number', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', 0),
  ('Value as number (average)', 'MEASUREMENT', 'AVG', 'm.value_as_number', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', 0),
  ('Range high', 'MEASUREMENT', null, 'm.range_high', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', 0),
  ('Range high (max)', 'MEASUREMENT', 'MAX', 'm.range_high', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', 0),
  ('Range high (min)', 'MEASUREMENT', 'MIN', 'm.range_high', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', 0),
  ('Range high (average)', 'MEASUREMENT', 'AVG', 'm.range_high', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', 0),
  ('Range low', 'MEASUREMENT', null, 'm.range_low', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', 0),
  ('Range low (max)', 'MEASUREMENT', 'MAX', 'm.range_low', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', 0),
  ('Range low (min)', 'MEASUREMENT', 'MIN', 'm.range_low', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', 0),
  ('Range low (average)', 'MEASUREMENT', 'AVG', 'm.range_low', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', 0),
  ('Visit counts per person', 'VISIT', 'COUNT', '*', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', 0),
  ('Visit duration per person (average)', 'VISIT', 'AVG', 'CASE WHEN vo.visit_start_date = vo.visit_end_date THEN 1 ELSE DATEDIFF(day, vo.visit_start_date, vo.visit_end_date) END', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', 0),
  ('Visit duration per person (max)', 'VISIT', 'MAX', 'CASE WHEN vo.visit_start_date = vo.visit_end_date THEN 1 ELSE DATEDIFF(day, vo.visit_start_date, vo.visit_end_date) END', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', 0),
  ('Visit duration per person (min)', 'VISIT', 'MIN', 'CASE WHEN vo.visit_start_date = vo.visit_end_date THEN 1 ELSE DATEDIFF(day, vo.visit_start_date, vo.visit_end_date) END', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', 0),
  ('Visit duration per person', 'VISIT', null, 'CASE WHEN vo.visit_start_date = vo.visit_end_date THEN 1 ELSE DATEDIFF(day, vo.visit_start_date, vo.visit_end_date) END', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', 0),
  ('Number of Outpatient visits per person', 'VISIT', 'COUNT', '*', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date and vo.visit_concept_id = 9202', 0),
  ('Number of Inpatient visits per person', 'VISIT', 'COUNT', '*', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date and vo.visit_concept_id = 9201', 0),
  ('Distinct drug concepts per person', 'DRUG', 'COUNT', 'DISTINCT drug_concept_id', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0),
  ('Refills', 'DRUG', null, 'de.refills', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0),
  ('Refills (max)', 'DRUG', 'MAX', 'de.refills', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0),
  ('Refills (min)', 'DRUG', 'MIN', 'de.refills', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0),
  ('Refills (average)', 'DRUG', 'AVG', 'de.refills', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0),
  ('Quantity', 'DRUG', null, 'de.quantity', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0),
  ('Quantity (max)', 'DRUG', 'MAX', 'de.quantity', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0),
  ('Quantity (min)', 'DRUG', 'MIN', 'de.quantity', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0),
  ('Quantity (average)', 'DRUG', 'AVG', 'de.quantity', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0),
  ('Days supply', 'DRUG', null, 'de.days_supply', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0),
  ('Days supply (max)', 'DRUG', 'MAX', 'de.days_supply', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0),
  ('Days supply (min)', 'DRUG', 'MIN', 'de.days_supply', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0),
  ('Days supply (average)', 'DRUG', 'AVG', 'de.days_supply', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0),
  ('Length of era', 'DRUG', null, 'CASE WHEN de.drug_era_start_date = de.drug_era_end_date THEN 1 ELSE DATEDIFF(day, de.drug_era_start_date, de.drug_era_end_date) END', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', 0),
  ('Length of era (max)', 'DRUG', 'MAX', 'CASE WHEN de.drug_era_start_date = de.drug_era_end_date THEN 1 ELSE DATEDIFF(day, de.drug_era_start_date, de.drug_era_end_date) END', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', 0),
  ('Length of era (min)', 'DRUG', 'MIN', 'CASE WHEN de.drug_era_start_date = de.drug_era_end_date THEN 1 ELSE DATEDIFF(day, de.drug_era_start_date, de.drug_era_end_date) END', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', 0),
  ('Length of era (average)', 'DRUG', 'AVG', 'CASE WHEN de.drug_era_start_date = de.drug_era_end_date THEN 1 ELSE DATEDIFF(day, de.drug_era_start_date, de.drug_era_end_date) END', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', 0),
  ('Drug exposure count', 'DRUG', null, 'de.drug_exposure_count', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', 0),
  ('Drug exposure count (max)', 'DRUG', 'MAX', 'de.drug_exposure_count', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', 0),
  ('Drug exposure count (min)', 'DRUG', 'MIN', 'de.drug_exposure_count', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', 0),
  ('Drug exposure count (average)', 'DRUG', 'AVG', 'de.drug_exposure_count', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', 0),
  ('Gap days', 'DRUG', null, 'de.gap_days', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', 0),
  ('Gap days (max)', 'DRUG', 'MAX', 'de.gap_days', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', 0),
  ('Gap days (min)', 'DRUG', 'MIN', 'de.gap_days', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', 0),
  ('Gap days (average)', 'DRUG', 'AVG', 'de.gap_days', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', 0),
  ('Length of era', 'CONDITION', null, 'CASE WHEN ce.condition_era_start_date = ce.condition_era_end_date THEN 1 ELSE DATEDIFF(day, ce.condition_era_start_date, ce.condition_era_end_date) END', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', 0),
  ('Length of era (max)', 'CONDITION', 'MAX', 'CASE WHEN ce.condition_era_start_date = ce.condition_era_end_date THEN 1 ELSE DATEDIFF(day, ce.condition_era_start_date, ce.condition_era_end_date) END', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', 0),
  ('Length of era (min)', 'CONDITION', 'MIN', 'CASE WHEN ce.condition_era_start_date = ce.condition_era_end_date THEN 1 ELSE DATEDIFF(day, ce.condition_era_start_date, ce.condition_era_end_date) END', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', 0),
  ('Length of era (average)', 'CONDITION', 'AVG', 'CASE WHEN ce.condition_era_start_date = ce.condition_era_end_date THEN 1 ELSE DATEDIFF(day, ce.condition_era_start_date, ce.condition_era_end_date) END', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', 0),
  ('Condition occurrence count', 'CONDITION', null, 'ce.condition_occurrence_count', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', 0),
  ('Condition occurrence count (max)', 'CONDITION', 'MAX', 'ce.condition_occurrence_count', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', 0),
  ('Condition occurrence count (min)', 'CONDITION', 'MIN', 'ce.condition_occurrence_count', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', 0),
  ('Condition occurrence count (average)', 'CONDITION', 'AVG', 'ce.condition_occurrence_count', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', 0),
  ('Value as number', 'OBSERVATION', null, 'o.value_as_number', 'join observation o on o.person_id = v.person_id and o.observation_date between E.start_date and E.end_date', 0),
  ('Value as number (max)', 'OBSERVATION', 'MAX', 'o.value_as_number', 'join observation o on o.person_id = v.person_id and o.observation_date between E.start_date and E.end_date', 0),
  ('Value as number (min)', 'OBSERVATION', 'MIN', 'o.value_as_number', 'join observation o on o.person_id = v.person_id and o.observation_date between E.start_date and E.end_date', 0),
  ('Value as number (average)', 'OBSERVATION', 'AVG', 'o.value_as_number', 'join observation o on o.person_id = v.person_id and o.observation_date between E.start_date and E.end_date', 0),
  ('Quantity', 'PROCEDURE', null, 'p.quantity', 'join procedure_occurrence  p on p.person_id = v.person_id and p.procedure_date between E.start_date and E.end_date', 0),
  ('Quantity (max)', 'PROCEDURE', 'MAX', 'p.quantity', 'join procedure_occurrence  p on p.person_id = v.person_id and p.procedure_date between E.start_date and E.end_date', 0),
  ('Quantity (min)', 'PROCEDURE', 'MIN', 'p.quantity', 'join procedure_occurrence  p on p.person_id = v.person_id and p.procedure_date between E.start_date and E.end_date', 0),
  ('Quantity (average)', 'PROCEDURE', 'AVG', 'p.quantity', 'join procedure_occurrence  p on p.person_id = v.person_id and p.procedure_date between E.start_date and E.end_date', 0);

UPDATE
  ${ohdsiSchema}.fe_analysis_criteria
SET
  fe_aggregate_id = ag.id
FROM
  ${ohdsiSchema}.fe_analysis_criteria feac JOIN
  ${ohdsiSchema}.fe_analysis fea ON fea.id = feac.fe_analysis_id,
  ${ohdsiSchema}.fe_analysis_aggregate ag
WHERE
  ag.name = 'Events count'
  AND fea.type = 'CRITERIA_SET'
  AND fea.stat_type = 'DISTRIBUTION';

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
    SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'feature-analysis:aggregates:get', 'List available aggregates for Feature Analyses';

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'feature-analysis:aggregates:get'
  ) AND sr.name IN ('Atlas users');