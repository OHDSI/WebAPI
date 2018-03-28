IF OBJECT_ID('@results_schema.heracles_periods', 'U') IS NULL
CREATE TABLE @results_schema.heracles_periods
(
	period_id int,
  period_order int,
	period_name varchar(255),
  period_type varchar(50),
	period_start_date date,
	period_end_date date
);

CREATE INDEX idx_heracles_periods_start_date ON @results_schema.heracles_periods (period_start_date);
CREATE INDEX idx_heracles_periods_end_date ON @results_schema.heracles_periods (period_end_date);