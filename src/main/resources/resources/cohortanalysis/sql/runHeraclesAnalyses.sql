
--HERACLES

--Patrick Ryan

--last updated: 21 Jan 2015


--chagnes for v4 to v5 that impact HERACLES

--death :  cause_of_death_concept_id -> cause_concept_id
--visit:  place_of_service_concept_id -> visit_concept_id
--f/r:  associated_provider_id -> provider_id
--	prescribing_provider_id -> provider_id

--remove:  disease_class_concept_id analyses
	
--observation:  no more range_high / range_low...now from measurement
--	-options:  remove observation graphs in v5?   add new measurement?
	



{DEFAULT @CDM_schema = 'CDM_schema'}    --CDM_schema = @CDM_schema
{DEFAULT @results_schema = 'scratch'}   --results_schema = @results_schema
-- {DEFAULT @results_schema = 'CDM_schema'}  --results_schema = @results_schema
{DEFAULT @cohort_table = 'COHORT'}  --cohort_table = @cohort_table
{DEFAULT @source_name = 'TRUVEN MDCD'}   --source_name = @source_name
{DEFAULT @source_id = -1}   --source_id = @source_id
{DEFAULT @smallcellcount = 5}    --smallcellcount = @smallcellcount
{DEFAULT @createTable = FALSE}    --createTable = @createTable
{DEFAULT @runHERACLESHeel = FALSE}   --runHERACLESHeel = @runHERACLESHeel
{DEFAULT @CDM_version = '4'}  --we support 4 or 5,   CDM_version = @CDM_version
{DEFAULT @cohort_period_only = FALSE}

{DEFAULT @cohort_definition_id = '2000003550,2000004386'}   --cohort_definition_id = @cohort_definition_id

--'2000002372'  1 large cohort
--'2000003550,2000004386'     2 10k sized cohorts

{DEFAULT @list_of_analysis_ids = '0,1,2,3,4,5,6,7,8,9,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,200,201,202,203,204,205,206,207,208,209,210,211,220,400,401,402,403,404,405,406,407,408,409,410,411,412,413,414,415,416,417,418,419,420,500,501,502,503,504,505,506,509,510,511,512,513,514,515,600,601,602,603,604,605,606,607,608,609,610,611,612,613,614,615,616,617,618,619,620,700,701,702,703,704,705,706,707,708,709,710,711,712,713,714,715,716,717,717,718,719,720,800,801,802,803,804,805,806,807,808,809,810,811,812,813,814,815,819,820,900,901,902,903,904,905,906,907,908,909,910,911,912,913,914,915,916,917,918,919,920,1000,1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1011,1012,1013,1014,1015,1016,1017,1018,1019,1020,1100,1101,1102,1103,1200,1201,1202,1203,1700,1701,1800,1801,1802,1803,1804,1805,1806,1807,1808,1809,1810,1811,1812,1813,1814,1815,1816,1817,1818,1819,1820,1821,1830,1831,1840,1841,1850,1851,1860,1861,1870,1871,1300,1301,1302,1303,1304,1305,1306,1307,1308,1309,1310,1311,1312,1313,1314,1315,1316,1317,1318,1319,1320'}
--list_of_analysis_ids = @list_of_analysis_ids


{DEFAULT @condition_concept_ids = ''}   --list of condition concepts to be used throughout
--condition_concept_ids = @condition_concept_ids
{DEFAULT @drug_concept_ids = ''}   --list of drug concepts to be used throughout
--drug_concept_ids = @drug_concept_ids
{DEFAULT @procedure_concept_ids = ''}   --list of procedure concepts to be used throughout
--procedure_concept_ids = @procedure_concept_ids
{DEFAULT @observation_concept_ids = ''}   --list of observation concepts to be used throughout
--observation_concept_ids = @observation_concept_ids
{DEFAULT @measurement_concept_ids = ''}   --list of measurement concepts to be used throughout
--measurement_concept_ids = @measurement_concept_ids


--all: '0,1,2,3,4,5,6,7,8,9,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,200,201,202,203,204,205,206,207,208,209,210,211,220,400,401,402,403,404,405,406,407,408,409,410,411,412,413,414,415,416,417,418,419,420,500,501,502,503,504,505,506,509,510,511,512,513,514,515,600,601,602,603,604,605,606,607,608,609,610,611,612,613,614,615,616,617,618,619,620,700,701,702,703,704,705,706,707,708,709,710,711,712,713,714,715,716,717,717,718,719,720,800,801,802,803,804,805,806,807,808,809,810,811,812,813,814,815,816,817,818,819,820,900,901,902,903,904,905,906,907,908,909,910,911,912,913,914,915,916,917,918,919,920,1000,1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1011,1012,1013,1014,1015,1016,1017,1018,1019,1020,1100,1101,1102,1103,1200,1201,1202,1203,1700,1701,1800,1801,1802,1803,1804,1805,1806,1807,1808,1809,1810,1811,1812,1813,1814,1815,1816,1817,1818,1819,1820,1821,1830,1831,1840,1841,1850,1851,1860,1861,1870,1871'
--person: '0,1,2,3,4,5,6,7,8,9'
--observation: '101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117'
--visits: '200,201,202,203,204,205,206,207,208,209,210,211,220'
--condition: '400,401,402,403,404,405,406,407,408,409,410,411,412,413,414,415,416,417,418,419,420'
--death: '500,501,502,503,504,505,506,509,510,511,512,513,514,515'
--procedure: '600,601,602,603,604,605,606,607,608,609,610,611,612,613,614,615,616,617,618,619,620'
--drug: '700,701,702,703,704,705,706,707,708,709,710,711,712,713,714,715,716,717,717,718,719,720'
--observation: '800,801,802,803,804,805,806,807,808,809,810,811,812,813,814,815,819,820'
--drug era: '900,901,902,903,904,905,906,907,908,909,910,911,912,913,914,915,916,917,918,919,920'
--condition era: '1000,1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1011,1012,1013,1014,1015,1016,1017,1018,1019,1020'
--location: '1100,1101,1102,1103'
--care site: '1200,1201,1202,1203'
--cohort: '1700,1701'
--cohort-specific analyses: '1800,1801,1802,1803,1804,1805,1806,1807,1808,1809,1810,1811,1812,1813,1814,1815,1816,1817,1818,1819,1820,1821,1830,1831,1840,1841,1850,1851,1860,1861,1870,1871'
--measurement: 1300,1301,1302,1303,1304,1305,1306,1307,1308,1309,1310,1311,1312,1313,1314,1315,1316,1317,1318,1319,1320
--data completeness: gender:    2001, 2002, 2003, 2004, 2005, 2006, 2007
--data completeness: race:      2011, 2012, 2013, 2014, 2015, 2016, 2017
--data completeness: ethnicity: 2021, 2022, 2023, 2024, 2025, 2026, 2027
--entropy: 2031

delete from @results_schema.HERACLES_results where cohort_definition_id IN (@cohort_definition_id) and analysis_id IN (@list_of_analysis_ids);
delete from @results_schema.HERACLES_results_dist where cohort_definition_id IN (@cohort_definition_id) and analysis_id IN (@list_of_analysis_ids);

--7. generate results for analysis_results

IF OBJECT_ID('HERACLES_cohort', 'U') IS NOT NULL --This should only do something in Oracle
  drop table HERACLES_cohort;

IF OBJECT_ID('tempdb..#HERACLES_cohort', 'U') IS NOT NULL
  drop table #HERACLES_cohort;
  
select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date 
	into #HERACLES_cohort
from @results_schema.cohort
where cohort_definition_id in (@cohort_definition_id)
;  

