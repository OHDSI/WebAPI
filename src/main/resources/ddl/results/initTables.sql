-- init heracles_analysis

TRUNCATE TABLE @results_schema.heracles_analysis;

insert into @results_schema.heracles_analysis
(analysis_id,analysis_name,stratum_1_name,stratum_2_name,stratum_3_name,stratum_4_name,stratum_5_name,analysis_type)
select    0 as analysis_id,
CAST('Source name' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PERSON' as VARCHAR(255)) as analysis_type
union all
select    1 as analysis_id,
CAST('Number of persons' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PERSON' as VARCHAR(255)) as analysis_type
union all
select    2 as analysis_id,
CAST('Number of persons by gender' as VARCHAR(255)) as analysis_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PERSON' as VARCHAR(255)) as analysis_type
union all
select    3 as analysis_id,
CAST('Number of persons by year of birth' as VARCHAR(255)) as analysis_name,
CAST('year_of_birth' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PERSON' as VARCHAR(255)) as analysis_type
union all
select    4 as analysis_id,
CAST('Number of persons by race' as VARCHAR(255)) as analysis_name,
CAST('race_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PERSON' as VARCHAR(255)) as analysis_type
union all
select    5 as analysis_id,
CAST('Number of persons by ethnicity' as VARCHAR(255)) as analysis_name,
CAST('ethnicity_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PERSON' as VARCHAR(255)) as analysis_type
union all
select    7 as analysis_id,
CAST('Number of persons with invalid provider_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PERSON' as VARCHAR(255)) as analysis_type
union all
select    8 as analysis_id,
CAST('Number of persons with invalid location_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PERSON' as VARCHAR(255)) as analysis_type
union all
select    9 as analysis_id,
CAST('Number of persons with invalid care_site_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PERSON' as VARCHAR(255)) as analysis_type
union all
select  101 as analysis_id,
CAST('Number of persons by age, with age at first observation period' as VARCHAR(255)) as analysis_name,
CAST('age' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  102 as analysis_id,
CAST('Number of persons by gender by age, with age at first observation period' as VARCHAR(255)) as analysis_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('age' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  103 as analysis_id,
CAST('Distribution of age at first observation period' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  104 as analysis_id,
CAST('Distribution of age at first observation period by gender' as VARCHAR(255)) as analysis_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  105 as analysis_id,
CAST('Length of observation (days) of first observation period' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  106 as analysis_id,
CAST('Length of observation (days) of first observation period by gender' as VARCHAR(255)) as analysis_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  107 as analysis_id,
CAST('Length of observation (days) of first observation period by age decile' as VARCHAR(255)) as analysis_name,
CAST('age decile' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  108 as analysis_id,
CAST('Number of persons by length of first observation period, in 30d increments' as VARCHAR(255)) as analysis_name,
CAST('Observation period length 30d increments' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  109 as analysis_id,
CAST('Number of persons with continuous observation in each year' as VARCHAR(255)) as analysis_name,
CAST('calendar year' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  110 as analysis_id,
CAST('Number of persons with continuous observation in each month' as VARCHAR(255)) as analysis_name,
CAST('calendar month' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  111 as analysis_id,
CAST('Number of persons by observation period start month' as VARCHAR(255)) as analysis_name,
CAST('calendar month' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  112 as analysis_id,
CAST('Number of persons by observation period end month' as VARCHAR(255)) as analysis_name,
CAST('calendar month' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  113 as analysis_id,
CAST('Number of persons by number of observation periods' as VARCHAR(255)) as analysis_name,
CAST('number of observation periods' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  114 as analysis_id,
CAST('Number of persons with observation period before year-of-birth' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  115 as analysis_id,
CAST('Number of persons with observation period end < observation period start' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  116 as analysis_id,
CAST('Number of persons with at least one day of observation in each year by gender and age decile' as VARCHAR(255)) as analysis_name,
CAST('calendar year' as VARCHAR(255)) as stratum_1_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_2_name,
CAST('age decile' as VARCHAR(255)) as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  117 as analysis_id,
CAST('Number of persons with at least one day of observation in each month' as VARCHAR(255)) as analysis_name,
CAST('calendar month' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  200 as analysis_id,
CAST('Number of persons with at least one visit occurrence, by visit_concept_id' as VARCHAR(255)) as analysis_name,
CAST('visit_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('VISITS' as VARCHAR(255)) as analysis_type
union all
select  201 as analysis_id,
CAST('Number of visit occurrence records, by visit_concept_id' as VARCHAR(255)) as analysis_name,
CAST('visit_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('VISITS' as VARCHAR(255)) as analysis_type
union all
select  202 as analysis_id,
CAST('Number of persons by visit occurrence start month, by visit_concept_id' as VARCHAR(255)) as analysis_name,
CAST('visit_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar month' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('VISITS' as VARCHAR(255)) as analysis_type
union all
select  203 as analysis_id,
CAST('Number of distinct visit occurrence concepts per person' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('VISITS' as VARCHAR(255)) as analysis_type
union all
select  204 as analysis_id,
CAST('Number of persons with at least one visit occurrence, by visit_concept_id by calendar year by gender by age decile' as VARCHAR(255)) as analysis_name,
CAST('visit_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar year' as VARCHAR(255)) as stratum_2_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_3_name,
CAST('age decile' as VARCHAR(255)) as stratum_4_name,
NULL as stratum_5_name,
CAST('VISITS' as VARCHAR(255)) as analysis_type
union all
select  206 as analysis_id,
CAST('Distribution of age by visit_concept_id' as VARCHAR(255)) as analysis_name,
CAST('visit_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('VISITS' as VARCHAR(255)) as analysis_type
union all
select  207 as analysis_id,
CAST('Number of visit records with invalid person_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('VISITS' as VARCHAR(255)) as analysis_type
union all
select  208 as analysis_id,
CAST('Number of visit records outside valid observation period' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('VISITS' as VARCHAR(255)) as analysis_type
union all
select  209 as analysis_id,
CAST('Number of visit records with end date < start date' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('VISITS' as VARCHAR(255)) as analysis_type
union all
select  210 as analysis_id,
CAST('Number of visit records with invalid care_site_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('VISITS' as VARCHAR(255)) as analysis_type
union all
select  211 as analysis_id,
CAST('Distribution of length of stay by visit_concept_id' as VARCHAR(255)) as analysis_name,
CAST('visit_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('VISITS' as VARCHAR(255)) as analysis_type
union all
select  220 as analysis_id,
CAST('Number of visit occurrence records by visit occurrence start month' as VARCHAR(255)) as analysis_name,
CAST('calendar month' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('VISITS' as VARCHAR(255)) as analysis_type
union all
select  400 as analysis_id,
CAST('Number of persons with at least one condition occurrence, by condition_concept_id' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION' as VARCHAR(255)) as analysis_type
union all
select  401 as analysis_id,
CAST('Number of condition occurrence records, by condition_concept_id' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION' as VARCHAR(255)) as analysis_type
union all
select  402 as analysis_id,
CAST('Number of persons by condition occurrence start month, by condition_concept_id' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar month' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION' as VARCHAR(255)) as analysis_type
union all
select  403 as analysis_id,
CAST('Number of distinct condition occurrence concepts per person' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION' as VARCHAR(255)) as analysis_type
union all
select  404 as analysis_id,
CAST('Number of persons with at least one condition occurrence, by condition_concept_id by calendar year by gender by age decile' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar year' as VARCHAR(255)) as stratum_2_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_3_name,
CAST('age decile' as VARCHAR(255)) as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION' as VARCHAR(255)) as analysis_type
union all
select  405 as analysis_id,
CAST('Number of condition occurrence records, by condition_concept_id by condition_type_concept_id' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('condition_type_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION' as VARCHAR(255)) as analysis_type
union all
select  406 as analysis_id,
CAST('Distribution of age by condition_concept_id' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION' as VARCHAR(255)) as analysis_type
union all
select  409 as analysis_id,
CAST('Number of condition occurrence records with invalid person_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION' as VARCHAR(255)) as analysis_type
union all
select  410 as analysis_id,
CAST('Number of condition occurrence records outside valid observation period' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION' as VARCHAR(255)) as analysis_type
union all
select  411 as analysis_id,
CAST('Number of condition occurrence records with end date < start date' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION' as VARCHAR(255)) as analysis_type
union all
select  412 as analysis_id,
CAST('Number of condition occurrence records with invalid provider_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION' as VARCHAR(255)) as analysis_type
union all
select  413 as analysis_id,
CAST('Number of condition occurrence records with invalid visit_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION' as VARCHAR(255)) as analysis_type
union all
select  420 as analysis_id,
CAST('Number of condition occurrence records by condition occurrence start month' as VARCHAR(255)) as analysis_name,
CAST('calendar month' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION' as VARCHAR(255)) as analysis_type
union all
select  500 as analysis_id,
CAST('Number of persons with death, by cause_of_death_concept_id' as VARCHAR(255)) as analysis_name,
CAST('cause_of_death_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DEATH' as VARCHAR(255)) as analysis_type
union all
select  501 as analysis_id,
CAST('Number of records of death, by cause_of_death_concept_id' as VARCHAR(255)) as analysis_name,
CAST('cause_of_death_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DEATH' as VARCHAR(255)) as analysis_type
union all
select  502 as analysis_id,
CAST('Number of persons by death month' as VARCHAR(255)) as analysis_name,
CAST('calendar month' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DEATH' as VARCHAR(255)) as analysis_type
union all
select  504 as analysis_id,
CAST('Number of persons with a death, by calendar year by gender by age decile' as VARCHAR(255)) as analysis_name,
CAST('calendar year' as VARCHAR(255)) as stratum_1_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_2_name,
CAST('age decile' as VARCHAR(255)) as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DEATH' as VARCHAR(255)) as analysis_type
union all
select  505 as analysis_id,
CAST('Number of death records, by death_type_concept_id' as VARCHAR(255)) as analysis_name,
CAST('death_type_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DEATH' as VARCHAR(255)) as analysis_type
union all
select  506 as analysis_id,
CAST('Distribution of age at death by gender' as VARCHAR(255)) as analysis_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DEATH' as VARCHAR(255)) as analysis_type
union all
select  509 as analysis_id,
CAST('Number of death records with invalid person_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DEATH' as VARCHAR(255)) as analysis_type
union all
select  510 as analysis_id,
CAST('Number of death records outside valid observation period' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DEATH' as VARCHAR(255)) as analysis_type
union all
select  511 as analysis_id,
CAST('Distribution of time from death to last condition' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DEATH' as VARCHAR(255)) as analysis_type
union all
select  512 as analysis_id,
CAST('Distribution of time from death to last drug' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DEATH' as VARCHAR(255)) as analysis_type
union all
select  513 as analysis_id,
CAST('Distribution of time from death to last visit' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DEATH' as VARCHAR(255)) as analysis_type
union all
select  514 as analysis_id,
CAST('Distribution of time from death to last procedure' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DEATH' as VARCHAR(255)) as analysis_type
union all
select  515 as analysis_id,
CAST('Distribution of time from death to last observation' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DEATH' as VARCHAR(255)) as analysis_type
union all
select  600 as analysis_id,
CAST('Number of persons with at least one procedure occurrence, by procedure_concept_id' as VARCHAR(255)) as analysis_name,
CAST('procedure_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PROCEDURE' as VARCHAR(255)) as analysis_type
union all
select  601 as analysis_id,
CAST('Number of procedure occurrence records, by procedure_concept_id' as VARCHAR(255)) as analysis_name,
CAST('procedure_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PROCEDURE' as VARCHAR(255)) as analysis_type
union all
select  602 as analysis_id,
CAST('Number of persons by procedure occurrence start month, by procedure_concept_id' as VARCHAR(255)) as analysis_name,
CAST('procedure_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar month' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PROCEDURE' as VARCHAR(255)) as analysis_type
union all
select  603 as analysis_id,
CAST('Number of distinct procedure occurrence concepts per person' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PROCEDURE' as VARCHAR(255)) as analysis_type
union all
select  604 as analysis_id,
CAST('Number of persons with at least one procedure occurrence, by procedure_concept_id by calendar year by gender by age decile' as VARCHAR(255)) as analysis_name,
CAST('procedure_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar year' as VARCHAR(255)) as stratum_2_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_3_name,
CAST('age decile' as VARCHAR(255)) as stratum_4_name,
NULL as stratum_5_name,
CAST('PROCEDURE' as VARCHAR(255)) as analysis_type
union all
select  605 as analysis_id,
CAST('Number of procedure occurrence records, by procedure_concept_id by procedure_type_concept_id' as VARCHAR(255)) as analysis_name,
CAST('procedure_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('procedure_type_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PROCEDURE' as VARCHAR(255)) as analysis_type
union all
select  606 as analysis_id,
CAST('Distribution of age by procedure_concept_id' as VARCHAR(255)) as analysis_name,
CAST('procedure_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PROCEDURE' as VARCHAR(255)) as analysis_type
union all
select  609 as analysis_id,
CAST('Number of procedure occurrence records with invalid person_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PROCEDURE' as VARCHAR(255)) as analysis_type
union all
select  610 as analysis_id,
CAST('Number of procedure occurrence records outside valid observation period' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PROCEDURE' as VARCHAR(255)) as analysis_type
union all
select  612 as analysis_id,
CAST('Number of procedure occurrence records with invalid provider_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PROCEDURE' as VARCHAR(255)) as analysis_type
union all
select  613 as analysis_id,
CAST('Number of procedure occurrence records with invalid visit_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PROCEDURE' as VARCHAR(255)) as analysis_type
union all
select  620 as analysis_id,
CAST('Number of procedure occurrence records  by procedure occurrence start month' as VARCHAR(255)) as analysis_name,
CAST('calendar month' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('PROCEDURE' as VARCHAR(255)) as analysis_type
union all
select  700 as analysis_id,
CAST('Number of persons with at least one drug exposure, by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  701 as analysis_id,
CAST('Number of drug exposure records, by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  702 as analysis_id,
CAST('Number of persons by drug exposure start month, by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar month' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  703 as analysis_id,
CAST('Number of distinct drug exposure concepts per person' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  704 as analysis_id,
CAST('Number of persons with at least one drug exposure, by drug_concept_id by calendar year by gender by age decile' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar year' as VARCHAR(255)) as stratum_2_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_3_name,
CAST('age decile' as VARCHAR(255)) as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  705 as analysis_id,
CAST('Number of drug exposure records, by drug_concept_id by drug_type_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('drug_type_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  706 as analysis_id,
CAST('Distribution of age by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  709 as analysis_id,
CAST('Number of drug exposure records with invalid person_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  710 as analysis_id,
CAST('Number of drug exposure records outside valid observation period' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  711 as analysis_id,
CAST('Number of drug exposure records with end date < start date' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  712 as analysis_id,
CAST('Number of drug exposure records with invalid provider_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  713 as analysis_id,
CAST('Number of drug exposure records with invalid visit_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  715 as analysis_id,
CAST('Distribution of days_supply by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  716 as analysis_id,
CAST('Distribution of refills by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  717 as analysis_id,
CAST('Distribution of quantity by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  720 as analysis_id,
CAST('Number of drug exposure records  by drug exposure start month' as VARCHAR(255)) as analysis_name,
CAST('calendar month' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG' as VARCHAR(255)) as analysis_type
union all
select  800 as analysis_id,
CAST('Number of persons with at least one observation occurrence, by observation_concept_id' as VARCHAR(255)) as analysis_name,
CAST('observation_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  801 as analysis_id,
CAST('Number of observation occurrence records, by observation_concept_id' as VARCHAR(255)) as analysis_name,
CAST('observation_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  802 as analysis_id,
CAST('Number of persons by observation occurrence start month, by observation_concept_id' as VARCHAR(255)) as analysis_name,
CAST('observation_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar month' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  803 as analysis_id,
CAST('Number of distinct observation occurrence concepts per person' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  804 as analysis_id,
CAST('Number of persons with at least one observation occurrence, by observation_concept_id by calendar year by gender by age decile' as VARCHAR(255)) as analysis_name,
CAST('observation_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar year' as VARCHAR(255)) as stratum_2_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_3_name,
CAST('age decile' as VARCHAR(255)) as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  805 as analysis_id,
CAST('Number of observation occurrence records, by observation_concept_id by observation_type_concept_id' as VARCHAR(255)) as analysis_name,
CAST('observation_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('observation_type_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  806 as analysis_id,
CAST('Distribution of age by observation_concept_id' as VARCHAR(255)) as analysis_name,
CAST('observation_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  807 as analysis_id,
CAST('Number of observation occurrence records, by observation_concept_id and unit_concept_id' as VARCHAR(255)) as analysis_name,
CAST('observation_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('unit_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  809 as analysis_id,
CAST('Number of observation records with invalid person_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  810 as analysis_id,
CAST('Number of observation records outside valid observation period' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  812 as analysis_id,
CAST('Number of observation records with invalid provider_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  813 as analysis_id,
CAST('Number of observation records with invalid visit_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  814 as analysis_id,
CAST('Number of observation records with no value (numeric, string, or concept)' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  815 as analysis_id,
CAST('Distribution of numeric values, by observation_concept_id and unit_concept_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  816 as analysis_id,
CAST('Distribution of low range, by observation_concept_id and unit_concept_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  817 as analysis_id,
CAST('Distribution of high range, by observation_concept_id and unit_concept_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  818 as analysis_id,
CAST('Number of observation records below/within/above normal range, by observation_concept_id and unit_concept_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  820 as analysis_id,
CAST('Number of observation records  by observation start month' as VARCHAR(255)) as analysis_name,
CAST('calendar month' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('OBSERVATION' as VARCHAR(255)) as analysis_type
union all
select  900 as analysis_id,
CAST('Number of persons with at least one drug era, by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG_ERA' as VARCHAR(255)) as analysis_type
union all
select  901 as analysis_id,
CAST('Number of drug era records, by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG_ERA' as VARCHAR(255)) as analysis_type
union all
select  902 as analysis_id,
CAST('Number of persons by drug era start month, by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar month' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG_ERA' as VARCHAR(255)) as analysis_type
union all
select  903 as analysis_id,
CAST('Number of distinct drug era concepts per person' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG_ERA' as VARCHAR(255)) as analysis_type
union all
select  904 as analysis_id,
CAST('Number of persons with at least one drug era, by drug_concept_id by calendar year by gender by age decile' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar year' as VARCHAR(255)) as stratum_2_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_3_name,
CAST('age decile' as VARCHAR(255)) as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG_ERA' as VARCHAR(255)) as analysis_type
union all
select  906 as analysis_id,
CAST('Distribution of age by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG_ERA' as VARCHAR(255)) as analysis_type
union all
select  907 as analysis_id,
CAST('Distribution of drug era length, by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG_ERA' as VARCHAR(255)) as analysis_type
union all
select  908 as analysis_id,
CAST('Number of drug eras without valid person' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG_ERA' as VARCHAR(255)) as analysis_type
union all
select  909 as analysis_id,
CAST('Number of drug eras outside valid observation period' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG_ERA' as VARCHAR(255)) as analysis_type
union all
select  910 as analysis_id,
CAST('Number of drug eras with end date < start date' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG_ERA' as VARCHAR(255)) as analysis_type
union all
select  920 as analysis_id,
CAST('Number of drug era records  by drug era start month' as VARCHAR(255)) as analysis_name,
CAST('calendar month' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('DRUG_ERA' as VARCHAR(255)) as analysis_type
union all
select 1000 as analysis_id,
CAST('Number of persons with at least one condition era, by condition_concept_id' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION_ERA' as VARCHAR(255)) as analysis_type
union all
select 1001 as analysis_id,
CAST('Number of condition era records, by condition_concept_id' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION_ERA' as VARCHAR(255)) as analysis_type
union all
select 1002 as analysis_id,
CAST('Number of persons by condition era start month, by condition_concept_id' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar month' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION_ERA' as VARCHAR(255)) as analysis_type
union all
select 1003 as analysis_id,
CAST('Number of distinct condition era concepts per person' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION_ERA' as VARCHAR(255)) as analysis_type
union all
select 1004 as analysis_id,
CAST('Number of persons with at least one condition era, by condition_concept_id by calendar year by gender by age decile' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar year' as VARCHAR(255)) as stratum_2_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_3_name,
CAST('age decile' as VARCHAR(255)) as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION_ERA' as VARCHAR(255)) as analysis_type
union all
select 1006 as analysis_id,
CAST('Distribution of age by condition_concept_id' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION_ERA' as VARCHAR(255)) as analysis_type
union all
select 1007 as analysis_id,
CAST('Distribution of condition era length, by condition_concept_id' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION_ERA' as VARCHAR(255)) as analysis_type
union all
select 1008 as analysis_id,
CAST('Number of condition eras without valid person' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION_ERA' as VARCHAR(255)) as analysis_type
union all
select 1009 as analysis_id,
CAST('Number of condition eras outside valid observation period' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION_ERA' as VARCHAR(255)) as analysis_type
union all
select 1010 as analysis_id,
CAST('Number of condition eras with end date < start date' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION_ERA' as VARCHAR(255)) as analysis_type
union all
select 1020 as analysis_id,
CAST('Number of condition era records by condition era start month' as VARCHAR(255)) as analysis_name,
CAST('calendar month' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CONDITION_ERA' as VARCHAR(255)) as analysis_type
union all
select 1100 as analysis_id,
CAST('Number of persons by location 3-digit zip' as VARCHAR(255)) as analysis_name,
CAST('3-digit zip' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('LOCATION' as VARCHAR(255)) as analysis_type
union all
select 1101 as analysis_id,
CAST('Number of persons by location state' as VARCHAR(255)) as analysis_name,
CAST('state' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('LOCATION' as VARCHAR(255)) as analysis_type
union all
select 1200 as analysis_id,
CAST('Number of persons by place of service' as VARCHAR(255)) as analysis_name,
CAST('place_of_service_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CARE_SITE' as VARCHAR(255)) as analysis_type
union all
select 1201 as analysis_id,
CAST('Number of visits by place of service' as VARCHAR(255)) as analysis_name,
CAST('place_of_service_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('CARE_SITE' as VARCHAR(255)) as analysis_type
union all
select 1300 as analysis_id,
CAST('Number of persons with at least one measurement occurrence, by measurement_concept_id' as VARCHAR(255)) as analysis_name,
CAST('measurement_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1301 as analysis_id,
CAST('Number of measurement occurrence records, by measurement_concept_id' as VARCHAR(255)) as analysis_name,
CAST('measurement_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1302 as analysis_id,
CAST('Number of persons by measurement occurrence start month, by measurement_concept_id' as VARCHAR(255)) as analysis_name,
CAST('measurement_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar month' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1303 as analysis_id,
CAST('Number of distinct measurement occurrence concepts per person' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1304 as analysis_id,
CAST('Number of persons with at least one measurement occurrence, by measurement_concept_id by calendar year by gender by age decile' as VARCHAR(255)) as analysis_name,
CAST('measurement_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('calendar year' as VARCHAR(255)) as stratum_2_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_3_name,
CAST('age decile' as VARCHAR(255)) as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1305 as analysis_id,
CAST('Number of measurement occurrence records, by measurement_concept_id by measurement_type_concept_id' as VARCHAR(255)) as analysis_name,
CAST('measurement_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('measurement_type_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1306 as analysis_id,
CAST('Distribution of age by measurement_concept_id' as VARCHAR(255)) as analysis_name,
CAST('measurement_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1307 as analysis_id,
CAST('Number of measurement occurrence records, by measurement_concept_id and unit_concept_id' as VARCHAR(255)) as analysis_name,
CAST('measurement_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('unit_concept_id' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1309 as analysis_id,
CAST('Number of measurement records with invalid person_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1310 as analysis_id,
CAST('Number of measurement records outside valid measurement period' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1312 as analysis_id,
CAST('Number of measurement records with invalid provider_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1313 as analysis_id,
CAST('Number of measurement records with invalid visit_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1314 as analysis_id,
CAST('Number of measurement records with no value (numeric, string, or concept)' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1315 as analysis_id,
CAST('Distribution of numeric values, by measurement_concept_id and unit_concept_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1316 as analysis_id,
CAST('Distribution of low range, by measurement_concept_id and unit_concept_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1317 as analysis_id,
CAST('Distribution of high range, by measurement_concept_id and unit_concept_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1318 as analysis_id,
CAST('Number of measurement records below/within/above normal range, by measurement_concept_id and unit_concept_id' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1320 as analysis_id,
CAST('Number of measurement records  by measurement start month' as VARCHAR(255)) as analysis_name,
CAST('calendar month' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('MEASUREMENT' as VARCHAR(255)) as analysis_type
union all
select 1700 as analysis_id,
CAST('Number of records by cohort_definition_id' as VARCHAR(255)) as analysis_name,
CAST('cohort_definition_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT' as VARCHAR(255)) as analysis_type
union all
select 1701 as analysis_id,
CAST('Number of records with cohort end date < cohort start date' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT' as VARCHAR(255)) as analysis_type
union all
select 1800 as analysis_id,
CAST('Number of persons by age, with age at cohort start' as VARCHAR(255)) as analysis_name,
CAST('age' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1801 as analysis_id,
CAST('Distribution of age at cohort start' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1802 as analysis_id,
CAST('Distribution of age at cohort start by gender' as VARCHAR(255)) as analysis_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1803 as analysis_id,
CAST('Distribution of age at cohort start by cohort start year' as VARCHAR(255)) as analysis_name,
CAST('calendar year' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1804 as analysis_id,
CAST('Number of persons by duration from cohort start to cohort end, in 30d increments' as VARCHAR(255)) as analysis_name,
CAST('Cohort period length 30d increments' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1805 as analysis_id,
CAST('Number of persons by duration from observation start to cohort start, in 30d increments' as VARCHAR(255)) as analysis_name,
CAST('Baseline period length 30d increments' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1806 as analysis_id,
CAST('Number of persons by duration from cohort start to observation end, in 30d increments' as VARCHAR(255)) as analysis_name,
CAST('Follow-up period length 30d increments' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1807 as analysis_id,
CAST('Number of persons by duration from cohort end to observation end, in 30d increments' as VARCHAR(255)) as analysis_name,
CAST('Post-cohort period length 30d increments' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1808 as analysis_id,
CAST('Distribution of duration (days) from cohort start to cohort end' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1809 as analysis_id,
CAST('Distribution of duration (days) from cohort start to cohort end, by gender' as VARCHAR(255)) as analysis_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1810 as analysis_id,
CAST('Distribution of duration (days) from cohort start to cohort end, by age decile' as VARCHAR(255)) as analysis_name,
CAST('age decile' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1811 as analysis_id,
CAST('Distribution of duration (days) from observation start to cohort start' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1812 as analysis_id,
CAST('Distribution of duration (days) from cohort start to observation end' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1813 as analysis_id,
CAST('Distribution of duration (days) from cohort end to observation end' as VARCHAR(255)) as analysis_name,
NULL as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1814 as analysis_id,
CAST('Number of persons by cohort start year by gender by age decile' as VARCHAR(255)) as analysis_name,
CAST('calendar year' as VARCHAR(255)) as stratum_1_name,
CAST('gender_concept_id' as VARCHAR(255)) as stratum_2_name,
CAST('age decile' as VARCHAR(255)) as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1815 as analysis_id,
CAST('Number of persons by cohort start month' as VARCHAR(255)) as analysis_name,
CAST('calendar month' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1816 as analysis_id,
CAST('Number of persons by number of cohort periods' as VARCHAR(255)) as analysis_name,
CAST('number of cohort periods' as VARCHAR(255)) as stratum_1_name,
NULL as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1820 as analysis_id,
CAST('Number of persons by duration from cohort start to first occurrence of condition occurrence, by condition_concept_id' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('time-to-event 30d increments' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1821 as analysis_id,
CAST('Number of events by duration from cohort start to all occurrences of condition occurrence, by condition_concept_id' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('time-to-event 30d increments' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1830 as analysis_id,
CAST('Number of persons by duration from cohort start to first occurrence of procedure occurrence, by procedure_concept_id' as VARCHAR(255)) as analysis_name,
CAST('procedure_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('time-to-event 30d increments' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1831 as analysis_id,
CAST('Number of events by duration from cohort start to all occurrences of procedure occurrence, by procedure_concept_id' as VARCHAR(255)) as analysis_name,
CAST('procedure_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('time-to-event 30d increments' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1840 as analysis_id,
CAST('Number of persons by duration from cohort start to first occurrence of drug exposure, by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('time-to-event 30d increments' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1841 as analysis_id,
CAST('Number of events by duration from cohort start to all occurrences of drug exposure, by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('time-to-event 30d increments' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1850 as analysis_id,
CAST('Number of persons by duration from cohort start to first occurrence of observation, by observation_concept_id' as VARCHAR(255)) as analysis_name,
CAST('observation_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('time-to-event 30d increments' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1851 as analysis_id,
CAST('Number of events by duration from cohort start to all occurrences of observation, by observation_concept_id' as VARCHAR(255)) as analysis_name,
CAST('observation_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('time-to-event 30d increments' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1860 as analysis_id,
CAST('Number of persons by duration from cohort start to first occurrence of condition era, by condition_concept_id' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('time-to-event 30d increments' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1861 as analysis_id,
CAST('Number of events by duration from cohort start to all occurrences of condition era, by condition_concept_id' as VARCHAR(255)) as analysis_name,
CAST('condition_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('time-to-event 30d increments' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1870 as analysis_id,
CAST('Number of persons by duration from cohort start to first occurrence of drug era, by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('time-to-event 30d increments' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
union all
select 1871 as analysis_id,
CAST('Number of events by duration from cohort start to all occurrences of drug era, by drug_concept_id' as VARCHAR(255)) as analysis_name,
CAST('drug_concept_id' as VARCHAR(255)) as stratum_1_name,
CAST('time-to-event 30d increments' as VARCHAR(255)) as stratum_2_name,
NULL as stratum_3_name,
NULL as stratum_4_name,
NULL as stratum_5_name,
CAST('COHORT_SPECIFIC_ANALYSES' as VARCHAR(255)) as analysis_type
;
