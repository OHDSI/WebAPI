CREATE SEQUENCE ${ohdsiSchema}.fe_aggregate_sequence;

CREATE TABLE ${ohdsiSchema}.fe_analysis_aggregate(
  id INTEGER NOT NULL DEFAULT nextval('${ohdsiSchema}.fe_aggregate_sequence'),
  name VARCHAR NOT NULL,
  domain VARCHAR,
  agg_function VARCHAR,
  criteria_columns VARCHAR,
  expression VARCHAR,
  join_table VARCHAR,
  join_type VARCHAR,
  join_condition VARCHAR,
  is_default BOOLEAN DEFAULT FALSE,
  missing_means_zero BOOLEAN DEFAULT FALSE,
  CONSTRAINT pk_fe_aggregate PRIMARY KEY(id)
);

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD fe_aggregate_id INTEGER;

INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  ('Events count', null, 'COUNT', null, '*', null, null, null, TRUE, TRUE),
  ('Distinct start dates', null, 'COUNT', 'START_DATE', 'DISTINCT v.start_date', null, null, null, FALSE, TRUE),
  ('Duration', null, null, 'DURATION', 'duration', null, null, null, FALSE, FALSE),
  ('Duration (max)', null, 'MAX', 'DURATION', 'duration', null, null, null, FALSE, FALSE),
  ('Duration (min)', null, 'MIN', 'DURATION', 'duration', null, null, null, FALSE, FALSE),
  ('Duration (average)', null, 'AVG', 'DURATION', 'duration', null, null, null, FALSE, FALSE),
  ('Value as number', 'MEASUREMENT', null, 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, FALSE, FALSE),
  ('Value as number (max)', 'MEASUREMENT', 'MAX', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, FALSE, FALSE),
  ('Value as number (min)', 'MEASUREMENT', 'MIN', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, FALSE, FALSE),
  ('Value as number (average)', 'MEASUREMENT', 'AVG', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, FALSE, FALSE),
  ('Range high', 'MEASUREMENT', null, 'RANGE_HIGH', 'range_high', null, null, null, FALSE, FALSE),
  ('Range high (max)', 'MEASUREMENT', 'MAX', 'RANGE_HIGH', 'range_high', null, null, null, FALSE, FALSE),
  ('Range high (min)', 'MEASUREMENT', 'MIN', 'RANGE_HIGH', 'range_high', null, null, null, FALSE, FALSE),
  ('Range high (average)', 'MEASUREMENT', 'AVG', 'RANGE_HIGH', 'range_high', null, null, null, FALSE, FALSE),
  ('Range low', 'MEASUREMENT', null, 'RANGE_LOW', 'range_low', null, null, null, FALSE, FALSE),
  ('Range low (max)', 'MEASUREMENT', 'MAX', 'RANGE_LOW', 'range_low', null, null, null, FALSE, FALSE),
  ('Range low (min)', 'MEASUREMENT', 'MIN', 'RANGE_LOW', 'range_low', null, null, null, FALSE, FALSE),
  ('Range low (average)', 'MEASUREMENT', 'AVG', 'RANGE_LOW', 'range_low', null, null, null, FALSE, FALSE),
  ('Refills', 'DRUG', null, 'REFILLS', 'refills', null, null, null, FALSE, FALSE),
  ('Refills (max)', 'DRUG', 'MAX', 'REFILLS', 'refills', null, null, null, FALSE, FALSE),
  ('Refills (min)', 'DRUG', 'MIN', 'REFILLS', 'refills', null, null, null, FALSE, FALSE),
  ('Refills (average)', 'DRUG', 'AVG', 'REFILLS', 'refills', null, null, null, FALSE, FALSE),
  ('Quantity', 'DRUG', null, 'QUANTITY', 'quantity', null, null, null, FALSE, FALSE),
  ('Quantity (max)', 'DRUG', 'MAX', 'QUANTITY', 'quantity', null, null, null, FALSE, FALSE),
  ('Quantity (min)', 'DRUG', 'MIN', 'QUANTITY', 'quantity', null, null, null, FALSE, FALSE),
  ('Quantity (average)', 'DRUG', 'AVG', 'QUANTITY', 'quantity', null, null, null, FALSE, FALSE),
  ('Days supply', 'DRUG', null, 'DAYS_SUPPLY', 'days_supply', null, null, null, FALSE, FALSE),
  ('Days supply (max)', 'DRUG', 'MAX', 'DAYS_SUPPLY', 'days_supply', null, null, null, FALSE, FALSE),
  ('Days supply (min)', 'DRUG', 'MIN', 'DAYS_SUPPLY', 'days_supply', null, null, null, FALSE, FALSE),
  ('Days supply (average)', 'DRUG', 'AVG', 'DAYS_SUPPLY', 'days_supply', null, null, null, FALSE, FALSE),
  ('Drug exposure count', 'DRUG_ERA', null, 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, FALSE, FALSE),
  ('Drug exposure count (max)', 'DRUG_ERA', 'MAX', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, FALSE, FALSE),
  ('Drug exposure count (min)', 'DRUG_ERA', 'MIN', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, FALSE, FALSE),
  ('Drug exposure count (average)', 'DRUG_ERA', 'AVG', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, FALSE, FALSE),
  ('Gap days', 'DRUG_ERA', null, 'GAP_DAYS', 'gap_days', null, null, null, FALSE, FALSE),
  ('Gap days (max)', 'DRUG_ERA', 'MAX', 'GAP_DAYS', 'gap_days', null, null, null, FALSE, FALSE),
  ('Gap days (min)', 'DRUG_ERA', 'MIN', 'GAP_DAYS', 'gap_days', null, null, null, FALSE, FALSE),
  ('Gap days (average)', 'DRUG_ERA', 'AVG', 'GAP_DAYS', 'gap_days', null, null, null, FALSE, FALSE),
  ('Condition occurrence count', 'CONDITION_ERA', null, 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, FALSE, FALSE),
  ('Condition occurrence count (max)', 'CONDITION_ERA', 'MAX', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, FALSE, FALSE),
  ('Condition occurrence count (min)', 'CONDITION_ERA', 'MIN', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, FALSE, FALSE),
  ('Condition occurrence count (average)', 'CONDITION_ERA', 'AVG', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, FALSE, FALSE),
  ('Value as number', 'OBSERVATION', null, 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, FALSE, FALSE),
  ('Value as number (max)', 'OBSERVATION', 'MAX', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, FALSE, FALSE),
  ('Value as number (min)', 'OBSERVATION', 'MIN', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, FALSE, FALSE),
  ('Value as number (average)', 'OBSERVATION', 'AVG', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, FALSE, FALSE),
  ('Quantity', 'PROCEDURE', null, 'QUANTITY', 'quantity', null, null, null, FALSE, FALSE),
  ('Quantity (max)', 'PROCEDURE', 'MAX', 'QUANTITY', 'quantity', null, null, null, FALSE, FALSE),
  ('Quantity (min)', 'PROCEDURE', 'MIN', 'QUANTITY', 'quantity', null, null, null, FALSE, FALSE),
  ('Quantity (average)', 'PROCEDURE', 'AVG', 'QUANTITY', 'quantity', null, null, null, FALSE, FALSE);

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

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD CONSTRAINT fk_criteria_aggregate
    FOREIGN KEY (fe_aggregate_id) REFERENCES fe_analysis_aggregate(id);