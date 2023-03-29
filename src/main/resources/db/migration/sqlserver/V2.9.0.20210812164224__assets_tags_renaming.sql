-- Rename sequence
EXEC sp_rename 'webapi.tags_seq', 'tag_seq';

-- Rename tables
EXEC sp_rename 'webapi.tags', 'tag', 'OBJECT';
EXEC sp_rename 'webapi.tag_groups', 'tag_group', 'OBJECT';
EXEC sp_rename 'webapi.concept_set_tags', 'concept_set_tag', 'OBJECT';
EXEC sp_rename 'webapi.cohort_tags', 'cohort_tag', 'OBJECT';
EXEC sp_rename 'webapi.cohort_characterization_tags', 'cohort_characterization_tag', 'OBJECT';
EXEC sp_rename 'webapi.ir_tags', 'ir_tag', 'OBJECT';
EXEC sp_rename 'webapi.pathway_tags', 'pathway_tag', 'OBJECT';
