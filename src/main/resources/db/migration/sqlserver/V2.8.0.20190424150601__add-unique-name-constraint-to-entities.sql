-- Alter Concept Set table

CREATE OR ALTER PROCEDURE ${ohdsiSchema}.rename_cs_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(100));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
    DECLARE @amount_of_constraints int;
    DECLARE @constraint_title varchar(100);
    DECLARE @schema_title varchar(100);
    DECLARE @k int;
    
    SET @k = (SELECT COUNT(*) FROM (SELECT concept_set_name FROM ${ohdsiSchema}.concept_set
                                    GROUP BY concept_set_name
                                    HAVING COUNT(*) > 1) AS temp);

    WHILE @k > 0
        BEGIN
            INSERT INTO @duplicate_names
            SELECT concept_set_name FROM ${ohdsiSchema}.concept_set
            GROUP BY concept_set_name
            HAVING COUNT(*) > 1;
        
            INSERT INTO @name_repeats
            SELECT COUNT(*) FROM ${ohdsiSchema}.concept_set
            GROUP BY concept_set_name
            HAVING COUNT(*) > 1;
        
            SET @amount_of_duplicate_names = (SELECT COUNT(*) FROM @duplicate_names);
        
            DECLARE @i int = 1;
            DECLARE @j int = 1;
            DECLARE @name_repeat int = 0;
            DECLARE @dupl_name varchar(100);
        
            WHILE @i <= coalesce(@amount_of_duplicate_names, 0)
            BEGIN
                SET @name_repeat = (SELECT repeat_number FROM @name_repeats WHERE id = @i);
                WHILE @j <= coalesce(@name_repeat, 0)
                BEGIN
                    SET @dupl_name = (SELECT duplicate_name FROM @duplicate_names WHERE id = @i);
        
                    UPDATE ${ohdsiSchema}.concept_set
                    SET concept_set_name = concept_set_name + ' (' + CAST(@j AS varchar(15)) + ')'
                    WHERE concept_set_id =
                          (SELECT TOP (1) concept_set_id FROM ${ohdsiSchema}.concept_set
                           WHERE concept_set_name = @dupl_name
                          );
                    SET @j = @j + 1;
                END;
                SET @i = @i + 1;
                SET @j = 1;
            END;
            
            DELETE FROM @duplicate_names WHERE id = id;
            DELETE FROM @name_repeats WHERE id = id;
            SET @k = (SELECT COUNT(*) FROM (SELECT concept_set_name FROM ${ohdsiSchema}.concept_set
                                            GROUP BY concept_set_name
                                            HAVING COUNT(*) > 1) AS temp);
        END;
    
    SET @constraint_title = 'uq_' + '${ohdsiSchema}' + '_' + 'cs_name';
    SET @schema_title = '${ohdsiSchema}';
    SET @amount_of_constraints = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                                  WHERE TABLE_SCHEMA = '${ohdsiSchema}'
                                    AND CONSTRAINT_NAME = @constraint_title AND TABLE_NAME = 'concept_set');
    IF @amount_of_constraints = 0
        BEGIN EXEC ('ALTER TABLE ' + @schema_title + '.concept_set ADD CONSTRAINT ' + @constraint_title +' UNIQUE (concept_set_name)'); END;
END;
GO

EXEC ${ohdsiSchema}.rename_cs_names;
GO

-- Alter Cohort Definition table

