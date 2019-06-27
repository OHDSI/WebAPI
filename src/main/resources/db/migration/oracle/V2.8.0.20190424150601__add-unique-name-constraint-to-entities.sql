-- Alter Concept Set table

CREATE OR REPLACE PROCEDURE ${ohdsiSchema}.rename_cs_names AS
    TYPE NumberArray IS TABLE OF NUMBER;
    TYPE CharArray IS TABLE OF VARCHAR(255);
    duplicate_names CharArray;
    name_repeats NumberArray;
    amount_of_duplicate_names INT;
    amount_of_constraints INT;
    constraint_title VARCHAR(255);
    schema_title VARCHAR(255);
    all_duplicates INT;

BEGIN
    SELECT COUNT(*) INTO all_duplicates FROM (SELECT concept_set_name FROM ${ohdsiSchema}.concept_set
                                                GROUP BY concept_set_name
                                                HAVING COUNT(*) > 1);

    FOR k IN 0 .. coalesce(all_duplicates, 0)
        LOOP
            SELECT concept_set_name BULK COLLECT INTO duplicate_names
            FROM ${ohdsiSchema}.concept_set
            GROUP BY concept_set_name
            HAVING COUNT(*) > 1;
        
            SELECT COUNT(*) BULK COLLECT INTO name_repeats
            FROM ${ohdsiSchema}.concept_set
            GROUP BY concept_set_name
            HAVING COUNT(*) > 1;
        
            amount_of_duplicate_names := duplicate_names.COUNT;
        
            FOR i IN 1 .. coalesce(amount_of_duplicate_names, 0)
                LOOP
                    FOR j IN 1 .. coalesce(name_repeats(i), 0)
                        LOOP
                            UPDATE ${ohdsiSchema}.concept_set
                            SET concept_set_name = concept_set_name || ' (' || j || ')'
                            WHERE concept_set_id = (SELECT concept_set_id
                                                    FROM ${ohdsiSchema}.concept_set
                                                    WHERE concept_set_name = duplicate_names(i)
                                                      AND ROWNUM = 1);
                        END LOOP;
                END LOOP;
            duplicate_names.DELETE();
            name_repeats.DELETE();
        END LOOP;
    

    constraint_title := 'uq_cs_name';
    schema_title := '${ohdsiSchema}';

    SELECT COUNT(*) INTO amount_of_constraints
    FROM ALL_CONSTRAINTS
    WHERE OWNER = '${ohdsiSchema}'
    AND CONSTRAINT_NAME = constraint_title
    AND TABLE_NAME = 'CONCEPT_SET';

    IF amount_of_constraints = 0 THEN
        EXECUTE IMMEDIATE ('ALTER TABLE ' || schema_title || '.CONCEPT_SET ADD CONSTRAINT ' || constraint_title ||' UNIQUE (concept_set_name)');
    END IF;
END;
/

begin
    rename_cs_names;
end;
/


-- Alter Cohort Definition table

CREATE OR REPLACE PROCEDURE ${ohdsiSchema}.rename_cd_names AS
    TYPE NumberArray IS TABLE OF NUMBER;
    TYPE CharArray IS TABLE OF VARCHAR(255);
    duplicate_names CharArray;
    name_repeats NumberArray;
    amount_of_duplicate_names INT;
    amount_of_constraints INT;
    constraint_title VARCHAR(255);
    schema_title VARCHAR(255);
    all_duplicates INT;

