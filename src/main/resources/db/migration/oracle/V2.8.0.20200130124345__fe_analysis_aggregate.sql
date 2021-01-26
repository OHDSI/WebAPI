CREATE SEQUENCE ${ohdsiSchema}.fe_aggregate_sequence;

CREATE TABLE ${ohdsiSchema}.fe_analysis_aggregate(
  id INTEGER,
  name VARCHAR2(255) NOT NULL,
  domain VARCHAR2(255),
  agg_function VARCHAR2(255),
  criteria_columns VARCHAR2(255),
  expression CLOB,
  join_table VARCHAR2(255),
  join_type VARCHAR2(255),
  join_condition CLOB,
  is_default SMALLINT,
  missing_means_zero SMALLINT,
  CONSTRAINT pk_fe_aggregate PRIMARY KEY(id)
);

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD fe_aggregate_id INTEGER;

INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Events count', null, 'COUNT', null, '*', null, null, null, 1, 1);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Distinct start dates', null, 'COUNT', 'START_DATE', 'DISTINCT v.start_date', null, null, null, 0, 1);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Duration', null, null, 'DURATION', 'duration', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Duration (max)', null, 'MAX', 'DURATION', 'duration', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Duration (min)', null, 'MIN', 'DURATION', 'duration', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Duration (average)', null, 'AVG', 'DURATION', 'duration', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Value as number', 'MEASUREMENT', null, 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Value as number (max)', 'MEASUREMENT', 'MAX', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Value as number (min)', 'MEASUREMENT', 'MIN', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Value as number (average)', 'MEASUREMENT', 'AVG', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Range high', 'MEASUREMENT', null, 'RANGE_HIGH', 'range_high', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Range high (max)', 'MEASUREMENT', 'MAX', 'RANGE_HIGH', 'range_high', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Range high (min)', 'MEASUREMENT', 'MIN', 'RANGE_HIGH', 'range_high', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Range high (average)', 'MEASUREMENT', 'AVG', 'RANGE_HIGH', 'range_high', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Range low', 'MEASUREMENT', null, 'RANGE_LOW', 'range_low', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Range low (max)', 'MEASUREMENT', 'MAX', 'RANGE_LOW', 'range_low', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Range low (min)', 'MEASUREMENT', 'MIN', 'RANGE_LOW', 'range_low', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Range low (average)', 'MEASUREMENT', 'AVG', 'RANGE_LOW', 'range_low', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Distinct drug concepts per person', 'DRUG', 'COUNT', 'DOMAIN_CONCEPT', 'DISTINCT domain_concept', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Refills', 'DRUG', null, 'REFILLS', 'refills', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Refills (max)', 'DRUG', 'MAX', 'REFILLS', 'refills', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Refills (min)', 'DRUG', 'MIN', 'REFILLS', 'refills', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Refills (average)', 'DRUG', 'AVG', 'REFILLS', 'refills', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Quantity', 'DRUG', null, 'QUANTITY', 'quantity', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Quantity (max)', 'DRUG', 'MAX', 'QUANTITY', 'quantity', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Quantity (min)', 'DRUG', 'MIN', 'QUANTITY', 'quantity', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Quantity (average)', 'DRUG', 'AVG', 'QUANTITY', 'quantity', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Days supply', 'DRUG', null, 'DAYS_SUPPLY', 'days_supply', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Days supply (max)', 'DRUG', 'MAX', 'DAYS_SUPPLY', 'days_supply', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Days supply (min)', 'DRUG', 'MIN', 'DAYS_SUPPLY', 'days_supply', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Days supply (average)', 'DRUG', 'AVG', 'DAYS_SUPPLY', 'days_supply', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Drug exposure count', 'DRUG_ERA', null, 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Drug exposure count (max)', 'DRUG_ERA', 'MAX', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Drug exposure count (min)', 'DRUG_ERA', 'MIN', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Drug exposure count (average)', 'DRUG_ERA', 'AVG', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Gap days', 'DRUG_ERA', null, 'GAP_DAYS', 'gap_days', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Gap days (max)', 'DRUG_ERA', 'MAX', 'GAP_DAYS', 'gap_days', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Gap days (min)', 'DRUG_ERA', 'MIN', 'GAP_DAYS', 'gap_days', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Gap days (average)', 'DRUG_ERA', 'AVG', 'GAP_DAYS', 'gap_days', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Condition occurrence count', 'CONDITION_ERA', null, 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Condition occurrence count (max)', 'CONDITION_ERA', 'MAX', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Condition occurrence count (min)', 'CONDITION_ERA', 'MIN', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Condition occurrence count (average)', 'CONDITION_ERA', 'AVG', 'ERA_OCCURRENCES', 'era_occurrences', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Value as number', 'OBSERVATION', null, 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Value as number (max)', 'OBSERVATION', 'MAX', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Value as number (min)', 'OBSERVATION', 'MIN', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Value as number (average)', 'OBSERVATION', 'AVG', 'VALUE_AS_NUMBER', 'value_as_number', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Quantity', 'PROCEDURE', null, 'QUANTITY', 'quantity', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Quantity (max)', 'PROCEDURE', 'MAX', 'QUANTITY', 'quantity', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Quantity (min)', 'PROCEDURE', 'MIN', 'QUANTITY', 'quantity', null, null, null, 0, 0);
INSERT INTO ${ohdsiSchema}.fe_analysis_aggregate(id, name, domain, agg_function, criteria_columns, expression, join_table, join_type, join_condition, is_default, missing_means_zero) VALUES
  (${ohdsiSchema}.fe_aggregate_sequence.nextval, 'Quantity (average)', 'PROCEDURE', 'AVG', 'QUANTITY', 'quantity', null, null, null, 0, 0);

UPDATE
  ${ohdsiSchema}.fe_analysis_criteria
SET
  fe_aggregate_id = (SELECT ag.id
    FROM
      ${ohdsiSchema}.fe_analysis_criteria feac JOIN
      ${ohdsiSchema}.fe_analysis fea ON fea.id = feac.fe_analysis_id,
      ${ohdsiSchema}.fe_analysis_aggregate ag
    WHERE
      ag.name = 'Events count'
      AND fea.type = 'CRITERIA_SET'
      AND fea.stat_type = 'DISTRIBUTION'
    );

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
    SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval,
           'feature-analysis:aggregates:get',
           'List available aggregates for Feature Analyses' FROM DUAL;

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
'feature-analysis:aggregates:get'
) AND sr.name IN ('Atlas users');

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD CONSTRAINT fk_criteria_aggregate
    FOREIGN KEY (fe_aggregate_id) REFERENCES fe_analysis_aggregate(id);