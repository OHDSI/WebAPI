WITH concepts AS (
    SELECT
      CAST(ancestor_concept_id AS VARCHAR)   ancestor_id,
      CAST(descendant_concept_id AS VARCHAR) descendant_id
    FROM @vocabularyTableQualifier.concept_ancestor ca
    WHERE ancestor_concept_id IN (@conceptIdentifiers)
), counts AS (
SELECT stratum_1 concept_id, MAX (count_value) agg_count_value
FROM @resultTableQualifier.achilles_results
WHERE analysis_id IN (2, 4, 5, 201, 301, 401, 501, 505, 601, 701, 801, 901, 1001, 1201, 1801)
GROUP BY stratum_1
UNION
SELECT stratum_2 AS concept_id, SUM (count_value) AS agg_count_value
FROM @resultTableQualifier.achilles_results
WHERE analysis_id IN (405, 605, 705, 805, 807, 1805, 1807)
GROUP BY stratum_2
)
SELECT
  concepts.ancestor_id               concept_id,
  isnull(max(c1.agg_count_value), 0) record_count,
  isnull(sum(c2.agg_count_value), 0) descendant_record_count
FROM concepts
  LEFT JOIN counts c1 ON concepts.ancestor_id = c1.concept_id
  LEFT JOIN counts c2 ON concepts.descendant_id = c2.concept_id
GROUP BY concepts.ancestor_id