BEGIN
    SELECT COUNT(*) INTO all_duplicates FROM (SELECT name FROM ${ohdsiSchema}.cohort_definition
                                                GROUP BY name
                                                HAVING COUNT(*) > 1);

    FOR k IN 0 .. coalesce(all_duplicates, 0)
        LOOP
            SELECT name BULK COLLECT INTO duplicate_names
            FROM ${ohdsiSchema}.cohort_definition
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            SELECT COUNT(*) BULK COLLECT INTO name_repeats
            FROM ${ohdsiSchema}.cohort_definition
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            amount_of_duplicate_names := duplicate_names.COUNT;
        
            FOR i IN 1 .. coalesce(amount_of_duplicate_names, 0)
                LOOP
                    FOR j IN 1 .. coalesce(name_repeats(i), 0)
                        LOOP
                            UPDATE ${ohdsiSchema}.cohort_definition
                            SET name = name || ' (' || j || ')'
                            WHERE id = (SELECT id
                                            FROM ${ohdsiSchema}.cohort_definition
                                            WHERE name = duplicate_names(i)
                                              AND ROWNUM = 1);
                        END LOOP;
                END LOOP;
            duplicate_names.DELETE();
            name_repeats.DELETE();
        END LOOP;    

    constraint_title := 'uq_cd_name';
    schema_title := '${ohdsiSchema}';

    SELECT COUNT(*) INTO amount_of_constraints
    FROM ALL_CONSTRAINTS
    WHERE OWNER = '${ohdsiSchema}'
    AND CONSTRAINT_NAME = constraint_title
    AND TABLE_NAME = 'COHORT_DEFINITION';

    IF amount_of_constraints = 0 THEN
        EXECUTE IMMEDIATE ('ALTER TABLE ' || schema_title || '.COHORT_DEFINITION ADD CONSTRAINT ' || constraint_title ||' UNIQUE (name)');
    END IF;
END;
/

begin
    rename_cd_names;
end;
/


-- Alter Cohort Characterization table

CREATE OR REPLACE PROCEDURE ${ohdsiSchema}.rename_cc_names AS
    TYPE NumberArray IS TABLE OF NUMBER;
    TYPE CharArray IS TABLE OF VARCHAR(255);
    duplicate_names CharArray;
    name_repeats NumberArray;
    amount_of_duplicate_names INT;
    amount_of_constraints INT;
    constraint_title VARCHAR(255);
    schema_title VARCHAR(255);
    all_duplicates INT;

BEGIN
    SELECT COUNT(*) INTO all_duplicates FROM (SELECT name FROM ${ohdsiSchema}.cohort_characterization
                                                GROUP BY name
                                                HAVING COUNT(*) > 1);

    FOR k IN 0 .. coalesce(all_duplicates, 0)
        LOOP
            SELECT name BULK COLLECT INTO duplicate_names
            FROM ${ohdsiSchema}.cohort_characterization
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            SELECT COUNT(*) BULK COLLECT INTO name_repeats
            FROM ${ohdsiSchema}.cohort_characterization
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            amount_of_duplicate_names := duplicate_names.COUNT;
        
            FOR i IN 1 .. coalesce(amount_of_duplicate_names, 0)
                LOOP
                    FOR j IN 1 .. coalesce(name_repeats(i), 0)
                        LOOP
                            UPDATE ${ohdsiSchema}.cohort_characterization
                            SET name = name || ' (' || j || ')'
                            WHERE id = (SELECT id
                                            FROM ${ohdsiSchema}.cohort_characterization
                                            WHERE name = duplicate_names(i)
                                              AND ROWNUM = 1);
                        END LOOP;
                END LOOP;        
            duplicate_names.DELETE();
            name_repeats.DELETE();
        END LOOP;    

    constraint_title := 'uq_cc_name';
    schema_title := '${ohdsiSchema}';

    SELECT COUNT(*) INTO amount_of_constraints
    FROM ALL_CONSTRAINTS
    WHERE OWNER = '${ohdsiSchema}'
    AND CONSTRAINT_NAME = constraint_title
    AND TABLE_NAME = 'COHORT_CHARACTERIZATION';

    IF amount_of_constraints = 0 THEN
        EXECUTE IMMEDIATE ('ALTER TABLE ' || schema_title || '.COHORT_CHARACTERIZATION ADD CONSTRAINT ' || constraint_title ||' UNIQUE (name)');
    END IF;
END;
/

begin
    rename_cc_names;
end;
/

-- Alter FeAnalysis table

CREATE OR REPLACE PROCEDURE ${ohdsiSchema}.rename_fe_names AS
    TYPE NumberArray IS TABLE OF NUMBER;
    TYPE CharArray IS TABLE OF VARCHAR(255);
    duplicate_names CharArray;
    name_repeats NumberArray;
    amount_of_duplicate_names INT;
    amount_of_constraints INT;
    constraint_title VARCHAR(255);
    schema_title VARCHAR(255);
    all_duplicates INT;