--{0 IN (@list_of_analysis_ids)}?{
-- 0	Number of persons
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 0 as analysis_id,  '@source_name' as stratum_1, COUNT_BIG(distinct person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id;

insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 0 as analysis_id, '@source_name' as stratum_1, COUNT_BIG(distinct person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id;

--}




--HERACLES Analyses on PERSON table


--{1 IN (@list_of_analysis_ids)}?{
-- 1	Number of persons
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id, 1 as analysis_id,  COUNT_BIG(distinct person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id;
--}


--{2 IN (@list_of_analysis_ids)}?{
-- 2	Number of persons by gender
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 2 as analysis_id,  gender_concept_id as stratum_1, COUNT_BIG(distinct person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id, GENDER_CONCEPT_ID
;
--}



--{3 IN (@list_of_analysis_ids)}?{
-- 3	Number of persons by year of birth
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 3 as analysis_id,  year_of_birth as stratum_1, COUNT_BIG(distinct person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id, YEAR_OF_BIRTH
;
--}


--{4 IN (@list_of_analysis_ids)}?{
-- 4	Number of persons by race
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 4 as analysis_id,  RACE_CONCEPT_ID as stratum_1, COUNT_BIG(distinct person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id, RACE_CONCEPT_ID
;
--}



--{5 IN (@list_of_analysis_ids)}?{
-- 5	Number of persons by ethnicity
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 5 as analysis_id,  ETHNICITY_CONCEPT_ID as stratum_1, COUNT_BIG(distinct person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id, ETHNICITY_CONCEPT_ID
;
--}





--{7 IN (@list_of_analysis_ids)}?{
-- 7	Number of persons with invalid provider_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id, 7 as analysis_id,  COUNT_BIG(p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
	left join @CDM_schema.provider pr1
	on p1.provider_id = pr1.provider_id
where p1.provider_id is not null
	and pr1.provider_id is null
group by c1.cohort_definition_id
;
--}



--{8 IN (@list_of_analysis_ids)}?{
-- 8	Number of persons with invalid location_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id, 8 as analysis_id,  COUNT_BIG(p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
	left join @CDM_schema.location l1
	on p1.location_id = l1.location_id
where p1.location_id is not null
	and l1.location_id is null
group by c1.cohort_definition_id
;

--}


--{9 IN (@list_of_analysis_ids)}?{
-- 9	Number of persons with invalid care_site_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id, 9 as analysis_id,  COUNT_BIG(p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
	left join @CDM_schema.care_site cs1
	on p1.care_site_id = cs1.care_site_id
where p1.care_site_id is not null
	and cs1.care_site_id is null
group by c1.cohort_definition_id
;
--}







----/********************************************

--HERACLES Analyses on OBSERVATION_PERIOD table

--*********************************************/

--{101 IN (@list_of_analysis_ids)}?{
-- 101	Number of persons by age, with age at first observation period
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 101 as analysis_id,   year(op1.index_date) - p1.YEAR_OF_BIRTH as stratum_1, COUNT_BIG(p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
	inner join (select person_id, MIN(observation_period_start_date) as index_date from @CDM_schema.OBSERVATION_PERIOD group by PERSON_ID) op1
	on p1.PERSON_ID = op1.PERSON_ID
group by c1.cohort_definition_id, year(op1.index_date) - p1.YEAR_OF_BIRTH
;
--}



--{102 IN (@list_of_analysis_ids)}?{
-- 102	Number of persons by gender by age, with age at first observation period
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id, 102 as analysis_id,  p1.gender_concept_id as stratum_1, year(op1.index_date) - p1.YEAR_OF_BIRTH as stratum_2, COUNT_BIG(p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
	inner join (select person_id, MIN(observation_period_start_date) as index_date from @CDM_schema.OBSERVATION_PERIOD group by PERSON_ID) op1
	on p1.PERSON_ID = op1.PERSON_ID
group by c1.cohort_definition_id, p1.gender_concept_id, year(op1.index_date) - p1.YEAR_OF_BIRTH
;
--}


--{103 IN (@list_of_analysis_ids)}?{
-- 103	Distribution of age at first observation period
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id, 
	103 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	year(op1.index_date) - p1.YEAR_OF_BIRTH as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id order by year(op1.index_date) - p1.YEAR_OF_BIRTH))/(COUNT_BIG(year(op1.index_date) - p1.YEAR_OF_BIRTH) over (partition by c1.cohort_definition_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
	inner join (select person_id, MIN(observation_period_start_date) as index_date from @CDM_schema.OBSERVATION_PERIOD group by PERSON_ID) op1
	on p1.PERSON_ID = op1.PERSON_ID
) t1
group by cohort_definition_id
;

--}




--{104 IN (@list_of_analysis_ids)}?{
-- 104	Distribution of age at first observation period by gender
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id, 
	104 as analysis_id,
	gender_concept_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	p1.gender_concept_id,
	year(op1.index_date) - p1.YEAR_OF_BIRTH as count_value,
	1.0*(row_number() over (partition by p1.gender_concept_id, c1.cohort_definition_id order by year(op1.index_date) - p1.YEAR_OF_BIRTH))/(COUNT_BIG(year(op1.index_date) - p1.YEAR_OF_BIRTH) over (partition by p1.gender_concept_id, c1.cohort_definition_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
	inner join (select person_id, MIN(observation_period_start_date) as index_date from @CDM_schema.OBSERVATION_PERIOD group by PERSON_ID) op1
	on p1.PERSON_ID = op1.PERSON_ID
) t1
group by cohort_definition_id, gender_concept_id
;
--}




--{105 IN (@list_of_analysis_ids)}?{
-- 105	Length of observation (days) of first observation period
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	105 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	DATEDIFF(dd,op1.observation_period_start_date, op1.observation_period_end_date) as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id order by DATEDIFF(dd,op1.observation_period_start_date, op1.observation_period_end_date)))/(COUNT_BIG(DATEDIFF(dd,op1.observation_period_start_date, op1.observation_period_end_date)) over (partition by c1.cohort_definition_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
	inner join 
	(select person_id, 
		OBSERVATION_PERIOD_START_DATE, 
		OBSERVATION_PERIOD_END_DATE, 
		ROW_NUMBER() over (PARTITION by person_id order by observation_period_start_date asc) as rn1
		 from @CDM_schema.OBSERVATION_PERIOD
	) op1
	on p1.PERSON_ID = op1.PERSON_ID
	where op1.rn1 = 1
) t1
group by cohort_definition_id
;
--}


--{106 IN (@list_of_analysis_ids)}?{
-- 106	Length of observation (days) of first observation period by gender
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	106 as analysis_id,
	gender_concept_id as stratum_1,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	p1.gender_concept_id,
	DATEDIFF(dd,op1.observation_period_start_date, op1.observation_period_end_date) as count_value,
	1.0*(row_number() over (partition by p1.gender_concept_id, c1.cohort_definition_id order by DATEDIFF(dd,op1.observation_period_start_date, op1.observation_period_end_date)))/(COUNT_BIG(DATEDIFF(dd,op1.observation_period_start_date, op1.observation_period_end_date)) over (partition by p1.gender_concept_id, c1.cohort_definition_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
	inner join 
	(select person_id, 
		OBSERVATION_PERIOD_START_DATE, 
		OBSERVATION_PERIOD_END_DATE, 
		ROW_NUMBER() over (PARTITION by person_id order by observation_period_start_date asc) as rn1
		 from @CDM_schema.OBSERVATION_PERIOD
	) op1
	on p1.PERSON_ID = op1.PERSON_ID
	where op1.rn1 = 1
) t1
group by cohort_definition_id, gender_concept_id
;
--}



--{107 IN (@list_of_analysis_ids)}?{
-- 107	Length of observation (days) of first observation period by age decile
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	107 as analysis_id,
	age_decile as stratum_1,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	floor((year(op1.OBSERVATION_PERIOD_START_DATE) - p1.YEAR_OF_BIRTH)/10) as age_decile,
	DATEDIFF(dd,op1.observation_period_start_date, op1.observation_period_end_date) as count_value,
	1.0*(row_number() over (partition by floor((year(op1.OBSERVATION_PERIOD_START_DATE) - p1.YEAR_OF_BIRTH)/10), c1.cohort_definition_id order by DATEDIFF(dd,op1.observation_period_start_date, op1.observation_period_end_date)))/(COUNT_BIG(DATEDIFF(dd,op1.observation_period_start_date, op1.observation_period_end_date)) over (partition by floor((year(op1.OBSERVATION_PERIOD_START_DATE) - p1.YEAR_OF_BIRTH)/10), c1.cohort_definition_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
	inner join 
	(select person_id, 
		OBSERVATION_PERIOD_START_DATE, 
		OBSERVATION_PERIOD_END_DATE, 
		ROW_NUMBER() over (PARTITION by person_id order by observation_period_start_date asc) as rn1
		 from @CDM_schema.OBSERVATION_PERIOD
	) op1
	on p1.PERSON_ID = op1.PERSON_ID
	where op1.rn1 = 1
) t1
group by cohort_definition_id, age_decile
;
--}






--{108 IN (@list_of_analysis_ids)}?{
-- 108	Number of persons by length of observation period, in 30d increments
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	108 as analysis_id,  
	floor(DATEDIFF(dd, op1.observation_period_start_date, op1.observation_period_end_date)/30) as stratum_1, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
	inner join 
	(select person_id, 
		OBSERVATION_PERIOD_START_DATE, 
		OBSERVATION_PERIOD_END_DATE, 
		ROW_NUMBER() over (PARTITION by person_id order by observation_period_start_date asc) as rn1
		 from @CDM_schema.OBSERVATION_PERIOD
	) op1
	on p1.PERSON_ID = op1.PERSON_ID
	where op1.rn1 = 1
group by c1.cohort_definition_id, floor(DATEDIFF(dd, op1.observation_period_start_date, op1.observation_period_end_date)/30)
;
--}




--{109 IN (@list_of_analysis_ids)}?{
-- 109	Number of persons with continuous observation in each year
-- Note: using temp table instead of nested query because this gives vastly improved performance in Oracle

IF OBJECT_ID('temp_dates', 'U') IS NOT NULL --This should only do something in Oracle
  drop table temp_dates;

SELECT DISTINCT 
  YEAR(observation_period_start_date) AS obs_year,
  CAST(CAST(YEAR(observation_period_start_date) AS VARCHAR(4)) +  '01' + '01' AS DATE) AS obs_year_start,	
  CAST(CAST(YEAR(observation_period_start_date) AS VARCHAR(4)) +  '12' + '31' AS DATE) AS obs_year_end
INTO
  #temp_dates_1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id as cohort_definition_id from @results_schema.COHORT where cohort_definition_id in (@cohort_definition_id)) c1
	on p1.person_id = c1.subject_id
inner join
  @CDM_schema.observation_period op1
  on p1.person_id = op1.person_id
;

INSERT INTO @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
  109 AS analysis_id,  
	obs_year AS stratum_1, 
	COUNT_BIG(DISTINCT p1.person_id) AS count_value
FROM @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id as cohort_definition_id from @results_schema.COHORT where cohort_definition_id in (@cohort_definition_id)) c1
	on p1.person_id = c1.subject_id
inner join
  @CDM_schema.observation_period op1
  on p1.person_id = op1.person_id,
	#temp_dates_1
WHERE  
		observation_period_start_date <= obs_year_start
	AND 
		observation_period_end_date >= obs_year_end
GROUP BY 
	c1.cohort_definition_id, obs_year
;

TRUNCATE TABLE #temp_dates_1;
DROP TABLE #temp_dates_1;

--}


--{110 IN (@list_of_analysis_ids)}?{
-- 110	Number of persons with continuous observation in each month
-- Note: using temp table instead of nested query because this gives vastly improved performance in Oracle

IF OBJECT_ID('temp_dates', 'U') IS NOT NULL --This should only do something in Oracle
  drop table temp_dates;

SELECT DISTINCT 
  YEAR(observation_period_start_date)*100 + MONTH(observation_period_start_date) AS obs_month,
  CAST(CAST(YEAR(observation_period_start_date) AS VARCHAR(4)) +  RIGHT('0' + CAST(MONTH(OBSERVATION_PERIOD_START_DATE) AS VARCHAR(2)), 2) + '01' AS DATE) AS obs_month_start,  
  DATEADD(dd,-1,DATEADD(mm,1,CAST(CAST(YEAR(observation_period_start_date) AS VARCHAR(4)) +  RIGHT('0' + CAST(MONTH(OBSERVATION_PERIOD_START_DATE) AS VARCHAR(2)), 2) + '01' AS DATE))) AS obs_month_end
INTO
  #temp_dates_2
FROM @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
  @CDM_schema.observation_period op1
  on p1.person_id = op1.person_id
;


INSERT INTO @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
  110 AS analysis_id, 
	obs_month AS stratum_1, 
	COUNT_BIG(DISTINCT p1.person_id) AS count_value
FROM @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
  @CDM_schema.observation_period op1
  on p1.person_id = op1.person_id,
	#temp_dates_2
WHERE 
		observation_period_start_date <= obs_month_start
	AND 
		observation_period_end_date >= obs_month_end
GROUP BY 
	c1.cohort_definition_id, obs_month
;

TRUNCATE TABLE #temp_dates_2;
DROP TABLE #temp_dates_2;

--}



--{111 IN (@list_of_analysis_ids)}?{
-- 111	Number of persons by observation period start month
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	111 as analysis_id, 
	YEAR(observation_period_start_date)*100 + month(OBSERVATION_PERIOD_START_DATE) as stratum_1, 
	COUNT_BIG(distinct op1.PERSON_ID) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
  @CDM_schema.observation_period op1
  on p1.person_id = op1.person_id
group by c1.cohort_definition_id, YEAR(observation_period_start_date)*100 + month(OBSERVATION_PERIOD_START_DATE)
;
--}



--{112 IN (@list_of_analysis_ids)}?{
-- 112	Number of persons by observation period end month
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	112 as analysis_id,  
	YEAR(observation_period_end_date)*100 + month(observation_period_end_date) as stratum_1, 
	COUNT_BIG(distinct op1.PERSON_ID) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
  @CDM_schema.observation_period op1
  on p1.person_id = op1.person_id
group by c1.cohort_definition_id, YEAR(observation_period_end_date)*100 + month(observation_period_end_date)
;
--}


--{113 IN (@list_of_analysis_ids)}?{
-- 113	Number of persons by number of observation periods
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select cohort_definition_id, 
	113 as analysis_id,  
	op1.num_periods as stratum_1, COUNT_BIG(distinct op1.PERSON_ID) as count_value
from
	(select cohort_definition_id, person_id, COUNT_BIG(OBSERVATION_period_start_date) as num_periods 
		from @CDM_schema.OBSERVATION_PERIOD op0
		inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
			on op0.person_id = c1.subject_id
		group by cohort_definition_id, PERSON_ID) op1
group by cohort_definition_id, op1.num_periods
;
--}

--{114 IN (@list_of_analysis_ids)}?{
-- 114	Number of persons with observation period before year-of-birth
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select cohort_definition_id,
	114 as analysis_id,  
	COUNT_BIG(distinct p1.PERSON_ID) as count_value
from
	@CDM_schema.PERSON p1
	inner join (select cohort_definition_id, person_id, MIN(year(OBSERVATION_period_start_date)) as first_obs_year 
		from @CDM_schema.OBSERVATION_PERIOD op0
		inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
			on op0.person_id = c1.subject_id
		group by cohort_definition_id, PERSON_ID) op1
	on p1.person_id = op1.person_id
where p1.year_of_birth > op1.first_obs_year
group by cohort_definition_id
;
--}

--{115 IN (@list_of_analysis_ids)}?{
-- 115	Number of persons with observation period end < start
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	115 as analysis_id,  
	COUNT_BIG(op1.PERSON_ID) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
  @CDM_schema.observation_period op1
  on p1.person_id = op1.person_id
where op1.observation_period_end_date < op1.observation_period_start_date
group by c1.cohort_definition_id
;
--}



--{116 IN (@list_of_analysis_ids)}?{
-- 116	Number of persons with at least one day of observation in each year by gender and age decile
-- Note: using temp table instead of nested query because this gives vastly improved performance in Oracle

IF OBJECT_ID('temp_dates', 'U') IS NOT NULL --This should only do something in Oracle
  drop table temp_dates;

select distinct 
  YEAR(observation_period_start_date) as obs_year 
INTO
  #temp_dates_3
FROM @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
  @CDM_schema.observation_period op1
  on p1.person_id = op1.person_id
;

insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, count_value)
select c1.cohort_definition_id,
	116 as analysis_id,  
	t1.obs_year as stratum_1, 
	p1.gender_concept_id as stratum_2,
	floor((t1.obs_year - p1.year_of_birth)/10) as stratum_3,
	COUNT_BIG(distinct p1.PERSON_ID) as count_value
FROM @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
  @CDM_schema.observation_period op1
  on p1.person_id = op1.person_id
	,
	#temp_dates_3 t1
where year(op1.OBSERVATION_PERIOD_START_DATE) <= t1.obs_year
	and year(op1.OBSERVATION_PERIOD_END_DATE) >= t1.obs_year
group by c1.cohort_definition_id,
	t1.obs_year,
	p1.gender_concept_id,
	floor((t1.obs_year - p1.year_of_birth)/10)
;

TRUNCATE TABLE #temp_dates_3;
DROP TABLE #temp_dates_3;

--}


--{117 IN (@list_of_analysis_ids)}?{
-- 117	Number of persons with at least one day of observation in each year by gender and age decile
-- Note: using temp table instead of nested query because this gives vastly improved performance in Oracle

IF OBJECT_ID('temp_dates', 'U') IS NOT NULL --This should only do something in Oracle
  drop table temp_dates;

select distinct 
  YEAR(observation_period_start_date)*100 + MONTH(observation_period_start_date)  as obs_month
into 
  #temp_dates_4
FROM @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
  @CDM_schema.observation_period op1
  on p1.person_id = op1.person_id
;

insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	117 as analysis_id,  
	t1.obs_month as stratum_1,
	COUNT_BIG(distinct op1.PERSON_ID) as count_value
FROM @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
  @CDM_schema.observation_period op1
  on p1.person_id = op1.person_id,
	#temp_dates_4 t1
where YEAR(observation_period_start_date)*100 + MONTH(observation_period_start_date) <= t1.obs_month
	and YEAR(observation_period_end_date)*100 + MONTH(observation_period_end_date) >= t1.obs_month
group by c1.cohort_definition_id, t1.obs_month
;

TRUNCATE TABLE #temp_dates_4;
DROP TABLE #temp_dates_4;

--}


--/********************************************

--HERACLES Analyses on VISIT_OCCURRENCE table

--*********************************************/


--{200 IN (@list_of_analysis_ids)}?{
-- 200	Number of persons with at least one visit occurrence, by visit_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 200 as analysis_id,
	--{@CDM_version == '4'}?{
	vo1.place_of_service_CONCEPT_ID as stratum_1,
	--}
	--{@CDM_version == '5'}?{
	vo1.visit_CONCEPT_ID as stratum_1,
	--}
	COUNT_BIG(distinct vo1.PERSON_ID) as count_value
from
	@CDM_schema.visit_occurrence vo1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on vo1.person_id = c1.subject_id
--{@cohort_period_only == 'TRUE'}?{
WHERE vo1.visit_start_date>=c1.cohort_start_date and vo1.visit_end_date<=c1.cohort_end_date
--} 
group by c1.cohort_definition_id,
	--{@CDM_version == '4'}?{
	vo1.place_of_service_CONCEPT_ID
	--}
	--{@CDM_version == '5'}?{
	vo1.visit_CONCEPT_ID
	--}
;
--}


--{201 IN (@list_of_analysis_ids)}?{
-- 201	Number of visit occurrence records, by visit_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 201 as analysis_id, 
	--{@CDM_version == '4'}?{
	vo1.place_of_service_CONCEPT_ID as stratum_1,
	--}
	--{@CDM_version == '5'}?{
	vo1.visit_CONCEPT_ID as stratum_1,
	--}
	COUNT_BIG(vo1.PERSON_ID) as count_value
from
	@CDM_schema.visit_occurrence vo1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on vo1.person_id = c1.subject_id
--{@cohort_period_only == 'TRUE'}?{
WHERE vo1.visit_start_date>=c1.cohort_start_date and vo1.visit_end_date<=c1.cohort_end_date
--}  
group by c1.cohort_definition_id,
	--{@CDM_version == '4'}?{
	vo1.place_of_service_CONCEPT_ID
	--}
	--{@CDM_version == '5'}?{
	vo1.visit_CONCEPT_ID
	--}
;
--}



--{202 IN (@list_of_analysis_ids)}?{
-- 202	Number of persons by visit occurrence start month, by visit_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
	202 as analysis_id,   
	--{@CDM_version == '4'}?{
	vo1.place_of_service_CONCEPT_ID as stratum_1,
	--}
	--{@CDM_version == '5'}?{
	vo1.visit_CONCEPT_ID as stratum_1,
	--}
	YEAR(visit_start_date)*100 + month(visit_start_date) as stratum_2, 
	COUNT_BIG(distinct PERSON_ID) as count_value
from
	@CDM_schema.visit_occurrence vo1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on vo1.person_id = c1.subject_id
--{@cohort_period_only == 'TRUE'}?{  
WHERE vo1.visit_start_date>=c1.cohort_start_date and vo1.visit_end_date<=c1.cohort_end_date
--}  
group by c1.cohort_definition_id,
	--{@CDM_version == '4'}?{
	vo1.place_of_service_CONCEPT_ID,
	--}
	--{@CDM_version == '5'}?{
	vo1.visit_CONCEPT_ID,
	--}
	YEAR(visit_start_date)*100 + month(visit_start_date)
;
--}



--{203 IN (@list_of_analysis_ids)}?{
-- 203	Number of distinct visit occurrence concepts per person
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	203 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id,
	num_visits as count_value,
	1.0*(row_number() over (partition by cohort_definition_id order by num_visits))/(COUNT_BIG(num_visits) over (partition by cohort_definition_id)+1) as p1
from
	(
	select c1.cohort_definition_id, vo1.person_id, 
		--{@CDM_version == '4'}?{
		COUNT_BIG(distinct vo1.place_of_service_concept_id) as num_visits
		--}
		--{@CDM_version == '5'}?{
		COUNT_BIG(distinct vo1.visit_concept_id) as num_visits
		--}
	from
	@CDM_schema.visit_occurrence vo1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on vo1.person_id = c1.subject_id
--{@cohort_period_only == 'TRUE'}?{  
WHERE vo1.visit_start_date>=c1.cohort_start_date and vo1.visit_end_date<=c1.cohort_end_date
--}
group by c1.cohort_definition_id, vo1.person_id
	) t0
) t1
group by cohort_definition_id
;
--}



--{204 IN (@list_of_analysis_ids)}?{
-- 204	Number of persons with at least one visit occurrence, by visit_concept_id by calendar year by gender by age decile
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)
select c1.cohort_definition_id,
	204 as analysis_id,   
	--{@CDM_version == '4'}?{
	vo1.place_of_service_CONCEPT_ID as stratum_1,
	--}
	--{@CDM_version == '5'}?{
	vo1.visit_CONCEPT_ID as stratum_1,
	--}
	YEAR(visit_start_date) as stratum_2,
	p1.gender_concept_id as stratum_3,
	floor((year(visit_start_date) - p1.year_of_birth)/10) as stratum_4, 
	COUNT_BIG(distinct p1.PERSON_ID) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
@CDM_schema.visit_occurrence vo1
on p1.person_id = vo1.person_id
--{@cohort_period_only == 'TRUE'}?{
WHERE vo1.visit_start_date>=c1.cohort_start_date and vo1.visit_end_date<=c1.cohort_end_date
--}
group by c1.cohort_definition_id,
	--{@CDM_version == '4'}?{
	vo1.place_of_service_CONCEPT_ID,
	--}
	--{@CDM_version == '5'}?{
	vo1.visit_CONCEPT_ID,
	--}
	YEAR(visit_start_date),
	p1.gender_concept_id,
	floor((year(visit_start_date) - p1.year_of_birth)/10)
;
--}





--{206 IN (@list_of_analysis_ids)}?{
-- 206	Distribution of age by visit_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	206 as analysis_id,
	{@CDM_version == '4'}?{ place_of_service_CONCEPT_ID } {@CDM_version == '5'}?{ visit_CONCEPT_ID } as stratum_1,
	gender_concept_id as stratum_2,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	--{@CDM_version == '4'}?{
	vo1.place_of_service_CONCEPT_ID,
	--}
	--{@CDM_version == '5'}?{
	vo1.visit_CONCEPT_ID,
	--}
	p1.gender_concept_id,
	vo1.visit_start_year - p1.year_of_birth as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, {@CDM_version == '4'}?{ vo1.place_of_service_CONCEPT_ID } {@CDM_version == '5'}?{ vo1.visit_CONCEPT_ID }, p1.gender_concept_id order by vo1.visit_start_year - p1.year_of_birth))/(COUNT_BIG(vo1.visit_start_year - p1.year_of_birth) over (partition by c1.cohort_definition_id, {@CDM_version == '4'}?{ vo1.place_of_service_CONCEPT_ID } {@CDM_version == '5'}?{ vo1.visit_CONCEPT_ID }, p1.gender_concept_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
(select vo0.person_id, 
	--{@CDM_version == '4'}?{
	vo0.place_of_service_CONCEPT_ID,
	--}
	--{@CDM_version == '5'}?{
	vo0.visit_CONCEPT_ID,
	--} 
	min(year(vo0.visit_start_date)) as visit_start_year
from @CDM_schema.visit_occurrence vo0
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on vo0.person_id = c1.subject_id
--{@cohort_period_only == 'TRUE'}?{  
WHERE vo0.visit_start_date>=c1.cohort_start_date and vo0.visit_end_date<=c1.cohort_end_date
--}
group by person_id, 
	--{@CDM_version == '4'}?{
	vo0.place_of_service_CONCEPT_ID
	--}
	--{@CDM_version == '5'}?{
	vo0.visit_CONCEPT_ID
	--}place_of_service_concept_id
) vo1
on p1.person_id = vo1.person_id
) t1
group by cohort_definition_id, 
	{@CDM_version == '4'}?{ place_of_service_CONCEPT_ID } {@CDM_version == '5'}?{ visit_CONCEPT_ID }, 
	gender_concept_id
;
--}


--{207 IN (@list_of_analysis_ids)}?{
--207	Number of visit records with invalid person_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	207 as analysis_id,  
	COUNT_BIG(vo1.PERSON_ID) as count_value
from
	@CDM_schema.visit_occurrence vo1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on vo1.person_id = c1.subject_id
	left join @CDM_schema.PERSON p1
	on p1.person_id = vo1.person_id
where p1.person_id is null
group by c1.cohort_definition_id
;
--}


--{208 IN (@list_of_analysis_ids)}?{
--208	Number of visit records outside valid observation period
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select cohort_definition_id,
	208 as analysis_id,  
	COUNT_BIG(vo1.PERSON_ID) as count_value
from
	@CDM_schema.visit_occurrence vo1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on vo1.person_id = c1.subject_id
	left join @CDM_schema.OBSERVATION_PERIOD op1
	on op1.person_id = vo1.person_id
	and vo1.visit_start_date >= op1.observation_period_start_date
	and vo1.visit_start_date <= op1.observation_period_end_date
where op1.person_id is null
group by c1.cohort_definition_id
;
--}


--{209 IN (@list_of_analysis_ids)}?{
--209	Number of visit records with end date < start date
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	209 as analysis_id,  
	COUNT_BIG(vo1.PERSON_ID) as count_value
from
	@CDM_schema.visit_occurrence vo1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on vo1.person_id = c1.subject_id
where visit_end_date < visit_start_date
group by c1.cohort_definition_id
;
--}

--{210 IN (@list_of_analysis_ids)}?{
--210	Number of visit records with invalid care_site_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	210 as analysis_id,  
	COUNT_BIG(vo1.PERSON_ID) as count_value
from
	@CDM_schema.visit_occurrence vo1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on vo1.person_id = c1.subject_id
	left join @CDM_schema.care_site cs1
	on vo1.care_site_id = cs1.care_site_id
where vo1.care_site_id is not null
	and cs1.care_site_id is null
group by c1.cohort_definition_id
;
--}


--{211 IN (@list_of_analysis_ids)}?{
-- 211	Distribution of length of stay by visit_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	211 as analysis_id,
	--{@CDM_version == '4'}?{
	place_of_service_CONCEPT_ID
	--}
	--{@CDM_version == '5'}?{
	visit_CONCEPT_ID
	--}
	as stratum_1,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	--{@CDM_version == '4'}?{
	vo1.place_of_service_CONCEPT_ID,
	--}
	--{@CDM_version == '5'}?{
	vo1.visit_CONCEPT_ID,
	--}
	datediff(dd,visit_start_date,visit_end_date) as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, {@CDM_version == '4'}?{ vo1.place_of_service_CONCEPT_ID } {@CDM_version == '5'}?{ vo1.visit_CONCEPT_ID } order by datediff(dd,visit_start_date,visit_end_date)))/(COUNT_BIG(datediff(dd,visit_start_date,visit_end_date)) over (partition by c1.cohort_definition_id, {@CDM_version == '4'}?{ vo1.place_of_service_CONCEPT_ID } {@CDM_version == '5'}?{ vo1.visit_CONCEPT_ID })+1) as p1
from @CDM_schema.visit_occurrence vo1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on vo1.person_id = c1.subject_id
--{@cohort_period_only == 'TRUE'}?{
WHERE vo1.visit_start_date>=c1.cohort_start_date and vo1.visit_end_date<=c1.cohort_end_date
--}  
) t1
group by cohort_definition_id, 
	--{@CDM_version == '4'}?{
	place_of_service_CONCEPT_ID
	--}
	--{@CDM_version == '5'}?{
	visit_CONCEPT_ID
	--}
;
--}





--{220 IN (@list_of_analysis_ids)}?{
-- 220	Number of visit occurrence records by condition occurrence start month
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	220 as analysis_id,   
	YEAR(visit_start_date)*100 + month(visit_start_date) as stratum_1, 
	COUNT_BIG(PERSON_ID) as count_value
from
@CDM_schema.visit_occurrence vo1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on vo1.person_id = c1.subject_id
--{@cohort_period_only == 'TRUE'}?{  
WHERE vo1.visit_start_date>=c1.cohort_start_date and vo1.visit_end_date<=c1.cohort_end_date
--}  
group by c1.cohort_definition_id, YEAR(visit_start_date)*100 + month(visit_start_date)
;
--}





--/********************************************

--HERACLES Analyses on CONDITION_OCCURRENCE table

--*********************************************/


--{400 IN (@list_of_analysis_ids)}?{
-- 400	Number of persons with at least one condition occurrence, by condition_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	400 as analysis_id, 
	co1.condition_CONCEPT_ID as stratum_1,
	COUNT_BIG(distinct co1.PERSON_ID) as count_value
from
	@CDM_schema.condition_occurrence co1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on co1.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{
WHERE
--{@cohort_period_only == 'TRUE'}?{
	co1.condition_start_date>=c1.cohort_start_date and co1.condition_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@condition_concept_ids != ''}?{
	co1.condition_concept_id in (@condition_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	co1.condition_CONCEPT_ID
;
--}


--{401 IN (@list_of_analysis_ids)}?{
-- 401	Number of condition occurrence records, by condition_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	401 as analysis_id, 
	co1.condition_CONCEPT_ID as stratum_1,
	COUNT_BIG(co1.PERSON_ID) as count_value
from
	@CDM_schema.condition_occurrence co1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on co1.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	co1.condition_start_date>=c1.cohort_start_date and co1.condition_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@condition_concept_ids != ''}?{
	co1.condition_concept_id in (@condition_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	co1.condition_CONCEPT_ID
;
--}



--{402 IN (@list_of_analysis_ids)}?{
-- 402	Number of persons by condition occurrence start month, by condition_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
	402 as analysis_id,   
	co1.condition_concept_id as stratum_1,
	YEAR(condition_start_date)*100 + month(condition_start_date) as stratum_2, 
	COUNT_BIG(distinct PERSON_ID) as count_value
from
@CDM_schema.condition_occurrence co1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on co1.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{  
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	co1.condition_start_date>=c1.cohort_start_date and co1.condition_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@condition_concept_ids != ''}?{
	co1.condition_concept_id in (@condition_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	co1.condition_concept_id, 
	YEAR(condition_start_date)*100 + month(condition_start_date)
;
--}



--{403 IN (@list_of_analysis_ids)}?{
-- 403	Number of distinct condition occurrence concepts per person
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	403 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id, 
	num_conditions as count_value,
	1.0*(row_number() over (partition by cohort_definition_id order by num_conditions))/(COUNT_BIG(num_conditions) over (partition by cohort_definition_id)+1) as p1
from
	(
	select c1.cohort_definition_id, co1.person_id, COUNT_BIG(distinct co1.condition_concept_id) as num_conditions
	from
	@CDM_schema.condition_occurrence co1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on co1.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{  
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	co1.condition_start_date>=c1.cohort_start_date and co1.condition_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@condition_concept_ids != ''}?{
	co1.condition_concept_id in (@condition_concept_ids)
--}  
--}
	group by c1.cohort_definition_id, co1.person_id
	) t0
) t1
group by cohort_definition_id
;
--}



--{404 IN (@list_of_analysis_ids)}?{
-- 404	Number of persons with at least one condition occurrence, by condition_concept_id by calendar year by gender by age decile
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)
select c1.cohort_definition_id,
	404 as analysis_id,   
	co1.condition_concept_id as stratum_1,
	YEAR(condition_start_date) as stratum_2,
	p1.gender_concept_id as stratum_3,
	floor((year(condition_start_date) - p1.year_of_birth)/10) as stratum_4, 
	COUNT_BIG(distinct p1.PERSON_ID) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
@CDM_schema.condition_occurrence co1
on p1.person_id = co1.person_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{ 
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	co1.condition_start_date>=c1.cohort_start_date and co1.condition_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@condition_concept_ids != ''}?{
	co1.condition_concept_id in (@condition_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	co1.condition_concept_id, 
	YEAR(condition_start_date),
	p1.gender_concept_id,
	floor((year(condition_start_date) - p1.year_of_birth)/10)
;
--}

--{405 IN (@list_of_analysis_ids)}?{
-- 405	Number of condition occurrence records, by condition_concept_id by condition_type_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
	405 as analysis_id, 
	co1.condition_CONCEPT_ID as stratum_1,
	co1.condition_type_concept_id as stratum_2,
	COUNT_BIG(co1.PERSON_ID) as count_value
from
	@CDM_schema.condition_occurrence co1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on co1.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{  
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	co1.condition_start_date>=c1.cohort_start_date and co1.condition_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@condition_concept_ids != ''}?{
	co1.condition_concept_id in (@condition_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	co1.condition_CONCEPT_ID,	
	co1.condition_type_concept_id
;
--}



--{406 IN (@list_of_analysis_ids)}?{
-- 406	Distribution of age by condition_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	406 as analysis_id,
	condition_concept_id as stratum_1,
	gender_concept_id as stratum_2,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	co1.condition_concept_id,
	p1.gender_concept_id,
	co1.condition_start_year - p1.year_of_birth as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, co1.condition_concept_id, p1.gender_concept_id order by co1.condition_start_year - p1.year_of_birth))/(COUNT_BIG(co1.condition_start_year - p1.year_of_birth) over (partition by c1.cohort_definition_id, co1.condition_concept_id, p1.gender_concept_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
(select person_id, condition_concept_id, min(year(condition_start_date)) as condition_start_year
from @CDM_schema.condition_occurrence co0
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on co0.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{
WHERE 
--{@condition_concept_ids != ''}?{
	condition_concept_id in (@condition_concept_ids)
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@cohort_period_only == 'TRUE'}?{
	co0.condition_start_date>=c1.cohort_start_date and co0.condition_end_date<=c1.cohort_end_date
--}
--}
group by person_id, condition_concept_id
) co1
on p1.person_id = co1.person_id
) t1
group by cohort_definition_id, condition_concept_id, gender_concept_id
;
--}





--{409 IN (@list_of_analysis_ids)}?{
-- 409	Number of condition occurrence records with invalid person_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id, 
	409 as analysis_id,  
	COUNT_BIG(co1.PERSON_ID) as count_value
from
	@CDM_schema.condition_occurrence co1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on co1.person_id = c1.subject_id
	left join @CDM_schema.PERSON p1
	on p1.person_id = co1.person_id
where p1.person_id is null
group by c1.cohort_definition_id
;
--}


--{410 IN (@list_of_analysis_ids)}?{
-- 410	Number of condition occurrence records outside valid observation period
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	410 as analysis_id,  
	COUNT_BIG(co1.PERSON_ID) as count_value
from
	@CDM_schema.condition_occurrence co1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on co1.person_id = c1.subject_id
	left join @CDM_schema.OBSERVATION_PERIOD op1
	on op1.person_id = co1.person_id
	and co1.condition_start_date >= op1.observation_period_start_date
	and co1.condition_start_date <= op1.observation_period_end_date
where op1.person_id is null
group by c1.cohort_definition_id
;
--}


--{411 IN (@list_of_analysis_ids)}?{
-- 411	Number of condition occurrence records with end date < start date
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	411 as analysis_id,  
	COUNT_BIG(co1.PERSON_ID) as count_value
from
	@CDM_schema.condition_occurrence co1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on co1.person_id = c1.subject_id
where co1.condition_end_date < co1.condition_start_date
group by c1.cohort_definition_id
;
--}


--{412 IN (@list_of_analysis_ids)}?{
-- 412	Number of condition occurrence records with invalid provider_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	412 as analysis_id,  
	COUNT_BIG(co1.PERSON_ID) as count_value
from
	@CDM_schema.condition_occurrence co1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on co1.person_id = c1.subject_id
	left join @CDM_schema.provider p1
	on p1.provider_id =   {@CDM_version == '4'}?{ co1.associated_provider_id } {@CDM_version == '5'}?{ co1.provider_id } 
where {@CDM_version == '4'}?{ co1.associated_provider_id } {@CDM_version == '5'}?{ co1.provider_id }  is not null
	and p1.provider_id is null
group by c1.cohort_definition_id
;
--}

--{413 IN (@list_of_analysis_ids)}?{
-- 413	Number of condition occurrence records with invalid visit_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	413 as analysis_id,  
	COUNT_BIG(co1.PERSON_ID) as count_value
from
	@CDM_schema.condition_occurrence co1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on co1.person_id = c1.subject_id
	left join @CDM_schema.visit_occurrence vo1
	on co1.visit_occurrence_id = vo1.visit_occurrence_id
where co1.visit_occurrence_id is not null
	and vo1.visit_occurrence_id is null
group by c1.cohort_definition_id
;
--}

--{420 IN (@list_of_analysis_ids)}?{
-- 420	Number of condition occurrence records by condition occurrence start month
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	420 as analysis_id,   
	YEAR(condition_start_date)*100 + month(condition_start_date) as stratum_1, 
	COUNT_BIG(PERSON_ID) as count_value
from
@CDM_schema.condition_occurrence co1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on co1.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{  
WHERE
--{@cohort_period_only == 'TRUE'}?{
	co1.condition_start_date>=c1.cohort_start_date and co1.condition_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@condition_concept_ids != ''}?{
	co1.condition_concept_id in (@condition_concept_ids)
--}  
--} 
group by c1.cohort_definition_id, 
	YEAR(condition_start_date)*100 + month(condition_start_date)
;
--}



--/********************************************

--HERACLES Analyses on DEATH table

--*********************************************/



--{500 IN (@list_of_analysis_ids)}?{
-- 500	Number of persons with death, by cause_of_death_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	500 as analysis_id, 
	{@CDM_version == '4'}?{ d1.cause_of_death_concept_id } {@CDM_version == '5'}?{ d1.cause_CONCEPT_ID } 	 as stratum_1,
	COUNT_BIG(distinct d1.PERSON_ID) as count_value
from
	@CDM_schema.death d1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on d1.person_id = c1.subject_id
--{@cohort_period_only == 'TRUE'}?{
WHERE d1.death_date>=c1.cohort_start_date and d1.death_date<=c1.cohort_end_date
--}
group by c1.cohort_definition_id,
	{@CDM_version == '4'}?{ d1.cause_of_death_concept_id } {@CDM_version == '5'}?{ d1.cause_CONCEPT_ID } 
;
--}


--{501 IN (@list_of_analysis_ids)}?{
-- 501	Number of records of death, by cause_of_death_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	501 as analysis_id, 
	{@CDM_version == '4'}?{ d1.cause_of_death_concept_id } {@CDM_version == '5'}?{ d1.cause_CONCEPT_ID }  as stratum_1,
	COUNT_BIG(d1.PERSON_ID) as count_value
from
	@CDM_schema.death d1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on d1.person_id = c1.subject_id
--{@cohort_period_only == 'TRUE'}?{
WHERE d1.death_date>=c1.cohort_start_date and d1.death_date<=c1.cohort_end_date
--}
group by c1.cohort_definition_id, 
	{@CDM_version == '4'}?{ d1.cause_of_death_concept_id } {@CDM_version == '5'}?{ d1.cause_CONCEPT_ID } 
;
--}



--{502 IN (@list_of_analysis_ids)}?{
-- 502	Number of persons by condition occurrence start month
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	502 as analysis_id,   
	YEAR(death_date)*100 + month(death_date) as stratum_1, 
	COUNT_BIG(distinct PERSON_ID) as count_value
from
	@CDM_schema.death d1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on d1.person_id = c1.subject_id
--{@cohort_period_only == 'TRUE'}?{
WHERE d1.death_date>=c1.cohort_start_date and d1.death_date<=c1.cohort_end_date
--}
group by c1.cohort_definition_id,
	YEAR(death_date)*100 + month(death_date)
;
--}



--{504 IN (@list_of_analysis_ids)}?{
-- 504	Number of persons with a death, by calendar year by gender by age decile
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, count_value)
select c1.cohort_definition_id,
	504 as analysis_id,   
	YEAR(death_date) as stratum_1,
	p1.gender_concept_id as stratum_2,
	floor((year(death_date) - p1.year_of_birth)/10) as stratum_3, 
	COUNT_BIG(distinct p1.PERSON_ID) as count_value
from @CDM_schema.PERSON p1
inner join
@CDM_schema.death d1
on p1.person_id = d1.person_id
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on d1.person_id = c1.subject_id
--{@cohort_period_only == 'TRUE'}?{  
WHERE d1.death_date>=c1.cohort_start_date and d1.death_date<=c1.cohort_end_date
--}
group by c1.cohort_definition_id,
	YEAR(death_date),
	p1.gender_concept_id,
	floor((year(death_date) - p1.year_of_birth)/10)
;
--}

--{505 IN (@list_of_analysis_ids)}?{
-- 505	Number of death records, by death_type_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	505 as analysis_id, 
	death_type_concept_id as stratum_1,
	COUNT_BIG(PERSON_ID) as count_value
from
	@CDM_schema.death d1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on d1.person_id = c1.subject_id
--{@cohort_period_only == 'TRUE'}?{  
WHERE d1.death_date>=c1.cohort_start_date and d1.death_date<=c1.cohort_end_date
--}  
group by c1.cohort_definition_id,
	death_type_concept_id
;
--}



--{506 IN (@list_of_analysis_ids)}?{
-- 506	Distribution of age by condition_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	506 as analysis_id,
	gender_concept_id as stratum_1,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	p1.gender_concept_id,
	d1.death_year - p1.year_of_birth as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, p1.gender_concept_id order by d1.death_year - p1.year_of_birth))/(COUNT_BIG(d1.death_year - p1.year_of_birth) over (partition by c1.cohort_definition_id, p1.gender_concept_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
(select person_id, min(year(death_date)) as death_year
from @CDM_schema.death d0
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on d0.person_id = c1.subject_id
--{@cohort_period_only == 'TRUE'}?{  
WHERE d0.death_date>=c1.cohort_start_date and d0.death_date<=c1.cohort_end_date
--}
group by person_id
) d1
on p1.person_id = d1.person_id
) t1
group by cohort_definition_id, gender_concept_id
;
--}



--{509 IN (@list_of_analysis_ids)}?{
-- 509	Number of death records with invalid person_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id, 509 as analysis_id, 
	COUNT_BIG(d1.PERSON_ID) as count_value
from
	@CDM_schema.death d1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on d1.person_id = c1.subject_id
		left join @CDM_schema.PERSON p1
		on d1.person_id = p1.person_id
where p1.person_id is null
group by c1.cohort_definition_id
;
--}



--{510 IN (@list_of_analysis_ids)}?{
-- 510	Number of death records outside valid observation period
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	510 as analysis_id, 
	COUNT_BIG(d1.PERSON_ID) as count_value
from
	@CDM_schema.death d1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on d1.person_id = c1.subject_id
		left join @CDM_schema.OBSERVATION_PERIOD op1
		on d1.person_id = op1.person_id
		and d1.death_date >= op1.observation_period_start_date
		and d1.death_date <= op1.observation_period_end_date
where op1.person_id is null
group by c1.cohort_definition_id
;
--}


--{511 IN (@list_of_analysis_ids)}?{
-- 511	Distribution of time from death to last condition
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	511 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	datediff(dd,d1.death_date, t0.max_date) as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id order by datediff(dd,d1.death_date, t0.max_date)))/(COUNT_BIG(datediff(dd,d1.death_date, t0.max_date)) over (partition by c1.cohort_definition_id)+1) as p1
from @CDM_schema.death d1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on d1.person_id = c1.subject_id
	inner join
	(
		select person_id, max(condition_start_date) as max_date
		from @CDM_schema.condition_occurrence co1
		inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
			on co1.person_id = c1.subject_id
		group by person_id
	) t0
	on d1.person_id = t0.person_id
--{@cohort_period_only == 'TRUE'}?{  
WHERE d1.death_date>=c1.cohort_start_date and d1.death_date<=c1.cohort_end_date
--}
) t1
group by cohort_definition_id
;
--}


--{512 IN (@list_of_analysis_ids)}?{
-- 512	Distribution of time from death to last drug
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	512 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	datediff(dd,d1.death_date, t0.max_date) as count_value,
	1.0*(row_number() over (order by c1.cohort_definition_id, datediff(dd,d1.death_date, t0.max_date)))/(COUNT_BIG(datediff(dd,d1.death_date, t0.max_date)) over (partition by c1.cohort_definition_id)+1) as p1
from @CDM_schema.death d1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on d1.person_id = c1.subject_id
	inner join
	(
		select person_id, max(drug_exposure_start_date) as max_date
		from @CDM_schema.drug_exposure de1
		inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
			on de1.person_id = c1.subject_id
		group by person_id
	) t0
	on d1.person_id = t0.person_id
--{@cohort_period_only == 'TRUE'}?{  
WHERE d1.death_date>=c1.cohort_start_date and d1.death_date<=c1.cohort_end_date
--}
) t1
group by cohort_definition_id
;
--}


--{513 IN (@list_of_analysis_ids)}?{
-- 513	Distribution of time from death to last visit
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	513 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	datediff(dd,d1.death_date, t0.max_date) as count_value,
	1.0*(row_number() over (partition by cohort_definition_id order by datediff(dd,d1.death_date, t0.max_date)))/(COUNT_BIG(datediff(dd,d1.death_date, t0.max_date)) over (partition by cohort_definition_id)+1) as p1
from @CDM_schema.death d1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on d1.person_id = c1.subject_id
	inner join
	(
		select person_id, max(visit_start_date) as max_date
		from @CDM_schema.visit_occurrence vo1
		inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
			on vo1.person_id = c1.subject_id
		group by person_id
	) t0
	on d1.person_id = t0.person_id
--{@cohort_period_only == 'TRUE'}?{  
WHERE d1.death_date>=c1.cohort_start_date and d1.death_date<=c1.cohort_end_date
--}
) t1
group by cohort_definition_id
;
--}


--{514 IN (@list_of_analysis_ids)}?{
-- 514	Distribution of time from death to last procedure
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	514 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	datediff(dd,d1.death_date, t0.max_date) as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id order by datediff(dd,d1.death_date, t0.max_date)))/(COUNT_BIG(datediff(dd,d1.death_date, t0.max_date)) over (partition by cohort_definition_id)+1) as p1
from @CDM_schema.death d1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on d1.person_id = c1.subject_id
	inner join
	(
		select person_id, max(procedure_date) as max_date
		from @CDM_schema.procedure_occurrence po1
		inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
			on po1.person_id = c1.subject_id
		group by person_id
	) t0
	on d1.person_id = t0.person_id
--{@cohort_period_only == 'TRUE'}?{  
WHERE d1.death_date>=c1.cohort_start_date and d1.death_date<=c1.cohort_end_date
--}
) t1
group by cohort_definition_id
;
--}


--{515 IN (@list_of_analysis_ids)}?{
-- 515	Distribution of time from death to last observation
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	515 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	datediff(dd,d1.death_date, t0.max_date) as count_value,
	1.0*(row_number() over (partition by cohort_definition_id order by datediff(dd,d1.death_date, t0.max_date)))/(COUNT_BIG(datediff(dd,d1.death_date, t0.max_date)) over (partition by cohort_definition_id)+1) as p1
from @CDM_schema.death d1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on d1.person_id = c1.subject_id
	inner join
	(
		select person_id, max(observation_date) as max_date
		from @CDM_schema.observation o1
			inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
			on o1.person_id = c1.subject_id
		group by person_id
	) t0
	on d1.person_id = t0.person_id
--{@cohort_period_only == 'TRUE'}?{  
WHERE d1.death_date>=c1.cohort_start_date and d1.death_date<=c1.cohort_end_date
--}
) t1
group by cohort_definition_id
;
--}



