SELECT
  s.x_calendar_month AS x_calendar_month,
  -- calendar year, note, there could be blanks
  SUM(s.y_prevalence_1000_pp) AS y_prevalence_1000_pp --prevalence, per 1000 persons
FROM (@source_union) s
GROUP BY s.x_calendar_month
ORDER BY CAST(s.x_calendar_month AS INT)