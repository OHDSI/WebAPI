CREATE PROCEDURE ${ohdsiSchema}.remove_identity_from_column (@tableName		VARCHAR (200),
                                                           @columnName		VARCHAR (200),
                                                           @constraintName	VARCHAR (200),
                                                           @deleteDefaultPK BIT = NULL,
                                                           @last			INT out)
AS
  BEGIN
      DECLARE @sqlcmd NVARCHAR(max),
              @columnFullName VARCHAR(200),
              @oldColumnName VARCHAR(200),
              @parmDefinition NVARCHAR(200);

      BEGIN TRAN

      BEGIN try
          SET @columnFullName = @tableName + '.' + @columnName;
          SET @oldColumnName = 'Old' + @columnName;

-- rename column with primary key to column with prefix 'Old'
          EXEC Sp_rename
            @columnFullName,
            @oldColumnName;

-- add new column with primary key with default set to null
          SET @sqlcmd = 'ALTER TABLE ' + @tableName + ' ADD '
                        + @columnName + ' int NULL;';

          EXEC(@sqlcmd);

-- copy values from column with prefix 'Old' to new column with primary key
          SET @sqlcmd = 'UPDATE ' + @tableName + ' SET ' + @columnName + '='
                        + @oldColumnName;

          EXEC(@sqlcmd);

-- disable check constraints
          SET @sqlcmd = 'ALTER TABLE ' + @tableName
                        + ' NOCHECK CONSTRAINT ALL';

          EXEC(@sqlcmd);

-- drop primary key if constraint name is undefined
          IF @deleteDefaultPK IS NOT NULL AND @deleteDefaultPK=1
            BEGIN
            DECLARE @pksql NVARCHAR(MAX),
                    @pkname NVARCHAR(200);

            SET @pksql = 'SELECT   @name = name
                                                     FROM     sysobjects
                                                     WHERE    xtype = ''PK'' AND parent_obj = OBJECT_ID(''' + @tableName + ''')';
            SET @parmDefinition = '@name NVARCHAR(200) OUTPUT';
            EXECUTE Sp_executesql
              @Query = @pksql,
              @Params = @parmDefinition,
              @name=@pkname out;

            SET @sqlcmd = 'ALTER TABLE ' + @tableName + ' DROP CONSTRAINT |ConstraintName| ';
            SET @sqlcmd = REPLACE(@sqlcmd, '|ConstraintName|', @pkname);
            EXEC (@sqlcmd);
		      END
		      ELSE
-- drop constraint on column with primary key if there is one
          IF @constraintName IS NOT NULL
             AND @constraintName != ''
            BEGIN
                SET @sqlcmd = 'ALTER TABLE ' + @tableName
                              + ' DROP CONSTRAINT ' + @constraintName;

                EXEC(@sqlcmd);
            END;

-- drop column with prefix 'Old'
          SET @sqlcmd = 'ALTER TABLE ' + @tableName + ' DROP COLUMN '
                        + @oldColumnName;

          EXEC(@sqlcmd);

-- set default value to not null
          SET @sqlcmd = 'ALTER TABLE ' + @tableName + ' ALTER COLUMN '
                        + @columnName + ' INTEGER NOT NULL';

          EXEC(@sqlcmd);

-- create new constraint on column with primary key if there was one
          IF @constraintName IS NOT NULL
             AND @constraintName != ''
            BEGIN
                SET @sqlcmd = 'ALTER TABLE ' + @tableName
                              + ' ADD CONSTRAINT ' + @constraintName
                              + ' PRIMARY KEY (' + @columnName + ')';

                EXEC(@sqlcmd);
            END;

-- enable check constraints
          SET @sqlcmd = 'ALTER TABLE ' + @tableName
                        + ' CHECK CONSTRAINT ALL';

          EXEC(@sqlcmd);

          COMMIT TRAN;
      END try

      BEGIN catch
          ROLLBACK TRAN

          SELECT Error_message ()
      END catch

-- get the max value from column with primary key (will be used during creation of sequence)
      SET @sqlcmd = 'SELECT @lastOut = MAX(' + @columnName
                    + ') from ' + @tableName;
      SET @parmDefinition = '@lastOut int OUTPUT';

      EXECUTE Sp_executesql
        @Query = @sqlcmd,
        @Params = @parmDefinition,
        @lastOut=@last out;

      SELECT @last
  END;

GO

CREATE PROCEDURE ${ohdsiSchema}.remove_identity_from_tables
AS
BEGIN
DECLARE	@last int,
        @max int,
		    @sql NVARCHAR(MAX),
        @parmDefinition NVARCHAR(200);

SET @max = 0;
-- remove identity from table analysis_execution
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[analysis_execution]',
		@columnName = N'id',
		@constraintName = N'pk_analysis_exec',
		@last = @last out;

-- remove identity from table BATCH_JOB_EXECUTION_SEQ
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[BATCH_JOB_EXECUTION_SEQ]',
		@columnName = N'ID',
		@constraintName = NULL,
		@last = @last out;