--/********************************************

--HERACLES Analyses on PROCEDURE_OCCURRENCE table

--*********************************************/



--{600 IN (@list_of_analysis_ids)}?{
-- 600	Number of persons with at least one procedure occurrence, by procedure_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	600 as analysis_id, 
	po1.procedure_CONCEPT_ID as stratum_1,
	COUNT_BIG(distinct po1.PERSON_ID) as count_value
from
	@CDM_schema.procedure_occurrence po1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on po1.person_id = c1.subject_id
--{@procedure_concept_ids != '' | @cohort_period_only == 'TRUE'}?{
WHERE
--{@cohort_period_only == 'TRUE'}?{
	po1.procedure_date>=c1.cohort_start_date and po1.procedure_date<=c1.cohort_end_date
--}
--{@procedure_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@procedure_concept_ids != ''}?{
	po1.procedure_concept_id in (@procedure_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	po1.procedure_CONCEPT_ID
;
--}


--{601 IN (@list_of_analysis_ids)}?{
-- 601	Number of procedure occurrence records, by procedure_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	601 as analysis_id, 
	po1.procedure_CONCEPT_ID as stratum_1,
	COUNT_BIG(po1.PERSON_ID) as count_value
from
	@CDM_schema.procedure_occurrence po1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on po1.person_id = c1.subject_id
--{@procedure_concept_ids != '' | @cohort_period_only == 'TRUE'}?{
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	po1.procedure_date>=c1.cohort_start_date and po1.procedure_date<=c1.cohort_end_date
--}
--{@procedure_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@procedure_concept_ids != ''}?{
	po1.procedure_concept_id in (@procedure_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	po1.procedure_CONCEPT_ID
;
--}



--{602 IN (@list_of_analysis_ids)}?{
-- 602	Number of persons by procedure occurrence start month, by procedure_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
	602 as analysis_id,   
	po1.procedure_concept_id as stratum_1,
	YEAR(procedure_date)*100 + month(procedure_date) as stratum_2, 
	COUNT_BIG(distinct PERSON_ID) as count_value
from
	@CDM_schema.procedure_occurrence po1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on po1.person_id = c1.subject_id
--{@procedure_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	po1.procedure_date>=c1.cohort_start_date and po1.procedure_date<=c1.cohort_end_date
--}
--{@procedure_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@procedure_concept_ids != ''}?{
	po1.procedure_concept_id in (@procedure_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	po1.procedure_concept_id, 
	YEAR(procedure_date)*100 + month(procedure_date)
;
--}



--{603 IN (@list_of_analysis_ids)}?{
-- 603	Number of distinct procedure occurrence concepts per person
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	603 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id,
	num_procedures as count_value,
	1.0*(row_number() over (partition by cohort_definition_id order by num_procedures))/(COUNT_BIG(num_procedures) over (partition by cohort_definition_id)+1) as p1
from
	(
	select c1.cohort_definition_id, po1.person_id, COUNT_BIG(distinct po1.procedure_concept_id) as num_procedures
	from
	@CDM_schema.procedure_occurrence po1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on po1.person_id = c1.subject_id
--{@procedure_concept_ids != '' | @cohort_period_only == 'TRUE'}?{     
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	po1.procedure_date>=c1.cohort_start_date and po1.procedure_date<=c1.cohort_end_date
--}
--{@procedure_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@procedure_concept_ids != ''}?{
	po1.procedure_concept_id in (@procedure_concept_ids)
--}
--}
	group by c1.cohort_definition_id, po1.person_id
	) t0
) t1
group by cohort_definition_id
;
--}



--{604 IN (@list_of_analysis_ids)}?{
-- 604	Number of persons with at least one procedure occurrence, by procedure_concept_id by calendar year by gender by age decile
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)
select c1.cohort_definition_id,
	604 as analysis_id,   
	po1.procedure_concept_id as stratum_1,
	YEAR(procedure_date) as stratum_2,
	p1.gender_concept_id as stratum_3,
	floor((year(procedure_date) - p1.year_of_birth)/10) as stratum_4, 
	COUNT_BIG(distinct p1.PERSON_ID) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
@CDM_schema.procedure_occurrence po1
on p1.person_id = po1.person_id
--{@procedure_concept_ids != '' | @cohort_period_only == 'TRUE'}?{
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	po1.procedure_date>=c1.cohort_start_date and po1.procedure_date<=c1.cohort_end_date
--}
--{@procedure_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@procedure_concept_ids != ''}?{
	po1.procedure_concept_id in (@procedure_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	po1.procedure_concept_id, 
	YEAR(procedure_date),
	p1.gender_concept_id,
	floor((year(procedure_date) - p1.year_of_birth)/10)
;
--}

--{605 IN (@list_of_analysis_ids)}?{
-- 605	Number of procedure occurrence records, by procedure_concept_id by procedure_type_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
	605 as analysis_id, 
	po1.procedure_CONCEPT_ID as stratum_1,
	po1.procedure_type_concept_id as stratum_2,
	COUNT_BIG(po1.PERSON_ID) as count_value
from
	@CDM_schema.procedure_occurrence po1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on po1.person_id = c1.subject_id
--{@procedure_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	po1.procedure_date>=c1.cohort_start_date and po1.procedure_date<=c1.cohort_end_date
--}
--{@procedure_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@procedure_concept_ids != ''}?{
	po1.procedure_concept_id in (@procedure_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	po1.procedure_CONCEPT_ID,	
	po1.procedure_type_concept_id
;
--}



--{606 IN (@list_of_analysis_ids)}?{
-- 606	Distribution of age by procedure_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	606 as analysis_id,
	procedure_concept_id as stratum_1,
	gender_concept_id as stratum_2,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	po1.procedure_concept_id,
	p1.gender_concept_id,
	po1.procedure_start_year - p1.year_of_birth as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, po1.procedure_concept_id, p1.gender_concept_id order by po1.procedure_start_year - p1.year_of_birth))/(COUNT_BIG(po1.procedure_start_year - p1.year_of_birth) over (partition by c1.cohort_definition_id, po1.procedure_concept_id, p1.gender_concept_id)+1) as p1
from @CDM_schema.PERSON p1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on p1.person_id = c1.subject_id
inner join
(select person_id, procedure_concept_id, min(year(procedure_date)) as procedure_start_year
from @CDM_schema.procedure_occurrence po0
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on po0.person_id = c1.subject_id
--{@procedure_concept_ids != '' | @cohort_period_only == 'TRUE'}?{ 
WHERE 
--{@procedure_concept_ids != ''}?{
	po0.procedure_concept_id in (@procedure_concept_ids)
--}
--{@procedure_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@cohort_period_only == 'TRUE'}?{
	po0.procedure_date>=c1.cohort_start_date and po0.procedure_date<=c1.cohort_end_date
--}
--}
group by person_id, procedure_concept_id
) po1
on p1.person_id = po1.person_id
) t1
group by cohort_definition_id, procedure_concept_id, gender_concept_id
;
--}







--{609 IN (@list_of_analysis_ids)}?{
-- 609	Number of procedure occurrence records with invalid person_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	609 as analysis_id,  
	COUNT_BIG(po1.PERSON_ID) as count_value
from
	@CDM_schema.procedure_occurrence po1
		inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on po1.person_id = c1.subject_id
	left join @CDM_schema.PERSON p1
	on p1.person_id = po1.person_id
where p1.person_id is null
group by c1.cohort_definition_id
;
--}


--{610 IN (@list_of_analysis_ids)}?{
-- 610	Number of procedure occurrence records outside valid observation period
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	610 as analysis_id,  
	COUNT_BIG(po1.PERSON_ID) as count_value
from
	@CDM_schema.procedure_occurrence po1
		inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on po1.person_id = c1.subject_id
	left join @CDM_schema.OBSERVATION_PERIOD op1
	on op1.person_id = po1.person_id
	and po1.procedure_date >= op1.observation_period_start_date
	and po1.procedure_date <= op1.observation_period_end_date
where op1.person_id is null
group by c1.cohort_definition_id
;
--}



--{612 IN (@list_of_analysis_ids)}?{
-- 612	Number of procedure occurrence records with invalid provider_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	612 as analysis_id,  
	COUNT_BIG(po1.PERSON_ID) as count_value
from
	@CDM_schema.procedure_occurrence po1
		inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on po1.person_id = c1.subject_id
	left join @CDM_schema.provider p1
	on p1.provider_id = {@CDM_version == '4'}?{ po1.associated_provider_id } {@CDM_version == '5'}?{ po1.provider_id } 
where {@CDM_version == '4'}?{ po1.associated_provider_id } {@CDM_version == '5'}?{ po1.provider_id }  is not null
	and p1.provider_id is null
group by c1.cohort_definition_id
;
--}

--{613 IN (@list_of_analysis_ids)}?{
-- 613	Number of procedure occurrence records with invalid visit_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	613 as analysis_id,  
	COUNT_BIG(po1.PERSON_ID) as count_value
from
	@CDM_schema.procedure_occurrence po1
		inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on po1.person_id = c1.subject_id
	left join @CDM_schema.visit_occurrence vo1
	on po1.visit_occurrence_id = vo1.visit_occurrence_id
where po1.visit_occurrence_id is not null
	and vo1.visit_occurrence_id is null
group by c1.cohort_definition_id
;
--}


--{620 IN (@list_of_analysis_ids)}?{
-- 620	Number of procedure occurrence records by condition occurrence start month
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	620 as analysis_id,   
	YEAR(procedure_date)*100 + month(procedure_date) as stratum_1, 
	COUNT_BIG(PERSON_ID) as count_value
from
@CDM_schema.procedure_occurrence po1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on po1.person_id = c1.subject_id
--{@procedure_concept_ids != '' | @cohort_period_only == 'TRUE'}?{
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	po1.procedure_date>=c1.cohort_start_date and po1.procedure_date<=c1.cohort_end_date
--}
--{@procedure_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@procedure_concept_ids != ''}?{
	po1.procedure_concept_id in (@procedure_concept_ids)
--} 
--} 
group by c1.cohort_definition_id,
	YEAR(procedure_date)*100 + month(procedure_date)
;
--}


--/********************************************

--HERACLES Analyses on DRUG_EXPOSURE table

--*********************************************/




--{700 IN (@list_of_analysis_ids)}?{
-- 700	Number of persons with at least one drug occurrence, by drug_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	700 as analysis_id, 
	de1.drug_CONCEPT_ID as stratum_1,
	COUNT_BIG(distinct de1.PERSON_ID) as count_value
from
	@CDM_schema.drug_exposure de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_exposure_start_date>=c1.cohort_start_date and de1.drug_exposure_start_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--}    
--} 
group by c1.cohort_definition_id,
	de1.drug_CONCEPT_ID
;
--}


--{701 IN (@list_of_analysis_ids)}?{
-- 701	Number of drug occurrence records, by drug_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	701 as analysis_id, 
	de1.drug_CONCEPT_ID as stratum_1,
	COUNT_BIG(de1.PERSON_ID) as count_value
from
	@CDM_schema.drug_exposure de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_exposure_start_date>=c1.cohort_start_date and de1.drug_exposure_start_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--} 
--} 
group by c1.cohort_definition_id,
	de1.drug_CONCEPT_ID
;
--}



