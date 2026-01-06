ALTER SEQUENCE ${ohdsiSchema}.tags_seq RENAME TO tag_seq;

ALTER TABLE ${ohdsiSchema}.tags RENAME TO tag;
ALTER TABLE ${ohdsiSchema}.tag_groups RENAME TO tag_group;
ALTER TABLE ${ohdsiSchema}.concept_set_tags RENAME TO concept_set_tag;
ALTER TABLE ${ohdsiSchema}.cohort_tags RENAME TO cohort_tag;
ALTER TABLE ${ohdsiSchema}.cohort_characterization_tags RENAME TO cohort_characterization_tag;
ALTER TABLE ${ohdsiSchema}.ir_tags RENAME TO ir_tag;
ALTER TABLE ${ohdsiSchema}.pathway_tags RENAME TO pathway_tag;