CREATE OR ALTER PROCEDURE ${ohdsiSchema}.rename_cd_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(100));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
    DECLARE @amount_of_constraints int;
    DECLARE @constraint_title varchar(100);
    DECLARE @schema_title varchar(100);
    DECLARE @k int;

    SET @k = (SELECT COUNT(*) FROM (SELECT name FROM ${ohdsiSchema}.cohort_definition
                                    GROUP BY name
                                    HAVING COUNT(*) > 1) AS temp);
                                    
    WHILE @k > 0
        BEGIN
            INSERT INTO @duplicate_names
            SELECT name FROM ${ohdsiSchema}.cohort_definition
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            INSERT INTO @name_repeats
            SELECT COUNT(*) FROM ${ohdsiSchema}.cohort_definition
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            SET @amount_of_duplicate_names = (SELECT COUNT(*) FROM @duplicate_names);
        
            DECLARE @i int = 1;
            DECLARE @j int = 1;
            DECLARE @name_repeat int = 0;
            DECLARE @dupl_name varchar(100);
        
            WHILE @i <= coalesce(@amount_of_duplicate_names, 0)
            BEGIN
                SET @name_repeat = (SELECT repeat_number FROM @name_repeats WHERE id = @i);
                WHILE @j <= coalesce(@name_repeat, 0)
                BEGIN
                    SET @dupl_name = (SELECT duplicate_name FROM @duplicate_names WHERE id = @i);
        
                    UPDATE ${ohdsiSchema}.cohort_definition
                    SET name = name + ' (' + CAST(@j AS varchar(15)) + ')'
                    WHERE id =
                          (SELECT TOP (1) id FROM ${ohdsiSchema}.cohort_definition
                           WHERE name = @dupl_name
                          );
                    SET @j = @j + 1;
                END;
                SET @i = @i + 1;
                SET @j = 1;
            END;
            
            DELETE FROM @duplicate_names WHERE id = id;
            DELETE FROM @name_repeats WHERE id = id;
            SET @k = (SELECT COUNT(*) FROM (SELECT name FROM ${ohdsiSchema}.cohort_definition
                                            GROUP BY name
                                            HAVING COUNT(*) > 1) AS temp);
        END;
    
    SET @constraint_title = 'uq_' + '${ohdsiSchema}' + '_' + 'cd_name';
    SET @schema_title = '${ohdsiSchema}';
    SET @amount_of_constraints = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                                  WHERE TABLE_SCHEMA = '${ohdsiSchema}'
                                    AND CONSTRAINT_NAME = @constraint_title AND TABLE_NAME = 'cohort_definition');
    IF @amount_of_constraints = 0
        BEGIN EXEC ('ALTER TABLE ' + @schema_title + '.cohort_definition ADD CONSTRAINT ' + @constraint_title +' UNIQUE (name)'); END;
END;
GO

EXEC ${ohdsiSchema}.rename_cd_names;
GO

-- Alter Cohort Characterization table

CREATE OR ALTER PROCEDURE ${ohdsiSchema}.rename_cc_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(100));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
    DECLARE @amount_of_constraints int;
    DECLARE @constraint_title varchar(100);
    DECLARE @schema_title varchar(100);
    DECLARE @k int;
    
    SET @k = (SELECT COUNT(*) FROM (SELECT name FROM ${ohdsiSchema}.cohort_characterization
                                    GROUP BY name
                                    HAVING COUNT(*) > 1) AS temp);
                                    
    WHILE @k > 0
        BEGIN
            INSERT INTO @duplicate_names
            SELECT name FROM ${ohdsiSchema}.cohort_characterization
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            INSERT INTO @name_repeats
            SELECT COUNT(*) FROM ${ohdsiSchema}.cohort_characterization
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            SET @amount_of_duplicate_names = (SELECT COUNT(*) FROM @duplicate_names);
        
            DECLARE @i int = 1;
            DECLARE @j int = 1;
            DECLARE @name_repeat int = 0;
            DECLARE @dupl_name varchar(100);
        
            WHILE @i <= coalesce(@amount_of_duplicate_names, 0)
            BEGIN
                SET @name_repeat = (SELECT repeat_number FROM @name_repeats WHERE id = @i);
                WHILE @j <= coalesce(@name_repeat, 0)
                BEGIN
                    SET @dupl_name = (SELECT duplicate_name FROM @duplicate_names WHERE id = @i);
        
                    UPDATE ${ohdsiSchema}.cohort_characterization
                    SET name = name + ' (' + CAST(@j AS varchar(15)) + ')'
                    WHERE id =
                          (SELECT TOP (1) id FROM ${ohdsiSchema}.cohort_characterization
                           WHERE name = @dupl_name
                          );
                    SET @j = @j + 1;
                END;
                SET @i = @i + 1;
                SET @j = 1;
            END;
        
            DELETE FROM @duplicate_names WHERE id = id;
            DELETE FROM @name_repeats WHERE id = id;
            SET @k = (SELECT COUNT(*) FROM (SELECT name FROM ${ohdsiSchema}.cohort_characterization
                                            GROUP BY name
                                            HAVING COUNT(*) > 1) AS temp);
        END;
    
    SET @constraint_title = 'uq_' + '${ohdsiSchema}' + '_' + 'cc_name';
    SET @schema_title = '${ohdsiSchema}';
    SET @amount_of_constraints = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                                  WHERE TABLE_SCHEMA = '${ohdsiSchema}'
                                    AND CONSTRAINT_NAME = @constraint_title AND TABLE_NAME = 'cohort_characterization');
    IF @amount_of_constraints = 0
        BEGIN EXEC ('ALTER TABLE ' + @schema_title + '.cohort_characterization ADD CONSTRAINT ' + @constraint_title +' UNIQUE (name)'); END;
