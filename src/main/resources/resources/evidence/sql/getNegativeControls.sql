WITH cte_concepts_of_interest AS
(
	/*********************************************
	Define The Concepts of Interest
	**********************************************/
	SELECT
	 DISTINCT 
		concept_id coi_concept_id
		, concept_name coi_concept_name

	FROM
	 @tableQualifier.concept c
	WHERE c.concept_id IN (
            @CONCEPT_IDS
        )
), cte_total_universe AS 
(
	SELECT DISTINCT 0 AS condition_concept_id,
			CAST('@CONCEPT_SET_NAME' AS VARCHAR(1000)) condition_concept_name,
			ingredient_concept_id drug_concept_id,
			ingredient_concept_name drug_concept_name,
			'DRUG' domain_id
	FROM @tableQualifier.drug_universe /*this table is created as part of the main anaylsis*/
	UNION 
	SELECT DISTINCT condition_concept_id,
                        condition_concept_name concept_name,
                        0 AS drug_concept_id,
			CAST('@CONCEPT_SET_NAME' AS VARCHAR(1000)) drug_concept_name,
			'CONDITION' domain_id
	FROM @tableQualifier.condition_universe /*this table is created as part of the main anaylsis*/
), cte_complete_universe AS
(
        /*********************************************
        Create a Complete Universe
        **********************************************/
	SELECT * 
	FROM cte_total_universe
	WHERE domain_id = '@TARGET_DOMAIN_ID'
), cte_filter_data_to_universe  AS (
	SELECT e.*, 
		c.concept_id AS drug_concept_id, 
		c.concept_name AS drug_concept_name,
		cc.condition_concept_id, 
		cc.condition_concept_name
	FROM @tableQualifier.drug_hoi_evidence e
		JOIN @tableQualifier.drug_hoi_relationship r
			ON r.ID = e.drug_hoi_relationship
			/*ROLL DOWN TO INGREDIENTS*/
		JOIN @tableQualifier.concept_ancestor ca
			ON ca.descendant_concept_id = r.DRUG
			AND ca.ancestor_concept_id IN (
				SELECT DISTINCT concept_id
				FROM @tableQualifier.concept
				WHERE vocabulary_id = 'RxNorm'
				AND concept_class_id = 'Ingredient'
				AND invalid_reason IS NULL
			)
		JOIN @tableQualifier.concept c
			ON c.concept_id = ca.ancestor_concept_id
			AND c.concept_id IN (
				/*KEEP TO DRUG UNIVERSE*/
				SELECT DISTINCT ingredient_concept_id FROM @tableQualifier.drug_universe
			)
		JOIN @tableQualifier.lu_conditions_children cc
			ON cc.concept_id = r.hoi
			AND cc.condition_concept_id IN (
				/*KEEP TO CONDITION UNIVERSE*/
				SELECT DISTINCT condition_concept_id FROM @tableQualifier.condition_universe
			)
), cte_summarize_evidence AS (
	/*********************************************
	Summarize the Evidence that Does Exist for the Condition
	**********************************************/
	 SELECT
	 
		0 @CONCEPT_DOMAIN_ID_CONCEPT_ID
		, CAST('@CONCEPT_SET_NAME' AS TEXT) CONDITION_CONCEPT_NAME
		, @TARGET_DOMAIN_ID_CONCEPT_ID
		, @TARGET_DOMAIN_ID_CONCEPT_NAME
		, evidence_type
		, supports
		, CASE 
			WHEN evidence_type IN ('MEDLINE_MeSH_ClinTrial','MEDLINE_MeSH_CR','MEDLINE_MeSH_Other','MEDLINE_SemMedDB_ClinTrial','MEDLINE_SemMedDB_CR','MEDLINE_SemMedDB_Other','SPL_eu_spc','SPL_SPLICER_ADR','aers_report_count') THEN SUM(STATISTIC_VALUE) 
			WHEN evidence_type IN ('aers_report_prr') THEN EXP(AVG(LOG(STATISTIC_VALUE)))
			ELSE NULL
		END STATISTIC_VALUE
		--, STRING_AGG(EVIDENCE_LINKOUT, '|') AS EVIDENCE_LINKOUTS
	FROM
	 cte_filter_data_to_universe
	WHERE @CONCEPT_DOMAIN_ID_CONCEPT_ID IN (
		SELECT coi_concept_id FROM cte_concepts_of_interest
	)
	GROUP BY @TARGET_DOMAIN_ID_CONCEPT_ID, @TARGET_DOMAIN_ID_CONCEPT_NAME, evidence_type, supports
), cte_summarize  AS  (
	SELECT DISTINCT 
                u.@TARGET_DOMAIN_ID_CONCEPT_ID, 
                u.@TARGET_DOMAIN_ID_CONCEPT_NAME, 
                u.domain_id,
		MAX(CASE WHEN c1.STATISTIC_VALUE IS NULL THEN 0 ELSE c1.STATISTIC_VALUE END) AS medline_ct,
		MAX(CASE WHEN c2.STATISTIC_VALUE IS NULL THEN 0 ELSE c2.STATISTIC_VALUE END) AS medline_case,
		MAX(CASE WHEN c3.STATISTIC_VALUE IS NULL THEN 0 ELSE c3.STATISTIC_VALUE END) AS medline_other,
		MAX(CASE WHEN c4.supports='t' THEN 
			CASE WHEN c4.STATISTIC_VALUE IS NULL THEN 0 ELSE 1 END 
			ELSE 0 END) AS semmeddb_ct_t,
		MAX(CASE WHEN c5.supports='t' THEN 
			CASE WHEN c5.STATISTIC_VALUE IS NULL THEN 0 ELSE 1 END 
			ELSE 0 END) AS semmeddb_case_t,
		MAX(CASE WHEN smo.supports='t' THEN 
			CASE WHEN smo.STATISTIC_VALUE IS NULL THEN 0 ELSE 1 END 
			ELSE 0 END) AS semmeddb_other_t,
		MAX(CASE WHEN c4.supports='f' THEN 
			CASE WHEN c4.STATISTIC_VALUE IS NULL THEN 0 ELSE 1 END 
			ELSE 0 END) AS semmeddb_ct_f,
		MAX(CASE WHEN c5.supports='f' THEN 
			CASE WHEN c5.STATISTIC_VALUE IS NULL THEN 0 ELSE 1 END 
			ELSE 0 END) AS semmeddb_case_f,
		MAX(CASE WHEN smo.supports='f' THEN 
			CASE WHEN smo.STATISTIC_VALUE IS NULL THEN 0 ELSE 1 END 
			ELSE 0 END) AS semmeddb_other_f,
		MAX(CASE WHEN coalesce(c6.STATISTIC_VALUE,0) > 0 THEN 1 ELSE 0 END) AS eu_spc,
		MAX(CASE WHEN coalesce(c7.STATISTIC_VALUE,0) > 0 THEN 1 ELSE 0 END) AS spl_adr,
		MAX(CASE WHEN c8.STATISTIC_VALUE IS NULL THEN 0 ELSE c8.STATISTIC_VALUE END) AS aers,
		MAX(CASE WHEN c9.STATISTIC_VALUE IS NULL THEN 0 ELSE c9.STATISTIC_VALUE END) AS aers_prr
	FROM cte_complete_universe u
		LEFT OUTER JOIN cte_summarize_evidence c1
			ON c1.condition_concept_id = u.condition_concept_id 
			AND c1.drug_concept_id = u.drug_concept_id 
			AND c1.evidence_type = 'MEDLINE_MeSH_ClinTrial'
		LEFT OUTER JOIN cte_summarize_evidence c2
			ON c2.condition_concept_id = u.condition_concept_id 
			AND c2.drug_concept_id = u.drug_concept_id 
			AND c2.evidence_type = 'MEDLINE_MeSH_CR'
		LEFT OUTER JOIN cte_summarize_evidence c3
			ON c3.condition_concept_id = u.condition_concept_id 
			AND c3.drug_concept_id = u.drug_concept_id
			AND c3.evidence_type = 'MEDLINE_MeSH_Other'
		LEFT OUTER JOIN cte_summarize_evidence c4
			ON c4.condition_concept_id = u.condition_concept_id 
			AND c4.drug_concept_id = u.drug_concept_id
			AND c4.evidence_type = 'MEDLINE_SemMedDB_ClinTrial'
		LEFT OUTER JOIN cte_summarize_evidence c5
			ON c5.condition_concept_id = u.condition_concept_id 
			AND c5.drug_concept_id = u.drug_concept_id 
			AND c5.evidence_type = 'MEDLINE_SemMedDB_CR'
		LEFT OUTER JOIN cte_summarize_evidence smo
			ON smo.condition_concept_id  = u.condition_concept_id 
			AND smo.drug_concept_id = u.drug_concept_id 
			AND smo.evidence_type = 'MEDLINE_SemMedDB_Other'
		LEFT OUTER JOIN cte_summarize_evidence c6
			ON c6.condition_concept_id  = u.condition_concept_id 
			AND c6.drug_concept_id = u.drug_concept_id 
			AND c6.evidence_type = 'SPL_eu_spc'
		LEFT OUTER JOIN cte_summarize_evidence c7
			ON c7.condition_concept_id  = u.condition_concept_id 
			AND c7.drug_concept_id = u.drug_concept_id 
			AND c7.evidence_type = 'SPL_SPLICER_ADR'
		LEFT OUTER JOIN cte_summarize_evidence c8
			ON c8.condition_concept_id  = u.condition_concept_id 
			AND c8.drug_concept_id = u.drug_concept_id 
			AND c8.evidence_type = 'aers_report_count'
		LEFT OUTER JOIN cte_summarize_evidence c9
			ON c9.condition_concept_id  = u.condition_concept_id 
			AND c9.drug_concept_id = u.drug_concept_id 
			AND c9.evidence_type = 'aers_report_prr'
	GROUP BY 
                u.@TARGET_DOMAIN_ID_CONCEPT_ID, 
                u.@TARGET_DOMAIN_ID_CONCEPT_NAME, 
                u.domain_id
), cte_summary AS (
	/*********************************************
	Identify all Drugs and their Sister Drugs that Have Evidence
	**********************************************/
	 SELECT	 
		s.@TARGET_DOMAIN_ID_CONCEPT_ID concept_id, 
		s.@TARGET_DOMAIN_ID_CONCEPT_NAME concept_name, 
		s.domain_id,
		CASE WHEN medline_ct IS NULL THEN 0 ELSE medline_ct END medline_ct, 
		CASE WHEN medline_case IS NULL THEN 0 ELSE medline_case END medline_case, 
		CASE WHEN medline_other IS NULL THEN 0 ELSE medline_other END medline_other,
		CASE WHEN semmeddb_ct_t IS NULL THEN 0 ELSE semmeddb_ct_t END semmeddb_ct_t, 
		CASE WHEN semmeddb_case_t IS NULL THEN 0 ELSE semmeddb_case_t END semmeddb_case_t, 
		CASE WHEN semmeddb_other_t IS NULL THEN 0 ELSE semmeddb_other_t END semmeddb_other_t,
		CASE WHEN semmeddb_ct_f IS NULL THEN 0 ELSE semmeddb_ct_f END semmeddb_ct_f, 
		CASE WHEN semmeddb_case_f IS NULL THEN 0 ELSE semmeddb_case_f END semmeddb_case_f, 
		CASE WHEN semmeddb_other_f IS NULL THEN 0 ELSE semmeddb_other_f END semmeddb_other_f,
		CASE WHEN eu_spc IS NULL THEN 0 ELSE eu_spc END eu_spc, 
		CASE WHEN spl_adr IS NULL THEN 0 ELSE spl_adr END spl_adr, 
		CASE WHEN aers IS NULL THEN 0 ELSE aers END aers, 
		CASE WHEN aers_prr IS NULL THEN 0 ELSE aers_prr END aers_prr,
                se.in_universe
	FROM 
            (SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END in_universe FROM cte_summarize_evidence) se
            , cte_complete_universe cu
	FULL OUTER JOIN CTE_SUMMARIZE s ON s.@TARGET_DOMAIN_ID_CONCEPT_ID = cu.@TARGET_DOMAIN_ID_CONCEPT_ID
), cte_summary_stdev AS (
	SELECT
		STDEV(medline_ct) medline_ct_stdev
		, STDEV(medline_case) medline_case_stdev
		, STDEV(medline_other) medline_other_stdev
		, STDEV(semmeddb_ct_t) semmeddb_ct_t_stdev
		, STDEV(semmeddb_case_t) semmeddb_case_t_stdev
		, STDEV(semmeddb_other_t) semmeddb_other_t_stdev
		, STDEV(semmeddb_ct_f) semmeddb_ct_f_stdev
		, STDEV(semmeddb_case_f) semmeddb_case_f_stdev
		, STDEV(semmeddb_other_f) semmeddb_other_f_stdev
		, STDEV(eu_spc) eu_spc_stdev
		, STDEV(spl_adr) spl_adr_stdev
		, STDEV(aers) aers_stdev
		, STDEV(aers_prr) aers_prr_stdev
	FROM cte_summary
        WHERE cte_summary.in_universe = 1
), cte_summary_scaled AS (
	SELECT	 
		concept_id
		, concept_name
		, DOMAIN_ID
		, medline_ct
		, medline_case
		, medline_other
		, semmeddb_ct_t
		, semmeddb_case_t
		, semmeddb_other_t
		, semmeddb_ct_f
		, semmeddb_case_f
		, semmeddb_other_f
		, eu_spc
		, spl_adr
		, aers
		, aers_prr
		, CASE WHEN medline_ct_stdev = 0 THEN 0 ELSE (medline_ct / medline_ct_stdev) END medline_ct_scaled
		, CASE WHEN medline_case_stdev = 0 THEN 0 ELSE (medline_case / medline_case_stdev) END medline_case_scaled
		, CASE WHEN medline_other_stdev = 0 THEN 0 ELSE (medline_other / medline_other_stdev) END medline_other_scaled
		, CASE WHEN semmeddb_ct_t_stdev = 0 THEN 0 ELSE (semmeddb_ct_t / semmeddb_ct_t_stdev) END semmeddb_ct_t_scaled
		, CASE WHEN semmeddb_case_t_stdev = 0 THEN 0 ELSE (semmeddb_case_t / semmeddb_case_t_stdev) END semmeddb_case_t_scaled
		, CASE WHEN semmeddb_other_t_stdev = 0 THEN 0 ELSE (semmeddb_other_t / semmeddb_other_t_stdev) END semmeddb_other_t_scaled
		, CASE WHEN semmeddb_ct_f_stdev = 0 THEN 0 ELSE (semmeddb_ct_f / semmeddb_ct_f_stdev) END semmeddb_ct_f_scaled
		, CASE WHEN semmeddb_case_f_stdev = 0 THEN 0 ELSE (semmeddb_case_f / semmeddb_case_f_stdev) END semmeddb_case_f_scaled
		, CASE WHEN semmeddb_other_f_stdev = 0 THEN 0 ELSE (semmeddb_other_f / semmeddb_other_f_stdev) END semmeddb_other_f_scaled
		, CASE WHEN eu_spc_stdev = 0 THEN 0 ELSE (eu_spc / eu_spc_stdev) END eu_spc_scaled
		, CASE WHEN spl_adr_stdev = 0 THEN 0 ELSE (spl_adr / spl_adr_stdev) END spl_adr_scaled
		, CASE WHEN aers_stdev = 0 THEN 0 ELSE (aers / aers_stdev) END aers_scaled 
		, CASE WHEN aers_prr_stdev = 0 THEN 0 ELSE (aers_prr / aers_prr_stdev) END aers_prr_scaled

	FROM
	 cte_summary, cte_summary_stdev
        WHERE
         cte_summary.in_universe = 1
), cte_model_applied AS (
	SELECT
                @CONCEPT_SET_ID concept_set_id
                , '@CONCEPT_SET_NAME' concept_set_name
		, concept_id
		, concept_name
		, domain_id
		, medline_ct
		, medline_case
		, medline_other
		, semmeddb_ct_t
		, semmeddb_case_t
		, semmeddb_other_t
		, semmeddb_ct_f
		, semmeddb_case_f
		, semmeddb_other_f
		, eu_spc
		, spl_adr
		, aers
		, aers_prr
		, medline_ct_scaled  
		, b1.beta "medline_ct_beta"
		, medline_case_scaled
		, b2.beta "medline_case_beta"
		, medline_other_scaled
		, b3.beta "medline_other_beta"
		, semmeddb_ct_t_scaled
		, b4.beta "semmeddb_ct_t_beta"
		, semmeddb_case_t_scaled
		, b5.beta "semmeddb_case_t_beta"
		, semmeddb_other_t_scaled
		, b3.beta "semmeddb_other_t_beta"
		, semmeddb_ct_f_scaled
		, b4.beta "semmeddb_ct_f_beta"
		, semmeddb_case_f_scaled
		, b5.beta "semmeddb_case_f_beta"
		, semmeddb_other_f_scaled
		, b3.beta "semmeddb_other_f_beta"
		, eu_spc_scaled
		, b6.beta "eu_spc_beta"
		, spl_adr_scaled
		, b7.beta "spl_adr_beta"
		, aers_scaled
		, b8.beta "aers_beta"
		, aers_prr_scaled
		, b9.beta "aers_prr_beta"
		, ((medline_ct_scaled * b1.beta) + 
		   (medline_case_scaled * b2.beta) + 
		   (medline_other_scaled * b3.beta) +
		   (semmeddb_ct_t_scaled * b4.beta) +
		   (semmeddb_case_t_scaled * b5.beta) +
		   (semmeddb_other_t_scaled * b4.beta) + 
		   --(semmeddb_ct_f_scaled * b4.beta) + 
		   --(semmeddb_case_f_scaled * b5.beta) +
		   --(semmeddb_other_f_scaled * b3.beta) +
		   (eu_spc_scaled * b6.beta) +
		   (spl_adr_scaled * b7.beta) +
		   (aers_scaled * b8.beta) +
		   (aers_prr_scaled * b9.beta)
		   ) + i.beta raw_prediction

	FROM
	 cte_summary_scaled
	LEFT OUTER JOIN @tableQualifier.positive_negative_control_betas b1 ON b1.evidence_type = 'MEDLINE_MeSH_ClinTrial'
	LEFT OUTER JOIN @tableQualifier.positive_negative_control_betas b2 ON b2.evidence_type = 'MEDLINE_MeSH_CR'
	LEFT OUTER JOIN @tableQualifier.positive_negative_control_betas b3 ON b3.evidence_type = 'MEDLINE_MeSH_Other'
	LEFT OUTER JOIN @tableQualifier.positive_negative_control_betas b4 ON b4.evidence_type = 'MEDLINE_SemMedDB_ClinTrial'
	LEFT OUTER JOIN @tableQualifier.positive_negative_control_betas b5 ON b5.evidence_type = 'MEDLINE_SemMedDB_CR'
	LEFT OUTER JOIN @tableQualifier.positive_negative_control_betas b6 ON b6.evidence_type = 'SPL_EU_SPC'
	LEFT OUTER JOIN @tableQualifier.positive_negative_control_betas b7 ON b7.evidence_type = 'SPL_SPLICER_ADR'
	LEFT OUTER JOIN @tableQualifier.positive_negative_control_betas b8 ON b8.evidence_type = 'aers_report_count'
	LEFT OUTER JOIN @tableQualifier.positive_negative_control_betas b9 ON b9.evidence_type = 'aers_report_prr'
	LEFT OUTER JOIN @tableQualifier.positive_negative_control_betas i ON i.evidence_type = 'intercept'
)
-- Provide the results and compute the predictive value
SELECT
        @SOURCE_ID source_id
        , concept_set_id
        , concept_set_name
	, concept_id
	, concept_name
	, domain_id
	, medline_ct
	, medline_case
	, medline_other
	, semmeddb_ct_t
	, semmeddb_case_t
	, semmeddb_other_t
	, semmeddb_ct_f
	, semmeddb_case_f
	, semmeddb_other_f
	, eu_spc
	, spl_adr
	, aers
	, aers_prr
	, medline_ct_scaled  
	, medline_ct_beta
	, medline_case_scaled
	, medline_case_beta
	, medline_other_scaled
	, medline_other_beta
	, semmeddb_ct_t_scaled
	, semmeddb_ct_t_beta
	, semmeddb_case_t_scaled
	, semmeddb_case_t_beta
	, semmeddb_other_t_scaled
	, semmeddb_other_t_beta
	, semmeddb_ct_f_scaled
	, semmeddb_ct_f_beta
	, semmeddb_case_f_scaled
	, semmeddb_case_f_beta
	, semmeddb_other_f_scaled
	, semmeddb_other_f_beta
	, eu_spc_scaled
	, eu_spc_beta
	, spl_adr_scaled
	, spl_adr_beta
	, aers_scaled
	, aers_beta
	, aers_prr_scaled
	, aers_prr_beta
	, raw_prediction
        , EXP(raw_prediction)/(1 + EXP(raw_prediction)) prediction
FROM cte_model_applied
;