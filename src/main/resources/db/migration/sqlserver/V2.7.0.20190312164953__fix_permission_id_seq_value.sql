-- Updates sec_permission_id_seq to maximum identity + 1
DECLARE @maxid INTEGER;
SELECT @maxid = (SELECT max(id) + 1 FROM ${ohdsiSchema}.sec_permission);
DECLARE @statement NVARCHAR(256);

SET @statement = N'ALTER SEQUENCE ${ohdsiSchema}.sec_permission_id_seq RESTART WITH ' + CAST(@maxid AS VARCHAR(12)) + ';';

EXEC sp_executesql @statement;