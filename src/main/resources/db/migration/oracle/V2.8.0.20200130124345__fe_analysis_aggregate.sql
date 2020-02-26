CREATE SEQUENCE ${ohdsiSchema}.fe_aggregate_sequence START WITH 1;

CREATE TABLE ${ohdsiSchema}.fe_analysis_aggregate(
  id INTEGER NOT NULL,
  name VARCHAR(255) NOT NULL,
  domain VARCHAR(64),
  agg_function VARCHAR(64),
  expression CLOB,
  agg_query CLOB,
  is_default NUMBER(1) DEFAULT 0,
  CONSTRAINT pk_fe_aggregate PRIMARY KEY(id)
);

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD fe_aggregate_id INTEGER;

INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, expression, agg_query, is_default)
  VALUES(${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Events count', null, 'COUNT', '*', null, 1);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, expression, agg_query, is_default)
  VALUES(${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Average measurement result per person', 'MEASUREMENT', 'AVG', 'm.value_as_number', 'join measurement m on m.person_id = v.person_id and m.measurement_date between E.start_date and E.end_date', 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, expression, agg_query, is_default)
  VALUES(${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Visit counts per person', 'VISIT', 'COUNT', '*', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, expression, agg_query, is_default)
  VALUES(${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Average visit duration per person', 'VISIT', 'AVG', 'CASE WHEN vo.visit_start_date = vo.visit_end_date THEN 1 ELSE DATEDIFF(day, vo.visit_start_date, vo.visit_end_date) END', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, expression, agg_query, is_default)
  VALUES(${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Maximum visit duration per person', 'VISIT', 'MAX', 'CASE WHEN vo.visit_start_date = vo.visit_end_date THEN 1 ELSE DATEDIFF(day, vo.visit_start_date, vo.visit_end_date) END', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, expression, agg_query, is_default)
  VALUES(${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Visit duration per person', 'VISIT', null, 'CASE WHEN vo.visit_start_date = vo.visit_end_date THEN 1 ELSE DATEDIFF(day, vo.visit_start_date, vo.visit_end_date) END', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date', 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, expression, agg_query, is_default)
  VALUES(${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Number of Outpatient visits per person', 'VISIT', 'COUNT', '*', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date and vo.visit_concept_id = 9202', 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, expression, agg_query, is_default)
  VALUES(${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Number of Inpatient visits per person', 'VISIT', 'COUNT', '*', 'join visit_occurrence vo on vo.person_id = v.person_id and vo.visit_start_date >= E.start_date and vo.visit_end_date <= E.end_date and vo.visit_concept_id = 9201', 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, expression, agg_query, is_default)
  VALUES(${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Distinct drug concepts per person', 'DRUG', 'COUNT', 'DISTINCT drug_concept_id', 'join drug_exposure de on de.person_id = v.person_id and de.drug_exposure_start_date >= E.start_date and de.drug_exposure_end_date <= E.end_date', 0);

UPDATE (SELECT ag.id
FROM
  ${ohdsiSchema}.fe_analysis_criteria feac JOIN
  ${ohdsiSchema}.fe_analysis fea ON fea.id = feac.fe_analysis_id,
  ${ohdsiSchema}.fe_analysis_aggregate ag
WHERE
  ag.name = 'Events count'
  AND fea.type = 'CRITERIA_SET'
  AND fea.stat_type = 'DISTRIBUTION')
SET feac.fe_analysis_criteria = ag.id;

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
    SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'feature-analysis:aggregates:get', 'List available aggregates for Feature Analyses' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'feature-analysis:aggregates:get'
  ) AND sr.name IN ('Atlas users');