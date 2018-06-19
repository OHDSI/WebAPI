SELECT 
	job_id, 
	@SOURCE_ID source_id,
	@CONCEPT_SET_ID concept_set_id,
	'@CONCEPT_SET_NAME' concept_set_name,
	negative_control, 
	outcome_of_interest_concept_id concept_id, 
	outcome_of_interest_concept_name concept_name, 
	'@outcomeOfInterest' domain_id,
	sort_order, 
	descendant_pmid_cnt, 
	exact_pmid_cnt, 
	parent_pmid_cnt, 
	ancestor_pmid_cnt, 
	ind_ci, 
	too_broad,
	drug_induced, 
	pregnancy,
	descendant_splicer_cnt,
	exact_splicer_cnt,
	parent_splicer_cnt,
	ancestor_splicer_cnt,
	descendant_faers_cnt,
	exact_faers_cnt,
	parent_faers_cnt,
	ancestor_faers_cnt,
	user_excluded, 
	user_included,
	optimized_out,
	not_prevalent
FROM @evidenceSchema.nc_results
WHERE job_id = 	@jobId;