-- Alter Concept Set table

CREATE PROCEDURE ${ohdsiSchema}.rename_cs_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(255));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
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
            DECLARE @dupl_name varchar(255);
        
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
    
    ALTER TABLE ${ohdsiSchema}.concept_set ADD CONSTRAINT uq_cs_name UNIQUE (concept_set_name);
END;
GO

EXEC ${ohdsiSchema}.rename_cs_names;
GO

-- Alter Cohort Definition table

CREATE PROCEDURE ${ohdsiSchema}.rename_cd_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(255));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
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
            DECLARE @dupl_name varchar(255);
        
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

    ALTER TABLE ${ohdsiSchema}.cohort_definition ADD CONSTRAINT uq_cd_name UNIQUE (name);
END;
GO

EXEC ${ohdsiSchema}.rename_cd_names;
GO

-- Alter Cohort Characterization table

CREATE PROCEDURE ${ohdsiSchema}.rename_cc_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(255));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
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
            DECLARE @dupl_name varchar(255);
        
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
    
    ALTER TABLE ${ohdsiSchema}.cohort_characterization ADD CONSTRAINT uq_cc_name UNIQUE (name);
END;
GO

EXEC ${ohdsiSchema}.rename_cc_names;
GO

-- Alter Fe Analysis Table

CREATE PROCEDURE ${ohdsiSchema}.rename_fe_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(255));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
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
            DECLARE @dupl_name varchar(255);
        
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
    
    ALTER TABLE ${ohdsiSchema}.fe_analysis ADD CONSTRAINT uq_fe_name UNIQUE (name);
END;
GO

EXEC ${ohdsiSchema}.rename_fe_names;
GO

-- Alter Pathway Analysis Table

CREATE PROCEDURE ${ohdsiSchema}.rename_pw_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(255));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
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
            DECLARE @dupl_name varchar(255);
        
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

    ALTER TABLE ${ohdsiSchema}.pathway_analysis ADD CONSTRAINT uq_pw_name UNIQUE (name);
END;
GO

EXEC ${ohdsiSchema}.rename_pw_names;
GO

-- Alter IR Analysis Table

CREATE PROCEDURE ${ohdsiSchema}.rename_ir_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(255));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
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
            DECLARE @dupl_name varchar(255);
        
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
    
    ALTER TABLE ${ohdsiSchema}.ir_analysis ADD CONSTRAINT uq_ir_name UNIQUE (name);
END;
GO

EXEC ${ohdsiSchema}.rename_ir_names;
GO

-- Alter Estimation table

CREATE PROCEDURE ${ohdsiSchema}.rename_estimation_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(255));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
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
            DECLARE @dupl_name varchar(255);
        
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

    ALTER TABLE ${ohdsiSchema}.estimation ADD CONSTRAINT uq_es_name UNIQUE (name);
END;
GO

EXEC ${ohdsiSchema}.rename_estimation_names;
GO

-- Alter Prediction table

CREATE PROCEDURE ${ohdsiSchema}.rename_prediction_names AS
BEGIN
    DECLARE @duplicate_names TABLE(id int IDENTITY (1, 1), duplicate_name varchar(255));
    DECLARE @name_repeats TABLE (id int IDENTITY (1, 1), repeat_number int);
    DECLARE @amount_of_duplicate_names int;
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
            DECLARE @dupl_name varchar(255);
        
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
    
    ALTER TABLE ${ohdsiSchema}.prediction ADD CONSTRAINT uq_pd_name UNIQUE (name);
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