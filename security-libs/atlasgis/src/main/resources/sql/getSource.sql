SELECT
	s.source_key as key,
	s.source_name as name,
	UPPER(s.source_dialect) as dialect,
	s.source_connection as connection_string,
  s.username,
	s.password,
	s.krb_auth_method,
	s.keytab_name,
	s.krb_keytab,
	s.krb_admin_server,
	cdm_daimon.table_qualifier as cdm_schema,
	results_daimon.table_qualifier as results_schema,
	temp_daimon.table_qualifier as temp_schema
FROM @webapiSchema.source s
LEFT JOIN @webapiSchema.source_daimon cdm_daimon on s.source_id = cdm_daimon.source_id AND cdm_daimon.daimon_type = 0
LEFT JOIN @webapiSchema.source_daimon results_daimon on s.source_id = results_daimon.source_id AND results_daimon.daimon_type = 2
LEFT JOIN @webapiSchema.source_daimon temp_daimon on s.source_id = temp_daimon.source_id AND temp_daimon.daimon_type = 5
WHERE s.deleted_date IS NULL AND s.source_key = '@sourceKey'
