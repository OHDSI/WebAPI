IF OBJECT_ID('@results_schema.heracles_analysis', 'U') IS NULL
CREATE TABLE @results_schema.heracles_analysis
(
	analysis_id int,
	analysis_name varchar(255),
	stratum_1_name varchar(255),
	stratum_2_name varchar(255),
	stratum_3_name varchar(255),
	stratum_4_name varchar(255),
	stratum_5_name varchar(255),
	analysis_type varchar(255)
);
