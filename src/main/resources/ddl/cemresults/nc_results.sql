IF OBJECT_ID('@cem_results_schema.nc_results', 'U') IS NULL
CREATE TABLE @cem_results_schema.nc_results
(
    concept_set_id bigint NOT NULL,
    negative_control int, 
    outcome_of_interest_concept_id int, 
    outcome_of_interest_concept_name varchar(255), 
    sort_order int, 
    descendant_pmid_cnt bigint, 
    exact_pmid_cnt bigint, 
    parent_pmid_cnt bigint, 
    ancestor_pmid_cnt bigint, 
    ind_ci int, 
    too_broad int, 
    drug_induced int, 
    pregnancy int, 
    descendant_splicer_cnt bigint, 
    exact_splicer_cnt bigint, 
    parent_splicer_cnt bigint, 
    ancestor_splicer_cnt bigint, 
    descendant_faers_cnt bigint, 
    exact_faers_cnt bigint, 
    parent_faers_cnt bigint, 
    ancestor_faers_cnt bigint, 
    user_excluded int, 
    user_included int, 
    optimized_out int, 
    not_prevalent int
);
