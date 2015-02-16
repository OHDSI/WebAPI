/*****************
HERACLES

Patrick Ryan

last updated: 21 Jan 2015


chagnes for v4 to v5 that impact HERACLES

death :  cause_of_death_concept_id -> cause_concept_id
visit:  place_of_service_concept_id -> visit_concept_id
f/r:  associated_provider_id -> provider_id
	prescribing_provider_id -> provider_id

remove:  disease_class_concept_id analyses
	
observation:  no more range_high / range_low...now from measurement
	-options:  remove observation graphs in v5?   add new measurement?
	


******************/

{DEFAULT @CDM_schema = 'CDM_schema'}    --CDM_schema = @CDM_schema
{DEFAULT @results_schema = 'scratch'}   --results_schema = @results_schema
{DEFAULT @results_schema = 'CDM_schema'}  --results_schema = @results_schema
{DEFAULT cohort = 'COHORT'}  --cohort_table = cohort
{DEFAULT @source_name = 'TRUVEN MDCD'}   --source_name = @source_name
{DEFAULT @smallcellcount = 5}    --smallcellcount = @smallcellcount
{DEFAULT @createTable = TRUE}    --createTable = @createTable
{DEFAULT @runHERACLESHeel = TRUE}   --runHERACLESHeel = @runHERACLESHeel
{DEFAULT @CDM_version = '4'}  --we support 4 or 5,   CDM_version = @CDM_version

{DEFAULT @cohort_definition_id = '2000003550,2000004386'}   --cohort_definition_id = @cohort_definition_id

--'2000002372'  1 large cohort
--'2000003550,2000004386'     2 10k sized cohorts


{DEFAULT @list_of_analysis_ids = '0,1,2,3,4,5,6,7,8,9,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,200,201,202,203,204,205,206,207,208,209,210,211,220,400,401,402,403,404,405,406,407,408,409,410,411,412,413,414,415,416,417,418,419,420,500,501,502,503,504,505,506,509,510,511,512,513,514,515,600,601,602,603,604,605,606,607,608,609,610,611,612,613,614,615,616,617,618,619,620,700,701,702,703,704,705,706,707,708,709,710,711,712,713,714,715,716,717,717,718,719,720,800,801,802,803,804,805,806,807,808,809,810,811,812,813,814,815,816,817,818,819,820,900,901,902,903,904,905,906,907,908,909,910,911,912,913,914,915,916,917,918,919,920,1000,1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1011,1012,1013,1014,1015,1016,1017,1018,1019,1020,1100,1101,1102,1103,1200,1201,1202,1203,1700,1701,1800,1801,1802,1803,1804,1805,1806,1807,1808,1809,1810,1811,1812,1813,1814,1815,1816,1817,1818,1819,1820,1821,1830,1831,1840,1841,1850,1851,1860,1861,1870,1871'}
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
--observation: '800,801,802,803,804,805,806,807,808,809,810,811,812,813,814,815,816,817,818,819,820'
--drug era: '900,901,902,903,904,905,906,907,908,909,910,911,912,913,914,915,916,917,918,919,920'
--condition era: '1000,1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1011,1012,1013,1014,1015,1016,1017,1018,1019,1020'
--location: '1100,1101,1102,1103'
--care site: '1200,1201,1202,1203'
--cohort: '1700,1701'
--cohort-specific analyses: '1800,1801,1802,1803,1804,1805,1806,1807,1808,1809,1810,1811,1812,1813,1814,1815,1816,1817,1818,1819,1820,1821,1830,1831,1840,1841,1850,1851,1860,1861,1870,1871'



use @results_schema;


