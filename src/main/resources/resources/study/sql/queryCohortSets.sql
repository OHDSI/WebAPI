SELECT scs.id, scs.name, scs.description, count(*) as members 
FROM @ohdsi_schema.STUDY_COHORTSET scs
JOIN @ohdsi_schema.STUDY_COHORTSET_XREF scsx on scsx.cohortset_id = scs.id
JOIN @ohdsi_schema.STUDY_COHORT sc on sc.id = scsx.cohort_id
JOIN @ohdsi_schema.STUDY_COHORT_XREF scx on scx.cohort_id = sc.id
WHERE scx.study_id = @study_id and lower(scs.name) like '%@search_term%' OR lower(scs.description) like '%@search_term%'
GROUP BY scs.id, scs.name, scs.description
