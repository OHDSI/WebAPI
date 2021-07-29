alter table ${ohdsiSchema}.cohort_generation_info drop column include_features;

delete
from
	${ohdsiSchema}.sec_role_permission
where
	id in (
	select
		srp.id
	from
		${ohdsiSchema}.sec_role_permission srp
	join ${ohdsiSchema}.sec_permission sp on
		sp.id = srp.permission_id
	where
		sp.value like 'featureextraction:query:prevalence:*:%:get'
		or sp.value like 'featureextraction:query:distributions:*:%:get'
		or sp.value like 'featureextraction:explore:prevalence:*:%:*:get'
		or sp.value like 'featureextraction:generatesql:%:*:get'
		or sp.value like 'featureextraction:generate:%:*:get');

delete
from
	${ohdsiSchema}.sec_permission
where
	value like 'featureextraction:query:prevalence:*:%:get'
	or value like 'featureextraction:query:distributions:*:%:get'
	or value like 'featureextraction:explore:prevalence:*:%:*:get'
	or value like 'featureextraction:generatesql:%:*:get'
	or value like 'featureextraction:generate:%:*:get';

drop table ${ohdsiSchema}.cohort_features_dist;

drop table ${ohdsiSchema}.cohort_features;

drop table ${ohdsiSchema}.cohort_features_ref;

drop table ${ohdsiSchema}.cohort_features_analysis_ref;
