-- When using role 15, remember to remove the role "source user" from all users.
-- See also https://github.com/OHDSI/WebAPI/wiki/Read-restricted-Configuration

delete from ${ohdsiSchema}.sec_role_permission where role_id = 15;

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
with vocab_source as (
 select source_key
 from ${ohdsiSchema}.source s
   inner join ${ohdsiSchema}.source_daimon sd on s.source_id = sd.source_id 
 where sd.daimon_type = 1
), vocab_perms as (
 select distinct concat(l,m,r) perm
 from (
 select *
 from (values 
		('vocabulary:')
	) t1 (l)
 cross join 
	( select source_key 
	  from vocab_source
	) t2 (m)
 cross join 
	(values
		(':*:get'),
		(':compare:post'),
		(':concept:*:ancestorAndDescendant:get'),
		(':concept:*:get'),
		(':concept:*:related:get'),
		(':included-concepts:count:post'),
		(':lookup:identifiers:ancestors:post'),
		(':lookup:identifiers:post'),
		(':lookup:mapped:post'),
		(':lookup:recommended:post'),
		(':lookup:sourcecodes:post'),
		(':optimize:post'),
		(':resolveConceptSetExpression:post'),
		(':search:*:get'),
		(':search:post')
	) t3 (r)
 ) combined
),
 source_perms as (
 select distinct concat(ls,ms,rs) perm
 from (
 select *
 from (values 
		('source:')
	) t11 (ls)
 cross join 
	( select source_key 
	  from vocab_source
	) t22 (ms)
 cross join 
	(values
		(':access')
	) t33 (rs)
 ) combined
),
 generate_perms as (
 select distinct concat(lg,mg,rg) perm
 from (
 select *
 from (values 
		('cohortdefinition:*:generate:')
	) t111 (lg)
 cross join 
	( select source_key 
	  from vocab_source
	) t222 (mg)
 cross join 
	(values
		(':get')
	) t333 (rg)
 ) combined
)
SELECT DISTINCT 15 role_id, permission_id
    FROM ${ohdsiSchema}.sec_role_permission srp  
       INNER JOIN ${ohdsiSchema}.sec_permission sp ON srp.permission_id = sp.id      
    WHERE 
       sp.value IN (select perm from vocab_perms) 
	   or
	   sp.value IN (select perm from source_perms)
       or
	   sp.value IN (select perm from generate_perms)
       or
       sp.value IN 
          (
		'cohort-characterization:byTags:post',
		'cohort-characterization:check:post',
		'cohort-characterization:get',
		'cohort-characterization:import:post',
		'cohort-characterization:post',
		'cohortanalysis:get',
		'cohortanalysis:post',
		'cohortdefinition:byTags:post',
		'cohortdefinition:check:post',
		'cohortdefinition:checkv2:post',
		'cohortdefinition:get',
		'cohortdefinition:post',
		'cohortdefinition:printfriendly:cohort:post',
		'cohortdefinition:printfriendly:conceptsets:post',
		'cohortdefinition:sql:post',
		'comparativecohortanalysis:get',
		'comparativecohortanalysis:post',
		'conceptset:byTags:post',
		'conceptset:check:post',
		'conceptset:get',
		'conceptset:post',
		'configuration:edit:ui',
		'estimation:check:post',
		'estimation:get',
		'estimation:import:post',
		'estimation:post',
		'executionservice:execution:run:post',
		'feasibility:get',
		'feature-analysis:aggregates:get',
		'feature-analysis:get',
		'feature-analysis:post',
		'ir:byTags:post',
		'ir:check:post',
		'ir:design:post',
		'ir:get',
		'ir:post',
		'ir:sql:post',
		'job:execution:get',
		'job:get',
		'notifications:get',
		'notifications:viewed:get',
		'notifications:viewed:post',
		'pathway-analysis:byTags:post',
		'pathway-analysis:check:post',
		'pathway-analysis:get',
		'pathway-analysis:import:post',
		'pathway-analysis:post',
		'plp:get',
		'plp:post',
		'prediction:get',
		'prediction:import:post',
		'prediction:post',
		'reusable:byTags:post',
		'reusable:get',
		'reusable:post',
		'source:daimon:priority:get',
		'source:priorityVocabulary:get',
		'sqlrender:translate:post',
		'tag:get',
		'tag:multiAssign:post',
		'tag:multiUnassign:post',
		'tag:post',
		'tag:search:get',
		'cohortdefinition:*:exists:get', -- weird one...but is needed / used by UI before saving a new cohortdefinition....
		'conceptset:*:exists:get', -- weird one...but is needed / used by UI before saving a new conceptset....
       )
;
