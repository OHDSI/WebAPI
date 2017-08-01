select COHORT_DEFINITION_ID, ANALYSIS_ID, STRATUM_1, STRATUM_2, STRATUM_3, STRATUM_4, STRATUM_5, count_value, last_update_time 
from @tableQualifier.heracles_results 
where cohort_definition_id = @cohortDefinitionId 
and analysis_id in 
(2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2021, 2022, 2023, 2024, 2025, 2026, 2027, 2028, 2029)