--{@createTable}?{

IF OBJECT_ID('HERACLES_analysis', 'U') IS NOT NULL
  drop table HERACLES_analysis;

create table HERACLES_analysis
(
	analysis_id int,
	analysis_name varchar(255),
	stratum_1_name varchar(255),
	stratum_2_name varchar(255),
	stratum_3_name varchar(255),
	stratum_4_name varchar(255),
	stratum_5_name varchar(255),
	analysis_type varchar(255)
);


IF OBJECT_ID('HERACLES_results', 'U') IS NOT NULL
  drop table HERACLES_results;

create table HERACLES_results
(
	cohort_definition_id int,
	analysis_id int,
	stratum_1 varchar(255),
	stratum_2 varchar(255),
	stratum_3 varchar(255),
	stratum_4 varchar(255),
	stratum_5 varchar(255),
	count_value bigint,
	last_update_time timestamp
);

ALTER TABLE HERACLES_results ADD CONSTRAINT DF_HERACLES_results DEFAULT GETDATE() FOR last_update_time;


IF OBJECT_ID('HERACLES_results_dist', 'U') IS NOT NULL
  drop table HERACLES_results_dist;

create table HERACLES_results_dist
(
	cohort_definition_id int,
	analysis_id int,
	stratum_1 varchar(255),
	stratum_2 varchar(255),
	stratum_3 varchar(255),
	stratum_4 varchar(255),
	stratum_5 varchar(255),
	count_value bigint,
	min_value float,
	max_value float,
	avg_value float,
	stdev_value float,
	median_value float,
	p10_value float,
	p25_value float,
	p75_value float,
	p90_value float,
	last_update_time timestamp
);

ALTER TABLE HERACLES_results_dist ADD CONSTRAINT DF_HERACLES_results_dist DEFAULT GETDATE() FOR last_update_time;

-- insert HERACLES analyses

insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (0,'Source name',null,null,null,null,null,'PERSON');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1,'Number of persons',null,null,null,null,null,'PERSON');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (2,'Number of persons by gender','gender_concept_id',null,null,null,null,'PERSON');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (3,'Number of persons by year of birth','year_of_birth',null,null,null,null,'PERSON');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (4,'Number of persons by race','race_concept_id',null,null,null,null,'PERSON');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (5,'Number of persons by ethnicity','ethnicity_concept_id',null,null,null,null,'PERSON');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (7,'Number of persons with invalid provider_id',null,null,null,null,null,'PERSON');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (8,'Number of persons with invalid location_id',null,null,null,null,null,'PERSON');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (9,'Number of persons with invalid care_site_id',null,null,null,null,null,'PERSON');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (101,'Number of persons by age, with age at first observation period','age',null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (102,'Number of persons by gender by age, with age at first observation period','gender_concept_id','age',null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (103,'Distribution of age at first observation period',null,null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (104,'Distribution of age at first observation period by gender','gender_concept_id',null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (105,'Length of observation (days) of first observation period',null,null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (106,'Length of observation (days) of first observation period by gender','gender_concept_id',null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (107,'Length of observation (days) of first observation period by age decile','age decile',null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (108,'Number of persons by length of observation period, in 30d increments','Observation period length 30d increments',null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (109,'Number of persons with continuous observation in each year','calendar year',null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (110,'Number of persons with continuous observation in each month','calendar month',null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (111,'Number of persons by observation period start month','calendar month',null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (112,'Number of persons by observation period end month','calendar month',null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (113,'Number of persons by number of observation periods','number of observation periods',null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (114,'Number of persons with observation period before year-of-birth',null,null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (115,'Number of persons with observation period end < observation period start',null,null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (116,'Number of persons with at least one day of observation in each year by gender and age decile','calendar year','gender_concept_id','age decile',null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (117,'Number of persons with at least one day of observation in each month','calendar month',null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (200,'Number of persons with at least one visit occurrence, by visit_concept_id','visit_concept_id',null,null,null,null,'VISITS');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (201,'Number of visit occurrence records, by visit_concept_id','visit_concept_id',null,null,null,null,'VISITS');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (202,'Number of persons by visit occurrence start month, by visit_concept_id','visit_concept_id','calendar month',null,null,null,'VISITS');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (203,'Number of distinct visit occurrence concepts per person',null,null,null,null,null,'VISITS');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (204,'Number of persons with at least one visit occurrence, by visit_concept_id by calendar year by gender by age decile','visit_concept_id','calendar year','gender_concept_id','age decile',null,'VISITS');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (206,'Distribution of age by visit_concept_id','visit_concept_id','gender_concept_id',null,null,null,'VISITS');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (207,'Number of visit records with invalid person_id',null,null,null,null,null,'VISITS');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (208,'Number of visit records outside valid observation period',null,null,null,null,null,'VISITS');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (209,'Number of visit records with end date < start date',null,null,null,null,null,'VISITS');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (210,'Number of visit records with invalid care_site_id',null,null,null,null,null,'VISITS');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (211,'Distribution of length of stay by visit_concept_id','visit_concept_id',null,null,null,null,'VISITS');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (220,'Number of visit occurrence records by visit occurrence start month','calendar month',null,null,null,null,'VISITS');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (400,'Number of persons with at least one condition occurrence, by condition_concept_id','condition_concept_id',null,null,null,null,'CONDITION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (401,'Number of condition occurrence records, by condition_concept_id','condition_concept_id',null,null,null,null,'CONDITION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (402,'Number of persons by condition occurrence start month, by condition_concept_id','condition_concept_id','calendar month',null,null,null,'CONDITION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (403,'Number of distinct condition occurrence concepts per person',null,null,null,null,null,'CONDITION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (404,'Number of persons with at least one condition occurrence, by condition_concept_id by calendar year by gender by age decile','condition_concept_id','calendar year','gender_concept_id','age decile',null,'CONDITION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (405,'Number of condition occurrence records, by condition_concept_id by condition_type_concept_id','condition_concept_id','condition_type_concept_id',null,null,null,'CONDITION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (406,'Distribution of age by condition_concept_id','condition_concept_id','gender_concept_id',null,null,null,'CONDITION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (409,'Number of condition occurrence records with invalid person_id',null,null,null,null,null,'CONDITION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (410,'Number of condition occurrence records outside valid observation period',null,null,null,null,null,'CONDITION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (411,'Number of condition occurrence records with end date < start date',null,null,null,null,null,'CONDITION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (412,'Number of condition occurrence records with invalid provider_id',null,null,null,null,null,'CONDITION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (413,'Number of condition occurrence records with invalid visit_id',null,null,null,null,null,'CONDITION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (420,'Number of condition occurrence records by condition occurrence start month','calendar month',null,null,null,null,'CONDITION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (500,'Number of persons with death, by cause_of_death_concept_id','cause_of_death_concept_id',null,null,null,null,'DEATH');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (501,'Number of records of death, by cause_of_death_concept_id','cause_of_death_concept_id',null,null,null,null,'DEATH');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (502,'Number of persons by death month','calendar month',null,null,null,null,'DEATH');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (504,'Number of persons with a death, by calendar year by gender by age decile','calendar year','gender_concept_id','age decile',null,null,'DEATH');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (505,'Number of death records, by death_type_concept_id','death_type_concept_id',null,null,null,null,'DEATH');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (506,'Distribution of age at death by gender','gender_concept_id',null,null,null,null,'DEATH');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (509,'Number of death records with invalid person_id',null,null,null,null,null,'DEATH');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (510,'Number of death records outside valid observation period',null,null,null,null,null,'DEATH');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (511,'Distribution of time from death to last condition',null,null,null,null,null,'DEATH');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (512,'Distribution of time from death to last drug',null,null,null,null,null,'DEATH');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (513,'Distribution of time from death to last visit',null,null,null,null,null,'DEATH');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (514,'Distribution of time from death to last procedure',null,null,null,null,null,'DEATH');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (515,'Distribution of time from death to last observation',null,null,null,null,null,'DEATH');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (600,'Number of persons with at least one procedure occurrence, by procedure_concept_id','procedure_concept_id',null,null,null,null,'PROCEDURE');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (601,'Number of procedure occurrence records, by procedure_concept_id','procedure_concept_id',null,null,null,null,'PROCEDURE');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (602,'Number of persons by procedure occurrence start month, by procedure_concept_id','procedure_concept_id','calendar month',null,null,null,'PROCEDURE');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (603,'Number of distinct procedure occurrence concepts per person',null,null,null,null,null,'PROCEDURE');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (604,'Number of persons with at least one procedure occurrence, by procedure_concept_id by calendar year by gender by age decile','procedure_concept_id','calendar year','gender_concept_id','age decile',null,'PROCEDURE');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (605,'Number of procedure occurrence records, by procedure_concept_id by procedure_type_concept_id','procedure_concept_id','procedure_type_concept_id',null,null,null,'PROCEDURE');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (606,'Distribution of age by procedure_concept_id','procedure_concept_id','gender_concept_id',null,null,null,'PROCEDURE');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (609,'Number of procedure occurrence records with invalid person_id',null,null,null,null,null,'PROCEDURE');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (610,'Number of procedure occurrence records outside valid observation period',null,null,null,null,null,'PROCEDURE');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (612,'Number of procedure occurrence records with invalid provider_id',null,null,null,null,null,'PROCEDURE');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (613,'Number of procedure occurrence records with invalid visit_id',null,null,null,null,null,'PROCEDURE');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (620,'Number of procedure occurrence records  by procedure occurrence start month','calendar month',null,null,null,null,'PROCEDURE');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (700,'Number of persons with at least one drug exposure, by drug_concept_id','drug_concept_id',null,null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (701,'Number of drug exposure records, by drug_concept_id','drug_concept_id',null,null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (702,'Number of persons by drug exposure start month, by drug_concept_id','drug_concept_id','calendar month',null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (703,'Number of distinct drug exposure concepts per person',null,null,null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (704,'Number of persons with at least one drug exposure, by drug_concept_id by calendar year by gender by age decile','drug_concept_id','calendar year','gender_concept_id','age decile',null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (705,'Number of drug exposure records, by drug_concept_id by drug_type_concept_id','drug_concept_id','drug_type_concept_id',null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (706,'Distribution of age by drug_concept_id','drug_concept_id','gender_concept_id',null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (709,'Number of drug exposure records with invalid person_id',null,null,null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (710,'Number of drug exposure records outside valid observation period',null,null,null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (711,'Number of drug exposure records with end date < start date',null,null,null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (712,'Number of drug exposure records with invalid provider_id',null,null,null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (713,'Number of drug exposure records with invalid visit_id',null,null,null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (715,'Distribution of days_supply by drug_concept_id','drug_concept_id',null,null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (716,'Distribution of refills by drug_concept_id','drug_concept_id',null,null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (717,'Distribution of quantity by drug_concept_id','drug_concept_id',null,null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (720,'Number of drug exposure records  by drug exposure start month','calendar month',null,null,null,null,'DRUG');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (800,'Number of persons with at least one observation occurrence, by observation_concept_id','observation_concept_id',null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (801,'Number of observation occurrence records, by observation_concept_id','observation_concept_id',null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (802,'Number of persons by observation occurrence start month, by observation_concept_id','observation_concept_id','calendar month',null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (803,'Number of distinct observation occurrence concepts per person',null,null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (804,'Number of persons with at least one observation occurrence, by observation_concept_id by calendar year by gender by age decile','observation_concept_id','calendar year','gender_concept_id','age decile',null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (805,'Number of observation occurrence records, by observation_concept_id by observation_type_concept_id','observation_concept_id','observation_type_concept_id',null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (806,'Distribution of age by observation_concept_id','observation_concept_id','gender_concept_id',null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (807,'Number of observation occurrence records, by observation_concept_id and unit_concept_id','observation_concept_id','unit_concept_id',null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (809,'Number of observation records with invalid person_id',null,null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (810,'Number of observation records outside valid observation period',null,null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (812,'Number of observation records with invalid provider_id',null,null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (813,'Number of observation records with invalid visit_id',null,null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (814,'Number of observation records with no value (numeric, string, or concept)',null,null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (815,'Distribution of numeric values, by observation_concept_id and unit_concept_id',null,null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (816,'Distribution of low range, by observation_concept_id and unit_concept_id',null,null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (817,'Distribution of high range, by observation_concept_id and unit_concept_id',null,null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (818,'Number of observation records below/within/above normal range, by observation_concept_id and unit_concept_id',null,null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (820,'Number of observation records  by observation start month','calendar month',null,null,null,null,'OBSERVATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (900,'Number of persons with at least one drug era, by drug_concept_id','drug_concept_id',null,null,null,null,'DRUG_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (901,'Number of drug era records, by drug_concept_id','drug_concept_id',null,null,null,null,'DRUG_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (902,'Number of persons by drug era start month, by drug_concept_id','drug_concept_id','calendar month',null,null,null,'DRUG_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (903,'Number of distinct drug era concepts per person',null,null,null,null,null,'DRUG_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (904,'Number of persons with at least one drug era, by drug_concept_id by calendar year by gender by age decile','drug_concept_id','calendar year','gender_concept_id','age decile',null,'DRUG_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (906,'Distribution of age by drug_concept_id','drug_concept_id','gender_concept_id',null,null,null,'DRUG_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (907,'Distribution of drug era length, by drug_concept_id','drug_concept_id',null,null,null,null,'DRUG_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (908,'Number of drug eras without valid person',null,null,null,null,null,'DRUG_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (909,'Number of drug eras outside valid observation period',null,null,null,null,null,'DRUG_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (910,'Number of drug eras with end date < start date',null,null,null,null,null,'DRUG_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (920,'Number of drug era records  by drug era start month','calendar month',null,null,null,null,'DRUG_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1000,'Number of persons with at least one condition era, by condition_concept_id','condition_concept_id',null,null,null,null,'CONDITION_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1001,'Number of condition era records, by condition_concept_id','condition_concept_id',null,null,null,null,'CONDITION_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1002,'Number of persons by condition era start month, by condition_concept_id','condition_concept_id','calendar month',null,null,null,'CONDITION_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1003,'Number of distinct condition era concepts per person',null,null,null,null,null,'CONDITION_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1004,'Number of persons with at least one condition era, by condition_concept_id by calendar year by gender by age decile','condition_concept_id','calendar year','gender_concept_id','age decile',null,'CONDITION_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1006,'Distribution of age by condition_concept_id','condition_concept_id','gender_concept_id',null,null,null,'CONDITION_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1007,'Distribution of condition era length, by condition_concept_id','condition_concept_id',null,null,null,null,'CONDITION_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1008,'Number of condition eras without valid person',null,null,null,null,null,'CONDITION_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1009,'Number of condition eras outside valid observation period',null,null,null,null,null,'CONDITION_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1010,'Number of condition eras with end date < start date',null,null,null,null,null,'CONDITION_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1020,'Number of condition era records by condition era start month','calendar month',null,null,null,null,'CONDITION_ERA');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1100,'Number of persons by location 3-digit zip','3-digit zip',null,null,null,null,'LOCATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1101,'Number of persons by location state','state',null,null,null,null,'LOCATION');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1200,'Number of persons by place of service','place_of_service_concept_id',null,null,null,null,'CARE_SITE');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1201,'Number of visits by place of service','place_of_service_concept_id',null,null,null,null,'CARE_SITE');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1700,'Number of records by cohort_definition_id','cohort_definition_id',null,null,null,null,'COHORT');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1701,'Number of records with cohort end date < cohort start date',null,null,null,null,null,'COHORT');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1800,'Number of persons by age, with age at cohort start','age',null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1801,'Distribution of age at cohort start',null,null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1802,'Distribution of age at cohort start by gender','gender_concept_id',null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1803,'Distribution of age at cohort start by cohort start year','calendar year',null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1804,'Number of persons by duration from cohort start to cohort end, in 30d increments','Cohort period length 30d increments',null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1805,'Number of persons by duration from observation start to cohort start, in 30d increments','Baseline period length 30d increments',null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1806,'Number of persons by duration from cohort start to observation end, in 30d increments','Follow-up period length 30d increments',null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1807,'Number of persons by duration from cohort end to observation end, in 30d increments','Post-cohort period length 30d increments',null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1808,'Distribution of duration (days) from cohort start to cohort end',null,null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1809,'Distribution of duration (days) from cohort start to cohort end, by gender','gender_concept_id',null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1810,'Distribution of duration (days) from cohort start to cohort end, by age decile','age decile',null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1811,'Distribution of duration (days) from observation start to cohort start',null,null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1812,'Distribution of duration (days) from cohort start to observation end',null,null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1813,'Distribution of duration (days) from cohort end to observation end',null,null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1814,'Number of persons by cohort start year by gender by age decile','calendar year','gender_concept_id','age decile',null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1815,'Number of persons by cohort start month','calendar month',null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1816,'Number of persons by number of cohort periods','number of cohort periods',null,null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1820,'Number of persons by duration from cohort start to first occurrence of condition occurrence, by condition_concept_id','condition_concept_id','time-to-event 30d increments',null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1821,'Number of events by duration from cohort start to all occurrences of condition occurrence, by condition_concept_id','condition_concept_id','time-to-event 30d increments',null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1830,'Number of persons by duration from cohort start to first occurrence of procedure occurrence, by procedure_concept_id','procedure_concept_id','time-to-event 30d increments',null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1831,'Number of events by duration from cohort start to all occurrences of procedure occurrence, by procedure_concept_id','procedure_concept_id','time-to-event 30d increments',null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1840,'Number of persons by duration from cohort start to first occurrence of drug exposure, by drug_concept_id','drug_concept_id','time-to-event 30d increments',null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1841,'Number of events by duration from cohort start to all occurrences of drug exposure, by drug_concept_id','drug_concept_id','time-to-event 30d increments',null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1850,'Number of persons by duration from cohort start to first occurrence of observation, by observation_concept_id','observation_concept_id','time-to-event 30d increments',null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1851,'Number of events by duration from cohort start to all occurrences of observation, by observation_concept_id','observation_concept_id','time-to-event 30d increments',null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1860,'Number of persons by duration from cohort start to first occurrence of condition era, by condition_concept_id','condition_concept_id','time-to-event 30d increments',null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1861,'Number of events by duration from cohort start to all occurrences of condition era, by condition_concept_id','condition_concept_id','time-to-event 30d increments',null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1870,'Number of persons by duration from cohort start to first occurrence of drug era, by drug_concept_id','drug_concept_id','time-to-event 30d increments',null,null,null,'COHORT_SPECIFIC_ANALYSES');
insert into HERACLES_ANALYSIS (ANALYSIS_ID,ANALYSIS_NAME,STRATUM_1_NAME,STRATUM_2_NAME,STRATUM_3_NAME,STRATUM_4_NAME,STRATUM_5_NAME,ANALYSIS_TYPE) values (1871,'Number of events by duration from cohort start to all occurrences of drug era, by drug_concept_id','drug_concept_id','time-to-event 30d increments',null,null,null,'COHORT_SPECIFIC_ANALYSES');

	
	
	
--} : {else if not createTable
delete from @results_schema.dbo.HERACLES_results where cohort_definition_id IN (@cohort_definition_id) and analysis_id IN (@list_of_analysis_ids);
delete from @results_schema.dbo.HERACLES_results_dist where cohort_definition_id IN (@cohort_definition_id) and analysis_id IN (@list_of_analysis_ids);
}


