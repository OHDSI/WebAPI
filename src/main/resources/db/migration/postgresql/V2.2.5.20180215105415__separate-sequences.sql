CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.cca_sequence;
SELECT setval('${ohdsiSchema}.cca_sequence', coalesce(max(cca_id), 1)) FROM ${ohdsiSchema}.cca;

CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.cohort_definition_sequence;
SELECT setval('${ohdsiSchema}.cohort_definition_sequence', coalesce(max(id), 1)) FROM ${ohdsiSchema}.cohort_definition;

CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.concept_set_sequence;
SELECT setval('${ohdsiSchema}.concept_set_sequence', coalesce(max(concept_set_id), 1)) FROM ${ohdsiSchema}.concept_set;

CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.concept_set_item_sequence;
SELECT setval('${ohdsiSchema}.concept_set_item_sequence', coalesce(max(concept_set_item_id), 1)) FROM ${ohdsiSchema}.concept_set_item;

CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.negative_controls_sequence;
SELECT setval('${ohdsiSchema}.concept_set_negative_controls_sequence', coalesce(max(id), 1)) FROM ${ohdsiSchema}.concept_set_negative_controls;

CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.feasibility_study_sequence;
SELECT setval('${ohdsiSchema}.feasibility_study_sequence', coalesce(max(id), 1)) FROM ${ohdsiSchema}.feasibility_study;

CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.ir_analysis_sequence;
SELECT setval('${ohdsiSchema}.ir_analysis_sequence', coalesce(max(id), 1)) FROM ${ohdsiSchema}.ir_analysis;

CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.plp_sequence;
SELECT setval('${ohdsiSchema}.plp_sequence', coalesce(max(plp_id), 1)) FROM ${ohdsiSchema}.plp;

