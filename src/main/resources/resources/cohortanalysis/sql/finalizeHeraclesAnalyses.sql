TRUNCATE TABLE #HERACLES_cohort_subject;
DROP TABLE #HERACLES_cohort_subject;

TRUNCATE TABLE #HERACLES_cohort;
DROP TABLE #HERACLES_cohort;

TRUNCATE TABLE #cohort_first;
DROP TABLE #cohort_first;

TRUNCATE TABLE #periods_baseline;
DROP TABLE #periods_baseline;

TRUNCATE TABLE #periods_atrisk;
DROP TABLE #periods_atrisk;

delete from @results_schema.heracles_results where count_value <= @smallcellcount and cohort_definition_id in (@cohort_definition_id);
delete from @results_schema.heracles_results_dist where count_value <= @smallcellcount and cohort_definition_id in (@cohort_definition_id);

-- cleanup dummy rows
delete from @results_schema.heracles_results where cohort_definition_id = -1;
delete from @results_schema.heracles_results_dist where cohort_definition_id = -1;

--{@refreshStats}?{
ANALYZE @results_schema.heracles_results;
ANALYZE @results_schema.heracles_results_dist;
--}

--{@runHERACLESHeel}?{
-- HERACLES_Heel part:

DELETE FROM @results_schema.HERACLES_HEEL_results where cohort_definition_id in (@cohort_definition_id);

-- check for non-zero counts from checks of improper data (invalid ids, out-of-bound data, inconsistent dates)
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT DISTINCT or1.cohort_definition_id, or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; count (n=', cast(or1.count_value as VARCHAR), ') should not be > 0') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
  WHERE or1.analysis_id IN (
    7,
    8,
    9,
    114,
    115,
    207,
    208,
    209,
    210,
    302,
    409,
    410,
    411,
    412,
    413,
    509,
    510,
    609,
    610,
    612,
    613,
    709,
    710,
    711,
    712,
    713,
    809,
    810,
    812,
    813,
    814,
    908,
    909,
    910,
    1008,
    1009,
    1010,
    1415,
    1500,
    1501,
    1600,
    1601,
    1701
  ) -- all explicit counts of data anamolies
        AND or1.count_value > 0
        and or1.cohort_definition_id in (@cohort_definition_id);

-- distributions where min should not be negative
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT DISTINCT ord1.cohort_definition_id, ord1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(ord1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; min (value=', cast(ord1.min_value as VARCHAR), ') should not be negative') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results_dist ord1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON ord1.analysis_id = oa1.analysis_id
  WHERE ord1.analysis_id IN (
    103,
    105,
    206,
    406,
    506,
    606,
    706,
    715,
    716,
    717,
    806,
    906,
    907,
    1006,
    1007,
    1502,
    1503,
    1504,
    1505,
    1506,
    1507,
    1508,
    1509,
    1510,
    1511,
    1602,
    1603,
    1604,
    1605,
    1606,
    1607,
    1608
  )
        AND ord1.min_value < 0
        and cohort_definition_id in (@cohort_definition_id);

--death distributions where max should not be positive
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT DISTINCT ord1.cohort_definition_id, ord1.analysis_id,
    CAST(CONCAT('WARNING: ', cast(ord1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; max (value=', cast(ord1.max_value as VARCHAR), ') should not be positive, otherwise its a zombie with data >1mo after death ') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results_dist ord1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON ord1.analysis_id = oa1.analysis_id
  WHERE ord1.analysis_id IN (
    511,
    512,
    513,
    514,
    515
  )
        AND ord1.max_value > 30
        and  cohort_definition_id in (@cohort_definition_id);

--invalid concept_id
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id, or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; ', cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR), ' concepts in data are not in vocabulary') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
    LEFT JOIN @CDM_schema.concept c1
      ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
  WHERE or1.analysis_id IN (
    2,
    4,
    5,
    200,
    301,
    400,
    500,
    505,
    600,
    700,
    800,
    900,
    1000,
    1609,
    1610
  )
        AND or1.stratum_1 IS NOT NULL
        AND c1.concept_id IS NULL
        and cohort_definition_id in (@cohort_definition_id)
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;

--invalid type concept_id
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; ', cast(COUNT_BIG(DISTINCT stratum_2) AS VARCHAR), ' concepts in data are not in vocabulary') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
    LEFT JOIN @CDM_schema.concept c1
      ON or1.stratum_2 = CAST(c1.concept_id AS VARCHAR)
  WHERE or1.analysis_id IN (
    405,
    605,
    705,
    805
  )
        and cohort_definition_id in (@cohort_definition_id)
        AND or1.stratum_2 IS NOT NULL
        AND c1.concept_id IS NULL
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;

--invalid concept_id
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('WARNING: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; data with unmapped concepts') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
  WHERE or1.analysis_id IN (
    2,
    4,
    5,
    200,
    301,
    400,
    500,
    505,
    600,
    700,
    800,
    900,
    1000,
    1609,
    1610
  )
        AND or1.stratum_1 = '0'
        and   cohort_definition_id in (@cohort_definition_id)
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;

--concept from the wrong vocabulary
--gender  - 12 HL7 -- TODO get the v5 version

INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; ', cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR), ' concepts in data are not in correct vocabulary (HL7 Sex)') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
    INNER JOIN @CDM_schema.concept c1
      ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
  WHERE or1.analysis_id IN (2)
        and cohort_definition_id in (@cohort_definition_id)
        AND or1.stratum_1 IS NOT NULL
        AND c1.vocabulary_id NOT IN (
    'Gender'
  )
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;


--race  - 13 CDC Race
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; ', cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR), ' concepts in data are not in correct vocabulary (CDC Race)') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
    INNER JOIN @CDM_schema.concept c1
      ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
  WHERE or1.analysis_id IN (4)
        and cohort_definition_id in (@cohort_definition_id)
        AND or1.stratum_1 IS NOT NULL
        AND c1.vocabulary_id NOT IN (
    'Race'
  )
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;

--ethnicity - 44 ethnicity
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; ', cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR), ' concepts in data are not in correct vocabulary (CMS Ethnicity)') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
    INNER JOIN @CDM_schema.concept c1
      ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
  WHERE or1.analysis_id IN (5)
        and cohort_definition_id in (@cohort_definition_id)
        AND or1.stratum_1 IS NOT NULL
        AND c1.vocabulary_id NOT IN (
    'Ethnicity'
  )
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;

--place of service - 14 CMS place of service, 24 OMOP visit
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; ', cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR), ' concepts in data are not in correct vocabulary (CMS place of service or OMOP visit)') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
    INNER JOIN @CDM_schema.concept c1
      ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
  WHERE or1.analysis_id IN (202)
        and cohort_definition_id in (@cohort_definition_id)
        AND or1.stratum_1 IS NOT NULL
        AND c1.vocabulary_id NOT IN (
    'Visit', 'Place of Service'
  )
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;

--specialty - 48 specialty
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; ', cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR), ' concepts in data are not in correct vocabulary (Specialty)') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
    INNER JOIN @CDM_schema.concept c1
      ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
  WHERE or1.analysis_id IN (301)
        and cohort_definition_id in (@cohort_definition_id)
        AND or1.stratum_1 IS NOT NULL
        AND c1.vocabulary_id NOT IN (
    'Specialty'
  )
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;

--condition occurrence, era - 1 SNOMED
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; ', cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR), ' concepts in data are not in correct vocabulary (SNOMED)') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
    INNER JOIN @CDM_schema.concept c1
      ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
  WHERE or1.analysis_id IN (
    400,
    1000
  )
        and cohort_definition_id in (@cohort_definition_id)
        AND or1.stratum_1 IS NOT NULL
        AND c1.vocabulary_id NOT IN (
    'SNOMED'
  )
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;

--drug exposure - 8 RxNorm
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; ', cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR), ' concepts in data are not in correct vocabulary (RxNorm)') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
    INNER JOIN @CDM_schema.concept c1
      ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
  WHERE or1.analysis_id IN (
    700,
    900
  )
        and cohort_definition_id in (@cohort_definition_id)
        AND or1.stratum_1 IS NOT NULL
        AND c1.vocabulary_id NOT IN (
    'RxNorm'
  )
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;

--procedure - 4 CPT4/5 HCPCS/3 ICD9P
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; ', cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR), ' concepts in data are not in correct vocabulary (CPT4/HCPCS/ICD9P)') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
    INNER JOIN @CDM_schema.concept c1
      ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
  WHERE or1.analysis_id IN (600)
        and or1.cohort_definition_id in (@cohort_definition_id)
        AND or1.stratum_1 IS NOT NULL
        AND c1.vocabulary_id NOT IN (
    'CPT4', 'HCPCS', 'ICD9Proc'
  )
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;

--observation  - 6 LOINC
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; ', cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR), ' concepts in data are not in correct vocabulary (LOINC)') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
    INNER JOIN @CDM_schema.concept c1
      ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
  WHERE or1.analysis_id IN (800)
        and or1.cohort_definition_id in (@cohort_definition_id)
        AND or1.stratum_1 IS NOT NULL
        AND c1.vocabulary_id NOT IN (
    'LOINC'
  )
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;


--disease class - 40 DRG
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; ', cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR), ' concepts in data are not in correct vocabulary (DRG)') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
    INNER JOIN @CDM_schema.concept c1
      ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
  WHERE or1.analysis_id IN (1609)
        and or1.cohort_definition_id in (@cohort_definition_id)
        AND or1.stratum_1 IS NOT NULL
        AND c1.vocabulary_id NOT IN (
    'DRG'
  )
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;

--revenue code - 43 revenue code
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; ', cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR), ' concepts in data are not in correct vocabulary (revenue code)') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
    INNER JOIN @CDM_schema.concept c1
      ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
  WHERE or1.analysis_id IN (1610)
        AND or1.stratum_1 IS NOT NULL
        and or1.cohort_definition_id in (@cohort_definition_id)
        AND c1.vocabulary_id NOT IN (
    'Revenue Code'
  )
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;


--ERROR:  year of birth in the future
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; should not have year of birth in the future, (n=', cast(sum(or1.count_value) as VARCHAR), ')') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
  WHERE or1.analysis_id IN (3)
        and or1.cohort_definition_id in (@cohort_definition_id)
        AND CAST((CASE WHEN (IsNumeric(or1.stratum_1) = 1) THEN or1.stratum_1 ELSE NULL END) AS INT) > year(getdate())
        AND or1.count_value > 0
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;


--WARNING:  year of birth < 1900
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; should not have year of birth < 1900, (n=', cast(sum(or1.count_value) as VARCHAR), ')') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
  WHERE or1.analysis_id IN (3)
        and cohort_definition_id in (@cohort_definition_id)
        AND CAST((CASE WHEN (IsNumeric(or1.stratum_1) = 1) THEN or1.stratum_1 ELSE NULL END) AS INT) < 1900
        AND or1.count_value > 0
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;

--ERROR:  age < 0
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; should not have age < 0, (n=', cast(sum(or1.count_value) as VARCHAR), ')') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
  WHERE or1.analysis_id IN (101)
        and cohort_definition_id in (@cohort_definition_id)
        AND CAST((CASE WHEN (IsNumeric(or1.stratum_1) = 1) THEN or1.stratum_1 ELSE NULL END) AS INT) < 0
        AND or1.count_value > 0
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;

--ERROR: age > 100
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT or1.cohort_definition_id,
    or1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(or1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; should not have age > 100, (n=', cast(sum(or1.count_value) as VARCHAR), ')') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results or1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON or1.analysis_id = oa1.analysis_id
  WHERE or1.analysis_id IN (101)
        and or1.cohort_definition_id in (@cohort_definition_id)
        AND CAST((CASE WHEN (IsNumeric(or1.stratum_1) = 1) THEN or1.stratum_1 ELSE NULL END) AS INT) > 100
        AND or1.count_value > 0
  GROUP BY or1.cohort_definition_id,
    or1.analysis_id,
    oa1.analysis_name;

--WARNING:  monthly change > 100%
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT DISTINCT her1.cohort_definition_id, her1.analysis_id,
    CAST(CONCAT('WARNING: ', cast(her1.analysis_id as VARCHAR), '-', aa1.analysis_name, '; theres a 100% change in monthly count of events') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_analysis aa1
    INNER JOIN (
                 SELECT
                   cohort_definition_id,
                   analysis_id,
                   CAST((CASE WHEN stratum_1 = ''
                     THEN NULL
                         ELSE stratum_1 END) AS INT) stratum_1,
                   stratum_2,
                   count_value
                 FROM @results_schema.heracles_results
                 WHERE analysis_id IN (
                   420,
                   620,
                   720,
                   820,
                   920,
                   1020
                 )
               ) her1
      ON aa1.analysis_id = her1.analysis_id
    INNER JOIN (
                 SELECT
                   cohort_definition_id,
                   analysis_id,
                   CAST((CASE WHEN stratum_1 = ''
                     THEN NULL
                         ELSE stratum_1 END) AS INT) stratum_1,
                   stratum_2,
                   count_value
                 FROM @results_schema.heracles_results
                 WHERE analysis_id IN (
                   420,
                   620,
                   720,
                   820,
                   920,
                   1020
                 )
               ) ar2
      ON her1.analysis_id = ar2.analysis_id
         and her1.cohort_definition_id = ar2.cohort_definition_id
  WHERE (
          CAST(her1.stratum_1 AS INT) + 1 = CAST(ar2.stratum_1 AS INT)
          OR CAST(her1.stratum_1 AS INT) + 89 = CAST(ar2.stratum_1 AS INT)
        )
        and her1.cohort_definition_id in (@cohort_definition_id)
        AND 1.0 * abs(ar2.count_value - her1.count_value) / her1.count_value > 1
        AND her1.count_value > 10;

--WARNING:  monthly change > 100% at concept level
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT her1.cohort_definition_id,
    her1.analysis_id,
    CAST(CONCAT('WARNING: ', CAST(her1.analysis_id  AS VARCHAR(1000)), '-', aa1.analysis_name, '; ', CAST(COUNT_BIG(DISTINCT her1.stratum_1)  AS VARCHAR(1000)), 'concepts have a 100% change in monthly count of events') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_analysis aa1
    INNER JOIN (
                 SELECT
                   cohort_definition_id,
                   analysis_id,
                   stratum_1,
                   CAST((CASE WHEN stratum_2 = ''
                     THEN NULL
                         ELSE stratum_2 END) AS INT) stratum_2,
                   count_value
                 FROM @results_schema.heracles_results
                 WHERE analysis_id IN (
                   402,
                   602,
                   702,
                   802,
                   902,
                   1002
                 )
               ) her1 ON aa1.analysis_id = her1.analysis_id
    INNER JOIN (
                 SELECT
                   cohort_definition_id,
                   analysis_id,
                   stratum_1,
                   CAST((CASE WHEN stratum_2 = ''
                     THEN NULL
                         ELSE stratum_2 END) AS INT) stratum_2,
                   count_value
                 FROM @results_schema.heracles_results
                 WHERE analysis_id IN (
                   402,
                   602,
                   702,
                   802,
                   902,
                   1002
                 )
               ) ar2 ON her1.analysis_id = ar2.analysis_id
                        and her1.cohort_definition_id = ar2.cohort_definition_id
                        AND her1.stratum_1 = ar2.stratum_1
  WHERE (
          her1.stratum_2 + 1 = ar2.stratum_2
          OR her1.stratum_2 + 89 = ar2.stratum_2
        )
        and her1.cohort_definition_id in (@cohort_definition_id)
        AND 1.0 * abs(ar2.count_value - her1.count_value) / her1.count_value > 1
        AND her1.count_value > 10
  GROUP BY her1.cohort_definition_id,
    her1.analysis_id,
    aa1.analysis_name;

--WARNING: days_supply > 180
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT DISTINCT ord1.cohort_definition_id,
    ord1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(ord1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; max (value=', cast(ord1.max_value as VARCHAR), ' should not be > 180') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results_dist ord1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON ord1.analysis_id = oa1.analysis_id
  WHERE ord1.analysis_id IN (715)
        and ord1.cohort_definition_id in (@cohort_definition_id)
        AND ord1.max_value > 180;

--WARNING:  refills > 10
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT DISTINCT ord1.cohort_definition_id,
    ord1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(ord1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; max (value=', cast(ord1.max_value as VARCHAR), ' should not be > 10') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results_dist ord1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON ord1.analysis_id = oa1.analysis_id
  WHERE ord1.analysis_id IN (716)
        and ord1.cohort_definition_id in (@cohort_definition_id)
        AND ord1.max_value > 10;

--WARNING: quantity > 600
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
  analysis_id,
  HERACLES_HEEL_warning
)
  SELECT DISTINCT ord1.cohort_definition_id,
    ord1.analysis_id,
    CAST(CONCAT('ERROR: ', cast(ord1.analysis_id as VARCHAR), '-', oa1.analysis_name, '; max (value=', cast(ord1.max_value as VARCHAR), ' should not be > 600') AS VARCHAR(255)) AS HERACLES_HEEL_warning
  FROM @results_schema.heracles_results_dist ord1
    INNER JOIN @results_schema.heracles_analysis oa1
      ON ord1.analysis_id = oa1.analysis_id
  WHERE ord1.analysis_id IN (717)
        and ord1.cohort_definition_id in (@cohort_definition_id)
        AND ord1.max_value > 600;


--}

IF OBJECT_ID('tempdb..#results_0', 'U') IS NOT NULL
drop table  #results_0;

IF OBJECT_ID('tempdb..#results_dist_0', 'U') IS NOT NULL
drop table  #results_dist_0;

IF OBJECT_ID('tempdb..#results_3000', 'U') IS NOT NULL
drop table  #results_3000;

IF OBJECT_ID('tempdb..#results_3001', 'U') IS NOT NULL
drop table  #results_3001;

IF OBJECT_ID('tempdb..#results_1', 'U') IS NOT NULL
drop table  #results_1;

IF OBJECT_ID('tempdb..#results_2', 'U') IS NOT NULL
drop table  #results_2;

IF OBJECT_ID('tempdb..#results_3', 'U') IS NOT NULL
drop table  #results_3;

IF OBJECT_ID('tempdb..#results_4', 'U') IS NOT NULL
drop table  #results_4;

IF OBJECT_ID('tempdb..#results_5', 'U') IS NOT NULL
drop table  #results_5;

IF OBJECT_ID('tempdb..#results_7', 'U') IS NOT NULL
drop table  #results_7;

IF OBJECT_ID('tempdb..#results_8', 'U') IS NOT NULL
drop table  #results_8;

IF OBJECT_ID('tempdb..#results_9', 'U') IS NOT NULL
drop table  #results_9;

IF OBJECT_ID('tempdb..#results_101', 'U') IS NOT NULL
drop table  #results_101;

IF OBJECT_ID('tempdb..#results_102', 'U') IS NOT NULL
drop table  #results_102;

IF OBJECT_ID('tempdb..#results_dist_103', 'U') IS NOT NULL
drop table  #results_dist_103;

IF OBJECT_ID('tempdb..#results_dist_104', 'U') IS NOT NULL
drop table  #results_dist_104;

IF OBJECT_ID('tempdb..#results_dist_105', 'U') IS NOT NULL
drop table  #results_dist_105;

IF OBJECT_ID('tempdb..#results_dist_106', 'U') IS NOT NULL
drop table  #results_dist_106;

IF OBJECT_ID('tempdb..#results_dist_107', 'U') IS NOT NULL
drop table  #results_dist_107;

IF OBJECT_ID('tempdb..#results_108', 'U') IS NOT NULL
drop table  #results_108;

IF OBJECT_ID('tempdb..#results_109', 'U') IS NOT NULL
drop table  #results_109;

IF OBJECT_ID('tempdb..#results_110', 'U') IS NOT NULL
drop table  #results_110;

IF OBJECT_ID('tempdb..#results_111', 'U') IS NOT NULL
drop table  #results_111;

IF OBJECT_ID('tempdb..#results_112', 'U') IS NOT NULL
drop table  #results_112;

IF OBJECT_ID('tempdb..#results_113', 'U') IS NOT NULL
drop table  #results_113;

IF OBJECT_ID('tempdb..#results_114', 'U') IS NOT NULL
drop table  #results_114;

IF OBJECT_ID('tempdb..#results_115', 'U') IS NOT NULL
drop table  #results_115;

IF OBJECT_ID('tempdb..#results_116', 'U') IS NOT NULL
drop table  #results_116;

IF OBJECT_ID('tempdb..#results_117', 'U') IS NOT NULL
drop table  #results_117;

IF OBJECT_ID('tempdb..#results_200', 'U') IS NOT NULL
drop table  #results_200;

IF OBJECT_ID('tempdb..#results_201', 'U') IS NOT NULL
drop table  #results_201;

IF OBJECT_ID('tempdb..#results_202', 'U') IS NOT NULL
drop table  #results_202;

IF OBJECT_ID('tempdb..#results_dist_203', 'U') IS NOT NULL
drop table  #results_dist_203;

IF OBJECT_ID('tempdb..#results_204', 'U') IS NOT NULL
drop table  #results_204;

IF OBJECT_ID('tempdb..#results_dist_206', 'U') IS NOT NULL
drop table  #results_dist_206;

IF OBJECT_ID('tempdb..#results_207', 'U') IS NOT NULL
drop table  #results_207;

IF OBJECT_ID('tempdb..#results_208', 'U') IS NOT NULL
drop table  #results_208;

IF OBJECT_ID('tempdb..#results_209', 'U') IS NOT NULL
drop table  #results_209;

IF OBJECT_ID('tempdb..#results_210', 'U') IS NOT NULL
drop table  #results_210;

IF OBJECT_ID('tempdb..#results_dist_211', 'U') IS NOT NULL
drop table  #results_dist_211;

IF OBJECT_ID('tempdb..#results_220', 'U') IS NOT NULL
drop table  #results_220;

IF OBJECT_ID('tempdb..#results_400', 'U') IS NOT NULL
drop table  #results_400;

IF OBJECT_ID('tempdb..#results_401', 'U') IS NOT NULL
drop table  #results_401;

IF OBJECT_ID('tempdb..#results_402', 'U') IS NOT NULL
drop table  #results_402;

IF OBJECT_ID('tempdb..#results_dist_403', 'U') IS NOT NULL
drop table  #results_dist_403;

IF OBJECT_ID('tempdb..#results_404', 'U') IS NOT NULL
drop table  #results_404;

IF OBJECT_ID('tempdb..#results_405', 'U') IS NOT NULL
drop table  #results_405;

IF OBJECT_ID('tempdb..#results_dist_406', 'U') IS NOT NULL
drop table  #results_dist_406;

IF OBJECT_ID('tempdb..#results_409', 'U') IS NOT NULL
drop table  #results_409;

IF OBJECT_ID('tempdb..#results_410', 'U') IS NOT NULL
drop table  #results_410;

IF OBJECT_ID('tempdb..#results_411', 'U') IS NOT NULL
drop table  #results_411;

IF OBJECT_ID('tempdb..#results_412', 'U') IS NOT NULL
drop table  #results_412;

IF OBJECT_ID('tempdb..#results_413', 'U') IS NOT NULL
drop table  #results_413;

IF OBJECT_ID('tempdb..#results_420', 'U') IS NOT NULL
drop table  #results_420;

IF OBJECT_ID('tempdb..#results_500', 'U') IS NOT NULL
drop table  #results_500;

IF OBJECT_ID('tempdb..#results_501', 'U') IS NOT NULL
drop table  #results_501;

IF OBJECT_ID('tempdb..#results_502', 'U') IS NOT NULL
drop table  #results_502;

IF OBJECT_ID('tempdb..#results_504', 'U') IS NOT NULL
drop table  #results_504;

IF OBJECT_ID('tempdb..#results_505', 'U') IS NOT NULL
drop table  #results_505;

IF OBJECT_ID('tempdb..#results_dist_506', 'U') IS NOT NULL
drop table  #results_dist_506;

IF OBJECT_ID('tempdb..#results_509', 'U') IS NOT NULL
drop table  #results_509;

IF OBJECT_ID('tempdb..#results_510', 'U') IS NOT NULL
drop table  #results_510;

IF OBJECT_ID('tempdb..#results_dist_511', 'U') IS NOT NULL
drop table  #results_dist_511;

IF OBJECT_ID('tempdb..#results_dist_512', 'U') IS NOT NULL
drop table  #results_dist_512;

IF OBJECT_ID('tempdb..#results_dist_513', 'U') IS NOT NULL
drop table  #results_dist_513;

IF OBJECT_ID('tempdb..#results_dist_514', 'U') IS NOT NULL
drop table  #results_dist_514;

IF OBJECT_ID('tempdb..#results_dist_515', 'U') IS NOT NULL
drop table  #results_dist_515;

IF OBJECT_ID('tempdb..#results_600', 'U') IS NOT NULL
drop table  #results_600;

IF OBJECT_ID('tempdb..#results_601', 'U') IS NOT NULL
drop table  #results_601;

IF OBJECT_ID('tempdb..#results_602', 'U') IS NOT NULL
drop table  #results_602;

IF OBJECT_ID('tempdb..#results_dist_603', 'U') IS NOT NULL
drop table  #results_dist_603;

IF OBJECT_ID('tempdb..#results_604', 'U') IS NOT NULL
drop table  #results_604;

IF OBJECT_ID('tempdb..#results_605', 'U') IS NOT NULL
drop table  #results_605;

IF OBJECT_ID('tempdb..#results_dist_606', 'U') IS NOT NULL
drop table  #results_dist_606;

IF OBJECT_ID('tempdb..#results_609', 'U') IS NOT NULL
drop table  #results_609;

IF OBJECT_ID('tempdb..#results_610', 'U') IS NOT NULL
drop table  #results_610;

IF OBJECT_ID('tempdb..#results_612', 'U') IS NOT NULL
drop table  #results_612;

IF OBJECT_ID('tempdb..#results_613', 'U') IS NOT NULL
drop table  #results_613;

IF OBJECT_ID('tempdb..#results_620', 'U') IS NOT NULL
drop table  #results_620;

IF OBJECT_ID('tempdb..#results_700', 'U') IS NOT NULL
drop table  #results_700;

IF OBJECT_ID('tempdb..#results_701', 'U') IS NOT NULL
drop table  #results_701;

IF OBJECT_ID('tempdb..#results_702', 'U') IS NOT NULL
drop table  #results_702;

IF OBJECT_ID('tempdb..#results_dist_703', 'U') IS NOT NULL
drop table  #results_dist_703;

IF OBJECT_ID('tempdb..#results_704', 'U') IS NOT NULL
drop table  #results_704;

IF OBJECT_ID('tempdb..#results_705', 'U') IS NOT NULL
drop table  #results_705;

IF OBJECT_ID('tempdb..#results_dist_706', 'U') IS NOT NULL
drop table  #results_dist_706;

IF OBJECT_ID('tempdb..#results_709', 'U') IS NOT NULL
drop table  #results_709;

IF OBJECT_ID('tempdb..#results_710', 'U') IS NOT NULL
drop table  #results_710;

IF OBJECT_ID('tempdb..#results_711', 'U') IS NOT NULL
drop table  #results_711;

IF OBJECT_ID('tempdb..#results_712', 'U') IS NOT NULL
drop table  #results_712;

IF OBJECT_ID('tempdb..#results_713', 'U') IS NOT NULL
drop table  #results_713;

IF OBJECT_ID('tempdb..#results_dist_715', 'U') IS NOT NULL
drop table  #results_dist_715;

IF OBJECT_ID('tempdb..#results_dist_716', 'U') IS NOT NULL
drop table  #results_dist_716;

IF OBJECT_ID('tempdb..#results_dist_717', 'U') IS NOT NULL
drop table  #results_dist_717;

IF OBJECT_ID('tempdb..#results_720', 'U') IS NOT NULL
drop table  #results_720;

IF OBJECT_ID('tempdb..#results_800', 'U') IS NOT NULL
drop table  #results_800;

IF OBJECT_ID('tempdb..#results_801', 'U') IS NOT NULL
drop table  #results_801;

IF OBJECT_ID('tempdb..#results_802', 'U') IS NOT NULL
drop table  #results_802;

IF OBJECT_ID('tempdb..#results_dist_803', 'U') IS NOT NULL
drop table  #results_dist_803;

IF OBJECT_ID('tempdb..#results_804', 'U') IS NOT NULL
drop table  #results_804;

IF OBJECT_ID('tempdb..#results_805', 'U') IS NOT NULL
drop table  #results_805;

IF OBJECT_ID('tempdb..#results_dist_806', 'U') IS NOT NULL
drop table  #results_dist_806;

IF OBJECT_ID('tempdb..#results_807', 'U') IS NOT NULL
drop table  #results_807;

IF OBJECT_ID('tempdb..#results_809', 'U') IS NOT NULL
drop table  #results_809;

IF OBJECT_ID('tempdb..#results_810', 'U') IS NOT NULL
drop table  #results_810;

IF OBJECT_ID('tempdb..#results_812', 'U') IS NOT NULL
drop table  #results_812;

IF OBJECT_ID('tempdb..#results_813', 'U') IS NOT NULL
drop table  #results_813;

IF OBJECT_ID('tempdb..#results_814', 'U') IS NOT NULL
drop table  #results_814;

IF OBJECT_ID('tempdb..#results_dist_815', 'U') IS NOT NULL
drop table  #results_dist_815;

IF OBJECT_ID('tempdb..#results_820', 'U') IS NOT NULL
drop table  #results_820;

IF OBJECT_ID('tempdb..#results_900', 'U') IS NOT NULL
drop table  #results_900;

IF OBJECT_ID('tempdb..#results_901', 'U') IS NOT NULL
drop table  #results_901;

IF OBJECT_ID('tempdb..#results_902', 'U') IS NOT NULL
drop table  #results_902;

IF OBJECT_ID('tempdb..#results_dist_903', 'U') IS NOT NULL
drop table  #results_dist_903;

IF OBJECT_ID('tempdb..#results_903', 'U') IS NOT NULL
drop table  #results_903;

IF OBJECT_ID('tempdb..#results_dist_906', 'U') IS NOT NULL
drop table  #results_dist_906;

IF OBJECT_ID('tempdb..#results_dist_907', 'U') IS NOT NULL
drop table  #results_dist_907;

IF OBJECT_ID('tempdb..#results_908', 'U') IS NOT NULL
drop table  #results_908;

IF OBJECT_ID('tempdb..#results_909', 'U') IS NOT NULL
drop table  #results_909;

IF OBJECT_ID('tempdb..#results_910', 'U') IS NOT NULL
drop table  #results_910;

IF OBJECT_ID('tempdb..#results_920', 'U') IS NOT NULL
drop table  #results_920;

IF OBJECT_ID('tempdb..#results_1000', 'U') IS NOT NULL
drop table  #results_1000;

IF OBJECT_ID('tempdb..#results_1001', 'U') IS NOT NULL
drop table  #results_1001;

IF OBJECT_ID('tempdb..#results_1002', 'U') IS NOT NULL
drop table  #results_1002;

IF OBJECT_ID('tempdb..#results_dist_1003', 'U') IS NOT NULL
drop table  #results_dist_1003;

IF OBJECT_ID('tempdb..#results_1004', 'U') IS NOT NULL
drop table  #results_1004;

IF OBJECT_ID('tempdb..#results_dist_1006', 'U') IS NOT NULL
drop table  #results_dist_1006;

IF OBJECT_ID('tempdb..#results_dist_1007', 'U') IS NOT NULL
drop table  #results_dist_1007;

IF OBJECT_ID('tempdb..#results_1008', 'U') IS NOT NULL
drop table  #results_1008;

IF OBJECT_ID('tempdb..#results_1009', 'U') IS NOT NULL
drop table  #results_1009;

IF OBJECT_ID('tempdb..#results_1010', 'U') IS NOT NULL
drop table  #results_1010;

IF OBJECT_ID('tempdb..#results_1020', 'U') IS NOT NULL
drop table  #results_1020;

IF OBJECT_ID('tempdb..#results_1100', 'U') IS NOT NULL
drop table  #results_1100;

IF OBJECT_ID('tempdb..#results_1101', 'U') IS NOT NULL
drop table  #results_1101;

IF OBJECT_ID('tempdb..#results_1200', 'U') IS NOT NULL
drop table  #results_1200;

IF OBJECT_ID('tempdb..#results_1201', 'U') IS NOT NULL
drop table  #results_1201;

IF OBJECT_ID('tempdb..#results_1300', 'U') IS NOT NULL
drop table  #results_1300;

IF OBJECT_ID('tempdb..#results_1301', 'U') IS NOT NULL
drop table  #results_1301;

IF OBJECT_ID('tempdb..#results_1302', 'U') IS NOT NULL
drop table  #results_1302;

IF OBJECT_ID('tempdb..#results_dist_1303', 'U') IS NOT NULL
drop table  #results_dist_1303;

IF OBJECT_ID('tempdb..#results_1304', 'U') IS NOT NULL
drop table  #results_1304;

IF OBJECT_ID('tempdb..#results_1305', 'U') IS NOT NULL
drop table  #results_1305;

IF OBJECT_ID('tempdb..#results_dist_1306', 'U') IS NOT NULL
drop table  #results_dist_1306;

IF OBJECT_ID('tempdb..#results_1307', 'U') IS NOT NULL
drop table  #results_1307;

IF OBJECT_ID('tempdb..#results_1309', 'U') IS NOT NULL
drop table  #results_1309;

IF OBJECT_ID('tempdb..#results_1310', 'U') IS NOT NULL
drop table  #results_1310;

IF OBJECT_ID('tempdb..#results_1312', 'U') IS NOT NULL
drop table  #results_1312;

IF OBJECT_ID('tempdb..#results_1313', 'U') IS NOT NULL
drop table  #results_1313;

IF OBJECT_ID('tempdb..#results_1314', 'U') IS NOT NULL
drop table  #results_1314;

IF OBJECT_ID('tempdb..#results_dist_1315', 'U') IS NOT NULL
drop table  #results_dist_1315;

IF OBJECT_ID('tempdb..#results_dist_1316', 'U') IS NOT NULL
drop table  #results_dist_1316;

IF OBJECT_ID('tempdb..#results_dist_1317', 'U') IS NOT NULL
drop table  #results_dist_1317;

IF OBJECT_ID('tempdb..#results_1318', 'U') IS NOT NULL
drop table  #results_1318;

IF OBJECT_ID('tempdb..#results_1320', 'U') IS NOT NULL
drop table  #results_1320;

IF OBJECT_ID('tempdb..#results_1700', 'U') IS NOT NULL
drop table  #results_1700;

IF OBJECT_ID('tempdb..#results_1701', 'U') IS NOT NULL
drop table  #results_1701;

IF OBJECT_ID('tempdb..#results_1800', 'U') IS NOT NULL
drop table  #results_1800;

IF OBJECT_ID('tempdb..#results_dist_1801', 'U') IS NOT NULL
drop table  #results_dist_1801;

IF OBJECT_ID('tempdb..#results_dist_1802', 'U') IS NOT NULL
drop table  #results_dist_1802;

IF OBJECT_ID('tempdb..#results_dist_1803', 'U') IS NOT NULL
drop table  #results_dist_1803;

IF OBJECT_ID('tempdb..#results_1804', 'U') IS NOT NULL
drop table  #results_1804;

IF OBJECT_ID('tempdb..#results_1805', 'U') IS NOT NULL
drop table  #results_1805;

IF OBJECT_ID('tempdb..#results_1806', 'U') IS NOT NULL
drop table  #results_1806;

IF OBJECT_ID('tempdb..#results_1807', 'U') IS NOT NULL
drop table  #results_1807;

IF OBJECT_ID('tempdb..#results_dist_1808', 'U') IS NOT NULL
drop table  #results_dist_1808;

IF OBJECT_ID('tempdb..#results_dist_1809', 'U') IS NOT NULL
drop table  #results_dist_1809;

IF OBJECT_ID('tempdb..#results_dist_1810', 'U') IS NOT NULL
drop table  #results_dist_1810;

IF OBJECT_ID('tempdb..#results_dist_1811', 'U') IS NOT NULL
drop table  #results_dist_1811;

IF OBJECT_ID('tempdb..#results_dist_1812', 'U') IS NOT NULL
drop table  #results_dist_1812;

IF OBJECT_ID('tempdb..#results_dist_1813', 'U') IS NOT NULL
drop table  #results_dist_1813;

IF OBJECT_ID('tempdb..#results_1814', 'U') IS NOT NULL
drop table  #results_1814;

IF OBJECT_ID('tempdb..#results_1815', 'U') IS NOT NULL
drop table  #results_1815;

IF OBJECT_ID('tempdb..#results_1816', 'U') IS NOT NULL
drop table  #results_1816;

IF OBJECT_ID('tempdb..#results_1817', 'U') IS NOT NULL
drop table  #results_1817;

IF OBJECT_ID('tempdb..#results_1820', 'U') IS NOT NULL
drop table  #results_1820;

IF OBJECT_ID('tempdb..#results_1821', 'U') IS NOT NULL
drop table  #results_1821;

IF OBJECT_ID('tempdb..#results_1830', 'U') IS NOT NULL
drop table  #results_1830;

IF OBJECT_ID('tempdb..#results_1831', 'U') IS NOT NULL
drop table  #results_1831;

IF OBJECT_ID('tempdb..#results_1840', 'U') IS NOT NULL
drop table  #results_1840;

IF OBJECT_ID('tempdb..#results_1841', 'U') IS NOT NULL
drop table  #results_1841;

IF OBJECT_ID('tempdb..#results_1850', 'U') IS NOT NULL
drop table  #results_1850;

IF OBJECT_ID('tempdb..#results_1851', 'U') IS NOT NULL
drop table  #results_1851;

IF OBJECT_ID('tempdb..#results_1860', 'U') IS NOT NULL
drop table  #results_1860;

IF OBJECT_ID('tempdb..#results_1861', 'U') IS NOT NULL
drop table  #results_1861;

IF OBJECT_ID('tempdb..#results_1870', 'U') IS NOT NULL
drop table  #results_1870;

IF OBJECT_ID('tempdb..#results_1871', 'U') IS NOT NULL
drop table  #results_1871;

IF OBJECT_ID('tempdb..#results_2001', 'U') IS NOT NULL
drop table  #results_2001;

IF OBJECT_ID('tempdb..#results_2002', 'U') IS NOT NULL
drop table  #results_2002;

IF OBJECT_ID('tempdb..#results_2003', 'U') IS NOT NULL
drop table  #results_2003;

IF OBJECT_ID('tempdb..#results_2004', 'U') IS NOT NULL
drop table  #results_2004;

IF OBJECT_ID('tempdb..#results_2005', 'U') IS NOT NULL
drop table  #results_2005;

IF OBJECT_ID('tempdb..#results_2006', 'U') IS NOT NULL
drop table  #results_2006;

IF OBJECT_ID('tempdb..#results_2007', 'U') IS NOT NULL
drop table  #results_2007;

IF OBJECT_ID('tempdb..#results_2011', 'U') IS NOT NULL
drop table  #results_2011;

IF OBJECT_ID('tempdb..#results_2012', 'U') IS NOT NULL
drop table  #results_2012;

IF OBJECT_ID('tempdb..#results_2013', 'U') IS NOT NULL
drop table  #results_2013;

IF OBJECT_ID('tempdb..#results_2014', 'U') IS NOT NULL
drop table  #results_2014;

IF OBJECT_ID('tempdb..#results_2015', 'U') IS NOT NULL
drop table  #results_2015;

IF OBJECT_ID('tempdb..#results_2016', 'U') IS NOT NULL
drop table  #results_2016;

IF OBJECT_ID('tempdb..#results_2017', 'U') IS NOT NULL
drop table  #results_2017;

IF OBJECT_ID('tempdb..#results_2021', 'U') IS NOT NULL
drop table  #results_2021;

IF OBJECT_ID('tempdb..#results_2022', 'U') IS NOT NULL
drop table  #results_2022;

IF OBJECT_ID('tempdb..#results_2023', 'U') IS NOT NULL
drop table  #results_2023;

IF OBJECT_ID('tempdb..#results_2024', 'U') IS NOT NULL
drop table  #results_2024;

IF OBJECT_ID('tempdb..#results_2025', 'U') IS NOT NULL
drop table  #results_2025;

IF OBJECT_ID('tempdb..#results_2026', 'U') IS NOT NULL
drop table  #results_2026;

IF OBJECT_ID('tempdb..#results_2027', 'U') IS NOT NULL
drop table  #results_2027;

IF OBJECT_ID('tempdb..#results_2031', 'U') IS NOT NULL
drop table  #results_2031;

IF OBJECT_ID('tempdb..#results_2032', 'U') IS NOT NULL
drop table  #results_2032;

IF OBJECT_ID('tempdb..#results_4000', 'U') IS NOT NULL
drop table  #results_dist_4000;

IF OBJECT_ID('tempdb..#results_4001', 'U') IS NOT NULL
drop table  #results_4001;

IF OBJECT_ID('tempdb..#results_dist_4002', 'U') IS NOT NULL
drop table  #results_dist_4002;

IF OBJECT_ID('tempdb..#results_dist_4003', 'U') IS NOT NULL
drop table  #results_dist_4003;

IF OBJECT_ID('tempdb..#results_dist_4004', 'U') IS NOT NULL
drop table  #results_dist_4004;

IF OBJECT_ID('tempdb..#results_dist_4005', 'U') IS NOT NULL
drop table  #results_dist_4005;

IF OBJECT_ID('tempdb..#results_dist_4006', 'U') IS NOT NULL
drop table  #results_dist_4006;

IF OBJECT_ID('tempdb..#results_4007', 'U') IS NOT NULL
drop table  #results_4007;

IF OBJECT_ID('tempdb..#results_dist_4008', 'U') IS NOT NULL
drop table  #results_dist_4008;

IF OBJECT_ID('tempdb..#results_dist_4009', 'U') IS NOT NULL
drop table  #results_dist_4009;

IF OBJECT_ID('tempdb..#results_dist_4010', 'U') IS NOT NULL
drop table  #results_dist_4010;

IF OBJECT_ID('tempdb..#results_dist_4011', 'U') IS NOT NULL
drop table  #results_dist_4011;

IF OBJECT_ID('tempdb..#results_4012', 'U') IS NOT NULL
drop table  #results_4012;

IF OBJECT_ID('tempdb..#results_dist_4013', 'U') IS NOT NULL
drop table  #results_dist_4013;

IF OBJECT_ID('tempdb..#results_dist_4014', 'U') IS NOT NULL
drop table  #results_dist_4014;

IF OBJECT_ID('tempdb..#results_dist_4015', 'U') IS NOT NULL
drop table  #results_dist_4015;

IF OBJECT_ID('tempdb..#results_4016', 'U') IS NOT NULL
drop table  #results_4016;

IF OBJECT_ID('tempdb..#results_dist_4017', 'U') IS NOT NULL
drop table  #results_dist_4017;

IF OBJECT_ID('tempdb..#results_dist_4018', 'U') IS NOT NULL
drop table  #results_dist_4018;

IF OBJECT_ID('tempdb..#results_dist_4019', 'U') IS NOT NULL
drop table  #results_dist_4019;

IF OBJECT_ID('tempdb..#results_dist_4020', 'U') IS NOT NULL
drop table  #results_dist_4020;

IF OBJECT_ID('tempdb..#results_dist_4021', 'U') IS NOT NULL
drop table  #results_dist_4021;

IF OBJECT_ID('tempdb..#results_dist_4022', 'U') IS NOT NULL
drop table  #results_dist_4022;

IF OBJECT_ID('tempdb..#results_dist_4023', 'U') IS NOT NULL
drop table  #results_dist_4023;

IF OBJECT_ID('tempdb..#tmp_years', 'U') IS NOT NULL
DROP TABLE  #tmp_years;

IF OBJECT_ID('tempdb..#tmp_months', 'U') IS NOT NULL
DROP TABLE  #tmp_months;