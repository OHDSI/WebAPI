alter table ${ohdsiSchema}.cohort_generation_info drop column include_features;

delete
from
	${ohdsiSchema}.sec_role_permission srp
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
  ${ohdsiSchema}.sec_permission sp
where
  sp.value like 'featureextraction:query:prevalence:*:%:get'
  or sp.value like 'featureextraction:query:distributions:*:%:get'
  or sp.value like 'featureextraction:explore:prevalence:*:%:*:get'
  or sp.value like 'featureextraction:generatesql:%:*:get'
	or sp.value like 'featureextraction:generate:%:*:get';

drop sequence cohort_features_dist_pk_seq;

drop sequence cohort_features_pk_seq;

drop sequence cohort_features_ref_pk_seq;

drop sequence cohort_feat_anlys_ref_pk_seq;

drop trigger ${ohdsiSchema}.cohort_features_dist_bir;

drop trigger ${ohdsiSchema}.cohort_features_bir;

drop trigger ${ohdsiSchema}.cohort_features_ref_bir;

drop trigger ${ohdsiSchema}.cohort_feat_anlys_ref_bir;

drop table ${ohdsiSchema}.cohort_features_dist;

drop table ${ohdsiSchema}.cohort_features;

drop table ${ohdsiSchema}.cohort_features_ref;

drop table ${ohdsiSchema}.cohort_features_analysis_ref;