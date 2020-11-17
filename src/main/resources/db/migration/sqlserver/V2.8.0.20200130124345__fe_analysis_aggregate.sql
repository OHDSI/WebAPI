CREATE SEQUENCE ${ohdsiSchema}.fe_aggregate_sequence START WITH 1;

CREATE TABLE ${ohdsiSchema}.fe_analysis_aggregate(
  id INTEGER NOT NULL DEFAULT NEXT VALUE FOR ${ohdsiSchema}.fe_aggregate_sequence,
  name VARCHAR(255) NOT NULL,
  domain VARCHAR(255),
  agg_function VARCHAR(255),
  criteria_columns VARCHAR(MAX),
  expression VARCHAR(MAX),
  join_table VARCHAR(255),
  join_type VARCHAR(255),
  join_condition VARCHAR(MAX),
  is_default BIT DEFAULT 0,
  missing_means_zero BIT DEFAULT 0,
  CONSTRAINT pk_fe_aggregate PRIMARY KEY(id)
);

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD fe_aggregate_id INTEGER;

INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  ('Events count', null, 'COUNT', null, '*', null, null, null, 1, 1),
  ('Distinct start dates', null, 'COUNT', 'START_DATE', 'DISTINCT v.start_date', null, null, null, 0, 1),
  ('Duration', null, null, 'DURATION', 'duration', null, null, null, 0, 0),
  ('Duration (max)', null, 'MAX', 'DURATION', 'duration', null, null, null, 0, 0),
  ('Duration (min)', null, 'MIN', 'DURATION', 'duration', null, null, null, 0, 0),
  ('Duration (average)', null, 'AVG', 'DURATION', 'duration', null, null, null, 0, 0),
  ('Value as number', 'MEASUREMENT', null, 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0),
  ('Value as number (max)', 'MEASUREMENT', 'MAX', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0),
  ('Value as number (min)', 'MEASUREMENT', 'MIN', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0),
  ('Value as number (average)', 'MEASUREMENT', 'AVG', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0),
  ('Range high', 'MEASUREMENT', null, 'RANGE_HIGH', 'range_high', null, null, null, 0, 0),
  ('Range high (max)', 'MEASUREMENT', 'MAX', 'RANGE_HIGH', 'range_high', null, null, null, 0, 0),
  ('Range high (min)', 'MEASUREMENT', 'MIN', 'RANGE_HIGH', 'range_high', null, null, null, 0, 0),
  ('Range high (average)', 'MEASUREMENT', 'AVG', 'RANGE_HIGH', 'range_high', null, null, null, 0, 0),
  ('Range low', 'MEASUREMENT', null, 'RANGE_LOW', 'range_low', null, null, null, 0, 0),
  ('Range low (max)', 'MEASUREMENT', 'MAX', 'RANGE_LOW', 'range_low', null, null, null, 0, 0),
  ('Range low (min)', 'MEASUREMENT', 'MIN', 'RANGE_LOW', 'range_low', null, null, null, 0, 0),
  ('Range low (average)', 'MEASUREMENT', 'AVG', 'RANGE_LOW', 'range_low', null, null, null, 0, 0),
  ('Distinct drug concepts per person', 'DRUG', 'COUNT', 'DOMAIN_CONCEPT', 'DISTINCT domain_concept', null, null, null, 0, 0),
  ('Refills', 'DRUG', null, 'REFILLS', 'refills', null, null, null, 0, 0),
  ('Refills (max)', 'DRUG', 'MAX', 'REFILLS', 'refills', null, null, null, 0, 0),
  ('Refills (min)', 'DRUG', 'MIN', 'REFILLS', 'refills', null, null, null, 0, 0),
  ('Refills (average)', 'DRUG', 'AVG', 'REFILLS', 'refills', null, null, null, 0, 0),
  ('Quantity', 'DRUG', null, 'QUANTITY', 'quantity', null, null, null, 0, 0),
  ('Quantity (max)', 'DRUG', 'MAX', 'QUANTITY', 'quantity', null, null, null, 0, 0),
  ('Quantity (min)', 'DRUG', 'MIN', 'QUANTITY', 'quantity', null, null, null, 0, 0),
  ('Quantity (average)', 'DRUG', 'AVG', 'QUANTITY', 'quantity', null, null, null, 0, 0),
  ('Days supply', 'DRUG', null, 'DAYS_SUPPLY', 'days_supply', null, null, null, 0, 0),
  ('Days supply (max)', 'DRUG', 'MAX', 'DAYS_SUPPLY', 'days_supply', null, null, null, 0, 0),
  ('Days supply (min)', 'DRUG', 'MIN', 'DAYS_SUPPLY', 'days_supply', null, null, null, 0, 0),
  ('Days supply (average)', 'DRUG', 'AVG', 'DAYS_SUPPLY', 'days_supply', null, null, null, 0, 0),
  ('Drug exposure count', 'DRUG_ERA', null, 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0),
  ('Drug exposure count (max)', 'DRUG_ERA', 'MAX', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0),
  ('Drug exposure count (min)', 'DRUG_ERA', 'MIN', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0),
  ('Drug exposure count (average)', 'DRUG_ERA', 'AVG', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0),
  ('Gap days', 'DRUG_ERA', null, 'GAP_DAYS', 'gap_days', null, null, null, 0, 0),
  ('Gap days (max)', 'DRUG_ERA', 'MAX', 'GAP_DAYS', 'gap_days', null, null, null, 0, 0),
  ('Gap days (min)', 'DRUG_ERA', 'MIN', 'GAP_DAYS', 'gap_days', null, null, null, 0, 0),
  ('Gap days (average)', 'DRUG_ERA', 'AVG', 'GAP_DAYS', 'gap_days', null, null, null, 0, 0),
  ('Condition occurrence count', 'CONDITION_ERA', null, 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0),
  ('Condition occurrence count (max)', 'CONDITION_ERA', 'MAX', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0),
  ('Condition occurrence count (min)', 'CONDITION_ERA', 'MIN', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0),
  ('Condition occurrence count (average)', 'CONDITION_ERA', 'AVG', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0),
  ('Value as number', 'OBSERVATION', null, 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0),
  ('Value as number (max)', 'OBSERVATION', 'MAX', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0),
  ('Value as number (min)', 'OBSERVATION', 'MIN', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0),
  ('Value as number (average)', 'OBSERVATION', 'AVG', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0),
  ('Quantity', 'PROCEDURE', null, 'QUANTITY', 'quantity', null, null, null, 0, 0),
  ('Quantity (max)', 'PROCEDURE', 'MAX', 'QUANTITY', 'quantity', null, null, null, 0, 0),
  ('Quantity (min)', 'PROCEDURE', 'MIN', 'QUANTITY', 'quantity', null, null, null, 0, 0),
  ('Quantity (average)', 'PROCEDURE', 'AVG', 'QUANTITY', 'quantity', null, null, null, 0, 0);

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

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD CONSTRAINT fk_criteria_aggregate
    FOREIGN KEY (fe_aggregate_id) REFERENCES ${ohdsiSchema}.fe_analysis_aggregate(id);