BEGIN
    SELECT COUNT(*) INTO all_duplicates FROM (SELECT name FROM ${ohdsiSchema}.fe_analysis
                                                GROUP BY name
                                                HAVING COUNT(*) > 1);

    FOR k IN 0 .. coalesce(all_duplicates, 0)
        LOOP
            SELECT name BULK COLLECT INTO duplicate_names
            FROM ${ohdsiSchema}.fe_analysis
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            SELECT COUNT(*) BULK COLLECT INTO name_repeats
            FROM ${ohdsiSchema}.fe_analysis
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            amount_of_duplicate_names := duplicate_names.COUNT;
        
            FOR i IN 1 .. coalesce(amount_of_duplicate_names, 0)
                LOOP
                    FOR j IN 1 .. coalesce(name_repeats(i), 0)
                        LOOP
                            UPDATE ${ohdsiSchema}.fe_analysis
                            SET name = name || ' (' || j || ')'
                            WHERE id = (SELECT id
                                            FROM ${ohdsiSchema}.fe_analysis
                                            WHERE name = duplicate_names(i)
                                              AND ROWNUM = 1);
                        END LOOP;
                END LOOP;        
            duplicate_names.DELETE();
            name_repeats.DELETE();
        END LOOP;    

    constraint_title := 'uq_fe_name';
    schema_title := '${ohdsiSchema}';

    SELECT COUNT(*) INTO amount_of_constraints
    FROM ALL_CONSTRAINTS
    WHERE OWNER = '${ohdsiSchema}'
    AND CONSTRAINT_NAME = constraint_title
    AND TABLE_NAME = 'FE_ANALYSIS';

    IF amount_of_constraints = 0 THEN
        EXECUTE IMMEDIATE ('ALTER TABLE ' || schema_title || '.FE_ANALYSIS ADD CONSTRAINT ' || constraint_title ||' UNIQUE (name)');
    END IF;
END;
/

begin
    rename_fe_names;
end;
/

-- Alter Pathway Analysis table

CREATE OR REPLACE PROCEDURE ${ohdsiSchema}.rename_pathway_names AS
    TYPE NumberArray IS TABLE OF NUMBER;
    TYPE CharArray IS TABLE OF VARCHAR(255);
    duplicate_names CharArray;
    name_repeats NumberArray;
    amount_of_duplicate_names INT;
    amount_of_constraints INT;
    constraint_title VARCHAR(255);
    schema_title VARCHAR(255);
    all_duplicates INT;

BEGIN
    SELECT COUNT(*) INTO all_duplicates FROM (SELECT name FROM ${ohdsiSchema}.pathway_analysis
                                                GROUP BY name
                                                HAVING COUNT(*) > 1);

    FOR k IN 0 .. coalesce(all_duplicates, 0)
        LOOP
            SELECT name BULK COLLECT INTO duplicate_names
            FROM ${ohdsiSchema}.pathway_analysis
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            SELECT COUNT(*) BULK COLLECT INTO name_repeats
            FROM ${ohdsiSchema}.pathway_analysis
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            amount_of_duplicate_names := duplicate_names.COUNT;
        
            FOR i IN 1 .. coalesce(amount_of_duplicate_names, 0)
                LOOP
                    FOR j IN 1 .. coalesce(name_repeats(i), 0)
                        LOOP
                            UPDATE ${ohdsiSchema}.pathway_analysis
                            SET name = name || ' (' || j || ')'
                            WHERE id = (SELECT id
                                            FROM ${ohdsiSchema}.pathway_analysis
                                            WHERE name = duplicate_names(i)
                                              AND ROWNUM = 1);
                        END LOOP;
                END LOOP;        
            duplicate_names.DELETE();
            name_repeats.DELETE();
        END LOOP;    

    constraint_title := 'uq_pw_name';
    schema_title := '${ohdsiSchema}';

    SELECT COUNT(*) INTO amount_of_constraints
    FROM ALL_CONSTRAINTS
    WHERE OWNER = '${ohdsiSchema}'
    AND CONSTRAINT_NAME = constraint_title
    AND TABLE_NAME = 'PATHWAY_ANALYSIS';

    IF amount_of_constraints = 0 THEN
        EXECUTE IMMEDIATE ('ALTER TABLE ' || schema_title || '.PATHWAY_ANALYSIS ADD CONSTRAINT ' || constraint_title ||' UNIQUE (name)');
    END IF;
