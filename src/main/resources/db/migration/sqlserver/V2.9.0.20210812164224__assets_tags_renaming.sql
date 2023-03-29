-- Rename sequence
EXEC sp_rename ${ohdsiSchema}.tags_seq, 'tag_seq';

-- Rename tables
EXEC sp_rename ${ohdsiSchema}.tags, 'tag', 'OBJECT';
EXEC sp_rename ${ohdsiSchema}.tag_groups, 'tag_group', 'OBJECT';
EXEC sp_rename ${ohdsiSchema}.concept_set_tags, 'concept_set_tag', 'OBJECT';
EXEC sp_rename ${ohdsiSchema}.cohort_tags, 'cohort_tag', 'OBJECT';
EXEC sp_rename ${ohdsiSchema}.cohort_characterization_tags, 'cohort_characterization_tag', 'OBJECT';
EXEC sp_rename ${ohdsiSchema}.ir_tags, 'ir_tag', 'OBJECT';
EXEC sp_rename ${ohdsiSchema}.pathway_tags, 'pathway_tag', 'OBJECT';
