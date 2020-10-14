CREATE SEQUENCE ${ohdsiSchema}.fe_aggregate_sequence;

CREATE TABLE ${ohdsiSchema}.fe_analysis_aggregate(
  id INTEGER NOT NULL DEFAULT nextval('${ohdsiSchema}.fe_aggregate_sequence'),
  name VARCHAR NOT NULL,
  domain VARCHAR,
  agg_function VARCHAR,
  expression VARCHAR,
  agg_query VARCHAR,
  is_default BOOLEAN DEFAULT FALSE,
  CONSTRAINT pk_fe_aggregate PRIMARY KEY(id)
);

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD fe_aggregate_id INTEGER;

INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(name, domain, agg_function, expression, agg_query, is_default) VALUES
  ('Events count', null, 'COUNT', '*', null, TRUE),
  ('Distinct start dates', null, 'COUNT', 'DISTINCT v.start_date', null, FALSE),
  ('Duration', null, null, 'datediff(day, v.start_date, v.end_date)', null, FALSE),
  ('Duration (max)', null, 'MAX', 'datediff(day, v.start_date, v.end_date)', null, FALSE),
  ('Duration (min)', null, 'MIN', 'datediff(day, v.start_date, v.end_date)', null, FALSE),
  ('Duration (average)', null, 'AVG', 'datediff(day, v.start_date, v.end_date)', null, FALSE),
  ('Value as number', 'MEASUREMENT', null, 'm.value_as_number', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', FALSE),
  ('Value as number (max)', 'MEASUREMENT', 'MAX', 'm.value_as_number', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', FALSE),
  ('Value as number (min)', 'MEASUREMENT', 'MIN', 'm.value_as_number', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', FALSE),
  ('Value as number (average)', 'MEASUREMENT', 'AVG', 'm.value_as_number', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', FALSE),
  ('Range high', 'MEASUREMENT', null, 'm.range_high', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', FALSE),
  ('Range high (max)', 'MEASUREMENT', 'MAX', 'm.range_high', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', FALSE),
  ('Range high (min)', 'MEASUREMENT', 'MIN', 'm.range_high', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', FALSE),
  ('Range high (average)', 'MEASUREMENT', 'AVG', 'm.range_high', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', FALSE),
  ('Range low', 'MEASUREMENT', null, 'm.range_low', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', FALSE),
  ('Range low (max)', 'MEASUREMENT', 'MAX', 'm.range_low', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', FALSE),
  ('Range low (min)', 'MEASUREMENT', 'MIN', 'm.range_low', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', FALSE),
  ('Range low (average)', 'MEASUREMENT', 'AVG', 'm.range_low', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', FALSE),
  ('Visit counts per person', 'VISIT', 'COUNT', '*', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', FALSE),
  ('Visit duration per person (average)', 'VISIT', 'AVG', 'CASE WHEN vo.visit_start_date = vo.visit_end_date THEN 1 ELSE DATEDIFF(day, vo.visit_start_date, vo.visit_end_date) END', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', FALSE),
  ('Visit duration per person (max)', 'VISIT', 'MAX', 'CASE WHEN vo.visit_start_date = vo.visit_end_date THEN 1 ELSE DATEDIFF(day, vo.visit_start_date, vo.visit_end_date) END', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', FALSE),
  ('Visit duration per person (min)', 'VISIT', 'MIN', 'CASE WHEN vo.visit_start_date = vo.visit_end_date THEN 1 ELSE DATEDIFF(day, vo.visit_start_date, vo.visit_end_date) END', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', FALSE),
  ('Visit duration per person', 'VISIT', null, 'CASE WHEN vo.visit_start_date = vo.visit_end_date THEN 1 ELSE DATEDIFF(day, vo.visit_start_date, vo.visit_end_date) END', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', FALSE),
  ('Number of Outpatient visits per person', 'VISIT', 'COUNT', '*', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date and vo.visit_concept_id = 9202', FALSE),
  ('Number of Inpatient visits per person', 'VISIT', 'COUNT', '*', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date and vo.visit_concept_id = 9201', FALSE),
  ('Distinct drug concepts per person', 'DRUG', 'COUNT', 'DISTINCT drug_concept_id', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', FALSE),
  ('Refills', 'DRUG', null, 'de.refills', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', FALSE),
  ('Refills (max)', 'DRUG', 'MAX', 'de.refills', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', FALSE),
  ('Refills (min)', 'DRUG', 'MIN', 'de.refills', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', FALSE),
  ('Refills (average)', 'DRUG', 'AVG', 'de.refills', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', FALSE),
  ('Quantity', 'DRUG', null, 'de.quantity', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', FALSE),
  ('Quantity (max)', 'DRUG', 'MAX', 'de.quantity', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', FALSE),
  ('Quantity (min)', 'DRUG', 'MIN', 'de.quantity', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', FALSE),
  ('Quantity (average)', 'DRUG', 'AVG', 'de.quantity', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', FALSE),
  ('Days supply', 'DRUG', null, 'de.days_supply', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', FALSE),
  ('Days supply (max)', 'DRUG', 'MAX', 'de.days_supply', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', FALSE),
  ('Days supply (min)', 'DRUG', 'MIN', 'de.days_supply', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', FALSE),
  ('Days supply (average)', 'DRUG', 'AVG', 'de.days_supply', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', FALSE),
  ('Length of era', 'DRUG', null, 'CASE WHEN de.drug_era_start_date = de.drug_era_end_date THEN 1 ELSE DATEDIFF(day, de.drug_era_start_date, de.drug_era_end_date) END', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', FALSE),
  ('Length of era (max)', 'DRUG', 'MAX', 'CASE WHEN de.drug_era_start_date = de.drug_era_end_date THEN 1 ELSE DATEDIFF(day, de.drug_era_start_date, de.drug_era_end_date) END', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', FALSE),
  ('Length of era (min)', 'DRUG', 'MIN', 'CASE WHEN de.drug_era_start_date = de.drug_era_end_date THEN 1 ELSE DATEDIFF(day, de.drug_era_start_date, de.drug_era_end_date) END', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', FALSE),
  ('Length of era (average)', 'DRUG', 'AVG', 'CASE WHEN de.drug_era_start_date = de.drug_era_end_date THEN 1 ELSE DATEDIFF(day, de.drug_era_start_date, de.drug_era_end_date) END', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', FALSE),
  ('Drug exposure count', 'DRUG', null, 'de.drug_exposure_count', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', FALSE),
  ('Drug exposure count (max)', 'DRUG', 'MAX', 'de.drug_exposure_count', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', FALSE),
  ('Drug exposure count (min)', 'DRUG', 'MIN', 'de.drug_exposure_count', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', FALSE),
  ('Drug exposure count (average)', 'DRUG', 'AVG', 'de.drug_exposure_count', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', FALSE),
  ('Gap days', 'DRUG', null, 'de.gap_days', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', FALSE),
  ('Gap days (max)', 'DRUG', 'MAX', 'de.gap_days', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', FALSE),
  ('Gap days (min)', 'DRUG', 'MIN', 'de.gap_days', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', FALSE),
  ('Gap days (average)', 'DRUG', 'AVG', 'de.gap_days', 'join drug_era de on de.person_id = v.person_id and de.drug_era_start_date >= E.start_date and de.drug_era_end_date <= E.end_date', FALSE),
  ('Length of era', 'CONDITION', null, 'CASE WHEN ce.condition_era_start_date = ce.condition_era_end_date THEN 1 ELSE DATEDIFF(day, ce.condition_era_start_date, ce.condition_era_end_date) END', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', FALSE),
  ('Length of era (max)', 'CONDITION', 'MAX', 'CASE WHEN ce.condition_era_start_date = ce.condition_era_end_date THEN 1 ELSE DATEDIFF(day, ce.condition_era_start_date, ce.condition_era_end_date) END', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', FALSE),
  ('Length of era (min)', 'CONDITION', 'MIN', 'CASE WHEN ce.condition_era_start_date = ce.condition_era_end_date THEN 1 ELSE DATEDIFF(day, ce.condition_era_start_date, ce.condition_era_end_date) END', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', FALSE),
  ('Length of era (average)', 'CONDITION', 'AVG', 'CASE WHEN ce.condition_era_start_date = ce.condition_era_end_date THEN 1 ELSE DATEDIFF(day, ce.condition_era_start_date, ce.condition_era_end_date) END', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', FALSE),
  ('Condition occurrence count', 'CONDITION', null, 'ce.condition_occurrence_count', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', FALSE),
  ('Condition occurrence count (max)', 'CONDITION', 'MAX', 'ce.condition_occurrence_count', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', FALSE),
  ('Condition occurrence count (min)', 'CONDITION', 'MIN', 'ce.condition_occurrence_count', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', FALSE),
  ('Condition occurrence count (average)', 'CONDITION', 'AVG', 'ce.condition_occurrence_count', 'join condition_era ce on ce.person_id = v.person_id and ce.condition_era_start_date >= E.start_date and ce.condition_era_end_date <= E.end_date', FALSE),
  ('Value as number', 'OBSERVATION', null, 'o.value_as_number', 'join observation o on o.person_id = v.person_id and o.observation_date between E.start_date and E.end_date', FALSE),
  ('Value as number (max)', 'OBSERVATION', 'MAX', 'o.value_as_number', 'join observation o on o.person_id = v.person_id and o.observation_date between E.start_date and E.end_date', FALSE),
  ('Value as number (min)', 'OBSERVATION', 'MIN', 'o.value_as_number', 'join observation o on o.person_id = v.person_id and o.observation_date between E.start_date and E.end_date', FALSE),
  ('Value as number (average)', 'OBSERVATION', 'AVG', 'o.value_as_number', 'join observation o on o.person_id = v.person_id and o.observation_date between E.start_date and E.end_date', FALSE),
  ('Quantity', 'PROCEDURE', null, 'p.quantity', 'join procedure_occurrence  p on p.person_id = v.person_id and p.procedure_date between E.start_date and E.end_date', FALSE),
  ('Quantity (max)', 'PROCEDURE', 'MAX', 'p.quantity', 'join procedure_occurrence  p on p.person_id = v.person_id and p.procedure_date between E.start_date and E.end_date', FALSE),
  ('Quantity (min)', 'PROCEDURE', 'MIN', 'p.quantity', 'join procedure_occurrence  p on p.person_id = v.person_id and p.procedure_date between E.start_date and E.end_date', FALSE),
  ('Quantity (average)', 'PROCEDURE', 'AVG', 'p.quantity', 'join procedure_occurrence  p on p.person_id = v.person_id and p.procedure_date between E.start_date and E.end_date', FALSE);

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
    SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), 'feature-analysis:aggregates:get', 'List available aggregates for Feature Analyses';

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'feature-analysis:aggregates:get'
  ) AND sr.name IN ('Atlas users');