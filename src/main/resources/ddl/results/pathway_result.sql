IF OBJECT_ID('@results_schema.pathway_code', 'U') IS NULL
CREATE TABLE @results_schema.pathway_code
(
	pathway_generation_id BIGINT NOT NULL,
	code INTEGER NOT NULL,
	name VARCHAR(255) NOT NULL,
	is_combo VARCHAR(255) NOT NULL
);

IF OBJECT_ID('@results_schema.cohort_pathway', 'U') IS NULL
CREATE TABLE @results_schema.cohort_pathway
(
	pathway_generation_id BIGINT NOT NULL,
	cohort_id INTEGER NOT NULL,
	target_cohort_count INTEGER NOT NULL,
	target_pathways_count INTEGER NOT NULL
);

IF OBJECT_ID('@results_schema.pathways_count', 'U') IS NULL
CREATE TABLE @results_schema.pathways_count
(
  cohort_id INTEGER NOT NULL,
  chain_name VARCHAR(255),
  chain_amount INTEGER NOT NULL
);