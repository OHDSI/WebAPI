INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type)
VALUES (
  'CUSTOM_FE',
  'Location by State during cohort period',
  'DEMOGRAPHICS',
  'One covariate per region_concept_id rolled up to State level in the location table during cohort period.',
  null,
  'SELECT
  CAST(C.concept_id AS BIGINT) * 1000 + 930 AS covariate_id,
  C.concept_name                            AS covariate_name,
  C.concept_id                              AS concept_id,
  COUNT(*)                                  AS sum_value,
  COUNT(*) * 1.0 / stat.total_cnt * 1.0     AS average_value
FROM (SELECT *
      FROM @cohort_table
      WHERE cohort_definition_id = @cohort_id) cohort
  JOIN @cdm_database_schema.location_history LH
    ON LH.start_date < cohort.cohort_end_date
       AND COALESCE(LH.end_date, CAST(''20991231'' AS DATE)) > cohort.cohort_start_date
       AND LH.domain_id = ''PERSON''
       AND LH.entity_id = cohort.subject_id
  JOIN @cdm_database_schema.location L ON L.location_id = LH.location_id
  JOIN @cdm_database_schema.concept_ancestor CA ON CA.descendant_concept_id = L.region_concept_id
  JOIN @cdm_database_schema.concept C ON C.concept_id = CA.ancestor_concept_id AND C.concept_class_id = ''4th level''
  CROSS JOIN (
               SELECT COUNT(*) total_cnt
               FROM @cohort_table
               WHERE cohort_definition_id = @cohort_id
             ) stat
GROUP BY C.concept_id, C.concept_name, stat.total_cnt',
  1,
  'PREVALENCE'
);

INSERT INTO ${ohdsiSchema}.fe_analysis (type, name, domain, descr, value, design, is_locked, stat_type)
VALUES (
  'CUSTOM_FE',
  'Location by State during 365d prior to cohort start date',
  'DEMOGRAPHICS',
  'One covariate per region_concept_id rolled up to State level in the location table in the 365d prior to cohort start date.',
  null,
  'SELECT
  CAST(C.concept_id AS BIGINT) * 1000 + 931 AS covariate_id,
  C.concept_name                            AS covariate_name,
  C.concept_id                              AS concept_id,
  COUNT(*)                                  AS sum_value,
  COUNT(*) * 1.0 / stat.total_cnt * 1.0     AS average_value
FROM (SELECT *
      FROM @cohort_table
      WHERE cohort_definition_id = @cohort_id) cohort
  JOIN @cdm_database_schema.location_history LH
    ON LH.start_date < cohort.cohort_start_date
       AND COALESCE(LH.end_date, CAST(''20991231'' AS DATE)) > dateadd(d, -365, cohort.cohort_start_date)
       AND LH.domain_id = ''PERSON''
       AND LH.entity_id = cohort.subject_id
  JOIN @cdm_database_schema.location L ON L.location_id = LH.location_id
  JOIN @cdm_database_schema.concept_ancestor CA ON CA.descendant_concept_id = L.region_concept_id
  JOIN @cdm_database_schema.concept C ON C.concept_id = CA.ancestor_concept_id AND C.concept_class_id = ''4th level''
  CROSS JOIN (
               SELECT COUNT(*) total_cnt
               FROM @cohort_table
               WHERE cohort_definition_id = @cohort_id
             ) stat
GROUP BY C.concept_id, C.concept_name, stat.total_cnt',
  1,
  'PREVALENCE'
);

