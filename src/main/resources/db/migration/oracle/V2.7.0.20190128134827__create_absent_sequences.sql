-- analysis_execution
-- sequence already exists

-- cca
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.cca_execution_sequence START WITH ';
  SELECT nvl2(max(cca_id), max(cca_id) + 1, 1) INTO val FROM ${ohdsiSchema}.cca;
  EXECUTE IMMEDIATE stmt || val;
END;
/

-- heracles_visualization_data
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.heracles_vis_data_sequence START WITH ';
  SELECT nvl2(max(id), max(id) + 1, 1) INTO val FROM ${ohdsiSchema}.heracles_visualization_data;
  EXECUTE IMMEDIATE stmt || val;
END;
/