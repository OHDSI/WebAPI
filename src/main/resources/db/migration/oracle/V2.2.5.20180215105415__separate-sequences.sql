-- cca
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.cca_sequence START WITH ';
  SELECT nvl(max(cca_id), 1) INTO val FROM ${ohdsiSchema}.cca;
  EXECUTE IMMEDIATE stmt || val;
END;
/

-- cohort_definition
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.cohort_definition_sequence START WITH ';
  SELECT nvl(max(id), 1) INTO val FROM ${ohdsiSchema}.cohort_definition;
  EXECUTE IMMEDIATE stmt || val;
END;
/

-- concept_set
DECLARE
  diff INTEGER;
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'ALTER SEQUENCE ${ohdsiSchema}.concept_set_sequence INCREMENT BY ';
  BEGIN
    SELECT ${ohdsiSchema}.concept_set_sequence.NEXTVAL INTO val FROM ${ohdsiSchema}.concept_set;
    EXCEPTION WHEN NO_DATA_FOUND THEN
      val := 1;
  END;
  SELECT (nvl(max(concept_set_id), 1) - val) INTO diff FROM ${ohdsiSchema}.concept_set;
  IF diff > 0 THEN
    EXECUTE IMMEDIATE stmt || val;
    SELECT ${ohdsiSchema}.concept_set_sequence.NEXTVAL INTO val FROM ${ohdsiSchema}.concept_set;
    EXECUTE IMMEDIATE stmt || 1;
  END IF;
END;
/

-- concept_set_item
DECLARE
  diff INTEGER;
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'ALTER SEQUENCE ${ohdsiSchema}.concept_set_item_sequence INCREMENT BY ';
  BEGIN
    SELECT ${ohdsiSchema}.concept_set_item_sequence.NEXTVAL INTO val FROM ${ohdsiSchema}.concept_set_item;
    EXCEPTION WHEN NO_DATA_FOUND THEN
      val := 1;
  END;
  SELECT (nvl(max(concept_set_id), 1) - val) INTO diff FROM ${ohdsiSchema}.concept_set_item;
  IF diff > 0 THEN
    EXECUTE IMMEDIATE stmt || val;
    SELECT ${ohdsiSchema}.concept_set_item_sequence.NEXTVAL INTO val FROM ${ohdsiSchema}.concept_set_item;
    EXECUTE IMMEDIATE stmt || 1;
  END IF;
END;
/

-- concept_set_negative_controls
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.negative_controls_sequence START WITH ';
  SELECT nvl(max(id), 1) INTO val FROM ${ohdsiSchema}.concept_set_negative_controls;
  EXECUTE IMMEDIATE stmt || val;
END;
/

-- feasibility_study
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.feasibility_study_sequence START WITH ';
  SELECT nvl(max(id), 1) INTO val FROM ${ohdsiSchema}.feasibility_study;
  EXECUTE IMMEDIATE stmt || val;
END;
/

-- ir_analysis
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.ir_analysis_sequence START WITH ';
  SELECT nvl(max(id), 1) INTO val FROM ${ohdsiSchema}.ir_analysis;
  EXECUTE IMMEDIATE stmt || val;
END;
/

-- plp
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.plp_sequence START WITH ';
  SELECT nvl(max(plp_id), 1) INTO val FROM ${ohdsiSchema}.plp;
  EXECUTE IMMEDIATE stmt || val;
END;
/