--{702 IN (@list_of_analysis_ids)}?{
-- 702	Number of persons by drug occurrence start month, by drug_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
	702 as analysis_id,   
	de1.drug_concept_id as stratum_1,
	YEAR(drug_exposure_start_date)*100 + month(drug_exposure_start_date) as stratum_2, 
	COUNT_BIG(distinct PERSON_ID) as count_value
from
@CDM_schema.drug_exposure de1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_exposure_start_date>=c1.cohort_start_date and de1.drug_exposure_start_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--}
--} 
group by c1.cohort_definition_id,
	de1.drug_concept_id, 
	YEAR(drug_exposure_start_date)*100 + month(drug_exposure_start_date)
;
--}



--{703 IN (@list_of_analysis_ids)}?{
-- 703	Number of distinct drug exposure concepts per person
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	703 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id, num_drugs as count_value,
	1.0*(row_number() over (partition by cohort_definition_id order by num_drugs))/(COUNT_BIG(num_drugs) over (partition by cohort_definition_id)+1) as p1
from
	(
	select c1.cohort_definition_id, de1.person_id, COUNT_BIG(distinct de1.drug_concept_id) as num_drugs
	from
	@CDM_schema.drug_exposure de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_exposure_start_date>=c1.cohort_start_date and de1.drug_exposure_start_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--} 
--}
	group by c1.cohort_definition_id, de1.person_id
	) t0
) t1

group by cohort_definition_id
;
--}



--{704 IN (@list_of_analysis_ids)}?{
-- 704	Number of persons with at least one drug occurrence, by drug_concept_id by calendar year by gender by age decile
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)
select c1.cohort_definition_id,
	704 as analysis_id,   
	de1.drug_concept_id as stratum_1,
	YEAR(drug_exposure_start_date) as stratum_2,
	p1.gender_concept_id as stratum_3,
	floor((year(drug_exposure_start_date) - p1.year_of_birth)/10) as stratum_4, 
	COUNT_BIG(distinct p1.PERSON_ID) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
@CDM_schema.drug_exposure de1
on p1.person_id = de1.person_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{ 
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_exposure_start_date>=c1.cohort_start_date and de1.drug_exposure_start_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--} 
--} 
group by c1.cohort_definition_id,
	de1.drug_concept_id, 
	YEAR(drug_exposure_start_date),
	p1.gender_concept_id,
	floor((year(drug_exposure_start_date) - p1.year_of_birth)/10)
;
--}

--{705 IN (@list_of_analysis_ids)}?{
-- 705	Number of drug occurrence records, by drug_concept_id by drug_type_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
	705 as analysis_id, 
	de1.drug_CONCEPT_ID as stratum_1,
	de1.drug_type_concept_id as stratum_2,
	COUNT_BIG(de1.PERSON_ID) as count_value
from
	@CDM_schema.drug_exposure de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_exposure_start_date>=c1.cohort_start_date and de1.drug_exposure_start_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--} 
--}
group by c1.cohort_definition_id,
	de1.drug_CONCEPT_ID,	
	de1.drug_type_concept_id
;
--}



--{706 IN (@list_of_analysis_ids)}?{
-- 706	Distribution of age by drug_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	706 as analysis_id,
	drug_concept_id as stratum_1,
	gender_concept_id as stratum_2,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	de1.drug_concept_id,
	p1.gender_concept_id,
	de1.drug_start_year - p1.year_of_birth as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, de1.drug_concept_id, p1.gender_concept_id order by de1.drug_start_year - p1.year_of_birth))/(COUNT_BIG(de1.drug_start_year - p1.year_of_birth) over (partition by c1.cohort_definition_id, de1.drug_concept_id, p1.gender_concept_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on p1.person_id = c1.subject_id
inner join
(select person_id, drug_concept_id, min(year(drug_exposure_start_date)) as drug_start_year
from @CDM_schema.drug_exposure de0
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de0.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{     
WHERE
--{@drug_concept_ids != ''}?{
	de0.drug_concept_id in (@drug_concept_ids)
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@cohort_period_only == 'TRUE'}?{
	de0.drug_exposure_start_date>=c1.cohort_start_date and de0.drug_exposure_start_date<=c1.cohort_end_date
--}
--}
group by person_id, drug_concept_id
) de1
on p1.person_id = de1.person_id
) t1
group by cohort_definition_id, drug_concept_id, gender_concept_id
;
--}




--{709 IN (@list_of_analysis_ids)}?{
-- 709	Number of drug exposure records with invalid person_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	709 as analysis_id,  
	COUNT_BIG(de1.PERSON_ID) as count_value
from
	@CDM_schema.drug_exposure de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
	left join @CDM_schema.PERSON p1
	on p1.person_id = de1.person_id
where p1.person_id is null
group by c1.cohort_definition_id
;
--}


--{710 IN (@list_of_analysis_ids)}?{
-- 710	Number of drug exposure records outside valid observation period
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	710 as analysis_id,  
	COUNT_BIG(de1.PERSON_ID) as count_value
from
	@CDM_schema.drug_exposure de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
	left join @CDM_schema.OBSERVATION_PERIOD op1
	on op1.person_id = de1.person_id
	and de1.drug_exposure_start_date >= op1.observation_period_start_date
	and de1.drug_exposure_start_date <= op1.observation_period_end_date
where op1.person_id is null
group by c1.cohort_definition_id
;
--}


--{711 IN (@list_of_analysis_ids)}?{
-- 711	Number of drug exposure records with end date < start date
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	711 as analysis_id,  
	COUNT_BIG(de1.PERSON_ID) as count_value
from
	@CDM_schema.drug_exposure de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
where de1.drug_exposure_end_date < de1.drug_exposure_start_date
group by c1.cohort_definition_id
;
--}


--{712 IN (@list_of_analysis_ids)}?{
-- 712	Number of drug exposure records with invalid provider_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	712 as analysis_id,  
	COUNT_BIG(de1.PERSON_ID) as count_value
from
	@CDM_schema.drug_exposure de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
	left join @CDM_schema.provider p1
	on p1.provider_id = {@CDM_version == '4'}?{ de1.prescribing_provider_id } {@CDM_version == '5'}?{ de1.provider_id } 
where {@CDM_version == '4'}?{ de1.prescribing_provider_id } {@CDM_version == '5'}?{ de1.provider_id }  is not null
	and p1.provider_id is null
group by c1.cohort_definition_id
;
--}

--{713 IN (@list_of_analysis_ids)}?{
-- 713	Number of drug exposure records with invalid visit_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	713 as analysis_id,  
	COUNT_BIG(de1.PERSON_ID) as count_value
from
	@CDM_schema.drug_exposure de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
	left join @CDM_schema.visit_occurrence vo1
	on de1.visit_occurrence_id = vo1.visit_occurrence_id
where de1.visit_occurrence_id is not null
	and vo1.visit_occurrence_id is null
group by cohort_definition_id
;
--}



--{715 IN (@list_of_analysis_ids)}?{
-- 715	Distribution of days_supply by drug_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	715 as analysis_id,
	drug_concept_id as stratum_1,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id, drug_concept_id,
	days_supply as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, drug_concept_id order by days_supply))/(COUNT_BIG(days_supply) over (partition by c1.cohort_definition_id, drug_concept_id)+1) as p1
from @CDM_schema.drug_exposure de1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{     
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_exposure_start_date>=c1.cohort_start_date and de1.drug_exposure_start_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--} 
--}
) t1
group by cohort_definition_id, drug_concept_id
;
--}



--{716 IN (@list_of_analysis_ids)}?{
-- 716	Distribution of refills by drug_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id, 
	716 as analysis_id,
	drug_concept_id as stratum_1,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	drug_concept_id,
	refills as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, drug_concept_id order by refills))/(COUNT_BIG(refills) over (partition by c1.cohort_definition_id, drug_concept_id)+1) as p1
from @CDM_schema.drug_exposure de1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{     
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_exposure_start_date>=c1.cohort_start_date and de1.drug_exposure_start_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--} 
--} 
) t1
group by cohort_definition_id, 
	drug_concept_id
;
--}





--{717 IN (@list_of_analysis_ids)}?{
-- 717	Distribution of quantity by drug_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	717 as analysis_id,
	drug_concept_id as stratum_1,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id, 
	drug_concept_id,
	quantity as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, drug_concept_id order by quantity))/(COUNT_BIG(quantity) over (partition by cohort_definition_id, drug_concept_id)+1) as p1
from @CDM_schema.drug_exposure de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_exposure_start_date>=c1.cohort_start_date and de1.drug_exposure_start_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--} 
--}
) t1
group by cohort_definition_id, drug_concept_id
;
--}


--{720 IN (@list_of_analysis_ids)}?{
-- 720	Number of drug exposure records by drug start month
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	720 as analysis_id,   
	YEAR(drug_exposure_start_date)*100 + month(drug_exposure_start_date) as stratum_1, 
	COUNT_BIG(PERSON_ID) as count_value
from
@CDM_schema.drug_exposure de1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_exposure_start_date>=c1.cohort_start_date and de1.drug_exposure_start_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--} 
--}  
group by c1.cohort_definition_id,
	YEAR(drug_exposure_start_date)*100 + month(drug_exposure_start_date)
;
--}

--/********************************************

--HERACLES Analyses on OBSERVATION table

--*********************************************/



--{800 IN (@list_of_analysis_ids)}?{
-- 800	Number of persons with at least one observation occurrence, by observation_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	800 as analysis_id, 
	o1.observation_CONCEPT_ID as stratum_1,
	COUNT_BIG(distinct o1.PERSON_ID) as count_value
from
	@CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@observation_concept_ids != '' | @cohort_period_only == 'TRUE'}?{
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.observation_date>=c1.cohort_start_date and o1.observation_date<=c1.cohort_end_date
--}
--{@observation_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@observation_concept_ids != ''}?{
	o1.observation_concept_id in (@observation_concept_ids)
--} 
--} 
group by c1.cohort_definition_id,
	o1.observation_CONCEPT_ID
;
--}


--{801 IN (@list_of_analysis_ids)}?{
-- 801	Number of observation occurrence records, by observation_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	801 as analysis_id, 
	o1.observation_CONCEPT_ID as stratum_1,
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@observation_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.observation_date>=c1.cohort_start_date and o1.observation_date<=c1.cohort_end_date
--}
--{@observation_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@observation_concept_ids != ''}?{
	o1.observation_concept_id in (@observation_concept_ids)
--} 
--} 
group by c1.cohort_definition_id,
	o1.observation_CONCEPT_ID
;
--}



--{802 IN (@list_of_analysis_ids)}?{
-- 802	Number of persons by observation occurrence start month, by observation_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
	802 as analysis_id,   
	o1.observation_concept_id as stratum_1,
	YEAR(observation_date)*100 + month(observation_date) as stratum_2, 
	COUNT_BIG(distinct PERSON_ID) as count_value
from
	@CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@observation_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.observation_date>=c1.cohort_start_date and o1.observation_date<=c1.cohort_end_date
--}
--{@observation_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@observation_concept_ids != ''}?{
	o1.observation_concept_id in (@observation_concept_ids)
--} 
--} 
group by c1.cohort_definition_id,
	o1.observation_concept_id, 
	YEAR(observation_date)*100 + month(observation_date)
;
--}



--{803 IN (@list_of_analysis_ids)}?{
-- 803	Number of distinct observation occurrence concepts per person
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	803 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id,
	num_observations as count_value,
	1.0*(row_number() over (partition by cohort_definition_id order by num_observations))/(COUNT_BIG(num_observations) over (partition by cohort_definition_id)+1) as p1
from
	(
	select c1.cohort_definition_id, o1.person_id, COUNT_BIG(distinct o1.observation_concept_id) as num_observations
	from
	@CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@observation_concept_ids != '' | @cohort_period_only == 'TRUE'}?{     
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.observation_date>=c1.cohort_start_date and o1.observation_date<=c1.cohort_end_date
--}
--{@observation_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@observation_concept_ids != ''}?{
	o1.observation_concept_id in (@observation_concept_ids)
--} 
--} 
	group by c1.cohort_definition_id, o1.person_id
	) t0
) t1
group by cohort_definition_id
;
--}



--{804 IN (@list_of_analysis_ids)}?{
-- 804	Number of persons with at least one observation occurrence, by observation_concept_id by calendar year by gender by age decile
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)
select c1.cohort_definition_id,
	804 as analysis_id,   
	o1.observation_concept_id as stratum_1,
	YEAR(observation_date) as stratum_2,
	p1.gender_concept_id as stratum_3,
	floor((year(observation_date) - p1.year_of_birth)/10) as stratum_4, 
	COUNT_BIG(distinct p1.PERSON_ID) as count_value
from @CDM_schema.PERSON p1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on p1.person_id = c1.subject_id
inner join
@CDM_schema.observation o1
on p1.person_id = o1.person_id
--{@observation_concept_ids != '' | @cohort_period_only == 'TRUE'}?{ 
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.observation_date>=c1.cohort_start_date and o1.observation_date<=c1.cohort_end_date
--}
--{@observation_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@observation_concept_ids != ''}?{
	o1.observation_concept_id in (@observation_concept_ids)
--} 
--} 
group by c1.cohort_definition_id,
	o1.observation_concept_id, 
	YEAR(observation_date),
	p1.gender_concept_id,
	floor((year(observation_date) - p1.year_of_birth)/10)
;
--}

--{805 IN (@list_of_analysis_ids)}?{
-- 805	Number of observation occurrence records, by observation_concept_id by observation_type_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
	805 as analysis_id, 
	o1.observation_CONCEPT_ID as stratum_1,
	o1.observation_type_concept_id as stratum_2,
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@observation_concept_ids != '' | @cohort_period_only == 'TRUE'}?{     
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.observation_date>=c1.cohort_start_date and o1.observation_date<=c1.cohort_end_date
--}
--{@observation_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@observation_concept_ids != ''}?{
	o1.observation_concept_id in (@observation_concept_ids)
--} 
--} 
group by c1.cohort_definition_id,
	o1.observation_CONCEPT_ID,	
	o1.observation_type_concept_id
;
--}



--{806 IN (@list_of_analysis_ids)}?{
-- 806	Distribution of age by observation_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	806 as analysis_id,
	observation_concept_id as stratum_1,
	gender_concept_id as stratum_2,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	o1.observation_concept_id,
	p1.gender_concept_id,
	o1.observation_start_year - p1.year_of_birth as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, o1.observation_concept_id, p1.gender_concept_id order by o1.observation_start_year - p1.year_of_birth))/(COUNT_BIG(o1.observation_start_year - p1.year_of_birth) over (partition by c1.cohort_definition_id, o1.observation_concept_id, p1.gender_concept_id)+1) as p1
from @CDM_schema.PERSON p1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on p1.person_id = c1.subject_id
inner join
(select person_id, observation_concept_id, min(year(observation_date)) as observation_start_year
from @CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@observation_concept_ids != '' | @cohort_period_only == 'TRUE'}?{ 
WHERE
--{@observation_concept_ids != ''}?{
	o1.observation_concept_id in (@observation_concept_ids)
--}
--{@observation_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@cohort_period_only == 'TRUE'}?{
	o1.observation_date>=c1.cohort_start_date and o1.observation_date<=c1.cohort_end_date
--}
--}
group by person_id, observation_concept_id
) o1
on p1.person_id = o1.person_id
) t1
group by cohort_definition_id, observation_concept_id, gender_concept_id
;
--}

--{807 IN (@list_of_analysis_ids)}?{
-- 807	Number of observation occurrence records, by observation_concept_id and unit_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
	807 as analysis_id, 
	o1.observation_CONCEPT_ID as stratum_1,
	o1.unit_concept_id as stratum_2,
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@observation_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.observation_date>=c1.cohort_start_date and o1.observation_date<=c1.cohort_end_date
--}
--{@observation_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@observation_concept_ids != ''}?{
	o1.observation_concept_id in (@observation_concept_ids)
--} 
--} 
group by c1.cohort_definition_id,
	o1.observation_CONCEPT_ID,
	o1.unit_concept_id
;
--}





--{809 IN (@list_of_analysis_ids)}?{
-- 809	Number of observation records with invalid person_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	809 as analysis_id,  
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
	left join @CDM_schema.PERSON p1
	on p1.person_id = o1.person_id
where p1.person_id is null
group by c1.cohort_definition_id
;
--}


--{810 IN (@list_of_analysis_ids)}?{
-- 810	Number of observation records outside valid observation period
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	810 as analysis_id,  
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
	left join @CDM_schema.OBSERVATION_PERIOD op1
	on op1.person_id = o1.person_id
	and o1.observation_date >= op1.observation_period_start_date
	and o1.observation_date <= op1.observation_period_end_date
where op1.person_id is null
group by c1.cohort_definition_id
;
--}



--{812 IN (@list_of_analysis_ids)}?{
-- 812	Number of observation records with invalid provider_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	812 as analysis_id,  
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
	left join @CDM_schema.provider p1
	on p1.provider_id = {@CDM_version == '4'}?{ o1.associated_provider_id } {@CDM_version == '5'}?{ o1.provider_id } 
where {@CDM_version == '4'}?{ o1.associated_provider_id } {@CDM_version == '5'}?{ o1.provider_id }  is not null
	and p1.provider_id is null
group by c1.cohort_definition_id
;
--}

--{813 IN (@list_of_analysis_ids)}?{
-- 813	Number of observation records with invalid visit_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	813 as analysis_id,  
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
	left join @CDM_schema.visit_occurrence vo1
	on o1.visit_occurrence_id = vo1.visit_occurrence_id
where o1.visit_occurrence_id is not null
	and vo1.visit_occurrence_id is null
group by c1.cohort_definition_id
;
--}


--{814 IN (@list_of_analysis_ids)}?{
-- 814	Number of observation records with no value (numeric, string, or concept)
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	814 as analysis_id,  
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
where o1.value_as_number is null
	and o1.value_as_string is null
	and o1.value_as_concept_id is null
group by c1.cohort_definition_id
;
--}


--{815 IN (@list_of_analysis_ids)}?{
-- 815	Distribution of numeric values, by observation_concept_id and unit_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	815 as analysis_id,
	observation_concept_id as stratum_1,
	unit_concept_id as stratum_2,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id,
	observation_concept_id, unit_concept_id,
	value_as_number as count_value,
	1.0*(row_number() over (partition by cohort_definition_id, observation_concept_id, unit_concept_id order by value_as_number))/(COUNT_BIG(value_as_number) over (partition by cohort_definition_id, observation_concept_id, unit_concept_id)+1) as p1
from @CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
where o1.unit_concept_id is not null
	and o1.value_as_number is not null
	--{@observation_concept_ids != ''}?{
	and o1.observation_concept_id in (@observation_concept_ids)
--}
--{@cohort_period_only == 'TRUE'}?{
	AND o1.observation_date>=c1.cohort_start_date and o1.observation_date<=c1.cohort_end_date
--}
) t1
group by cohort_definition_id, observation_concept_id, unit_concept_id
;
--}


--{@CDM_version == '4'}?{  
--{816 IN (@list_of_analysis_ids)}?{
-- 816	Distribution of low range, by observation_concept_id and unit_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	816 as analysis_id,
	observation_concept_id as stratum_1,
	unit_concept_id as stratum_2,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id, observation_concept_id, unit_concept_id,
	range_low as count_value,
	1.0*(row_number() over (partition by cohort_definition_id, observation_concept_id, unit_concept_id order by range_low))/(COUNT_BIG(range_low) over (partition by cohort_definition_id, observation_concept_id, unit_concept_id)+1) as p1
from @CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
where o1.unit_concept_id is not null
	and o1.value_as_number is not null
	and o1.range_low is not null
	and o1.range_high is not null
	--{@observation_concept_ids != ''}?{
	and o1.observation_concept_id in (@observation_concept_ids)
--}
--{@cohort_period_only == 'TRUE'}?{
	AND o1.observation_date>=c1.cohort_start_date and o1.observation_date<=c1.cohort_end_date
--}
) t1
group by cohort_definition_id, observation_concept_id, unit_concept_id
;
--}


--{817 IN (@list_of_analysis_ids)}?{
-- 817	Distribution of high range, by observation_concept_id and unit_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	817 as analysis_id,
	observation_concept_id as stratum_1,
	unit_concept_id as stratum_2,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id, observation_concept_id, unit_concept_id,
	range_high as count_value,
	1.0*(row_number() over (partition by cohort_definition_id, observation_concept_id, unit_concept_id order by range_high))/(COUNT_BIG(range_high) over (partition by cohort_definition_id, observation_concept_id, unit_concept_id)+1) as p1
from @CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
where o1.unit_concept_id is not null
	and o1.value_as_number is not null
	and o1.range_low is not null
	and o1.range_high is not null
--{@observation_concept_ids != ''}?{
	and o1.observation_concept_id in (@observation_concept_ids)
--}
--{@cohort_period_only == 'TRUE'}?{
	AND o1.observation_date>=c1.cohort_start_date and o1.observation_date<=c1.cohort_end_date
--}
) t1
group by cohort_definition_id, observation_concept_id, unit_concept_id
;
--}



