
/*********************************************************************/
/***** Create hierarchy lookup table for the treemap hierarchies *****/
/*********************************************************************/

IF OBJECT_ID('@results_schema.concept_hierarchy', 'U') IS NULL
CREATE TABLE @results_schema.concept_hierarchy
(
  concept_id             INT,
  concept_name           VARCHAR(400),
  treemap                VARCHAR(20),
  concept_hierarchy_type VARCHAR(20),
  level1_concept_name    VARCHAR(255),
  level2_concept_name    VARCHAR(255),
  level3_concept_name    VARCHAR(255),
  level4_concept_name    VARCHAR(255)
);
