-- Mark V3.0.0.0 baseline as applied for existing databases (skips baseline script)

INSERT INTO ${ohdsiSchema}.schema_version
    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
SELECT
    (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM ${ohdsiSchema}.schema_version),
    '3.0.0.0',
    'webapi 3 0 baseline',
    'SQL',
    'V3.0.0.0__webapi_3_0_baseline.sql',
    0,
    'migration',
    NOW(),
    0,
    true
WHERE NOT EXISTS (
    SELECT 1 FROM ${ohdsiSchema}.schema_version WHERE version = '3.0.0.0'
);
