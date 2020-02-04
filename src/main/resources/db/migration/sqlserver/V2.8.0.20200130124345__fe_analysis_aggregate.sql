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

ALTER TABLE ${ohdsiSchema}.fe_analysis ADD fe_aggregate_id INTEGER;

INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(name, domain, agg_function, expression, agg_query, is_default) VALUES
  ('Events count', null, 'COUNT', '*', null, 1),
  ('Average measurement result per person', 'MEASUREMENT', 'AVG', 'm.value_as_number', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', 0),
  ('Visit counts per person', 'VISIT', 'COUNT', '*', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', 0),
  ('Average visit duration per person', 'VISIT', 'AVG', 'CASE WHEN vo.visit_start_date = vo.visit_end_date THEN 1 ELSE DATEDIFF(day, vo.visit_start_date, vo.visit_end_date) END', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', 0),
  ('Maximum visit duration per person', 'VISIT', 'MAX', 'CASE WHEN vo.visit_start_date = vo.visit_end_date THEN 1 ELSE DATEDIFF(day, vo.visit_start_date, vo.visit_end_date) END', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', 0),
  ('Visit duration per person', 'VISIT', null, 'CASE WHEN vo.visit_start_date = vo.visit_end_date THEN 1 ELSE DATEDIFF(day, vo.visit_start_date, vo.visit_end_date) END', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', 0),
  ('Number of Outpatient visits per person', 'VISIT', 'COUNT', '*', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date and vo.visit_concept_id = 9202', 0),
  ('Number of Inpatient visits per person', 'VISIT', 'COUNT', '*', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date and vo.visit_concept_id = 9201', 0),
  ('Distinct drug concepts per person', 'DRUG', 'COUNT', 'DISTINCT drug_concept_id', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0);

UPDATE
  ${ohdsiSchema}.fe_analysis
SET
  fe_aggregate_id = ag.id
FROM
  ${ohdsiSchema}.fe_analysis_aggregate ag
WHERE
  ag.name = 'Events count'
  AND [type] = 'CRITERIA_SET'
  AND stat_type = 'DISTRIBUTION';

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
    SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'feature-analysis:aggregates:get', 'List available aggregates for Feature Analyses';

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'feature-analysis:aggregates:get'
  ) AND sr.name IN ('Atlas users');