END;
/

begin
    rename_pathway_names;
end;
/

-- Alter IR Analysis table

CREATE OR REPLACE PROCEDURE ${ohdsiSchema}.rename_ir_names AS
    TYPE NumberArray IS TABLE OF NUMBER;
    TYPE CharArray IS TABLE OF VARCHAR(255);
    duplicate_names CharArray;
    name_repeats NumberArray;
    amount_of_duplicate_names INT;
    amount_of_constraints INT;
    constraint_title VARCHAR(255);
    schema_title VARCHAR(255);
    all_duplicates INT;

BEGIN
    SELECT COUNT(*) INTO all_duplicates FROM (SELECT name FROM ${ohdsiSchema}.ir_analysis
                                                GROUP BY name
                                                HAVING COUNT(*) > 1);

    FOR k IN 0 .. coalesce(all_duplicates, 0)
        LOOP
            SELECT name BULK COLLECT INTO duplicate_names
            FROM ${ohdsiSchema}.ir_analysis
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            SELECT COUNT(*) BULK COLLECT INTO name_repeats
            FROM ${ohdsiSchema}.ir_analysis
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            amount_of_duplicate_names := duplicate_names.COUNT;
        
            FOR i IN 1 .. coalesce(amount_of_duplicate_names, 0)
                LOOP
                    FOR j IN 1 .. coalesce(name_repeats(i), 0)
                        LOOP
                            UPDATE ${ohdsiSchema}.ir_analysis
                            SET name = name || ' (' || j || ')'
                            WHERE id = (SELECT id
                                            FROM ${ohdsiSchema}.ir_analysis
                                            WHERE name = duplicate_names(i)
                                              AND ROWNUM = 1);
                        END LOOP;
                END LOOP;        
            duplicate_names.DELETE();
            name_repeats.DELETE();
        END LOOP;    

    constraint_title := 'uq_ir_name';
    schema_title := '${ohdsiSchema}';

    SELECT COUNT(*) INTO amount_of_constraints
    FROM ALL_CONSTRAINTS
    WHERE OWNER = '${ohdsiSchema}'
    AND CONSTRAINT_NAME = constraint_title
    AND TABLE_NAME = 'IR_ANALYSIS';

    IF amount_of_constraints = 0 THEN
        EXECUTE IMMEDIATE ('ALTER TABLE ' || schema_title || '.IR_ANALYSIS ADD CONSTRAINT ' || constraint_title ||' UNIQUE (name)');
    END IF;
END;
/

begin
    rename_ir_names;
end;
/

-- Alter Estimation table

CREATE OR REPLACE PROCEDURE ${ohdsiSchema}.rename_estimation_names AS
    TYPE NumberArray IS TABLE OF NUMBER;
    TYPE CharArray IS TABLE OF VARCHAR(255);
    duplicate_names CharArray;
    name_repeats NumberArray;
    amount_of_duplicate_names INT;
    amount_of_constraints INT;
    constraint_title VARCHAR(255);
    schema_title VARCHAR(255);
    all_duplicates INT;

