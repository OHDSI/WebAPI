IF OBJECT_ID('@results_schema.ir_strata', 'U') IS NULL
CREATE TABLE @results_schema.ir_strata(
  analysis_id int NOT NULL,
  strata_sequence int NOT NULL,
  name varchar(255) NULL,
  description varchar(1000) NULL
);
