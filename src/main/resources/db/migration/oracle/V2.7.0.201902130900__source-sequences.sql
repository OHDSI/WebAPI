-- Source sequence
DECLARE 
	val INTEGER;
	stmt VARCHAR2(255);
BEGIN
	stmt := 'CREATE SEQUENCE ${ohdsiSchema}.source_sequence START WITH ';
	SELECT nvl2(max(source_id), max(source_id) + 1, 1) INTO val FROM ${ohdsiSchema}.source;
	EXECUTE IMMEDIATE stmt || val;
END;
/

-- Source_daimon sequence
DECLARE 
	val INTEGER;
	stmt VARCHAR2(255);
BEGIN
	stmt := 'CREATE SEQUENCE ${ohdsiSchema}.source_daimon_sequence START WITH ';
	SELECT nvl2(max(source_daimon_id), max(source_daimon_id) + 1, 1) INTO val FROM ${ohdsiSchema}.source_daimon;
	EXECUTE IMMEDIATE stmt || val;
END;
/