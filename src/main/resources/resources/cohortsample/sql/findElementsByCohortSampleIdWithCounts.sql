SELECT *,
       ((select COUNT(*)
        from @CDM_schema.drug_exposure c
        where c.person_id = s.person_id)
           +
        (select COUNT(*)
         from @CDM_schema.condition_occurrence c
         where c.person_id = s.person_id)
           +
        (select COUNT(*)
         from @CDM_schema.condition_era c
         where c.person_id = s.person_id)
           +
        (select COUNT(*)
         from @CDM_schema.observation c
         where c.person_id = s.person_id)
           +
        (select COUNT(*)
         from @CDM_schema.visit_occurrence c
         where c.person_id = s.person_id)
           +
        (select COUNT(*)
         from @CDM_schema.death c
         where c.person_id = s.person_id)
           +
        (select COUNT(*)
         from @CDM_schema.measurement c
         where c.person_id = s.person_id)
           +
        (select COUNT(*)
         from @CDM_schema.device_exposure c
         where c.person_id = s.person_id)
           +
        (select COUNT(*)
         from @CDM_schema.procedure_occurrence c
         where c.person_id = s.person_id)
           +
        (select COUNT(*)
         from @CDM_schema.specimen c
         where c.person_id = s.person_id)) as record_count
FROM @results_schema.cohort_sample_element s
WHERE s.cohort_sample_id = @cohortSampleId
ORDER BY s.rank_value
;