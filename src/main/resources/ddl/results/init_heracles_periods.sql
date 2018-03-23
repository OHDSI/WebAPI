with generate_dates (d_years, d_months) AS
(
	SELECT
		y1.n + (10*y10.n) + (100*y100.n) + (1000*y1000.n) AS d_years,
		mths.n as d_months
	FROM
	(select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) y1(n),
	(select 0 union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) y10(n),
	(select 0 union all select 1 union all select 9) y100(n),
	(select 1 union all select 2) y1000(n),
	(select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9 union all select 10 union all select 11 union all select 12) mths(n)
	where y1.n + (10*y10.n) + (100*y100.n) + (1000*y1000.n) >= 1900 and y1.n + (10*y10.n) + (100*y100.n) + (1000*y1000.n) < 2100
), 
monthly_dates (generated_date) as
(
	SELECT DATEFROMPARTS(d_years, d_months, 01) as generated_date
	from generate_dates
),
weekly_dates (generated_date) as (
  SELECT DATEFROMPARTS(d_years, d_months, i.day_of_month) as generated_date
	from generate_dates
	CROSS JOIN ( 
		select 1 as day_of_month
		UNION ALL select 8 as day_of_month
		UNION ALL select 15 as day_of_month
		UNION ALL select 22 as day_of_month
	) i
),
quarterly_dates (generated_date) as (
  SELECT DATEFROMPARTS(d_years, d_months, 1) as generated_date
	from generate_dates
	where d_months in (1,4,7,10)
)

select row_number() over (order by period_order, period_start_date) as period_id, period_name, period_order, period_type, period_start_date, period_end_date
INTO #temp_period
FROM (
  -- monthly dates
  select 'Monthly' as period_name
		, 1 as period_order
		, 'mm' as period_type
		, md.generated_date as period_start_date
		, dateadd(mm,1,md.generated_date) as period_end_date
	from monthly_dates md

	UNION ALL
  select 'Weekly' as period_name
		, 2 as period_order
		, 'ww' as period_type
		, wd.generated_date as period_start_date
		, case when DAY(wd.generated_date) = 22 then dateadd(dd,1,EOMONTH(wd.generated_date)) else dateadd(d, 7, wd.generated_date) end as period_end_date
	from weekly_dates wd

	UNION ALL
  select 'Quarterly' as period_name
		, 3 as period_order
		, 'qq' as period_type
		, qd.generated_date as period_start_date
		, dateadd(mm,3,qd.generated_date) as period_end_date
	from quarterly_dates qd

	UNION ALL
  select 'Yearly' as period_name
		, 4 as period_order
		, 'yy' as period_type
		, qd.generated_date as period_start_date
		, dateadd(yy,1,qd.generated_date) as period_end_date
	from quarterly_dates qd

	-- ADD UNION ALLs for additional period definitions
) P
;

INSERT INTO @results_schema.heracles_periods (period_id, period_name, period_order, period_type, period_start_date, period_end_date)
select period_id, period_name, period_order, period_type, period_start_date, period_end_date from #temp_period;

TRUNCATE TABLE #temp_period;
DROP TABLE #temp_period;