-- remove identity from table BATCH_STEP_EXECUTION_SEQ
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[BATCH_STEP_EXECUTION_SEQ]',
		@columnName = N'ID',
		@constraintName = NULL,
		@last = @last out;

-- remove identity from table cca_execution
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[cca_execution]',
		@columnName = N'cca_execution_id',
		@constraintName = N'[cca_execution_pk]',
		@last = @last out;
if @max < @last set @max = @last;

-- remove identity from table cohort_features
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[cohort_features]',
		@columnName = N'id',
		@constraintName = N'[pk_cohort_features]',
		@last = @last out;

-- remove identity from table cohort_features_analysis_ref
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[cohort_features_analysis_ref]',
		@columnName = N'id',
		@constraintName = N'[pk_coh_features_an_ref]',
		@last = @last out;

-- remove identity from table cohort_features_dist
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[cohort_features_dist]',
		@columnName = N'id',
		@constraintName = N'[pk_coh_features_dist]',
		@last = @last out;

-- remove identity from table cohort_features_ref
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[cohort_features_ref]',
		@columnName = N'id',
		@constraintName = N'[pk_coh_features_ref]',
		@last = @last out;

-- remove identity from table COHORT_STUDY
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[COHORT_STUDY]',
		@columnName = N'cohort_study_id',
		@constraintName = N'[PK_COHORT_STUDY]',
		@last = @last out;

-- remove identity from table CONCEPT_OF_INTEREST
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[CONCEPT_OF_INTEREST]',
		@columnName = N'id',
		@constraintName = N'[PK_CONCEPT_OF_INTEREST]',
		@last = @last out;

-- remove identity from table EXAMPLEAPP_WIDGET
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[EXAMPLEAPP_WIDGET]',
		@columnName = N'ID',
		@constraintName = N'[PK_EXAMPLEAPP_WIDGET]',
		@last = @last out;
if @max < @last set @max = @last;

-- remove identity from table HERACLES_HEEL_results
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[HERACLES_HEEL_results]',
		@columnName = N'id',
		@constraintName = N'[pk_heracles_heel_res]',
		@last = @last out;

-- remove identity from table heracles_results
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[heracles_results]',
		@columnName = N'id',
		@constraintName = N'[pk_heracles_res]',
		@last = @last out;

-- remove identity from table heracles_results_dist
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[heracles_results_dist]',
		@columnName = N'id',
		@constraintName = N'[pk_heracles_res_dist]',
		@last = @last out;

-- remove identity from table HERACLES_VISUALIZATION_DATA
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[HERACLES_VISUALIZATION_DATA]',
		@columnName = N'id',
		@constraintName = N'[PK_heracles_viz_data]',
		@last = @last out;
if @max < @last set @max = @last;

-- remove identity from table ir_analysis_dist
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[ir_analysis_dist]',
		@columnName = N'id',
		@constraintName = N'[pk_ir_analysis_dist]',
		@last = @last out;

-- remove identity from table ir_analysis_result
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[ir_analysis_result]',
		@columnName = N'id',
		@constraintName = N'[pk_ir_analysis_res]',
		@last = @last out;

-- remove identity from table ir_analysis_strata_stats
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[ir_analysis_strata_stats]',
		@columnName = N'id',
		@constraintName = N'[pk_ir_an_strata_stats]',
		@last = @last out;

-- remove identity from table ir_strata
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[ir_strata]',
		@columnName = N'id',
		@constraintName = N'[pk_ir_strata]',
		@last = @last out;

-- remove identity from table penelope_laertes_uni_pivot
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[penelope_laertes_uni_pivot]',
		@columnName = N'id',
		@constraintName = N'[pk_penelope_lae_uni_piv]',
		@last = @last out;

-- remove identity from table source
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[source]',
		@columnName = N'SOURCE_ID',
		@constraintName = N'pk_source',
		@last = @last out;
if @max < @last set @max = @last;

-- remove identity from table source_daimon
EXEC	${ohdsiSchema}.[remove_identity_from_column]
		@tableName = N'${ohdsiSchema}.[source_daimon]',
		@columnName = N'source_daimon_id',
		@constraintName = N'pk_source_daimon',
		@deleteDefaultPK = 1,
		@last = @last out;
if @max < @last set @max = @last;

-- get the max value from column with primary key for table input_files
SET @sql = 'SELECT @lastOut = MAX(id) from ${ohdsiSchema}.[input_files]';
SET @parmDefinition = '@lastOut int OUTPUT';

EXECUTE Sp_executesql
  @Query = @sql,
  @Params = @parmDefinition,
  @lastOut=@last out;
if @max < @last set @max = @last;

-- get the max value from column with primary key for table output_files
SET @sql = 'SELECT @lastOut = MAX(id) from ${ohdsiSchema}.[output_files]';
SET @parmDefinition = '@lastOut int OUTPUT';

EXECUTE Sp_executesql
  @Query = @sql,
  @Params = @parmDefinition,
  @lastOut=@last out;
if @max < @last set @max = @last;

set @max = @max + 1;
-- create new sequence
SET @sql = concat('CREATE SEQUENCE ${ohdsiSchema}.hibernate_sequence START WITH ', @max);
EXEC Sp_executesql @sql
END;