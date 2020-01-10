-- Count of results without threshold

select count(*)
  from @results_database_schema.cc_results r
  where r.cc_generation_id = @cohort_characterization_generation_id