--{818 IN (@list_of_analysis_ids)}?{
-- 818	Number of observation records below/within/above normal range, by observation_concept_id and unit_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, count_value)
select cohort_definition_id,
	818 as analysis_id,  
	observation_concept_id as stratum_1,
	unit_concept_id as stratum_2,
	case when o1.value_as_number < o1.range_low then 'Below Range Low'
		when o1.value_as_number >= o1.range_low and o1.value_as_number <= o1.range_high then 'Within Range'
		when o1.value_as_number > o1.range_high then 'Above Range High'
		else 'Other' end as stratum_3,
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
where o1.value_as_number is not null
	and o1.unit_concept_id is not null
	and o1.range_low is not null
	and o1.range_high is not null
--{@observation_concept_ids != ''}?{
	and o1.observation_concept_id in (@observation_concept_ids)
--}  
--{@cohort_period_only == 'TRUE'}?{
	AND o1.observation_date>=c1.cohort_start_date and o1.observation_date<=c1.cohort_end_date
--}
group by cohort_definition_id, 
	observation_concept_id,
	unit_concept_id,
	  case when o1.value_as_number < o1.range_low then 'Below Range Low'
		when o1.value_as_number >= o1.range_low and o1.value_as_number <= o1.range_high then 'Within Range'
		when o1.value_as_number > o1.range_high then 'Above Range High'
		else 'Other' end
;
--}

--} --end of if in CDMv5


--{820 IN (@list_of_analysis_ids)}?{
-- 820	Number of observation records by condition occurrence start month
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	820 as analysis_id,   
	YEAR(observation_date)*100 + month(observation_date) as stratum_1, 
	COUNT_BIG(PERSON_ID) as count_value
from
	@CDM_schema.observation o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@observation_concept_ids != '' | @cohort_period_only == 'TRUE'}?{     
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.observation_date>=c1.cohort_start_date and o1.observation_date<=c1.cohort_end_date
--}
--{@observation_concept_ids != '' & @cohort_period_only == 'TRUE'}?{
AND
--}
--{@observation_concept_ids != ''}?{
	o1.observation_concept_id in (@observation_concept_ids)
--}   
--} 
group by c1.cohort_definition_id,
	YEAR(observation_date)*100 + month(observation_date)
;
--}




--/********************************************

--HERACLES Analyses on DRUG_ERA table

--*********************************************/


--{900 IN (@list_of_analysis_ids)}?{
-- 900	Number of persons with at least one drug occurrence, by drug_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	900 as analysis_id, 
	de1.drug_CONCEPT_ID as stratum_1,
	COUNT_BIG(distinct de1.PERSON_ID) as count_value
from
	@CDM_schema.drug_era de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_era_start_date>=c1.cohort_start_date and de1.drug_era_end_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--}
--} 
group by c1.cohort_definition_id, 
	de1.drug_CONCEPT_ID
;
--}


--{901 IN (@list_of_analysis_ids)}?{
-- 901	Number of drug occurrence records, by drug_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	901 as analysis_id, 
	de1.drug_CONCEPT_ID as stratum_1,
	COUNT_BIG(de1.PERSON_ID) as count_value
from
	@CDM_schema.drug_era de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{     
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_era_start_date>=c1.cohort_start_date and de1.drug_era_end_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	de1.drug_CONCEPT_ID
;
--}



--{902 IN (@list_of_analysis_ids)}?{
-- 902	Number of persons by drug occurrence start month, by drug_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select cohort_definition_id,
	902 as analysis_id,   
	de1.drug_concept_id as stratum_1,
	YEAR(drug_era_start_date)*100 + month(drug_era_start_date) as stratum_2, 
	COUNT_BIG(distinct PERSON_ID) as count_value
from
	@CDM_schema.drug_era de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{     
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_era_start_date>=c1.cohort_start_date and de1.drug_era_end_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{  
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	de1.drug_concept_id, 
	YEAR(drug_era_start_date)*100 + month(drug_era_start_date)
;
--}



--{903 IN (@list_of_analysis_ids)}?{
-- 903	Number of distinct drug era concepts per person
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	903 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id,
	num_drugs as count_value,
	1.0*(row_number() over (partition by cohort_definition_id order by num_drugs))/(COUNT_BIG(num_drugs) over (partition by cohort_definition_id)+1) as p1
from
	(
	select c1.cohort_definition_id, de1.person_id, COUNT_BIG(distinct de1.drug_concept_id) as num_drugs
	from
	@CDM_schema.drug_era de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{     
WHERE     
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_era_start_date>=c1.cohort_start_date and de1.drug_era_end_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{  
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--}   
--} 
	group by c1.cohort_definition_id, de1.person_id
	) t0
) t1
group by cohort_definition_id
;
--}



--{904 IN (@list_of_analysis_ids)}?{
-- 904	Number of persons with at least one drug occurrence, by drug_concept_id by calendar year by gender by age decile
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)
select c1.cohort_definition_id,
	904 as analysis_id,   
	de1.drug_concept_id as stratum_1,
	YEAR(drug_era_start_date) as stratum_2,
	p1.gender_concept_id as stratum_3,
	floor((year(drug_era_start_date) - p1.year_of_birth)/10) as stratum_4, 
	COUNT_BIG(distinct p1.PERSON_ID) as count_value
from @CDM_schema.PERSON p1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on p1.person_id = c1.subject_id
inner join
@CDM_schema.drug_era de1
on p1.person_id = de1.person_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{ 
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_era_start_date>=c1.cohort_start_date and de1.drug_era_end_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{  
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	de1.drug_concept_id, 
	YEAR(drug_era_start_date),
	p1.gender_concept_id,
	floor((year(drug_era_start_date) - p1.year_of_birth)/10)
;
--}




--{906 IN (@list_of_analysis_ids)}?{
-- 906	Distribution of age by drug_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	906 as analysis_id,
	drug_concept_id as stratum_1,
	gender_concept_id as stratum_2,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	de1.drug_concept_id,
	p1.gender_concept_id,
	de1.drug_start_year - p1.year_of_birth as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, de1.drug_concept_id, p1.gender_concept_id order by de1.drug_start_year - p1.year_of_birth))/(COUNT_BIG(de1.drug_start_year - p1.year_of_birth) over (partition by c1.cohort_definition_id, de1.drug_concept_id, p1.gender_concept_id)+1) as p1
from @CDM_schema.PERSON p1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on p1.person_id = c1.subject_id
inner join
(select person_id, drug_concept_id, min(year(drug_era_start_date)) as drug_start_year
from @CDM_schema.drug_era de0
		inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de0.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{     
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de0.drug_era_start_date>=c1.cohort_start_date and de0.drug_era_end_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{  
AND
--}
--{@drug_concept_ids != ''}?{
	de0.drug_concept_id in (@drug_concept_ids)
--}
--}
group by person_id, drug_concept_id
) de1
on p1.person_id = de1.person_id
) t1
group by cohort_definition_id, drug_concept_id, gender_concept_id
;
--}





--{907 IN (@list_of_analysis_ids)}?{
-- 907	Distribution of drug era length, by drug_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	907 as analysis_id,
	drug_concept_id as stratum_1,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id,
	drug_concept_id,
	datediff(dd,drug_era_start_date, drug_era_end_date) as count_value,
	1.0*(row_number() over (partition by cohort_definition_id, drug_concept_id order by datediff(dd,drug_era_start_date, drug_era_end_date)))/(COUNT_BIG(datediff(dd,drug_era_start_date, drug_era_end_date)) over (partition by cohort_definition_id, drug_concept_id)+1) as p1
from  @CDM_schema.drug_era de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_era_start_date>=c1.cohort_start_date and de1.drug_era_end_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{  
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--}
--}
) t1
group by cohort_definition_id, 
	drug_concept_id
;
--}



--{908 IN (@list_of_analysis_ids)}?{
-- 908	Number of drug eras with invalid person
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	908 as analysis_id,  
	COUNT_BIG(de1.PERSON_ID) as count_value
from
	@CDM_schema.drug_era de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
	left join @CDM_schema.PERSON p1
	on p1.person_id = de1.person_id
where p1.person_id is null
group by c1.cohort_definition_id
;
--}


--{909 IN (@list_of_analysis_ids)}?{
-- 909	Number of drug eras outside valid observation period
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	909 as analysis_id,  
	COUNT_BIG(de1.PERSON_ID) as count_value
from
	@CDM_schema.drug_era de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
	left join @CDM_schema.OBSERVATION_PERIOD op1
	on op1.person_id = de1.person_id
	and de1.drug_era_start_date >= op1.observation_period_start_date
	and de1.drug_era_start_date <= op1.observation_period_end_date
where op1.person_id is null
group by c1.cohort_definition_id
;
--}


--{910 IN (@list_of_analysis_ids)}?{
-- 910	Number of drug eras with end date < start date
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	910 as analysis_id,  
	COUNT_BIG(de1.PERSON_ID) as count_value
from
	@CDM_schema.drug_era de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
where de1.drug_era_end_date < de1.drug_era_start_date
group by c1.cohort_definition_id
;
--}



--{920 IN (@list_of_analysis_ids)}?{
-- 920	Number of drug era records by drug era start month
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	920 as analysis_id,   
	YEAR(drug_era_start_date)*100 + month(drug_era_start_date) as stratum_1, 
	COUNT_BIG(PERSON_ID) as count_value
from
@CDM_schema.drug_era de1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on de1.person_id = c1.subject_id
--{@drug_concept_ids != '' | @cohort_period_only == 'TRUE'}?{     
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	de1.drug_era_start_date>=c1.cohort_start_date and de1.drug_era_end_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'TRUE'}?{  
AND
--}
--{@drug_concept_ids != ''}?{
	de1.drug_concept_id in (@drug_concept_ids)
--}   
--} 
group by c1.cohort_definition_id,
	YEAR(drug_era_start_date)*100 + month(drug_era_start_date)
;
--}





--/********************************************

--HERACLES Analyses on CONDITION_ERA table

--*********************************************/


--{1000 IN (@list_of_analysis_ids)}?{
-- 1000	Number of persons with at least one condition occurrence, by condition_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1000 as analysis_id, 
	ce1.condition_CONCEPT_ID as stratum_1,
	COUNT_BIG(distinct ce1.PERSON_ID) as count_value
from
	@CDM_schema.condition_era ce1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on ce1.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	ce1.condition_era_start_date>=c1.cohort_start_date and ce1.condition_era_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@condition_concept_ids != ''}?{
	ce1.condition_concept_id in (@condition_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	ce1.condition_CONCEPT_ID
;
--}


--{1001 IN (@list_of_analysis_ids)}?{
-- 1001	Number of condition occurrence records, by condition_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1001 as analysis_id, 
	ce1.condition_CONCEPT_ID as stratum_1,
	COUNT_BIG(ce1.PERSON_ID) as count_value
from
	@CDM_schema.condition_era ce1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on ce1.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{     
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	ce1.condition_era_start_date>=c1.cohort_start_date and ce1.condition_era_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@condition_concept_ids != ''}?{
	ce1.condition_concept_id in (@condition_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	ce1.condition_CONCEPT_ID
;
--}



--{1002 IN (@list_of_analysis_ids)}?{
-- 1002	Number of persons by condition occurrence start month, by condition_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
	1002 as analysis_id,   
	ce1.condition_concept_id as stratum_1,
	YEAR(condition_era_start_date)*100 + month(condition_era_start_date) as stratum_2, 
	COUNT_BIG(distinct PERSON_ID) as count_value
from
	@CDM_schema.condition_era ce1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on ce1.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	ce1.condition_era_start_date>=c1.cohort_start_date and ce1.condition_era_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@condition_concept_ids != ''}?{
	ce1.condition_concept_id in (@condition_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	ce1.condition_concept_id, 
	YEAR(condition_era_start_date)*100 + month(condition_era_start_date)
;
--}



--{1003 IN (@list_of_analysis_ids)}?{
-- 1003	Number of distinct condition era concepts per person
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	1003 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id, num_conditions as count_value,
	1.0*(row_number() over (partition by cohort_definition_id order by num_conditions))/(COUNT_BIG(num_conditions) over (partition by cohort_definition_id)+1) as p1
from
	(
	select c1.cohort_definition_id, ce1.person_id, COUNT_BIG(distinct ce1.condition_concept_id) as num_conditions
	from
	@CDM_schema.condition_era ce1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on ce1.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	ce1.condition_era_start_date>=c1.cohort_start_date and ce1.condition_era_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@condition_concept_ids != ''}?{
	ce1.condition_concept_id in (@condition_concept_ids)
--}   
--} 
	group by c1.cohort_definition_id, ce1.person_id
	) t0
) t1
group by cohort_definition_id
;
--}



--{1004 IN (@list_of_analysis_ids)}?{
-- 1004	Number of persons with at least one condition occurrence, by condition_concept_id by calendar year by gender by age decile
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)
select c1.cohort_definition_id,
	1004 as analysis_id,   
	ce1.condition_concept_id as stratum_1,
	YEAR(condition_era_start_date) as stratum_2,
	p1.gender_concept_id as stratum_3,
	floor((year(condition_era_start_date) - p1.year_of_birth)/10) as stratum_4, 
	COUNT_BIG(distinct p1.PERSON_ID) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
@CDM_schema.condition_era ce1
on p1.person_id = ce1.person_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{  
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	ce1.condition_era_start_date>=c1.cohort_start_date and ce1.condition_era_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@condition_concept_ids != ''}?{
	ce1.condition_concept_id in (@condition_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	ce1.condition_concept_id, 
	YEAR(condition_era_start_date),
	p1.gender_concept_id,
	floor((year(condition_era_start_date) - p1.year_of_birth)/10)
;
--}




--{1006 IN (@list_of_analysis_ids)}?{
-- 1006	Distribution of age by condition_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	1006 as analysis_id,
	condition_concept_id as stratum_1,
	gender_concept_id as stratum_2,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	ce1.condition_concept_id,
	p1.gender_concept_id,
	ce1.condition_start_year - p1.year_of_birth as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, ce1.condition_concept_id, p1.gender_concept_id order by ce1.condition_start_year - p1.year_of_birth))/(COUNT_BIG(ce1.condition_start_year - p1.year_of_birth) over (partition by c1.cohort_definition_id, ce1.condition_concept_id, p1.gender_concept_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
inner join
(select person_id, condition_concept_id, min(year(condition_era_start_date)) as condition_start_year
from @CDM_schema.condition_era ce0
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on ce0.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{      
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	ce0.condition_era_start_date>=c1.cohort_start_date and ce0.condition_era_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@condition_concept_ids != ''}?{
	ce0.condition_concept_id in (@condition_concept_ids)
--}
--}
group by person_id, condition_concept_id
) ce1
on p1.person_id = ce1.person_id
) t1
group by cohort_definition_id, condition_concept_id, gender_concept_id
;
--}





--{1007 IN (@list_of_analysis_ids)}?{
-- 1007	Distribution of condition era length, by condition_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	1007 as analysis_id,
	condition_concept_id as stratum_1,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	condition_concept_id,
	datediff(dd,condition_era_start_date, condition_era_end_date) as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, condition_concept_id order by datediff(dd,condition_era_start_date, condition_era_end_date)))/(COUNT_BIG(datediff(dd,condition_era_start_date, condition_era_end_date)) over (partition by c1.cohort_definition_id, condition_concept_id)+1) as p1
from  @CDM_schema.condition_era ce1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
	on ce1.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{  
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	ce1.condition_era_start_date>=c1.cohort_start_date and ce1.condition_era_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@condition_concept_ids != ''}?{
	ce1.condition_concept_id in (@condition_concept_ids)
--}
--}
) t1
group by cohort_definition_id,
	condition_concept_id
;
--}



--{1008 IN (@list_of_analysis_ids)}?{
-- 1008	Number of condition eras with invalid person
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	1008 as analysis_id,  
	COUNT_BIG(ce1.PERSON_ID) as count_value
from
	@CDM_schema.condition_era ce1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on ce1.person_id = c1.subject_id
	left join @CDM_schema.PERSON p1
	on p1.person_id = ce1.person_id
where p1.person_id is null
group by c1.cohort_definition_id
;
--}


--{1009 IN (@list_of_analysis_ids)}?{
-- 1009	Number of condition eras outside valid observation period
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	1009 as analysis_id,  
	COUNT_BIG(ce1.PERSON_ID) as count_value
from
	@CDM_schema.condition_era ce1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on ce1.person_id = c1.subject_id
	left join @CDM_schema.OBSERVATION_PERIOD op1
	on op1.person_id = ce1.person_id
	and ce1.condition_era_start_date >= op1.observation_period_start_date
	and ce1.condition_era_start_date <= op1.observation_period_end_date
where op1.person_id is null
group by c1.cohort_definition_id
;
--}


--{1010 IN (@list_of_analysis_ids)}?{
-- 1010	Number of condition eras with end date < start date
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	1010 as analysis_id,  
	COUNT_BIG(ce1.PERSON_ID) as count_value
from
	@CDM_schema.condition_era ce1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on ce1.person_id = c1.subject_id
where ce1.condition_era_end_date < ce1.condition_era_start_date
group by c1.cohort_definition_id
;
--}


--{1020 IN (@list_of_analysis_ids)}?{
-- 1020	Number of drug era records by drug era start month
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1020 as analysis_id,   
	YEAR(condition_era_start_date)*100 + month(condition_era_start_date) as stratum_1, 
	COUNT_BIG(PERSON_ID) as count_value
from
@CDM_schema.condition_era ce1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on ce1.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	ce1.condition_era_start_date>=c1.cohort_start_date and ce1.condition_era_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@condition_concept_ids != ''}?{
	ce1.condition_concept_id in (@condition_concept_ids)
--}    
--} 
group by c1.cohort_definition_id,
	YEAR(condition_era_start_date)*100 + month(condition_era_start_date)
;
--}




--/********************************************

--HERACLES Analyses on LOCATION table

--*********************************************/

--{1100 IN (@list_of_analysis_ids)}?{
-- 1100	Number of persons by location 3-digit zip
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1100 as analysis_id,  
	left(l1.zip,3) as stratum_1, COUNT_BIG(distinct person_id) as count_value
from @CDM_schema.PERSON p1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on p1.person_id = c1.subject_id
	inner join @CDM_schema.LOCATION l1
	on p1.location_id = l1.location_id
where p1.location_id is not null
	and l1.zip is not null
group by c1.cohort_definition_id,
	left(l1.zip,3);
--}


--{1101 IN (@list_of_analysis_ids)}?{
-- 1101	Number of persons by location state
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1101 as analysis_id,  
	l1.state as stratum_1, COUNT_BIG(distinct person_id) as count_value
from @CDM_schema.PERSON p1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on p1.person_id = c1.subject_id
	inner join @CDM_schema.LOCATION l1
	on p1.location_id = l1.location_id
where p1.location_id is not null
	and l1.state is not null
group by c1.cohort_definition_id,
	l1.state;
--}




--/********************************************

--HERACLES Analyses on CARE_SITE table
--
--*********************************************/


--{1200 IN (@list_of_analysis_ids)}?{
-- 1200	Number of persons by place of service
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1200 as analysis_id,  
	cs1.place_of_service_concept_id as stratum_1, 
	COUNT_BIG(person_id) as count_value
from @CDM_schema.PERSON p1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on p1.person_id = c1.subject_id
	inner join @CDM_schema.care_site cs1
	on p1.care_site_id = cs1.care_site_id
where p1.care_site_id is not null
	and cs1.place_of_service_concept_id is not null
group by c1.cohort_definition_id,
	cs1.place_of_service_concept_id;
--}


--{1201 IN (@list_of_analysis_ids)}?{
-- 1201	Number of visits by place of service
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1201 as analysis_id,  
	cs1.place_of_service_concept_id as stratum_1, 
	COUNT_BIG(visit_occurrence_id) as count_value
from @CDM_schema.visit_occurrence vo1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on vo1.person_id = c1.subject_id
	inner join @CDM_schema.care_site cs1
	on vo1.care_site_id = cs1.care_site_id
where vo1.care_site_id is not null
	and cs1.place_of_service_concept_id is not null
group by c1.cohort_definition_id,
	cs1.place_of_service_concept_id;
--}

	
/********************************************

HERACLES Analyses on MEASUREMENT table

*********************************************/



