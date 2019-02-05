-- analysis_execution
-- sequence already exists

CREATE OR REPLACE TRIGGER ${ohdsiSchema}.analysis_execution_bir
  BEFORE INSERT ON ${ohdsiSchema}.analysis_execution
  FOR EACH ROW
  BEGIN
    SELECT analysis_execution_sequence.nextval INTO :new.id FROM dual;
  END;
/

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
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.cca_execution_bir
  BEFORE INSERT ON ${ohdsiSchema}.cca_execution
  FOR EACH ROW
  BEGIN
    SELECT cca_execution_sequence.nextval INTO :new.cca_id FROM dual;
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
CREATE OR REPLACE TRIGGER ${ohdsiSchema}.heracles_vis_data_bir
  BEFORE INSERT ON ${ohdsiSchema}.heracles_visualization_data
  FOR EACH ROW
  BEGIN
    SELECT heracles_vis_data_sequence.nextval INTO :new.id FROM dual;
  END;
/