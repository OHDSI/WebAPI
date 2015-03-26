@codesetQuery
@indexCohortQuery

create table #inclusionRuleCohorts 
(
  inclusion_rule_id bigint,
  subject_id bigint,
  cohort_start_date datetime,
  cohort_end_date datetime,
)
;
@inclusionInserts

create table #BestMatchEvent
(
  person_id bigint,
  start_date datetime,
  end_date datetime
)
;

insert into #BestMatchEvent
select person_id, start_date, end_date
from
(
  select p.person_id, p.start_date, p.end_date, count(i.inclusion_rule_id) as cnt, row_number() over (partition by person_id order by count(i.inclusion_rule_id) desc, start_date) as rn
  FROM #PrimaryCriteriaEvents p
  LEFT JOIN #inclusionRuleCohorts i on p.person_id = i.subject_id and p.start_date = i.cohort_start_date and p.end_date = i.cohort_end_date
  group by p.person_id, p.start_date, p.end_date
) Q
where Q.rn = 1

-- the matching group with all bits set ( POWER(2,# of inclusion rules) - 1 = inclusion_rule_mask
DELETE FROM @cohortTable where cohort_definition_id = @resultCohortId;
INSERT INTO @cohortTable (cohort_definition_id, subject_id, cohort_start_date, cohort_end_date)
select @resultCohortId as cohort_definition_id, MG.person_id, MG.start_date, MG.end_date
from
(
  select C.person_id, C.start_date, C.end_date, SUM(coalesce(POWER(2, I.inclusion_rule_id), 0)) as inclusion_rule_mask
  from #BestMatchEvent C
  LEFT JOIN #inclusionRuleCohorts I on c.person_id = i.subject_id and c.start_date = i.cohort_start_date and c.end_date = i.cohort_end_date
  GROUP BY C.person_id, C.start_date, C.end_date
) MG -- matching groups
CROSS APPLY (select count(*) as total_rules from feasibility_inclusion where study_id = @studyId) RuleTotal
WHERE MG.inclusion_rule_mask = POWER(2,RuleTotal.total_rules)-1

-- calculte matching group counts
delete from feas_study_result where study_id = @studyId;
insert into feas_study_result (study_id, inclusion_rule_mask, person_count)
select @studyId as study_id, inclusion_rule_mask, count(*) as person_count
from
(
  select C.person_id, C.start_date, C.end_date, SUM(coalesce(POWER(2, I.inclusion_rule_id), 0)) as inclusion_rule_mask
  from #BestMatchEvent C
  LEFT JOIN #inclusionRuleCohorts I on c.person_id = i.subject_id and c.start_date = i.cohort_start_date and c.end_date = i.cohort_end_date
  GROUP BY C.person_id, C.start_date, C.end_date
) MG -- matching groups
group by inclusion_rule_mask;

-- calculate gain counts
delete from feas_study_inclusion_stats where study_id = @studyId;
insert into feas_study_inclusion_stats (study_id, rule_sequence, name, person_count, gain_count, person_total)
select cti.study_id, cti.sequence, cti.name, coalesce(T.person_count, 0) as person_count, coalesce(SR.person_count, 0) gain_count, PersonTotal.total
from feasibility_inclusion cti
left join
(
  select i.inclusion_rule_id, count(i.subject_id) as person_count
  from #BestMatchEvent C
  JOIN #inclusionRuleCohorts i on C.person_id = i.subject_id and c.start_date = i.cohort_start_date and c.end_date = i.cohort_end_date
  group by i.inclusion_rule_id
) T on cti.sequence = T.inclusion_rule_id
CROSS APPLY (select count(*) as total_rules from feasibility_inclusion where study_id = @studyId) RuleTotal
CROSS APPLY (select count(distinct person_id) as total from #PrimaryCriteriaEvents) PersonTotal
LEFT JOIN feas_study_result SR on (POWER(2,RuleTotal.total_rules) - POWER(2,cti.sequence) - 1) = SR.inclusion_rule_mask -- POWER(2,rule count) - POWER(2,rule sequence) - 1 is the mask for 'all except this rule' 
WHERE cti.study_id = @studyId
;

-- calculate totals
delete from feas_study_index_stats where study_id = @studyId;
insert into feas_study_index_stats (study_id, person_count, match_count)
select @studyId as study_id, 
(select count(distinct person_id) as total from #PrimaryCriteriaEvents) as person_count,
coalesce((
  select sr.person_count 
  from feas_study_result sr
  CROSS APPLY (select count(*) as total_rules from feasibility_inclusion where study_id = @studyId) RuleTotal
  where study_id = @studyId and sr.inclusion_rule_mask = POWER(2,RuleTotal.total_rules)-1
),0) as match_count
;

TRUNCATE TABLE #BestMatchEvent;
DROP TABLE #BestMatchEvent;

TRUNCATE TABLE #inclusionRuleCohorts 
DROP TABLE #inclusionRuleCohorts;

TRUNCATE TABLE #PrimaryCriteriaEvents;
DROP TABLE #PrimaryCriteriaEvents;

TRUNCATE TABLE #Codesets;
DROP TABLE #Codesets;
