SELECT
  s.trellis_name AS trellis_name,
  --age decile
  s.series_name AS series_name,
  --gender
  s.x_calendar_year AS x_calendar_year,
  -- calendar year, note, there could be blanks
  SUM(s.y_prevalence_1000_pp) AS y_prevalence_1000_pp --prevalence, per 1000 persons
FROM (@source_union) s
GROUP BY s.trellis_name, s.series_name, s.x_calendar_year
ORDER BY s.x_calendar_year
