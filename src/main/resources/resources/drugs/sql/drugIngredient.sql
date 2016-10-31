-- set where clause in service
-- might want to filter on dex.person_id
-- or dex.drug_concept_id or both
select		cing.concept_name ingredient, cdex.concept_name drug_name,
		ctype.concept_name drug_type,
		dex.drug_exposure_start_date,
		dex.stop_reason,
		dex.refills,
		dex.quantity,
		dex.days_supply
from @tableQualifier.drug_exposure dex
join @tableQualifier.concept cdex on dex.drug_concept_id = cdex.concept_id
join @tableQualifier.concept ctype on dex.drug_type_concept_id = ctype.concept_id
join @tableQualifier.concept_ancestor ca on dex.drug_concept_id = ca.descendant_concept_id														and ca.ancestor_concept_id is not null
join @tableQualifier.concept cing on ca.ancestor_concept_id = cing.concept_id
                                    and cing.concept_class_id = 'Ingredient'
                                    and cing.standard_concept = 'S'
                                    and cing.invalid_reason is null
                                    and ca.ancestor_concept_id is not null


