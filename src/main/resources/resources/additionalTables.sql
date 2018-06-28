create table @resultTableQualifier.cohort
(
  cohort_definition_id integer not null,
  subject_id bigint not null,
  cohort_start_date date not null,
  cohort_end_date date not null
)
;

create table @resultTableQualifier.heracles_analysis
(
  analysis_id integer,
  analysis_name varchar(255),
  stratum_1_name varchar(255),
  stratum_2_name varchar(255),
  stratum_3_name varchar(255),
  stratum_4_name varchar(255),
  stratum_5_name varchar(255),
  analysis_type varchar(255)
)
;

create table @resultTableQualifier.heracles_results
(
  cohort_definition_id integer,
  analysis_id integer,
  stratum_1 varchar(255),
  stratum_2 varchar(255),
  stratum_3 varchar(255),
  stratum_4 varchar(255),
  stratum_5 varchar(255),
  count_value bigint,
  last_update_time timestamp default now()
)
;

create index hr_idx_cohort_id_first_res
  on @resultTableQualifier.heracles_results (cohort_definition_id, analysis_id, count_value, stratum_1)
;

create index hr_idx_cohort_id_analysis_id
  on @resultTableQualifier.heracles_results (cohort_definition_id, analysis_id)
;

create index hr_idx_cohort_def_id_dt
  on @resultTableQualifier.heracles_results (cohort_definition_id, last_update_time)
;

create index hr_idx_cohort_def_id
  on @resultTableQualifier.heracles_results (cohort_definition_id)
;

create table @resultTableQualifier.heracles_results_dist
(
  cohort_definition_id integer,
  analysis_id integer,
  stratum_1 varchar(255),
  stratum_2 varchar(255),
  stratum_3 varchar(255),
  stratum_4 varchar(255),
  stratum_5 varchar(255),
  count_value bigint,
  min_value double precision,
  max_value double precision,
  avg_value double precision,
  stdev_value double precision,
  median_value double precision,
  p10_value double precision,
  p25_value double precision,
  p75_value double precision,
  p90_value double precision,
  last_update_time timestamp default now()
)
;

create index hrd_idx_cohort_id_first_res
  on @resultTableQualifier.heracles_results_dist (cohort_definition_id, analysis_id, count_value, stratum_1)
;

create index hrd_idx_cohort_id_analysis_id
  on @resultTableQualifier.heracles_results_dist (cohort_definition_id, analysis_id)
;

create index hrd_idx_cohort_def_id_dt
  on @resultTableQualifier.heracles_results_dist (cohort_definition_id, last_update_time)
;

create index hrd_idx_cohort_def_id
  on @resultTableQualifier.heracles_results_dist (cohort_definition_id)
;

create table @resultTableQualifier.heracles_heel_results
(
  cohort_definition_id integer,
  analysis_id integer,
  heracles_heel_warning varchar(255)
)
;

create index hh_idx_cohort_id_analysis_id
  on @resultTableQualifier.heracles_heel_results (cohort_definition_id, analysis_id)
;

create table @resultTableQualifier.feas_study_result
(
  study_id integer not null,
  inclusion_rule_mask bigint not null,
  person_count bigint not null
)
;

create table @resultTableQualifier.feas_study_index_stats
(
  study_id integer not null,
  person_count bigint not null,
  match_count bigint not null
)
;

create table @resultTableQualifier.feas_study_inclusion_stats
(
  study_id integer not null,
  rule_sequence integer not null,
  name varchar(255) not null,
  person_count bigint not null,
  gain_count bigint not null,
  person_total bigint not null
)
;

create table @resultTableQualifier.cohort_inclusion
(
  cohort_definition_id integer not null,
  rule_sequence integer not null,
  name varchar(255),
  description varchar(1000)
)
;

create table @resultTableQualifier.cohort_inclusion_result
(
  cohort_definition_id integer not null,
  inclusion_rule_mask bigint not null,
  person_count bigint not null
)
;

create table @resultTableQualifier.cohort_inclusion_stats
(
  cohort_definition_id integer not null,
  rule_sequence integer not null,
  person_count bigint not null,
  gain_count bigint not null,
  person_total bigint not null
)
;

create table @resultTableQualifier.cohort_summary_stats
(
  cohort_definition_id integer not null,
  base_count bigint not null,
  final_count bigint not null
)
;

create table @resultTableQualifier.ir_strata
(
  analysis_id integer not null,
  strata_sequence integer not null,
  name varchar(255),
  description varchar(1000)
)
;

create table @resultTableQualifier.ir_analysis_result
(
  analysis_id integer not null,
  target_id integer not null,
  outcome_id integer not null,
  strata_mask bigint not null,
  person_count bigint not null,
  time_at_risk bigint not null,
  cases bigint not null
)
;

create table @resultTableQualifier.ir_analysis_strata_stats
(
  analysis_id integer not null,
  target_id integer not null,
  outcome_id integer not null,
  strata_sequence integer not null,
  person_count bigint not null,
  time_at_risk bigint not null,
  cases bigint not null
)
;

create table @resultTableQualifier.ir_analysis_dist
(
  analysis_id integer not null,
  target_id integer not null,
  outcome_id integer not null,
  strata_sequence integer,
  dist_type integer not null,
  total bigint not null,
  avg_value double precision not null,
  std_dev double precision not null,
  min_value integer not null,
  p10_value integer not null,
  p25_value integer not null,
  median_value integer not null,
  p75_value integer not null,
  p90_value integer not null,
  max_value integer
)
;

create table @resultTableQualifier.cohort_features_dist
(
  cohort_definition_id bigint,
  covariate_id bigint,
  count_value double precision,
  min_value double precision,
  max_value double precision,
  average_value double precision,
  standard_deviation double precision,
  median_value double precision,
  p10_value double precision,
  p25_value double precision,
  p75_value double precision,
  p90_value double precision
)
;

create table @resultTableQualifier.cohort_features
(
  cohort_definition_id bigint,
  covariate_id bigint,
  sum_value bigint,
  average_value double precision
)
;

create table @resultTableQualifier.cohort_features_ref
(
  cohort_definition_id bigint,
  covariate_id bigint,
  covariate_name varchar(1000),
  analysis_id integer,
  concept_id integer
)
;

create table @resultTableQualifier.cohort_features_analysis_ref
(
  cohort_definition_id bigint,
  analysis_id integer,
  analysis_name varchar(1000),
  domain_id varchar(100),
  start_day integer,
  end_day integer,
  is_binary char,
  missing_means_zero char
)
;
