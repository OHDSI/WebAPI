--HINT PARTITION(cohort_definition_id int)
--HINT BUCKET(analysis_id, 64)
IF OBJECT_ID('@results_schema.heracles_results_dist', 'U') IS NULL
create table @results_schema.heracles_results_dist
(
	cohort_definition_id int,
	analysis_id int,
	stratum_1 varchar(255),
	stratum_2 varchar(255),
	stratum_3 varchar(255),
	stratum_4 varchar(255),
	stratum_5 varchar(255),
	count_value bigint,
	min_value float,
	max_value float,
	avg_value float,
	stdev_value float,
	median_value float,
	p10_value float,
	p25_value float,
	p75_value float,
	p90_value float,
	last_update_time datetime DEFAULT GETDATE()
);
