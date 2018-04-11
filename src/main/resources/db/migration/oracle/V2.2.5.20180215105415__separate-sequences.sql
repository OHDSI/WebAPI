-- cca
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.cca_sequence START WITH ';
  SELECT nvl2(max(cca_id), max(cca_id) + 1, 1) INTO val FROM ${ohdsiSchema}.cca;
  EXECUTE IMMEDIATE stmt || val;
END;
/

-- cohort_definition
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.cohort_definition_sequence START WITH ';
  SELECT nvl2(max(id), max(id) + 1, 1) INTO val FROM ${ohdsiSchema}.cohort_definition;
  EXECUTE IMMEDIATE stmt || val;
END;
/

DROP SEQUENCE ${ohdsiSchema}.CONCEPT_SET_SEQUENCE;

-- concept_set
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.concept_set_sequence START WITH ';
  SELECT nvl2(max(concept_set_id), max(concept_set_id) + 1, 1) INTO val FROM ${ohdsiSchema}.concept_set;
  EXECUTE IMMEDIATE stmt || val;
END;
/

DROP SEQUENCE ${ohdsiSchema}.CONCEPT_SET_ITEM_SEQUENCE;

-- concept_set_item
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.concept_set_item_sequence START WITH ';
  SELECT nvl2(max(concept_set_item_id), max(concept_set_item_id) + 1, 1) INTO val FROM ${ohdsiSchema}.concept_set_item;
  EXECUTE IMMEDIATE stmt || val;
END;
/

-- concept_set_negative_controls
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.negative_controls_sequence START WITH ';
  SELECT nvl2(max(id), max(id) + 1, 1) INTO val FROM ${ohdsiSchema}.concept_set_negative_controls;
  EXECUTE IMMEDIATE stmt || val;
END;
/

-- feasibility_study
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.feasibility_study_sequence START WITH ';
  SELECT nvl2(max(id), max(id) + 1, 1) INTO val FROM ${ohdsiSchema}.feasibility_study;
  EXECUTE IMMEDIATE stmt || val;
END;
/

-- ir_analysis
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.ir_analysis_sequence START WITH ';
  SELECT nvl2(max(id), max(id) + 1, 1) INTO val FROM ${ohdsiSchema}.ir_analysis;
  EXECUTE IMMEDIATE stmt || val;
END;
/

-- plp
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.plp_sequence START WITH ';
  SELECT nvl2(max(plp_id), max(plp_id) + 1, 1) INTO val FROM ${ohdsiSchema}.plp;
  EXECUTE IMMEDIATE stmt || val;
END;
/