--{1300 IN (@list_of_analysis_ids)}?{
-- 1300	Number of persons with at least one measurement occurrence, by measurement_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1300 as analysis_id, 
	o1.measurement_CONCEPT_ID as stratum_1,
	COUNT_BIG(distinct o1.PERSON_ID) as count_value
from
	@CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@measurement_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
--{@measurement_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@measurement_concept_ids != ''}?{
	o1.measurement_concept_id in (@measurement_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	o1.measurement_CONCEPT_ID
;
--}


--{1301 IN (@list_of_analysis_ids)}?{
-- 1301	Number of measurement occurrence records, by measurement_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1301 as analysis_id, 
	o1.measurement_CONCEPT_ID as stratum_1,
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@measurement_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
--{@measurement_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@measurement_concept_ids != ''}?{
	o1.measurement_concept_id in (@measurement_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	o1.measurement_CONCEPT_ID
;
--}



--{1302 IN (@list_of_analysis_ids)}?{
-- 1302	Number of persons by measurement occurrence start month, by measurement_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
	1302 as analysis_id,   
	o1.measurement_concept_id as stratum_1,
	YEAR(measurement_date)*100 + month(measurement_date) as stratum_2, 
	COUNT_BIG(distinct PERSON_ID) as count_value
from
	@CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@measurement_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
--{@measurement_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@measurement_concept_ids != ''}?{
	o1.measurement_concept_id in (@measurement_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	o1.measurement_concept_id, 
	YEAR(measurement_date)*100 + month(measurement_date)
;
--}



--{1303 IN (@list_of_analysis_ids)}?{
-- 1303	Number of distinct measurement occurrence concepts per person
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	1303 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id,
	num_measurements as count_value,
	1.0*(row_number() over (partition by cohort_definition_id order by num_measurements))/(COUNT_BIG(num_measurements) over (partition by cohort_definition_id)+1) as p1
from
	(
	select c1.cohort_definition_id, o1.person_id, COUNT_BIG(distinct o1.measurement_concept_id) as num_measurements
	from
	@CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@measurement_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
--{@measurement_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@measurement_concept_ids != ''}?{
	o1.measurement_concept_id in (@measurement_concept_ids)
--}
--}    
	group by c1.cohort_definition_id, o1.person_id
	) t0
) t1
group by cohort_definition_id
;
--}



--{1304 IN (@list_of_analysis_ids)}?{
-- 1304	Number of persons with at least one measurement occurrence, by measurement_concept_id by calendar year by gender by age decile
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)
select c1.cohort_definition_id,
	1304 as analysis_id,   
	o1.measurement_concept_id as stratum_1,
	YEAR(measurement_date) as stratum_2,
	p1.gender_concept_id as stratum_3,
	floor((year(measurement_date) - p1.year_of_birth)/10) as stratum_4, 
	COUNT_BIG(distinct p1.PERSON_ID) as count_value
from @CDM_schema.PERSON p1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on p1.person_id = c1.subject_id
inner join
@CDM_schema.measurement o1
on p1.person_id = o1.person_id
--{@measurement_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
--{@measurement_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@measurement_concept_ids != ''}?{
	o1.measurement_concept_id in (@measurement_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	o1.measurement_concept_id, 
	YEAR(measurement_date),
	p1.gender_concept_id,
	floor((year(measurement_date) - p1.year_of_birth)/10)
;
--}

--{1305 IN (@list_of_analysis_ids)}?{
-- 1305	Number of measurement occurrence records, by measurement_concept_id by measurement_type_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
	1305 as analysis_id, 
	o1.measurement_CONCEPT_ID as stratum_1,
	o1.measurement_type_concept_id as stratum_2,
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@measurement_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
--{@measurement_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@measurement_concept_ids != ''}?{
	o1.measurement_concept_id in (@measurement_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	o1.measurement_CONCEPT_ID,	
	o1.measurement_type_concept_id
;
--}



--{1306 IN (@list_of_analysis_ids)}?{
-- 1306	Distribution of age by measurement_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	1306 as analysis_id,
	measurement_concept_id as stratum_1,
	gender_concept_id as stratum_2,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	o1.measurement_concept_id,
	p1.gender_concept_id,
	o1.measurement_start_year - p1.year_of_birth as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, o1.measurement_concept_id, p1.gender_concept_id order by o1.measurement_start_year - p1.year_of_birth))/(COUNT_BIG(o1.measurement_start_year - p1.year_of_birth) over (partition by c1.cohort_definition_id, o1.measurement_concept_id, p1.gender_concept_id)+1) as p1
from @CDM_schema.PERSON p1
	inner join (select subject_id, cohort_definition_id from #HERACLES_cohort) c1
		on p1.person_id = c1.subject_id
inner join
(select person_id, measurement_concept_id, min(year(measurement_date)) as measurement_start_year
from @CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@measurement_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
--{@measurement_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@measurement_concept_ids != ''}?{
	o1.measurement_concept_id in (@measurement_concept_ids)
--}
--}
group by person_id, measurement_concept_id
) o1
on p1.person_id = o1.person_id
) t1
group by cohort_definition_id, measurement_concept_id, gender_concept_id
;
--}

--{1307 IN (@list_of_analysis_ids)}?{
-- 1307	Number of measurement occurrence records, by measurement_concept_id and unit_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
	1307 as analysis_id, 
	o1.measurement_CONCEPT_ID as stratum_1,
	o1.unit_concept_id as stratum_2,
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@measurement_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
--{@measurement_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@measurement_concept_ids != ''}?{
	o1.measurement_concept_id in (@measurement_concept_ids)
--}
--}
group by c1.cohort_definition_id,
	o1.measurement_CONCEPT_ID,
	o1.unit_concept_id
;
--}





--{1309 IN (@list_of_analysis_ids)}?{
-- 1309	Number of measurement records with invalid person_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	1309 as analysis_id,  
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
	left join @CDM_schema.PERSON p1
	on p1.person_id = o1.person_id
where p1.person_id is null
group by c1.cohort_definition_id
;
--}


--{1310 IN (@list_of_analysis_ids)}?{
-- 1310	Number of measurement records outside valid observation period
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	1310 as analysis_id,  
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
	left join @CDM_schema.OBSERVATION_PERIOD op1
	on op1.person_id = o1.person_id
	and o1.measurement_date >= op1.OBSERVATION_PERIOD_start_date
	and o1.measurement_date <= op1.OBSERVATION_PERIOD_end_date
where op1.person_id is null
group by c1.cohort_definition_id
;
--}



--{1312 IN (@list_of_analysis_ids)}?{
-- 1312	Number of measurement records with invalid provider_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	1312 as analysis_id,  
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
	left join @CDM_schema.provider p1
	on p1.provider_id = {@CDM_version == '4'}?{ o1.associated_provider_id } {@CDM_version == '5'}?{ o1.provider_id } 
where {@CDM_version == '4'}?{ o1.associated_provider_id } {@CDM_version == '5'}?{ o1.provider_id }  is not null
	and p1.provider_id is null
group by c1.cohort_definition_id
;
--}

--{1313 IN (@list_of_analysis_ids)}?{
-- 1313	Number of measurement records with invalid visit_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	1313 as analysis_id,  
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
	left join @CDM_schema.visit_occurrence vo1
	on o1.visit_occurrence_id = vo1.visit_occurrence_id
where o1.visit_occurrence_id is not null
	and vo1.visit_occurrence_id is null
group by c1.cohort_definition_id
;
--}


--{1314 IN (@list_of_analysis_ids)}?{
-- 1314	Number of measurement records with no value (numeric, string, or concept)
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
	1314 as analysis_id,  
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
where o1.value_as_number is null
	and o1.value_as_concept_id is null
group by c1.cohort_definition_id
;
--}


--{1315 IN (@list_of_analysis_ids)}?{
-- 1315	Distribution of numeric values, by measurement_concept_id and unit_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	1315 as analysis_id,
	measurement_concept_id as stratum_1,
	unit_concept_id as stratum_2,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id,
	measurement_concept_id, unit_concept_id,
	value_as_number as count_value,
	1.0*(row_number() over (partition by cohort_definition_id, measurement_concept_id, unit_concept_id order by value_as_number))/(COUNT_BIG(value_as_number) over (partition by cohort_definition_id, measurement_concept_id, unit_concept_id)+1) as p1
from @CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
where o1.unit_concept_id is not null
	and o1.value_as_number is not null
	--{@observation_concept_ids != ''}?{
	and o1.measurement_concept_id in (@observation_concept_ids)
--}
--{@cohort_period_only == 'TRUE'}?{
	and o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
) t1
group by cohort_definition_id, measurement_concept_id, unit_concept_id
;
--}

--{1316 IN (@list_of_analysis_ids)}?{
-- 1316	Distribution of low range, by measurement_concept_id and unit_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	1316 as analysis_id,
	measurement_concept_id as stratum_1,
	unit_concept_id as stratum_2,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id, measurement_concept_id, unit_concept_id,
	range_low as count_value,
	1.0*(row_number() over (partition by cohort_definition_id, measurement_concept_id, unit_concept_id order by range_low))/(COUNT_BIG(range_low) over (partition by cohort_definition_id, measurement_concept_id, unit_concept_id)+1) as p1
from @CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
where o1.unit_concept_id is not null
	and o1.value_as_number is not null
	and o1.range_low is not null
	and o1.range_high is not null
	--{@observation_concept_ids != ''}?{
	and o1.measurement_concept_id in (@observation_concept_ids)
--}
--{@cohort_period_only == 'TRUE'}?{
	and o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
) t1
group by cohort_definition_id, measurement_concept_id, unit_concept_id
;
--}


--{1317 IN (@list_of_analysis_ids)}?{
-- 1317	Distribution of high range, by measurement_concept_id and unit_concept_id
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	1317 as analysis_id,
	measurement_concept_id as stratum_1,
	unit_concept_id as stratum_2,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select cohort_definition_id, measurement_concept_id, unit_concept_id,
	range_high as count_value,
	1.0*(row_number() over (partition by cohort_definition_id, measurement_concept_id, unit_concept_id order by range_high))/(COUNT_BIG(range_high) over (partition by cohort_definition_id, measurement_concept_id, unit_concept_id)+1) as p1
from @CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
where o1.unit_concept_id is not null
	and o1.value_as_number is not null
	and o1.range_low is not null
	and o1.range_high is not null
	--{@observation_concept_ids != ''}?{
	and o1.measurement_concept_id in (@observation_concept_ids)
--}
--{@cohort_period_only == 'TRUE'}?{
	and o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
) t1
group by cohort_definition_id, measurement_concept_id, unit_concept_id
;
--}



--{1318 IN (@list_of_analysis_ids)}?{
-- 1318	Number of measurement records below/within/above normal range, by measurement_concept_id and unit_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, count_value)
select cohort_definition_id,
	1318 as analysis_id,  
	measurement_concept_id as stratum_1,
	unit_concept_id as stratum_2,
	case when o1.value_as_number < o1.range_low then 'Below Range Low'
		when o1.value_as_number >= o1.range_low and o1.value_as_number <= o1.range_high then 'Within Range'
		when o1.value_as_number > o1.range_high then 'Above Range High'
		else 'Other' end as stratum_3,
	COUNT_BIG(o1.PERSON_ID) as count_value
from
	@CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
where o1.value_as_number is not null
	and o1.unit_concept_id is not null
	and o1.range_low is not null
	and o1.range_high is not null
	--{@observation_concept_ids != ''}?{
	and o1.measurement_concept_id in (@observation_concept_ids)
--}
--{@cohort_period_only == 'TRUE'}?{
	and o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
group by cohort_definition_id, 
	measurement_concept_id,
	unit_concept_id,
	  case when o1.value_as_number < o1.range_low then 'Below Range Low'
		when o1.value_as_number >= o1.range_low and o1.value_as_number <= o1.range_high then 'Within Range'
		when o1.value_as_number > o1.range_high then 'Above Range High'
		else 'Other' end
;
--}

--{1320 IN (@list_of_analysis_ids)}?{
-- 1320	Number of measurement records by condition occurrence start month
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1320 as analysis_id,   
	YEAR(measurement_date)*100 + month(measurement_date) as stratum_1, 
	COUNT_BIG(PERSON_ID) as count_value
from
	@CDM_schema.measurement o1
	inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
		on o1.person_id = c1.subject_id
--{@measurement_concept_ids != '' | @cohort_period_only == 'TRUE'}?{    
WHERE 
--{@cohort_period_only == 'TRUE'}?{
	o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
--{@measurement_concept_ids != '' & @cohort_period_only == 'TRUE'}?{ 
AND
--}
--{@measurement_concept_ids != ''}?{
	o1.measurement_concept_id in (@measurement_concept_ids)
--}
--}    
group by c1.cohort_definition_id,
	YEAR(measurement_date)*100 + month(measurement_date)
;
--}





--/********************************************

--HERACLES Analyses on COHORT table

--*********************************************/

--{1700 IN (@list_of_analysis_ids)}?{
-- 1700	Number of records by cohort_definition_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c2.cohort_definition_id, 
	1700 as analysis_id, 
	c1.cohort_definition_id as stratum_1, 
	COUNT_BIG(c1.subject_ID) as count_value
from
	@results_schema.COHORT c1
	inner join (select subject_id, cohort_definition_id from #HERACLES_cohort) c2
on c1.subject_id = c2.subject_id
group by c2.cohort_definition_id,
	c1.cohort_definition_id
;
--}


--{1701 IN (@list_of_analysis_ids)}?{
-- 1701	Number of records with cohort end date < cohort start date
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, count_value)
select c2.cohort_definition_id,
	1701 as analysis_id, 
	COUNT_BIG(c1.subject_ID) as count_value
from	
	@results_schema.COHORT c1
	inner join (select subject_id, cohort_definition_id from #HERACLES_cohort) c2
on c1.subject_id = c2.subject_id
where c1.cohort_end_date < c1.cohort_start_date
group by c2.cohort_definition_id
;
--}





--/********************************************

--HERACLES Analyses on analysis relative to selected COHORT

--*********************************************/

--{1800 IN (@list_of_analysis_ids)}?{
-- 1800	Number of persons by age, with age at cohort start
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1800 as analysis_id, 
	year(c1.cohort_start_date) - p1.YEAR_OF_BIRTH as stratum_1, 
	COUNT_BIG(p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id, year(c1.cohort_start_date) - p1.YEAR_OF_BIRTH
;
--}	
	
	

--{1801 IN (@list_of_analysis_ids)}?{
-- 1801	Distribution of age at cohort start
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id, 
	1801 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	year(c1.cohort_start_date) - p1.YEAR_OF_BIRTH as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id order by year(c1.cohort_start_date) - p1.YEAR_OF_BIRTH))/(COUNT_BIG(year(c1.cohort_start_date) - p1.YEAR_OF_BIRTH) over (partition by c1.cohort_definition_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
) t1
group by cohort_definition_id
;

--}
	

--{1802 IN (@list_of_analysis_ids)}?{
-- 1802	Distribution of age at cohort start by gender
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id, 
	1802 as analysis_id,
	gender_concept_id as stratum_1,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	p1.gender_concept_id,
	year(c1.cohort_start_date) - p1.YEAR_OF_BIRTH as count_value,
	1.0*(row_number() over (partition by p1.gender_concept_id, c1.cohort_definition_id order by year(c1.cohort_start_date) - p1.YEAR_OF_BIRTH))/(COUNT_BIG(year(c1.cohort_start_date) - p1.YEAR_OF_BIRTH) over (partition by p1.gender_concept_id, c1.cohort_definition_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
) t1
group by cohort_definition_id, gender_concept_id
;
--}	
	
	
--{1803 IN (@list_of_analysis_ids)}?{
-- 1803	Distribution of age at cohort start by cohort start year
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id, 
	1803 as analysis_id,
	cohort_year as stratum_1,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	year(c1.cohort_start_date) as cohort_year,
	year(c1.cohort_start_date) - p1.YEAR_OF_BIRTH as count_value,
	1.0*(row_number() over (partition by year(c1.cohort_start_date), c1.cohort_definition_id order by year(c1.cohort_start_date) - p1.YEAR_OF_BIRTH))/(COUNT_BIG(year(c1.cohort_start_date) - p1.YEAR_OF_BIRTH) over (partition by year(c1.cohort_start_date), c1.cohort_definition_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
) t1
group by cohort_definition_id, cohort_year
;
--}	


--{1804 IN (@list_of_analysis_ids)}?{
-- 1804	Number of persons by duration from cohort start to cohort end, in 30d increments
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1804 as analysis_id,  
	floor(DATEDIFF(dd, c1.cohort_start_date, c1.cohort_end_date)/30) as stratum_1, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id, floor(DATEDIFF(dd, c1.cohort_start_date, c1.cohort_end_date)/30)
;
--}	
	
	
--{1805 IN (@list_of_analysis_ids)}?{
-- 1805	Number of persons by duration from observation start to cohort start, in 30d increments
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1805 as analysis_id,  
	floor(DATEDIFF(dd, op1.observation_period_start_date, c1.cohort_start_date)/30) as stratum_1, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join @CDM_schema.observation_period op1
on p1.person_id = op1.person_id
where c1.cohort_start_date >= op1.observation_period_start_date
and c1.cohort_start_date <= op1.observation_period_end_date
group by c1.cohort_definition_id, floor(DATEDIFF(dd, op1.observation_period_start_date, c1.cohort_start_date)/30)
;
--}	
	
--{1806 IN (@list_of_analysis_ids)}?{	
-- 1806	Number of persons by duration from cohort start to observation end, in 30d increments
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1806 as analysis_id,  
	floor(DATEDIFF(dd, c1.cohort_start_date, op1.observation_period_end_date)/30) as stratum_1, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join @CDM_schema.observation_period op1
on p1.person_id = op1.person_id
where c1.cohort_start_date >= op1.observation_period_start_date
and c1.cohort_start_date <= op1.observation_period_end_date
group by c1.cohort_definition_id, floor(DATEDIFF(dd, c1.cohort_start_date, op1.observation_period_end_date)/30)
;
--}	

--{1807 IN (@list_of_analysis_ids)}?{
-- 1807	Number of persons by duration from cohort end to observation end, in 30d increments
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, 
	1807 as analysis_id,  
	floor(DATEDIFF(dd, c1.cohort_end_date, op1.observation_period_end_date)/30) as stratum_1, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join @CDM_schema.observation_period op1
on p1.person_id = op1.person_id
where c1.cohort_start_date >= op1.observation_period_start_date
and c1.cohort_start_date <= op1.observation_period_end_date
group by c1.cohort_definition_id, floor(DATEDIFF(dd, c1.cohort_end_date, op1.observation_period_end_date)/30)
;
--}	
		
	
--{1808 IN (@list_of_analysis_ids)}?{
-- 1808	Distribution of duration (days) from cohort start to cohort end
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	1808 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	DATEDIFF(dd,c1.cohort_start_date, c1.cohort_end_date) as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id order by DATEDIFF(dd,c1.cohort_start_date, c1.cohort_end_date)))/(COUNT_BIG(DATEDIFF(dd,c1.cohort_start_date, c1.cohort_end_date)) over (partition by c1.cohort_definition_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
) t1
group by cohort_definition_id
;
--}

	
--{1809 IN (@list_of_analysis_ids)}?{
-- 1809	Distribution of duration (days) from cohort start to cohort end, by gender
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	1809 as analysis_id,
	gender_concept_id as stratum_1,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	p1.gender_concept_id,
	DATEDIFF(dd,c1.cohort_start_date, c1.cohort_end_date) as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, p1.gender_concept_id order by DATEDIFF(dd,c1.cohort_start_date, c1.cohort_end_date)))/(COUNT_BIG(DATEDIFF(dd,c1.cohort_start_date, c1.cohort_end_date)) over (partition by c1.cohort_definition_id, p1.gender_concept_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
) t1
group by cohort_definition_id,
	gender_concept_id
;
--}
	
	
--{1810 IN (@list_of_analysis_ids)}?{
-- 1810	Distribution of duration (days) from cohort start to cohort end, by age decile
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	1810 as analysis_id,
	age_decile as stratum_1,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	floor((year(c1.cohort_start_date) - p1.YEAR_OF_BIRTH)/10) as age_decile,
	DATEDIFF(dd,c1.cohort_start_date, c1.cohort_end_date) as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id, floor((year(c1.cohort_start_date) - p1.YEAR_OF_BIRTH)/10) order by DATEDIFF(dd,c1.cohort_start_date, c1.cohort_end_date)))/(COUNT_BIG(DATEDIFF(dd,c1.cohort_start_date, c1.cohort_end_date)) over (partition by c1.cohort_definition_id, floor((year(c1.cohort_start_date) - p1.YEAR_OF_BIRTH)/10))+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
) t1
group by cohort_definition_id,
	age_decile
;
--}
	
--{1811 IN (@list_of_analysis_ids)}?{
-- 1811	Distribution of duration (days) from observation start to cohort start
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	1811 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	DATEDIFF(dd,op1.observation_period_start_date, c1.cohort_start_date) as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id order by DATEDIFF(dd,op1.observation_period_start_date, c1.cohort_start_date)))/(COUNT_BIG(DATEDIFF(dd,op1.observation_period_start_date, c1.cohort_start_date)) over (partition by c1.cohort_definition_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join @CDM_schema.observation_period op1
on p1.person_id = op1.person_id
where c1.cohort_start_date >= op1.observation_period_start_date
and c1.cohort_start_date <= op1.observation_period_end_date
) t1
group by cohort_definition_id
;
--}
	
	
--{1812 IN (@list_of_analysis_ids)}?{
-- 1812	Distribution of duration (days) from cohort start to observation end
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	1812 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	DATEDIFF(dd,c1.cohort_start_date, op1.observation_period_end_date) as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id order by DATEDIFF(dd,c1.cohort_start_date, op1.observation_period_end_date)))/(COUNT_BIG(DATEDIFF(dd,c1.cohort_start_date, op1.observation_period_end_date)) over (partition by c1.cohort_definition_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join @CDM_schema.observation_period op1
on p1.person_id = op1.person_id
where c1.cohort_start_date >= op1.observation_period_start_date
and c1.cohort_start_date <= op1.observation_period_end_date
) t1
group by cohort_definition_id
;
--}
	

--{1813 IN (@list_of_analysis_ids)}?{
-- 1813	Distribution of duration (days) from cohort end to observation end
insert into @results_schema.HERACLES_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
select cohort_definition_id,
	1813 as analysis_id,
	COUNT_BIG(count_value) as count_value,
	min(count_value) as min_value,
	max(count_value) as max_value,
	avg(1.0*count_value) as avg_value,
	stdev(count_value) as stdev_value,
	max(case when p1<=0.50 then count_value else -9999 end) as median_value,
	max(case when p1<=0.10 then count_value else -9999 end) as p10_value,
	max(case when p1<=0.25 then count_value else -9999 end) as p25_value,
	max(case when p1<=0.75 then count_value else -9999 end) as p75_value,
	max(case when p1<=0.90 then count_value else -9999 end) as p90_value
from
(
select c1.cohort_definition_id,
	DATEDIFF(dd,c1.cohort_end_date, op1.observation_period_end_date) as count_value,
	1.0*(row_number() over (partition by c1.cohort_definition_id order by DATEDIFF(dd,c1.cohort_end_date, op1.observation_period_end_date)))/(COUNT_BIG(DATEDIFF(dd,c1.cohort_end_date, op1.observation_period_end_date)) over (partition by c1.cohort_definition_id)+1) as p1
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join @CDM_schema.observation_period op1
on p1.person_id = op1.person_id
where c1.cohort_start_date >= op1.observation_period_start_date
and c1.cohort_start_date <= op1.observation_period_end_date
) t1
group by cohort_definition_id
;
--}

	
--{1814 IN (@list_of_analysis_ids)}?{
-- 1814	Number of persons by cohort start year by gender by age decile
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, count_value)
select c1.cohort_definition_id,
	1814 as analysis_id,   
	YEAR(c1.cohort_start_date) as stratum_1,
	p1.gender_concept_id as stratum_2,
	floor((year(c1.cohort_start_date) - p1.year_of_birth)/10) as stratum_3, 
	COUNT_BIG(distinct p1.PERSON_ID) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
group by c1.cohort_definition_id, 
	YEAR(c1.cohort_start_date),
	p1.gender_concept_id,
	floor((YEAR(c1.cohort_start_date) - p1.year_of_birth)/10)
;
--}

	
--{1815 IN (@list_of_analysis_ids)}?{
-- 1815	Number of persons by cohort start month
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
	1815 as analysis_id,   
	YEAR(c1.cohort_start_date)*100 + month(c1.cohort_start_date) as stratum_1, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date from #HERACLES_cohort) c1
	on p1.person_id = c1.subject_id
group by c1.cohort_definition_id,
	YEAR(c1.cohort_start_date)*100 + month(c1.cohort_start_date)
;
--}

	
--{1816 IN (@list_of_analysis_ids)}?{
-- 1816	Number of persons by number of cohort periods
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select cohort_definition_id, 
	1816 as analysis_id,  
	num_periods as stratum_1, 
	COUNT_BIG(distinct person_id) as count_value
from
	(select c1.cohort_definition_id, p1.person_id, COUNT_BIG(c1.cohort_start_date) as num_periods 
		from @CDM_schema.PERSON p1
		inner join (select subject_id, cohort_definition_id, cohort_start_date from #HERACLES_cohort) c1
			on p1.person_id = c1.subject_id
		group by c1.cohort_definition_id, p1.person_id) nc1
group by cohort_definition_id, num_periods
;
--}
	
	
--{1820 IN (@list_of_analysis_ids)}?{
-- 1820	Number of persons by duration from cohort start to first occurrence of condition occurrence, by condition_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id, 
	1820 as analysis_id,
	co1.condition_concept_id as stratum_1,
	case when c1.cohort_start_date = co1.first_date then 0
		when c1.cohort_start_date < co1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, co1.first_date)/30)+1
		when c1.cohort_start_date > co1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, co1.first_date)/30)-1
	end as stratum_2, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join
	(
		select co0.person_id, co0.condition_concept_id, min(co0.condition_start_date) as first_date
		from @CDM_schema.condition_occurrence co0
			inner join (select subject_id, cohort_definition_id from #HERACLES_cohort) c0
			on co0.person_id = c0.subject_id
		--{@condition_concept_ids != ''}?{
			where co0.condition_concept_id in (@condition_concept_ids)
		--}
		group by co0.person_id, co0.condition_concept_id
	) co1
on p1.person_id = co1.person_id
group by c1.cohort_definition_id,
	co1.condition_concept_id,
	case when c1.cohort_start_date = co1.first_date then 0
		when c1.cohort_start_date < co1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, co1.first_date)/30)+1
		when c1.cohort_start_date > co1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, co1.first_date)/30)-1
	end
