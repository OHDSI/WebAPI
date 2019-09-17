IF OBJECT_ID('@results_schema.heracles_results', 'U') IS NULL
create table @results_schema.heracles_results
(
    analysis_id int,
    stratum_1 varchar(255),
    stratum_2 varchar(255),
    stratum_3 varchar(255),
    stratum_4 varchar(255),
    stratum_5 varchar(255),
    count_value bigint,
    last_update_time timestamp
)
PARTITIONED BY(cohort_definition_id int)
clustered by (analysis_id) into 64 buckets;
