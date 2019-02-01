INSERT INTO @cem_results_schema.NC_RESULTS (
	concept_set_id, 
	negative_control, 
	outcome_of_interest_concept_id, 
	outcome_of_interest_concept_name, 
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
)
select 
	@conceptSetId,
	CASE WHEN o.OPTIMIZED = 1 THEN 1 ELSE 0 END NEGATIVE_CONTROL,
	d.outcome_of_interest_concept_id, 
	d.outcome_of_interest_concept_name, 
	d.sort_order, 
	d.descendant_pmid_count, 
	d.exact_pmid_count, 
	d.parent_pmid_count, 
	d.ancestor_pmid_count, 
	d.ind_ci, 
	d.too_broad,
	d.drug_induced,
	d.pregnancy,
	d.DESCENDANT_SPLICER,
	d.EXACT_SPLICER,
	d.PARENT_SPLICER,
	d.ANCESTOR_SPLICER,
	d.DESCENDANT_FAERS,
	d.EXACT_FAERS,
	d.PARENT_FAERS,
	d.ANCESTOR_FAERS,
	d.user_excluded, 
	d.user_included,
	CASE WHEN o.OPTIMIZED = 0 AND o.NOT_PREVELANT = 0 THEN 1 ELSE 0 END OPTIMIZED_OUT,
	CASE WHEN o.NOT_PREVELANT = 1 THEN 1 ELSE 0 END NOT_PREVALENT
FROM #NC_SUMMARY d
  LEFT OUTER JOIN #NC_SUMMARY_OPTIMIZED o
    ON d.OUTCOME_OF_INTEREST_CONCEPT_ID = o.OUTCOME_OF_INTEREST_CONCEPT_ID
{@outcomeOfInterest == 'drug'}?{
  WHERE d.OUTCOME_OF_INTEREST_CONCEPT_ID IN (
    SELECT CONCEPT_ID FROM @vocabulary.CONCEPT WHERE DOMAIN_ID = 'Drug' AND CONCEPT_CLASS_ID = 'Ingredient'
  )
}
;