BEGIN
    SELECT COUNT(*) INTO all_duplicates FROM (SELECT name FROM ${ohdsiSchema}.estimation
                                                GROUP BY name
                                                HAVING COUNT(*) > 1);

    FOR k IN 0 .. coalesce(all_duplicates, 0)
        LOOP
            SELECT name BULK COLLECT INTO duplicate_names
            FROM ${ohdsiSchema}.estimation
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            SELECT COUNT(*) BULK COLLECT INTO name_repeats
            FROM ${ohdsiSchema}.estimation
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            amount_of_duplicate_names := duplicate_names.COUNT;
        
            FOR i IN 1 .. coalesce(amount_of_duplicate_names, 0)
                LOOP
                    FOR j IN 1 .. coalesce(name_repeats(i), 0)
                        LOOP
                            UPDATE ${ohdsiSchema}.estimation
                            SET name = name || ' (' || j || ')'
                            WHERE estimation_id = (SELECT estimation_id
                                            FROM ${ohdsiSchema}.estimation
                                            WHERE name = duplicate_names(i)
                                              AND ROWNUM = 1);
                        END LOOP;
                END LOOP;
            duplicate_names.DELETE();
            name_repeats.DELETE();        
        END LOOP;    

    constraint_title := 'uq_es_name';
    schema_title := '${ohdsiSchema}';

    SELECT COUNT(*) INTO amount_of_constraints
    FROM ALL_CONSTRAINTS
    WHERE OWNER = '${ohdsiSchema}'
    AND CONSTRAINT_NAME = constraint_title
    AND TABLE_NAME = 'ESTIMATION';

    IF amount_of_constraints = 0 THEN
        EXECUTE IMMEDIATE ('ALTER TABLE ' || schema_title || '.ESTIMATION ADD CONSTRAINT ' || constraint_title ||' UNIQUE (name)');
    END IF;
END;
/

begin
    rename_estimation_names;
end;
/

-- Alter Prediction table

CREATE OR REPLACE PROCEDURE ${ohdsiSchema}.rename_prediction_names AS
    TYPE NumberArray IS TABLE OF NUMBER;
    TYPE CharArray IS TABLE OF VARCHAR(255);
    duplicate_names CharArray;
    name_repeats NumberArray;
    amount_of_duplicate_names INT;
    amount_of_constraints INT;
    constraint_title VARCHAR(255);
    schema_title VARCHAR(255);
    all_duplicates INT;

BEGIN
    SELECT COUNT(*) INTO all_duplicates FROM (SELECT name FROM ${ohdsiSchema}.prediction
                                                GROUP BY name
                                                HAVING COUNT(*) > 1);

    FOR k IN 0 .. coalesce(all_duplicates, 0)
        LOOP
            SELECT name BULK COLLECT INTO duplicate_names
            FROM ${ohdsiSchema}.prediction
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            SELECT COUNT(*) BULK COLLECT INTO name_repeats
            FROM ${ohdsiSchema}.prediction
            GROUP BY name
            HAVING COUNT(*) > 1;
        
            amount_of_duplicate_names := duplicate_names.COUNT;
        
            FOR i IN 1 .. coalesce(amount_of_duplicate_names, 0)
                LOOP
                    FOR j IN 1 .. coalesce(name_repeats(i), 0)
                        LOOP
                            UPDATE ${ohdsiSchema}.prediction
                            SET name = name || ' (' || j || ')'
                            WHERE prediction_id = (SELECT prediction_id
                                            FROM ${ohdsiSchema}.prediction
                                            WHERE name = duplicate_names(i)
                                              AND ROWNUM = 1);
                        END LOOP;
                END LOOP;        
            duplicate_names.DELETE();
            name_repeats.DELETE();
        END LOOP;    

    constraint_title := 'uq_pd_name';
    schema_title := '${ohdsiSchema}';

    SELECT COUNT(*) INTO amount_of_constraints
    FROM ALL_CONSTRAINTS
    WHERE OWNER = '${ohdsiSchema}'
    AND CONSTRAINT_NAME = constraint_title
    AND TABLE_NAME = 'PREDICTION';

    IF amount_of_constraints = 0 THEN
        EXECUTE IMMEDIATE ('ALTER TABLE ' || schema_title || '.PREDICTION ADD CONSTRAINT ' || constraint_title ||' UNIQUE (name)');
    END IF;
END;
/

begin
    rename_prediction_names;
end;
/

DROP PROCEDURE ${ohdsiSchema}.rename_cs_names;
DROP PROCEDURE ${ohdsiSchema}.rename_cd_names;
DROP PROCEDURE ${ohdsiSchema}.rename_cc_names;
DROP PROCEDURE ${ohdsiSchema}.rename_fe_names;
DROP PROCEDURE ${ohdsiSchema}.rename_pathway_names;
DROP PROCEDURE ${ohdsiSchema}.rename_ir_names;
DROP PROCEDURE ${ohdsiSchema}.rename_estimation_names;
DROP PROCEDURE ${ohdsiSchema}.rename_prediction_names;