;
--}	
	
	
--{1821 IN (@list_of_analysis_ids)}?{
-- 1821	Number of events by duration from cohort start to all occurrences of condition occurrence, by condition_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id, 
	1821 as analysis_id,
	co1.condition_concept_id as stratum_1,
	case when c1.cohort_start_date = co1.condition_start_date then 0
		when c1.cohort_start_date < co1.condition_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, co1.condition_start_date)/30)+1
		when c1.cohort_start_date > co1.condition_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, co1.condition_start_date)/30)-1
	end as stratum_2, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join
	@CDM_schema.condition_occurrence co1
on p1.person_id = co1.person_id
--{@condition_concept_ids != ''}?{
	where co1.condition_concept_id in (@condition_concept_ids)
--}
group by c1.cohort_definition_id,
	co1.condition_concept_id,
	case when c1.cohort_start_date = co1.condition_start_date then 0
		when c1.cohort_start_date < co1.condition_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, co1.condition_start_date)/30)+1
		when c1.cohort_start_date > co1.condition_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, co1.condition_start_date)/30)-1
	end
;
--}	
	
	
	
--{1830 IN (@list_of_analysis_ids)}?{
-- 1830	Number of persons by duration from cohort start to first occurrence of procedure occurrence, by procedure_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id, 
	1830 as analysis_id,
	po1.procedure_concept_id as stratum_1,
	case when c1.cohort_start_date = po1.first_date then 0
		when c1.cohort_start_date < po1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, po1.first_date)/30)+1
		when c1.cohort_start_date > po1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, po1.first_date)/30)-1
	end as stratum_2, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join
	(
		select po0.person_id, po0.procedure_concept_id, min(po0.procedure_date) as first_date
		from @CDM_schema.procedure_occurrence po0
			inner join (select subject_id, cohort_definition_id from #HERACLES_cohort) c0
			on po0.person_id = c0.subject_id
		--{@procedure_concept_ids != ''}?{
			where po0.procedure_concept_id in (@procedure_concept_ids)
		--}
		group by po0.person_id, po0.procedure_concept_id
	) po1
on p1.person_id = po1.person_id
group by c1.cohort_definition_id,
	po1.procedure_concept_id,
	case when c1.cohort_start_date = po1.first_date then 0
		when c1.cohort_start_date < po1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, po1.first_date)/30)+1
		when c1.cohort_start_date > po1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, po1.first_date)/30)-1
	end
;
--}	
	
	
--{1831 IN (@list_of_analysis_ids)}?{
-- 1831	Number of events by duration from cohort start to all occurrences of procedure occurrence, by procedure_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id, 
	1831 as analysis_id,
	po1.procedure_concept_id as stratum_1,
	case when c1.cohort_start_date = po1.procedure_date then 0
		when c1.cohort_start_date < po1.procedure_date then floor(DATEDIFF(dd, c1.cohort_start_date, po1.procedure_date)/30)+1
		when c1.cohort_start_date > po1.procedure_date then floor(DATEDIFF(dd, c1.cohort_start_date, po1.procedure_date)/30)-1
	end as stratum_2, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join
	@CDM_schema.procedure_occurrence po1
on p1.person_id = po1.person_id
		--{@procedure_concept_ids != ''}?{
			where po1.procedure_concept_id in (@procedure_concept_ids)
		--}
group by c1.cohort_definition_id,
	po1.procedure_concept_id,
	case when c1.cohort_start_date = po1.procedure_date then 0
		when c1.cohort_start_date < po1.procedure_date then floor(DATEDIFF(dd, c1.cohort_start_date, po1.procedure_date)/30)+1
		when c1.cohort_start_date > po1.procedure_date then floor(DATEDIFF(dd, c1.cohort_start_date, po1.procedure_date)/30)-1
	end
;
--}		
	
	

--{1840 IN (@list_of_analysis_ids)}?{
-- 1840	Number of persons by duration from cohort start to first occurrence of drug_exposure, by drug_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id, 
	1840 as analysis_id,
	de1.drug_concept_id as stratum_1,
	case when c1.cohort_start_date = de1.first_date then 0
		when c1.cohort_start_date < de1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.first_date)/30)+1
		when c1.cohort_start_date > de1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.first_date)/30)-1
	end as stratum_2, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join
	(
		select de0.person_id, de0.drug_concept_id, min(de0.drug_exposure_start_date) as first_date
		from @CDM_schema.drug_exposure de0
			inner join (select subject_id, cohort_definition_id from #HERACLES_cohort) c0
			on de0.person_id = c0.subject_id
			--{@drug_concept_ids != ''}?{
			where de0.drug_concept_id in (@drug_concept_ids)
			--}
		group by de0.person_id, de0.drug_concept_id
	) de1
on p1.person_id = de1.person_id
group by c1.cohort_definition_id,
	de1.drug_concept_id,
	case when c1.cohort_start_date = de1.first_date then 0
		when c1.cohort_start_date < de1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.first_date)/30)+1
		when c1.cohort_start_date > de1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.first_date)/30)-1
	end
;
--}	
	
	
--{1841 IN (@list_of_analysis_ids)}?{
-- 1841	Number of events by duration from cohort start to all occurrences of drug_exposure, by drug_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id, 
	1841 as analysis_id,
	de1.drug_concept_id as stratum_1,
	case when c1.cohort_start_date = de1.drug_exposure_start_date then 0
		when c1.cohort_start_date < de1.drug_exposure_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.drug_exposure_start_date)/30)+1
		when c1.cohort_start_date > de1.drug_exposure_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.drug_exposure_start_date)/30)-1
	end as stratum_2, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join
	@CDM_schema.drug_exposure de1
on p1.person_id = de1.person_id
--{@drug_concept_ids != ''}?{
			where de1.drug_concept_id in (@drug_concept_ids)
			--}
group by c1.cohort_definition_id,
	de1.drug_concept_id,
	case when c1.cohort_start_date = de1.drug_exposure_start_date then 0
		when c1.cohort_start_date < de1.drug_exposure_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.drug_exposure_start_date)/30)+1
		when c1.cohort_start_date > de1.drug_exposure_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.drug_exposure_start_date)/30)-1
	end
;
--}		
	

--{1850 IN (@list_of_analysis_ids)}?{
-- 1850	Number of persons by duration from cohort start to first occurrence of observation, by observation_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id, 
	1850 as analysis_id,
	o1.observation_concept_id as stratum_1,
	case when c1.cohort_start_date = o1.first_date then 0
		when c1.cohort_start_date < o1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, o1.first_date)/30)+1
		when c1.cohort_start_date > o1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, o1.first_date)/30)-1
	end as stratum_2, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join
	(
		select o0.person_id, o0.observation_concept_id, min(o0.observation_date) as first_date
		from @CDM_schema.observation o0
			inner join (select subject_id, cohort_definition_id from #HERACLES_cohort) c0
			on o0.person_id = c0.subject_id
		--{@observation_concept_ids != ''}?{
			where o0.observation_concept_id in (@observation_concept_ids)
			--}
		group by o0.person_id, o0.observation_concept_id
	) o1
on p1.person_id = o1.person_id
group by c1.cohort_definition_id,
	o1.observation_concept_id,
	case when c1.cohort_start_date = o1.first_date then 0
		when c1.cohort_start_date < o1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, o1.first_date)/30)+1
		when c1.cohort_start_date > o1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, o1.first_date)/30)-1
	end
;
--}	
	
	
--{1851 IN (@list_of_analysis_ids)}?{
-- 1851	Number of events by duration from cohort start to all occurrences of observation, by observation_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id, 
	1851 as analysis_id,
	o1.observation_concept_id as stratum_1,
	case when c1.cohort_start_date = o1.observation_date then 0
		when c1.cohort_start_date < o1.observation_date then floor(DATEDIFF(dd, c1.cohort_start_date, o1.observation_date)/30)+1
		when c1.cohort_start_date > o1.observation_date then floor(DATEDIFF(dd, c1.cohort_start_date, o1.observation_date)/30)-1
	end as stratum_2, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join
	@CDM_schema.observation o1
on p1.person_id = o1.person_id
		--{@observation_concept_ids != ''}?{
			where o1.observation_concept_id in (@observation_concept_ids)
			--}
group by c1.cohort_definition_id,
	o1.observation_concept_id,
	case when c1.cohort_start_date = o1.observation_date then 0
		when c1.cohort_start_date < o1.observation_date then floor(DATEDIFF(dd, c1.cohort_start_date, o1.observation_date)/30)+1
		when c1.cohort_start_date > o1.observation_date then floor(DATEDIFF(dd, c1.cohort_start_date, o1.observation_date)/30)-1
	end
;
--}

	

--{1860 IN (@list_of_analysis_ids)}?{
-- 1860	Number of persons by duration from cohort start to first occurrence of condition era, by condition_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id, 
	1860 as analysis_id,
	ce1.condition_concept_id as stratum_1,
	case when c1.cohort_start_date = ce1.first_date then 0
		when c1.cohort_start_date < ce1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, ce1.first_date)/30)+1
		when c1.cohort_start_date > ce1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, ce1.first_date)/30)-1
	end as stratum_2, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join
	(
		select ce0.person_id, ce0.condition_concept_id, min(ce0.condition_era_start_date) as first_date
		from @CDM_schema.condition_era ce0
			inner join (select subject_id, cohort_definition_id from #HERACLES_cohort) c0
			on ce0.person_id = c0.subject_id
			--{@condition_concept_ids != ''}?{
			where ce0.condition_concept_id in (@condition_concept_ids)
			--}
		group by ce0.person_id, ce0.condition_concept_id
	) ce1
on p1.person_id = ce1.person_id
group by c1.cohort_definition_id,
	ce1.condition_concept_id,
	case when c1.cohort_start_date = ce1.first_date then 0
		when c1.cohort_start_date < ce1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, ce1.first_date)/30)+1
		when c1.cohort_start_date > ce1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, ce1.first_date)/30)-1
	end
;
--}	
	
	
--{1861 IN (@list_of_analysis_ids)}?{
-- 1861	Number of events by duration from cohort start to all occurrences of condition era, by condition_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id, 
	1861 as analysis_id,
	ce1.condition_concept_id as stratum_1,
	case when c1.cohort_start_date = ce1.condition_era_start_date then 0
		when c1.cohort_start_date < ce1.condition_era_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, ce1.condition_era_start_date)/30)+1
		when c1.cohort_start_date > ce1.condition_era_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, ce1.condition_era_start_date)/30)-1
	end as stratum_2, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join
	@CDM_schema.condition_era ce1
on p1.person_id = ce1.person_id
--{@condition_concept_ids != ''}?{
			where ce1.condition_concept_id in (@condition_concept_ids)
			--}
group by c1.cohort_definition_id,
	ce1.condition_concept_id,
	case when c1.cohort_start_date = ce1.condition_era_start_date then 0
		when c1.cohort_start_date < ce1.condition_era_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, ce1.condition_era_start_date)/30)+1
		when c1.cohort_start_date > ce1.condition_era_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, ce1.condition_era_start_date)/30)-1
	end
;
--}	

	
	
--{1870 IN (@list_of_analysis_ids)}?{
-- 1870	Number of persons by duration from cohort start to first occurrence of drug era, by drug_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id, 
	1870 as analysis_id,
	de1.drug_concept_id as stratum_1,
	case when c1.cohort_start_date = de1.first_date then 0
		when c1.cohort_start_date < de1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.first_date)/30)+1
		when c1.cohort_start_date > de1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.first_date)/30)-1
	end as stratum_2, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join
	(
		select de0.person_id, de0.drug_concept_id, min(de0.drug_era_start_date) as first_date
		from @CDM_schema.drug_era de0
			inner join (select subject_id, cohort_definition_id from #HERACLES_cohort) c0
			on de0.person_id = c0.subject_id
		--{@drug_concept_ids != ''}?{
			where de0.drug_concept_id in (@drug_concept_ids)
			--}
		group by de0.person_id, de0.drug_concept_id
	) de1
on p1.person_id = de1.person_id
group by c1.cohort_definition_id,
	de1.drug_concept_id,
	case when c1.cohort_start_date = de1.first_date then 0
		when c1.cohort_start_date < de1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.first_date)/30)+1
		when c1.cohort_start_date > de1.first_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.first_date)/30)-1
	end
;
--}	
	
	
--{1871 IN (@list_of_analysis_ids)}?{
-- 1871	Number of events by duration from cohort start to all occurrences of drug era, by drug_concept_id
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id, 
	1871 as analysis_id,
	de1.drug_concept_id as stratum_1,
	case when c1.cohort_start_date = de1.drug_era_start_date then 0
		when c1.cohort_start_date < de1.drug_era_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.drug_era_start_date)/30)+1
		when c1.cohort_start_date > de1.drug_era_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.drug_era_start_date)/30)-1
	end as stratum_2, 
	COUNT_BIG(distinct p1.person_id) as count_value
from @CDM_schema.PERSON p1
inner join (select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date from #HERACLES_cohort) c1
on p1.person_id = c1.subject_id
inner join
	@CDM_schema.drug_era de1
on p1.person_id = de1.person_id
--{@drug_concept_ids != ''}?{
			where de1.drug_concept_id in (@drug_concept_ids)
			--}
group by c1.cohort_definition_id,
	de1.drug_concept_id,
	case when c1.cohort_start_date = de1.drug_era_start_date then 0
		when c1.cohort_start_date < de1.drug_era_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.drug_era_start_date)/30)+1
		when c1.cohort_start_date > de1.drug_era_start_date then floor(DATEDIFF(dd, c1.cohort_start_date, de1.drug_era_start_date)/30)-1
	end
;
--}	

--{2001 IN (@list_of_analysis_ids)}?{
-- 2001	Count and percentage of gender data completeness for age less than 10
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2001 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -10, getdate()))
                    --EXTRACT (
                    --   YEAR FROM TRUNC (SYSDATE, 'yyyy') - INTERVAL '10' YEAR)
       LEFT JOIN @CDM_schema.CONCEPT c ON p.GENDER_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.gender_concept_id IS NOT NULL
 and (lower(c.CONCEPT_NAME) = 'female'
 or lower(c.CONCEPT_NAME) = 'male' )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -10, getdate()))
                    --EXTRACT (
                    --   YEAR FROM TRUNC (SYSDATE, 'yyyy') - INTERVAL '10' YEAR)
 ) all_data) innerT;
--}

--{2002 IN (@list_of_analysis_ids)}?{
-- 2002	Count and percentage of gender data completeness for age between 10~20
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2002 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -20, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -10, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.GENDER_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.gender_concept_id IS NOT NULL
 and (lower(c.CONCEPT_NAME) = 'female'
 or lower(c.CONCEPT_NAME) = 'male' )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -20, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -10, getdate()))
 ) all_data) innerT;
--}

--{2003 IN (@list_of_analysis_ids)}?{
-- 2003	Count and percentage of gender data completeness for age between 20~30
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2003 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -30, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -20, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.GENDER_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.gender_concept_id IS NOT NULL
 and (lower(c.CONCEPT_NAME) = 'female'
 or lower(c.CONCEPT_NAME) = 'male' )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -30, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -20, getdate()))
 ) all_data) innerT;
--}

--{2004 IN (@list_of_analysis_ids)}?{
-- 2004	Count and percentage of gender data completeness for age between 30~40
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2004 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -40, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -30, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.GENDER_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.gender_concept_id IS NOT NULL
 and (lower(c.CONCEPT_NAME) = 'female'
 or lower(c.CONCEPT_NAME) = 'male' )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -40, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -30, getdate()))
 ) all_data) innerT;
--}

--{2005 IN (@list_of_analysis_ids)}?{
-- 2005	Count and percentage of gender data completeness for age between 40~50
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2005 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -50, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -40, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.GENDER_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.gender_concept_id IS NOT NULL
 and (lower(c.CONCEPT_NAME) = 'female'
 or lower(c.CONCEPT_NAME) = 'male' )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -50, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -40, getdate()))
 ) all_data) innerT;
--}

--{2006 IN (@list_of_analysis_ids)}?{
-- 2006	Count and percentage of gender data completeness for age between 50~60
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2006 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -60, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -50, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.GENDER_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.gender_concept_id IS NOT NULL
 and (lower(c.CONCEPT_NAME) = 'female'
 or lower(c.CONCEPT_NAME) = 'male' )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -60, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -50, getdate()))
 ) all_data) innerT;
--}

--{2007 IN (@list_of_analysis_ids)}?{
-- 2007	Count and percentage of gender data completeness for age between 60+
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2007 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -60, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.GENDER_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.gender_concept_id IS NOT NULL
 and (lower(c.CONCEPT_NAME) = 'female'
 or lower(c.CONCEPT_NAME) = 'male' )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -60, getdate()))
 ) all_data) innerT;
--}

--{2011 IN (@list_of_analysis_ids)}?{
-- 2011	Count and percentage of race data completeness for age less than 10
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2011 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -10, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.RACE_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.RACE_CONCEPT_ID IS NOT NULL
 and (lower(c.CONCEPT_NAME) not like 'other%'
	and lower(c.CONCEPT_NAME) not like 'non%'
	and lower(c.CONCEPT_NAME) not like '%not %'
	and lower(c.CONCEPT_NAME) not like 'unknown'
 )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -10, getdate()))
 ) all_data) innerT;
--}

--{2012 IN (@list_of_analysis_ids)}?{
-- 2012	Count and percentage of race data completeness for age between 10~20
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2012 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -20, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -10, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.RACE_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.RACE_CONCEPT_ID IS NOT NULL
 and (lower(c.CONCEPT_NAME) not like 'other%'
	and lower(c.CONCEPT_NAME) not like 'non%'
	and lower(c.CONCEPT_NAME) not like '%not %'
	and lower(c.CONCEPT_NAME) not like 'unknown'
 )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -20, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -10, getdate()))
 ) all_data) innerT;
--}

--{2013 IN (@list_of_analysis_ids)}?{
-- 2013	Count and percentage of race data completeness for age between 20~30
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2013 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -30, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -20, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.RACE_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.RACE_CONCEPT_ID IS NOT NULL
 and (lower(c.CONCEPT_NAME) not like 'other%'
	and lower(c.CONCEPT_NAME) not like 'non%'
	and lower(c.CONCEPT_NAME) not like '%not %'
	and lower(c.CONCEPT_NAME) not like 'unknown'
 )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -30, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -20, getdate()))
 ) all_data) innerT;
--}

--{2014 IN (@list_of_analysis_ids)}?{
-- 2014	Count and percentage of race data completeness for age between 30~40
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2014 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -40, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -30, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.RACE_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.RACE_CONCEPT_ID IS NOT NULL
 and (lower(c.CONCEPT_NAME) not like 'other%'
	and lower(c.CONCEPT_NAME) not like 'non%'
	and lower(c.CONCEPT_NAME) not like '%not %'
	and lower(c.CONCEPT_NAME) not like 'unknown'
 )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -40, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -30, getdate()))
 ) all_data) innerT;
--}

--{2015 IN (@list_of_analysis_ids)}?{
-- 2015	Count and percentage of race data completeness for age between 40~50
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2015 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -50, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -40, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.RACE_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.RACE_CONCEPT_ID IS NOT NULL
 and (lower(c.CONCEPT_NAME) not like 'other%'
	and lower(c.CONCEPT_NAME) not like 'non%'
	and lower(c.CONCEPT_NAME) not like '%not %'
	and lower(c.CONCEPT_NAME) not like 'unknown'
 )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -50, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -40, getdate()))
 ) all_data) innerT;
--}

--{2016 IN (@list_of_analysis_ids)}?{
-- 2016	Count and percentage of race data completeness for age between 50~60
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2016 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -60, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -50, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.RACE_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.RACE_CONCEPT_ID IS NOT NULL
 and (lower(c.CONCEPT_NAME) not like 'other%'
	and lower(c.CONCEPT_NAME) not like 'non%'
	and lower(c.CONCEPT_NAME) not like '%not %'
	and lower(c.CONCEPT_NAME) not like 'unknown'
 )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -60, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -50, getdate()))
 ) all_data) innerT;
