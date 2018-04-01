CREATE SEQUENCE ${ohdsiSchema}.cca_sequence;
SELECT setval('${ohdsiSchema}.cca_sequence', coalesce(max(cca_id), 1)) FROM ${ohdsiSchema}.cca;

CREATE SEQUENCE ${ohdsiSchema}.cohort_definition_sequence;
SELECT setval('${ohdsiSchema}.cohort_definition_sequence', coalesce(max(id), 1)) FROM ${ohdsiSchema}.cohort_definition;

-- concept_set_sequence already exists - no need to create
SELECT setval('${ohdsiSchema}.concept_set_sequence', coalesce(max(concept_set_id), 1)) FROM ${ohdsiSchema}.concept_set;

-- concept_set_item_sequence already exists - no need to create
SELECT setval('${ohdsiSchema}.concept_set_item_sequence', coalesce(max(concept_set_item_id), 1)) FROM ${ohdsiSchema}.concept_set_item;

-- negative_controls_sequence already exists - no need to create
SELECT setval('${ohdsiSchema}.negative_controls_sequence', coalesce(max(id), 1)) FROM ${ohdsiSchema}.concept_set_negative_controls;

CREATE SEQUENCE ${ohdsiSchema}.feasibility_study_sequence;
SELECT setval('${ohdsiSchema}.feasibility_study_sequence', coalesce(max(id), 1)) FROM ${ohdsiSchema}.feasibility_study;

CREATE SEQUENCE ${ohdsiSchema}.ir_analysis_sequence;
SELECT setval('${ohdsiSchema}.ir_analysis_sequence', coalesce(max(id), 1)) FROM ${ohdsiSchema}.ir_analysis;

CREATE SEQUENCE ${ohdsiSchema}.plp_sequence;
SELECT setval('${ohdsiSchema}.plp_sequence', coalesce(max(plp_id), 1)) FROM ${ohdsiSchema}.plp;
