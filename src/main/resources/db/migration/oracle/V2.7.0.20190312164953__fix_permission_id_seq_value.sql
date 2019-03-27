-- Updates sec_permission_id_seq to maximum identity + 1
DECLARE maxid NUMBER(10);
  last_num NUMBER;
  inc_by NUMBER;
BEGIN
  SELECT (max(id) + 1) INTO maxid FROM ${ohdsiSchema}.SEC_PERMISSION;
  SELECT last_number, increment_by INTO last_num, inc_by FROM all_sequences WHERE sequence_owner = UPPER('${ohdsiSchema}')
    AND sequence_name = 'SEC_PERMISSION_ID_SEQ';
  EXECUTE IMMEDIATE 'ALTER SEQUENCE ${ohdsiSchema}.SEC_PERMISSION_ID_SEQ INCREMENT BY ' || (maxid - last_num);
  EXECUTE IMMEDIATE 'SELECT ${ohdsiSchema}.SEC_PERMISSION_ID_SEQ.NEXTVAL FROM DUAL';
  EXECUTE IMMEDIATE 'ALTER SEQUENCE ${ohdsiSchema}.SEC_PERMISSION_ID_SEQ INCREMENT BY ' || inc_by;
END;
/