--}

--{2017 IN (@list_of_analysis_ids)}?{
-- 2017	Count and percentage of race data completeness for age between 60+
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2017 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -60, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.RACE_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.RACE_CONCEPT_ID IS NOT NULL
 and (lower(c.CONCEPT_NAME) not like 'other%'
	and lower(c.CONCEPT_NAME) not like 'non%'
	and lower(c.CONCEPT_NAME) not like '%not %'
	and lower(c.CONCEPT_NAME) not like 'unknown'
 )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -60, getdate()))
 ) all_data) innerT;
--}

--{2021 IN (@list_of_analysis_ids)}?{
-- 2021	Count and percentage of ethnicity data completeness for age less than 10
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2021 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -10, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.ETHNICITY_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.ETHNICITY_CONCEPT_ID IS NOT NULL
 and (
 	lower(c.CONCEPT_NAME) not like '%unknown%'
 )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -10, getdate()))
 ) all_data) innerT;
--}

--{2022 IN (@list_of_analysis_ids)}?{
-- 2022	Count and percentage of ethnicity data completeness for age between 10~20
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2022 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -20, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -10, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.ETHNICITY_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.ETHNICITY_CONCEPT_ID IS NOT NULL
 and (
 	lower(c.CONCEPT_NAME) not like '%unknown%'
 )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -20, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -10, getdate()))
 ) all_data) innerT;
--}

--{2023 IN (@list_of_analysis_ids)}?{
-- 2023	Count and percentage of ethnicity data completeness for age between 20~30
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2023 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -30, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -20, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.ETHNICITY_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.ETHNICITY_CONCEPT_ID IS NOT NULL
 and (
 	lower(c.CONCEPT_NAME) not like '%unknown%'
 )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -30, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -20, getdate()))
 ) all_data) innerT;
--}

--{2024 IN (@list_of_analysis_ids)}?{
-- 2024	Count and percentage of ethnicity data completeness for age between 30~40
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2024 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -40, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -30, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.ETHNICITY_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.ETHNICITY_CONCEPT_ID IS NOT NULL
 and (
 	lower(c.CONCEPT_NAME) not like '%unknown%'
 )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -40, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -30, getdate()))
 ) all_data) innerT;
--}

--{2025 IN (@list_of_analysis_ids)}?{
-- 2025	Count and percentage of ethnicity data completeness for age between 40~50
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2025 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -50, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -40, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.ETHNICITY_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.ETHNICITY_CONCEPT_ID IS NOT NULL
 and (
 	lower(c.CONCEPT_NAME) not like '%unknown%'
 )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -50, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -40, getdate()))
 ) all_data) innerT;
--}

--{2026 IN (@list_of_analysis_ids)}?{
-- 2026	Count and percentage of ethnicity data completeness for age between 50~60
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2026 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -60, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -50, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.ETHNICITY_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.ETHNICITY_CONCEPT_ID IS NOT NULL
 and (
 	lower(c.CONCEPT_NAME) not like '%unknown%'
 )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH >
             				year(DATEADD(year, -60, getdate()))
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -50, getdate()))
 ) all_data) innerT;
--}

--{2027 IN (@list_of_analysis_ids)}?{
-- 2027	Count and percentage of ethnicity data completeness for age 60+
insert into @results_schema.HERACLES_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3)
select @cohort_definition_id as cohort_definition_id, 2027 as analysis_id, round(innerT.valid_percentage, 2), 
innerT.all_data_count, innerT.valid_data_count
from 
(select valid_data.valid_data_count valid_data_count, all_data.all_data_count all_data_count, 
	CASE
       WHEN all_data.all_data_count = 0 THEN -999  
       ELSE valid_data.valid_data_count * 100/all_data.all_data_count 
	END as valid_percentage
from 
(SELECT count(distinct co.subject_id) as valid_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -60, getdate()))
       LEFT JOIN @CDM_schema.CONCEPT c ON p.ETHNICITY_CONCEPT_ID = c.CONCEPT_ID
 WHERE    
 p.ETHNICITY_CONCEPT_ID IS NOT NULL
 and (
 	lower(c.CONCEPT_NAME) not like '%unknown%'
 )) valid_data,
(SELECT count(distinct co.subject_id) as all_data_count
  FROM #HERACLES_cohort co
       JOIN @CDM_schema.person p
          ON     co.SUBJECT_ID = p.PERSON_ID
             AND p.YEAR_OF_BIRTH <=
             				year(DATEADD(year, -60, getdate()))
 ) all_data) innerT;
--}

--{2031 IN (@list_of_analysis_ids)}?{
-- 2031	entropy 
INSERT INTO @results_schema.HERACLES_results (cohort_definition_id,
                              analysis_id,
                              stratum_1,
                              stratum_2)
    SELECT @cohort_definition_id AS cohort_definition_id,
          2031 AS analysis_id,
          entropyT.d,
		  cast(round(entropyT.entropy, 3) as varchar(max))
     FROM
(select
	obs_date as d,
--	CAST(YEAR(obs_date) as varchar(4)) + '-' + RIGHT('0' + RTRIM(MONTH(obs_date)), 2) + '-' + RIGHT('0' + RTRIM(day(obs_date)), 2) as d,
--	cast(obs_date as varchar(10)) d, 
	sum(probTimesLog) entropy from
	(select obs_date, value_as_string, (1.0 * cnt) / (1.0 * total_per_day) prob, 
--		(-1.0) * (((1.0 * cnt) / (1.0 * total_per_day))* log(2, (1.0 * cnt) / (1.0 * total_per_day))) probTimesLog,
		(-1.0) * (((1.0 * cnt) / (1.0 * total_per_day)) * (log((1.0 * cnt) / (1.0 * total_per_day))/log(2))) probTimesLog,
		cnt, total_per_day
		from
		(select obs_date, value_as_string, cnt, sum(cnt)   
			over (partition by obs_date)
			as total_per_day
			from 
			(
				select all_observ.value_as_string,
				CAST(YEAR(all_observ.observation_date) as varchar(4)) + '-' + RIGHT('0' + RTRIM(MONTH(all_observ.observation_date)), 2) + '-' + RIGHT('0' + RTRIM(day(all_observ.observation_date)), 2) as obs_date,
				--observation_date as obs_date, 
				COUNT_BIG(*) 
				OVER (PARTITION BY all_observ.value_as_string,
--				DATEFROMPARTS(YEAR(all_observ.observation_date),MONTH(all_observ.observation_date),DAY(all_observ.observation_date))
				CAST(YEAR(all_observ.observation_date) as varchar(4)) + '-' + RIGHT('0' + RTRIM(MONTH(all_observ.observation_date)), 2) + '-' + RIGHT('0' + RTRIM(day(all_observ.observation_date)), 2)
				--trunc(all_observ.observation_date)
				)
				as cnt
				from 
				(select * from @CDM_schema.OBSERVATION observ
					join #HERACLES_cohort co
					on co.SUBJECT_ID = observ.PERSON_ID
					and observ.observation_date >= co.cohort_start_date
					and observ.observation_date <= co.cohort_end_date) all_observ
					) value_day_cnt 
					) with_sum
				) allProb
		group by obs_date
) entropyT;
--}

TRUNCATE TABLE #HERACLES_cohort;
DROP TABLE #HERACLES_cohort;


delete from @results_schema.HERACLES_results where count_value <= @smallcellcount;
delete from @results_schema.HERACLES_results_dist where count_value <= @smallcellcount;

--{@runHERACLESHeel}?{
-- HERACLES_Heel part:

DELETE FROM @results_schema.HERACLES_HEEL_results where cohort_definition_id in (@cohort_definition_id);

-- check for non-zero counts from checks of improper data (invalid ids, out-of-bound data, inconsistent dates)
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT DISTINCT or1.cohort_definition_id, or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; count (n=' + cast(or1.count_value as VARCHAR) + ') should not be > 0' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
WHERE or1.analysis_id IN (
		7,
		8,
		9,
		114,
		115,
		207,
		208,
		209,
		210,
		302,
		409,
		410,
		411,
		412,
		413,
		509,
		510,
		609,
		610,
		612,
		613,
		709,
		710,
		711,
		712,
		713,
		809,
		810,
		812,
		813,
		814,
		908,
		909,
		910,
		1008,
		1009,
		1010,
		1415,
		1500,
		1501,
		1600,
		1601,
		1701
		) -- all explicit counts of data anamolies
	AND or1.count_value > 0
	 and or1.cohort_definition_id in (@cohort_definition_id);

-- distributions where min should not be negative
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id, 
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT DISTINCT ord1.cohort_definition_id, ord1.analysis_id,
	'ERROR: ' + cast(ord1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; min (value=' + cast(ord1.min_value as VARCHAR) + ') should not be negative' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results_dist ord1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON ord1.analysis_id = oa1.analysis_id
WHERE ord1.analysis_id IN (
		103,
		105,
		206,
		406,
		506,
		606,
		706,
		715,
		716,
		717,
		806,
		906,
		907,
		1006,
		1007,
		1502,
		1503,
		1504,
		1505,
		1506,
		1507,
		1508,
		1509,
		1510,
		1511,
		1602,
		1603,
		1604,
		1605,
		1606,
		1607,
		1608
		)
	AND ord1.min_value < 0
	 and cohort_definition_id in (@cohort_definition_id);

--death distributions where max should not be positive
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT DISTINCT ord1.cohort_definition_id, ord1.analysis_id,
	'WARNING: ' + cast(ord1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; max (value=' + cast(ord1.max_value as VARCHAR) + ') should not be positive, otherwise its a zombie with data >1mo after death ' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results_dist ord1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON ord1.analysis_id = oa1.analysis_id
WHERE ord1.analysis_id IN (
		511,
		512,
		513,
		514,
		515
		)
	AND ord1.max_value > 30
	and  cohort_definition_id in (@cohort_definition_id);

--invalid concept_id
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; ' + cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR) + ' concepts in data are not in vocabulary' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
LEFT JOIN @CDM_schema.concept c1
	ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
WHERE or1.analysis_id IN (
		2,
		4,
		5,
		200,
		301,
		400,
		500,
		505,
		600,
		700,
		800,
		900,
		1000,
		1609,
		1610
		)
	AND or1.stratum_1 IS NOT NULL
	AND c1.concept_id IS NULL
	 and cohort_definition_id in (@cohort_definition_id)
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;

--invalid type concept_id
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; ' + cast(COUNT_BIG(DISTINCT stratum_2) AS VARCHAR) + ' concepts in data are not in vocabulary' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
LEFT JOIN @CDM_schema.concept c1
	ON or1.stratum_2 = CAST(c1.concept_id AS VARCHAR)
WHERE or1.analysis_id IN (
		405,
		605,
		705,
		805
		)
		 and cohort_definition_id in (@cohort_definition_id)
	AND or1.stratum_2 IS NOT NULL
	AND c1.concept_id IS NULL
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;

--invalid concept_id
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'WARNING: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; data with unmapped concepts' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
WHERE or1.analysis_id IN (
		2,
		4,
		5,
		200,
		301,
		400,
		500,
		505,
		600,
		700,
		800,
		900,
		1000,
		1609,
		1610
		)
	AND or1.stratum_1 = '0'
	and   cohort_definition_id in (@cohort_definition_id)
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;

--concept from the wrong vocabulary
--gender  - 12 HL7 -- TODO get the v5 version

INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; ' + cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR) + ' concepts in data are not in correct vocabulary (HL7 Sex)' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
INNER JOIN @CDM_schema.concept c1
	ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
WHERE or1.analysis_id IN (2)
 and cohort_definition_id in (@cohort_definition_id)
	AND or1.stratum_1 IS NOT NULL
	AND c1.vocabulary_id NOT IN (
		'Gender'
		)
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;
	

--race  - 13 CDC Race
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; ' + cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR) + ' concepts in data are not in correct vocabulary (CDC Race)' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
INNER JOIN @CDM_schema.concept c1
	ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
WHERE or1.analysis_id IN (4)
 and cohort_definition_id in (@cohort_definition_id)
	AND or1.stratum_1 IS NOT NULL
	AND c1.vocabulary_id NOT IN (
		'Race'
		)
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;

--ethnicity - 44 ethnicity
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; ' + cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR) + ' concepts in data are not in correct vocabulary (CMS Ethnicity)' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
INNER JOIN @CDM_schema.concept c1
	ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
WHERE or1.analysis_id IN (5)
 and cohort_definition_id in (@cohort_definition_id)
	AND or1.stratum_1 IS NOT NULL
	AND c1.vocabulary_id NOT IN (
		'Ethnicity'
		)
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;

--place of service - 14 CMS place of service, 24 OMOP visit
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; ' + cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR) + ' concepts in data are not in correct vocabulary (CMS place of service or OMOP visit)' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
INNER JOIN @CDM_schema.concept c1
	ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
WHERE or1.analysis_id IN (202)
 and cohort_definition_id in (@cohort_definition_id)
	AND or1.stratum_1 IS NOT NULL
	AND c1.vocabulary_id NOT IN (
		'Visit', 'Place of Service'
		)
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;

--specialty - 48 specialty
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; ' + cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR) + ' concepts in data are not in correct vocabulary (Specialty)' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
INNER JOIN @CDM_schema.concept c1
	ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
WHERE or1.analysis_id IN (301)
 and cohort_definition_id in (@cohort_definition_id)
	AND or1.stratum_1 IS NOT NULL
	AND c1.vocabulary_id NOT IN (
		'Specialty'
		)
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;

--condition occurrence, era - 1 SNOMED
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; ' + cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR) + ' concepts in data are not in correct vocabulary (SNOMED)' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
INNER JOIN @CDM_schema.concept c1
	ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
WHERE or1.analysis_id IN (
		400,
		1000
		)
		 and cohort_definition_id in (@cohort_definition_id)
	AND or1.stratum_1 IS NOT NULL
	AND c1.vocabulary_id NOT IN (
		'SNOMED'
		)
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;

--drug exposure - 8 RxNorm
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; ' + cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR) + ' concepts in data are not in correct vocabulary (RxNorm)' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
INNER JOIN @CDM_schema.concept c1
	ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
WHERE or1.analysis_id IN (
		700,
		900
		)
		 and cohort_definition_id in (@cohort_definition_id)
	AND or1.stratum_1 IS NOT NULL
	AND c1.vocabulary_id NOT IN (
		'RxNorm'
		)
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;

--procedure - 4 CPT4/5 HCPCS/3 ICD9P
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; ' + cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR) + ' concepts in data are not in correct vocabulary (CPT4/HCPCS/ICD9P)' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
INNER JOIN @CDM_schema.concept c1
	ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
WHERE or1.analysis_id IN (600)
 and or1.cohort_definition_id in (@cohort_definition_id)
	AND or1.stratum_1 IS NOT NULL
	AND c1.vocabulary_id NOT IN (
		'CPT4', 'HCPCS', 'ICD9Proc'
		)
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;

--observation  - 6 LOINC
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; ' + cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR) + ' concepts in data are not in correct vocabulary (LOINC)' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
INNER JOIN @CDM_schema.concept c1
	ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
WHERE or1.analysis_id IN (800)
 and or1.cohort_definition_id in (@cohort_definition_id)
	AND or1.stratum_1 IS NOT NULL
	AND c1.vocabulary_id NOT IN (
		'LOINC'
		)
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;


--disease class - 40 DRG
INSERT INTO @results_schema.HERACLES_HEEL_results (
  cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; ' + cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR) + ' concepts in data are not in correct vocabulary (DRG)' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
INNER JOIN @CDM_schema.concept c1
	ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
WHERE or1.analysis_id IN (1609)
 and or1.cohort_definition_id in (@cohort_definition_id)
	AND or1.stratum_1 IS NOT NULL
	AND c1.vocabulary_id NOT IN (
		'DRG'
		)
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;

--revenue code - 43 revenue code
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; ' + cast(COUNT_BIG(DISTINCT stratum_1) AS VARCHAR) + ' concepts in data are not in correct vocabulary (revenue code)' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
INNER JOIN @CDM_schema.concept c1
	ON or1.stratum_1 = CAST(c1.concept_id AS VARCHAR)
WHERE or1.analysis_id IN (1610)
	AND or1.stratum_1 IS NOT NULL
	 and or1.cohort_definition_id in (@cohort_definition_id)
	AND c1.vocabulary_id NOT IN (
		'Revenue Code'
		)
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;


--ERROR:  year of birth in the future
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; should not have year of birth in the future, (n=' + cast(sum(or1.count_value) as VARCHAR) + ')' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
WHERE or1.analysis_id IN (3)
 and or1.cohort_definition_id in (@cohort_definition_id)
	AND CAST(or1.stratum_1 AS INT) > year(getdate())
	AND or1.count_value > 0
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
	oa1.analysis_name;


--WARNING:  year of birth < 1900
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; should not have year of birth < 1900, (n=' + cast(sum(or1.count_value) as VARCHAR) + ')' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
WHERE or1.analysis_id IN (3)
 and cohort_definition_id in (@cohort_definition_id)
	AND cAST(or1.stratum_1 AS INT) < 1900
	AND or1.count_value > 0
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
  oa1.analysis_name;

--ERROR:  age < 0
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; should not have age < 0, (n=' + cast(sum(or1.count_value) as VARCHAR) + ')' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
WHERE or1.analysis_id IN (101)
 and cohort_definition_id in (@cohort_definition_id)
	AND CAST(or1.stratum_1 AS INT) < 0
	AND or1.count_value > 0
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
  oa1.analysis_name;

--ERROR: age > 100
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT or1.cohort_definition_id, 
	or1.analysis_id,
	'ERROR: ' + cast(or1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; should not have age > 100, (n=' + cast(sum(or1.count_value) as VARCHAR) + ')' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results or1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON or1.analysis_id = oa1.analysis_id
WHERE or1.analysis_id IN (101)
 and or1.cohort_definition_id in (@cohort_definition_id)
	AND CAST(or1.stratum_1 AS INT) > 100
	AND or1.count_value > 0
GROUP BY or1.cohort_definition_id, 
	or1.analysis_id,
  oa1.analysis_name;

--WARNING:  monthly change > 100%
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT DISTINCT her1.cohort_definition_id, her1.analysis_id,
	'WARNING: ' + cast(her1.analysis_id as VARCHAR) + '-' + aa1.analysis_name + '; theres a 100% change in monthly count of events' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_analysis aa1
INNER JOIN @results_schema.HERACLES_results her1
	ON aa1.analysis_id = her1.analysis_id
INNER JOIN @results_schema.HERACLES_results ar2
	ON her1.analysis_id = ar2.analysis_id
	and her1.cohort_definition_id = ar2.cohort_definition_id
		AND her1.analysis_id IN (
			420,
			620,
			720,
			820,
			920,
			1020
			)
WHERE (
		CAST(her1.stratum_1 AS INT) + 1 = CAST(ar2.stratum_1 AS INT)
		OR CAST(her1.stratum_1 AS INT) + 89 = CAST(ar2.stratum_1 AS INT)
		)
		 and her1.cohort_definition_id in (@cohort_definition_id)
	AND 1.0 * abs(ar2.count_value - her1.count_value) / her1.count_value > 1
	AND her1.count_value > 10;

--WARNING:  monthly change > 100% at concept level
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT her1.cohort_definition_id,
	her1.analysis_id,
	'WARNING: ' + cast(her1.analysis_id as VARCHAR) + '-' + aa1.analysis_name + '; ' + cast(COUNT_BIG(DISTINCT her1.stratum_1) AS VARCHAR) + ' concepts have a 100% change in monthly count of events' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_analysis aa1
INNER JOIN @results_schema.HERACLES_results her1
	ON aa1.analysis_id = her1.analysis_id
INNER JOIN @results_schema.HERACLES_results ar2
	ON her1.analysis_id = ar2.analysis_id
	and her1.cohort_definition_id = ar2.cohort_definition_id
		AND her1.stratum_1 = ar2.stratum_1
		AND her1.analysis_id IN (
			402,
			602,
			702,
			802,
			902,
			1002
			)
WHERE (
		CAST(her1.stratum_2 AS INT) + 1 = CAST(ar2.stratum_2 AS INT)
		OR CAST(her1.stratum_2 AS INT) + 89 = CAST(ar2.stratum_2 AS INT)
		)
		 and her1.cohort_definition_id in (@cohort_definition_id)
	AND 1.0 * abs(ar2.count_value - her1.count_value) / her1.count_value > 1
	AND her1.count_value > 10
GROUP BY her1.cohort_definition_id,
	her1.analysis_id,
	aa1.analysis_name;

--WARNING: days_supply > 180 
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT DISTINCT ord1.cohort_definition_id, 
	ord1.analysis_id,
	'ERROR: ' + cast(ord1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; max (value=' + cast(ord1.max_value as VARCHAR) + ' should not be > 180' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results_dist ord1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON ord1.analysis_id = oa1.analysis_id
WHERE ord1.analysis_id IN (715)
 and ord1.cohort_definition_id in (@cohort_definition_id)
	AND ord1.max_value > 180;

--WARNING:  refills > 10
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT DISTINCT ord1.cohort_definition_id, 
	ord1.analysis_id,
	'ERROR: ' + cast(ord1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; max (value=' + cast(ord1.max_value as VARCHAR) + ' should not be > 10' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results_dist ord1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON ord1.analysis_id = oa1.analysis_id
WHERE ord1.analysis_id IN (716)
 and ord1.cohort_definition_id in (@cohort_definition_id)
	AND ord1.max_value > 10;

--WARNING: quantity > 600
INSERT INTO @results_schema.HERACLES_HEEL_results (
	cohort_definition_id,
	analysis_id,
	HERACLES_HEEL_warning
	)
SELECT DISTINCT ord1.cohort_definition_id, 
	ord1.analysis_id,
	'ERROR: ' + cast(ord1.analysis_id as VARCHAR) + '-' + oa1.analysis_name + '; max (value=' + cast(ord1.max_value as VARCHAR) + ' should not be > 600' AS HERACLES_HEEL_warning
FROM @results_schema.HERACLES_results_dist ord1
INNER JOIN @results_schema.HERACLES_analysis oa1
	ON ord1.analysis_id = oa1.analysis_id
WHERE ord1.analysis_id IN (717)
 and ord1.cohort_definition_id in (@cohort_definition_id)
	AND ord1.max_value > 600;
	

--}
