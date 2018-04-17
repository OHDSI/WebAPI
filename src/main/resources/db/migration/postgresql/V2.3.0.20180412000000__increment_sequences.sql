-- cca_sequence
SELECT setval('${ohdsiSchema}.cca_sequence', coalesce(max(cca_id) + 1, 1)) FROM ${ohdsiSchema}.cca;
-- cohort_definition_sequence
SELECT setval('${ohdsiSchema}.cohort_definition_sequence', coalesce(max(id) + 1, 1)) FROM ${ohdsiSchema}.cohort_definition;
-- concept_set_item_sequence
SELECT setval('${ohdsiSchema}.concept_set_item_sequence', coalesce(max(concept_set_item_id) + 1, 1)) FROM ${ohdsiSchema}.concept_set_item;
-- concept_set_sequence
SELECT setval('${ohdsiSchema}.concept_set_sequence', coalesce(max(concept_set_id) + 1, 1)) FROM ${ohdsiSchema}.concept_set;
-- feasibility_study_sequence
SELECT setval('${ohdsiSchema}.feasibility_study_sequence', coalesce(max(id) + 1, 1)) FROM ${ohdsiSchema}.feasibility_study;
-- ir_analysis_sequence
SELECT setval('${ohdsiSchema}.ir_analysis_sequence', coalesce(max(id) + 1, 1)) FROM ${ohdsiSchema}.ir_analysis;
-- negative_controls_sequence
SELECT setval('${ohdsiSchema}.negative_controls_sequence', coalesce(max(id) + 1, 1)) FROM ${ohdsiSchema}.concept_set_negative_controls;
-- plp_sequence
SELECT setval('${ohdsiSchema}.plp_sequence', coalesce(max(plp_id) + 1, 1)) FROM ${ohdsiSchema}.plp;