END;
GO

EXEC ${ohdsiSchema}.rename_cc_names;
GO

-- Alter Fe Analysis Table

CREATE OR ALTER PROCEDURE ${ohdsiSchema}.rename_fe_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(100));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
    DECLARE @amount_of_constraints int;
    DECLARE @constraint_title varchar(100);
    DECLARE @schema_title varchar(100);
    DECLARE @k int;

    SET @k = (SELECT COUNT(*) FROM (SELECT name FROM ${ohdsiSchema}.fe_analysis
                                    GROUP BY name
                                    HAVING COUNT(*) > 1) AS temp);
                                    
    WHILE @k > 0
        BEGIN
            INSERT INTO @duplicate_names
            SELECT name FROM ${ohdsiSchema}.fe_analysis
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            INSERT INTO @name_repeats
            SELECT COUNT(*) FROM ${ohdsiSchema}.fe_analysis
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            SET @amount_of_duplicate_names = (SELECT COUNT(*) FROM @duplicate_names);
        
            DECLARE @i int = 1;
            DECLARE @j int = 1;
            DECLARE @name_repeat int = 0;
            DECLARE @dupl_name varchar(100);
        
            WHILE @i <= coalesce(@amount_of_duplicate_names, 0)
            BEGIN
                SET @name_repeat = (SELECT repeat_number FROM @name_repeats WHERE id = @i);
                WHILE @j <= coalesce(@name_repeat, 0)
                BEGIN
                    SET @dupl_name = (SELECT duplicate_name FROM @duplicate_names WHERE id = @i);
        
                    UPDATE ${ohdsiSchema}.fe_analysis
                    SET name = name + ' (' + CAST(@j AS varchar(15)) + ')'
                    WHERE id =
                          (SELECT TOP (1) id FROM ${ohdsiSchema}.fe_analysis
                           WHERE name = @dupl_name
                          );
                    SET @j = @j + 1;
                END;
                SET @i = @i + 1;
                SET @j = 1;
            END;
        
            DELETE FROM @duplicate_names WHERE id = id;
            DELETE FROM @name_repeats WHERE id = id;

            SET @k = (SELECT COUNT(*) FROM (SELECT name FROM ${ohdsiSchema}.fe_analysis
                                            GROUP BY name
                                            HAVING COUNT(*) > 1) AS temp);
        END;
    
    SET @constraint_title = 'uq_' + '${ohdsiSchema}' + '_' + 'fe_name';
    SET @schema_title = '${ohdsiSchema}';
    SET @amount_of_constraints = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                                  WHERE TABLE_SCHEMA = '${ohdsiSchema}'
                                    AND CONSTRAINT_NAME = @constraint_title AND TABLE_NAME = 'fe_analysis');
    IF @amount_of_constraints = 0
        BEGIN EXEC ('ALTER TABLE ' + @schema_title + '.fe_analysis ADD CONSTRAINT ' + @constraint_title +' UNIQUE (name)'); END;
END;
GO

EXEC ${ohdsiSchema}.rename_fe_names;
GO

-- Alter Pathway Analysis Table

