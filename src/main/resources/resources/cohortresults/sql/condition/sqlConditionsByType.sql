select c1.concept_id as condition_concept_id, 
       c1.concept_name as condition_concept_name,
       c2.concept_group_id as concept_id,
       c2.concept_group_name as concept_name, 
       sum(ar1.count_value) as count_value
from ACHILLES_results ar1
       inner join
       @cdmSchema.dbo.concept c1
       on CAST(ar1.stratum_1 AS INT) = c1.concept_id
       inner join
       (
       select concept_id,
             case when concept_name like 'Inpatient%' then 10
                    when concept_name like 'Outpatient%' then 20
                    else concept_id end  
                    +
                    case when (concept_name like 'Inpatient%' or concept_name like 'Outpatient%' ) and (concept_name like '%primary%' or concept_name like '%1st position%') then 1
                    when (concept_name like 'Inpatient%' or concept_name like 'Outpatient%' ) and (concept_name not like '%primary%' and concept_name not like '%1st position%') then 2
                    else 0 end as concept_group_id,
             case when concept_name like 'Inpatient%' then 'Claim- Inpatient: '
                    when concept_name like 'Outpatient%' then 'Claim- Outpatient: '
                    else concept_name end  
                    +
                    ''
                    +
                    case when (concept_name like 'Inpatient%' or concept_name like 'Outpatient%' ) and (concept_name like '%primary%' or concept_name like '%1st position%') then 'Primary diagnosis'
                    when (concept_name like 'Inpatient%' or concept_name like 'Outpatient%' ) and (concept_name not like '%primary%' and concept_name not like '%1st position%') then 'Secondary diagnosis'
                    else '' end as concept_group_name
       from @cdmSchema.dbo.concept
       where lower(domain_id) = 'condition type' 
       
       ) c2
       on CAST(ar1.stratum_2 AS INT) = c2.concept_id
where ar1.analysis_id = 405
group by c1.concept_id, 
       c1.concept_name,
       c2.concept_group_id,
       c2.concept_group_name