-- This study had no inclusion rules, so clear out inclusion_stats, and hardcode index stats to the count of the index population

-- remove matching group counts
delete from @ohdsi_database_schema.feas_study_result where study_id = @studyId;

-- remove gain counts
delete from @ohdsi_database_schema.feas_study_inclusion_stats where study_id = @studyId;

-- remove feas_study_inclusion_stats for this study
delete from @ohdsi_database_schema.feas_study_inclusion_stats where study_id = @studyId;

-- calculate totals for index
delete from @ohdsi_database_schema.feas_study_index_stats where study_id = @studyId;
insert into @ohdsi_database_schema.feas_study_index_stats (study_id, person_count, match_count)
select @studyId as study_id, COUNT(*), 0 as match_count
from @cohortTable
where cohort_definition_id = @indexCohortId
group by cohort_definition_id
;