CREATE OR ALTER PROCEDURE ${ohdsiSchema}.rename_pw_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(100));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
    DECLARE @amount_of_constraints int;
    DECLARE @constraint_title varchar(100);
    DECLARE @schema_title varchar(100);
    DECLARE @k int;

    SET @k = (SELECT COUNT(*) FROM (SELECT name FROM ${ohdsiSchema}.pathway_analysis
                                    GROUP BY name
                                    HAVING COUNT(*) > 1) AS temp);

    WHILE @k > 0
        BEGIN
            INSERT INTO @duplicate_names
            SELECT name FROM ${ohdsiSchema}.pathway_analysis
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            INSERT INTO @name_repeats
            SELECT COUNT(*) FROM ${ohdsiSchema}.pathway_analysis
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            SET @amount_of_duplicate_names = (SELECT COUNT(*) FROM @duplicate_names);
        
            DECLARE @i int = 1;
            DECLARE @j int = 1;
            DECLARE @name_repeat int = 0;
            DECLARE @dupl_name varchar(100);
        
            WHILE @i <= coalesce(@amount_of_duplicate_names, 0)
            BEGIN
                SET @name_repeat = (SELECT repeat_number FROM @name_repeats WHERE id = @i);
                WHILE @j <= coalesce(@name_repeat, 0)
                BEGIN
                    SET @dupl_name = (SELECT duplicate_name FROM @duplicate_names WHERE id = @i);
        
                    UPDATE ${ohdsiSchema}.pathway_analysis
                    SET name = name + ' (' + CAST(@j AS varchar(15)) + ')'
                    WHERE id =
                          (SELECT TOP (1) id FROM ${ohdsiSchema}.pathway_analysis
                           WHERE name = @dupl_name
                          );
                    SET @j = @j + 1;
                END;
                SET @i = @i + 1;
                SET @j = 1;
            END;
        
            DELETE FROM @duplicate_names WHERE id = id;
            DELETE FROM @name_repeats WHERE id = id;
            SET @k = (SELECT COUNT(*) FROM (SELECT name FROM ${ohdsiSchema}.pathway_analysis
                                            GROUP BY name
                                            HAVING COUNT(*) > 1) AS temp);
        END;
            
    SET @constraint_title = 'uq_' + '${ohdsiSchema}' + '_' + 'pw_name';
    SET @schema_title = '${ohdsiSchema}';
    SET @amount_of_constraints = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                                  WHERE TABLE_SCHEMA = '${ohdsiSchema}'
                                    AND CONSTRAINT_NAME = @constraint_title AND TABLE_NAME = 'pathway_analysis');
    IF @amount_of_constraints = 0
        BEGIN EXEC ('ALTER TABLE ' + @schema_title + '.pathway_analysis ADD CONSTRAINT ' + @constraint_title +' UNIQUE (name)'); END;
END;
GO

EXEC ${ohdsiSchema}.rename_pw_names;
GO

-- Alter IR Analysis Table

