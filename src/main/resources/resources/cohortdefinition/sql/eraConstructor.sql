-- era constructor
WITH cteSource (@eraGroup, start_date, end_date, groupid) AS
(
	SELECT
		@eraGroup  
		, start_date
		, end_date
		, dense_rank() over(order by @eraGroup) as groupid
	FROM #collapse_constructor_input as so
)
,
--------------------------------------------------------------------------------------------------------------
cteEndDates (groupid, end_date) AS -- the magic
(	
	SELECT
		groupid
		, DATEADD(day,-1 * @eraconstructorpad, event_date)  as end_date
	FROM
	(
		SELECT
			groupid
			, event_date
			, event_type
			, MAX(start_ordinal) OVER (PARTITION BY groupid ORDER BY event_date, event_type ROWS UNBOUNDED PRECEDING) AS start_ordinal 
			, ROW_NUMBER() OVER (PARTITION BY groupid ORDER BY event_date, event_type) AS overall_ord
		FROM
		(

			SELECT
				groupid
				, start_date AS event_date
				, -1 AS event_type
				, ROW_NUMBER() OVER (PARTITION BY groupid ORDER BY start_date) AS start_ordinal
			FROM cteSource
		
			UNION ALL
		

			SELECT
				groupid
				, DATEADD(day,@eraconstructorpad,end_date) as end_date
				, 1 AS event_type
				, NULL
			FROM cteSource
		) RAWDATA
	) e
	WHERE (2 * e.start_ordinal) - e.overall_ord = 0
),
--------------------------------------------------------------------------------------------------------------
cteEnds (groupid, start_date, end_date) AS
(
	SELECT
		 c.groupid
		, c.start_date
		, MIN(e.end_date) AS era_end_date
	FROM cteSource c
	JOIN cteEndDates e ON c.groupid = e.groupid AND e.end_date >= c.start_date
	GROUP BY
		 c.groupid
		, c.start_date
)
select @eraGroup, start_date, end_date
into #collapse_constructor_output
from
(
	select distinct @eraGroup , min(b.start_date) as start_date, b.end_date
	from
		(select distinct @eraGroup, groupid from cteSource) as a
	inner join
		cteEnds as b
	on a.groupid = b.groupid
	group by @eraGroup, end_date
) q
;
