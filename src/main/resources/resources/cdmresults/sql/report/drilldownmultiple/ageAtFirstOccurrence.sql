SELECT
  s.category   AS category,
  avg(s.min_value) AS min_value,
  avg(s.p10_value) AS p10_value,
  avg(s.p25_value) AS p25_value,
  avg(s.median_value) AS median_value,
  avg(s.p75_value) AS p75_value,
  avg(s.p90_value) AS p90_value,
  avg(s.max_value) AS max_value
FROM (@source_union) s
GROUP BY s.category
