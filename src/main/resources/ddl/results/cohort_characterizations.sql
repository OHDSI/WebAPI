IF OBJECT_ID('@results_schema.cc_results', 'U') IS NULL
CREATE TABLE @results_schema.cc_results
(
  type VARCHAR(255) NOT NULL,
  fa_type VARCHAR(255) NOT NULL,
  cc_generation_id BIGINT NOT NULL,
  analysis_id INTEGER,
  analysis_name VARCHAR(1000),
  covariate_id BIGINT,
  covariate_name VARCHAR(1000),
  strata_id BIGINT,
  strata_name VARCHAR(1000),
  time_window VARCHAR(255),
  concept_id INTEGER NOT NULL,
  count_value BIGINT,
  avg_value DOUBLE PRECISION,
  stdev_value DOUBLE PRECISION,
  min_value DOUBLE PRECISION,
  p10_value DOUBLE PRECISION,
  p25_value DOUBLE PRECISION,
  median_value DOUBLE PRECISION,
  p75_value DOUBLE PRECISION,
  p90_value DOUBLE PRECISION,
  max_value DOUBLE PRECISION,
  cohort_definition_id BIGINT,
  aggregate_id INTEGER,
  aggregate_name VARCHAR(1000),
  missing_means_zero INTEGER
);

IF OBJECT_ID('@results_schema.cc_temporal_results') IS NULL
CREATE TABLE @results_schema.cc_temporal_results(
  type                 varchar(255),
  fa_type              varchar(255),
  cc_generation_id     bigint,
  analysis_id          integer,
  analysis_name        varchar(1000),
  covariate_id         bigint,
  covariate_name       varchar(1000),
  strata_id            bigint,
  strata_name          varchar(1000),
  concept_id           integer,
  count_value          bigint,
  avg_value            double precision,
  cohort_definition_id bigint,
  time_id              integer,
  start_day            integer,
  end_day              integer
);

IF OBJECT_ID('@results_schema.cc_temporal_annual_results') IS NULL
    CREATE TABLE @results_schema.cc_temporal_annual_results(
      type                 varchar(255),
      fa_type              varchar(255),
      cc_generation_id     bigint,
      analysis_id          integer,
      analysis_name        varchar(1000),
      covariate_id         bigint,
      covariate_name       varchar(1000),
      strata_id            bigint,
      strata_name          varchar(1000),
      concept_id           integer,
      count_value          bigint,
      avg_value            double precision,
      cohort_definition_id bigint,
      event_year           integer
);