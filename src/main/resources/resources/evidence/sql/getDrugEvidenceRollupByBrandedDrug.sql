WITH branded_to_clin_drug AS (
  SELECT c2.CONCEPT_ID AS CONCEPT_ID
  FROM @CDM_schema.concept c
      JOIN @CDM_schema.concept_ancestor ca ON ca.descendant_concept_id = c.CONCEPT_ID
      JOIN @CDM_schema.concept c2 ON c2.CONCEPT_ID = ca.ANCESTOR_CONCEPT_ID
                     AND c2.vocabulary_id = 'RxNorm'
                     AND c2.concept_class_id = 'Clinical Drug'
                     AND c2.invalid_reason IS NULL
 WHERE c.concept_id = @id
   AND c.VOCABULARY_ID = 'RxNorm'
   AND c.concept_class_id IN ( 'Branded Drug', 'Branded Pack')
   AND c.invalid_reason IS NULL
)
SELECT REPORT_ORDER, REPORT_NAME, INGREDIENT_ID, INGREDIENT, CLINICAL_DRUG_ID, CLINICAL_DRUG, HOI_ID, HOI, MEDLINE_CT_COUNT, MEDLINE_CASE_COUNT, MEDLINE_OTHER_COUNT, CTD_CHEMICAL_DISEASE_COUNT, SPLICER_COUNT, EU_SPC_COUNT, SEMMEDDB_CT_COUNT, SEMMEDDB_CASE_COUNT, SEMMEDDB_OTHER_COUNT,  SEMMEDDB_NEG_CT_COUNT, SEMMEDDB_NEG_CASE_COUNT, SEMMEDDB_NEG_OTHER_COUNT, AERS_REPORT_COUNT, PRR
FROM @OHDSI_schema.LAERTES_SUMMARY, branded_to_clin_drug
WHERE CLINICAL_DRUG_ID = branded_to_clin_drug.CONCEPT_ID