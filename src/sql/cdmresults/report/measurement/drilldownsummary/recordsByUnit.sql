select c1.concept_id as measurement_concept_id,
       c1.concept_name as measurement_concept_name,
       c2.concept_id as concept_id,
       c2.concept_name as concept_name,
       ar1.count_value as count_value
from (
       select cast(CASE WHEN analysis_id = 1807 THEN stratum_1 ELSE '0' END as int) stratum_1,
              cast(CASE WHEN analysis_id = 1807 THEN stratum_2 ELSE '0' END as int) stratum_2, count_value
       from @results_database_schema.achilles_results
       where analysis_id = 1807
       group by analysis_id, stratum_1, stratum_2, count_value
     ) ar1
  inner join @vocab_database_schema.concept c1 on ar1.stratum_1 = c1.concept_id
  inner join @vocab_database_schema.concept c2 on ar1.stratum_2 = c2.concept_id