CREATE OR ALTER PROCEDURE ${ohdsiSchema}.rename_ir_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(100));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
    DECLARE @amount_of_constraints int;
    DECLARE @constraint_title varchar(100);
    DECLARE @schema_title varchar(100);
    DECLARE @k int;

    SET @k = (SELECT COUNT(*) FROM (SELECT name FROM ${ohdsiSchema}.ir_analysis
                                    GROUP BY name
                                    HAVING COUNT(*) > 1) AS temp);
                                    
    WHILE @k > 0
        BEGIN
            INSERT INTO @duplicate_names
            SELECT name FROM ${ohdsiSchema}.ir_analysis
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            INSERT INTO @name_repeats
            SELECT COUNT(*) FROM ${ohdsiSchema}.ir_analysis
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            SET @amount_of_duplicate_names = (SELECT COUNT(*) FROM @duplicate_names);
        
            DECLARE @i int = 1;
            DECLARE @j int = 1;
            DECLARE @name_repeat int = 0;
            DECLARE @dupl_name varchar(100);
        
            WHILE @i <= coalesce(@amount_of_duplicate_names, 0)
            BEGIN
                SET @name_repeat = (SELECT repeat_number FROM @name_repeats WHERE id = @i);
                WHILE @j <= coalesce(@name_repeat, 0)
                BEGIN
                    SET @dupl_name = (SELECT duplicate_name FROM @duplicate_names WHERE id = @i);
        
                    UPDATE ${ohdsiSchema}.ir_analysis
                    SET name = name + ' (' + CAST(@j AS varchar(15)) + ')'
                    WHERE id =
                          (SELECT TOP (1) id FROM ${ohdsiSchema}.ir_analysis
                           WHERE name = @dupl_name
                          );
                    SET @j = @j + 1;
                END;
                SET @i = @i + 1;
                SET @j = 1;
            END;
        
            DELETE FROM @duplicate_names WHERE id = id;
            DELETE FROM @name_repeats WHERE id = id;
            SET @k = (SELECT COUNT(*) FROM (SELECT name FROM ${ohdsiSchema}.ir_analysis
                                            GROUP BY name
                                            HAVING COUNT(*) > 1) AS temp);
        END;
    
    SET @constraint_title = 'uq_' + '${ohdsiSchema}' + '_' + 'ir_name';
    SET @schema_title = '${ohdsiSchema}';
    SET @amount_of_constraints = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                                  WHERE TABLE_SCHEMA = '${ohdsiSchema}'
                                    AND CONSTRAINT_NAME = @constraint_title AND TABLE_NAME = 'ir_analysis');
    IF @amount_of_constraints = 0
        BEGIN EXEC ('ALTER TABLE ' + @schema_title + '.ir_analysis ADD CONSTRAINT ' + @constraint_title +' UNIQUE (name)'); END;
END;
GO

EXEC ${ohdsiSchema}.rename_ir_names;
GO

-- Alter Estimation table

CREATE OR ALTER PROCEDURE ${ohdsiSchema}.rename_estimation_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(100));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
    DECLARE @amount_of_constraints int;
    DECLARE @constraint_title varchar(100);
    DECLARE @schema_title varchar(100);
    DECLARE @k int;

    SET @k = (SELECT COUNT(*) FROM (SELECT name FROM ${ohdsiSchema}.estimation
                                    GROUP BY name
                                    HAVING COUNT(*) > 1) AS temp);
                                    
    WHILE @k > 0
        BEGIN
            INSERT INTO @duplicate_names
            SELECT name FROM ${ohdsiSchema}.estimation
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            INSERT INTO @name_repeats
            SELECT COUNT(*) FROM ${ohdsiSchema}.estimation
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            SET @amount_of_duplicate_names = (SELECT COUNT(*) FROM @duplicate_names);
        
            DECLARE @i int = 1;
            DECLARE @j int = 1;
            DECLARE @name_repeat int = 0;
            DECLARE @dupl_name varchar(100);
        
            WHILE @i <= coalesce(@amount_of_duplicate_names, 0)
            BEGIN
                SET @name_repeat = (SELECT repeat_number FROM @name_repeats WHERE id = @i);
                WHILE @j <= coalesce(@name_repeat, 0)
                BEGIN
                    SET @dupl_name = (SELECT duplicate_name FROM @duplicate_names WHERE id = @i);
        
                    UPDATE ${ohdsiSchema}.estimation
                    SET name = name + ' (' + CAST(@j AS varchar(15)) + ')'
                    WHERE estimation_id =
                          (SELECT TOP (1) estimation_id FROM ${ohdsiSchema}.estimation
                           WHERE name = @dupl_name
                          );
                    SET @j = @j + 1;
                END;
                SET @i = @i + 1;
                SET @j = 1;
            END;
        
            SET @k = (SELECT COUNT(*) FROM (SELECT name FROM ${ohdsiSchema}.estimation
                                            GROUP BY name
                                            HAVING COUNT(*) > 1) AS temp);
            DELETE FROM @duplicate_names WHERE id = id;
            DELETE FROM @name_repeats WHERE id = id;
        END;

    
    SET @constraint_title = 'uq_' + '${ohdsiSchema}' + '_' + 'es_name';
    SET @schema_title = '${ohdsiSchema}';
    SET @amount_of_constraints = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                                  WHERE TABLE_SCHEMA = '${ohdsiSchema}'
                                    AND CONSTRAINT_NAME = @constraint_title AND TABLE_NAME = 'estimation');
    IF @amount_of_constraints = 0
        BEGIN EXEC ('ALTER TABLE ' + @schema_title + '.estimation ADD CONSTRAINT ' + @constraint_title +' UNIQUE (name)'); END;
