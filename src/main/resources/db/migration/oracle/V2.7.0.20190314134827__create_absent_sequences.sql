DROP SEQUENCE ${ohdsiSchema}.heracles_vis_data_sequence;
/
-- heracles_visualization_data
DECLARE
  val INTEGER;
  stmt VARCHAR2(255);
BEGIN
  stmt := 'CREATE SEQUENCE ${ohdsiSchema}.heracles_visualization_data_sequence START WITH ';
  SELECT nvl2(max(id), max(id) + 1, 1) INTO val FROM ${ohdsiSchema}.heracles_visualization_data;
  EXECUTE IMMEDIATE stmt || val;
END;
/