END;
GO

EXEC ${ohdsiSchema}.rename_estimation_names;
GO

-- Alter Prediction table

CREATE OR ALTER PROCEDURE ${ohdsiSchema}.rename_prediction_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(100));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
    DECLARE @amount_of_constraints int;
    DECLARE @constraint_title varchar(100);
    DECLARE @schema_title varchar(100);
    DECLARE @k int;

    SET @k = (SELECT COUNT(*) FROM (SELECT name FROM ${ohdsiSchema}.prediction
                                    GROUP BY name
                                    HAVING COUNT(*) > 1) AS temp);
    
    WHILE @k > 0
        BEGIN
            INSERT INTO @duplicate_names
            SELECT name FROM ${ohdsiSchema}.prediction
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            INSERT INTO @name_repeats
            SELECT COUNT(*) FROM ${ohdsiSchema}.prediction
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            SET @amount_of_duplicate_names = (SELECT COUNT(*) FROM @duplicate_names);
        
            DECLARE @i int = 1;
            DECLARE @j int = 1;
            DECLARE @name_repeat int = 0;
            DECLARE @dupl_name varchar(100);
        
            WHILE @i <= coalesce(@amount_of_duplicate_names, 0)
            BEGIN
                SET @name_repeat = (SELECT repeat_number FROM @name_repeats WHERE id = @i);
                WHILE @j <= coalesce(@name_repeat, 0)
                BEGIN
                    SET @dupl_name = (SELECT duplicate_name FROM @duplicate_names WHERE id = @i);
        
                    UPDATE ${ohdsiSchema}.prediction
                    SET name = name + ' (' + CAST(@j AS varchar(15)) + ')'
                    WHERE prediction_id =
                          (SELECT TOP (1) prediction_id FROM ${ohdsiSchema}.prediction
                           WHERE name = @dupl_name
                          );
                    SET @j = @j + 1;
                END;
                SET @i = @i + 1;
                SET @j = 1;
            END;
            
            SET @k = (SELECT COUNT(*) FROM (SELECT name FROM ${ohdsiSchema}.prediction
                                            GROUP BY name
                                            HAVING COUNT(*) > 1) AS temp);
            DELETE FROM @duplicate_names WHERE id = id;
            DELETE FROM @name_repeats WHERE id = id;
        END;
    
    SET @constraint_title = 'uq_' + '${ohdsiSchema}' + '_' + 'pd_name';
    SET @schema_title = '${ohdsiSchema}';
    SET @amount_of_constraints = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                                  WHERE TABLE_SCHEMA = '${ohdsiSchema}'
                                    AND CONSTRAINT_NAME = @constraint_title AND TABLE_NAME = 'prediction');
    IF @amount_of_constraints = 0
        BEGIN EXEC ('ALTER TABLE ' + @schema_title + '.prediction ADD CONSTRAINT ' + @constraint_title +' UNIQUE (name)'); END;
END;
GO

EXEC ${ohdsiSchema}.rename_prediction_names;
GO

DROP PROCEDURE ${ohdsiSchema}.rename_cs_names;
DROP PROCEDURE ${ohdsiSchema}.rename_cd_names;
DROP PROCEDURE ${ohdsiSchema}.rename_cc_names;
DROP PROCEDURE ${ohdsiSchema}.rename_fe_names;
DROP PROCEDURE ${ohdsiSchema}.rename_pw_names;
DROP PROCEDURE ${ohdsiSchema}.rename_ir_names;
DROP PROCEDURE ${ohdsiSchema}.rename_estimation_names;
DROP PROCEDURE ${ohdsiSchema}.rename_prediction_names;