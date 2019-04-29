CREATE OR REPLACE FUNCTION ${ohdsiSchema}.rename_duplicate_names(name_title VARCHAR(20), id_title VARCHAR(15),
                                                  table_title VARCHAR(30)) RETURNS VOID
    LANGUAGE 'plpgsql'
AS
$$
DECLARE
    duplicate_names           VARCHAR(100)[];
    name_repeats              INT[];
    amount_of_duplicate_names INT;
    amount_of_constraints     INT;
    constraint_name           VARCHAR(100);

BEGIN
    EXECUTE format('SELECT ARRAY(SELECT %I
                                 FROM %I.%I
                                 GROUP BY %I
                                 HAVING COUNT(*) > 1)', name_title, ${ohdsiSchemaQuotes}, table_title,
                   name_title) INTO duplicate_names;

    EXECUTE format('SELECT ARRAY(SELECT COUNT(*)
                                 FROM %I.%I
                                 GROUP BY %I
                                 HAVING COUNT(*) > 1);', ${ohdsiSchemaQuotes}, table_title, name_title) INTO name_repeats;


    amount_of_duplicate_names := (SELECT array_length(duplicate_names, 1));

    FOR i IN 1 .. coalesce(amount_of_duplicate_names, 0)
        LOOP
            FOR j IN 1 .. coalesce(name_repeats[i], 0)
                LOOP
                    EXECUTE format('UPDATE %I.%I
                     SET %I = concat(%I, '' ('', $1, '')'')
                     WHERE %I = (SELECT %I
                                 FROM %I.%I
                                 WHERE %I = $2
                                 ORDER BY %I
                                 LIMIT 1);', ${ohdsiSchemaQuotes}, table_title, name_title, name_title, id_title, id_title,
                                   ${ohdsiSchemaQuotes}, table_title,
                                   name_title, id_title) USING j, duplicate_names[i];
                END LOOP;
        END LOOP;

    constraint_name := concat('unique_name_', ${ohdsiSchemaQuotes}, '_', table_title);

    EXECUTE format('SELECT COUNT(*)
                  FROM information_schema.table_constraints
                  WHERE constraint_schema = ''%I''
                    AND constraint_name = ''%I''
                    AND table_name = ''%I''', ${ohdsiSchemaQuotes}, constraint_name, table_title) INTO amount_of_constraints;

    IF amount_of_constraints = 0 THEN
        EXECUTE format('ALTER TABLE %I.%I
                             ADD CONSTRAINT %I UNIQUE (%I);', ${ohdsiSchemaQuotes}, table_title, constraint_name, name_title);
    END IF;
END;
$$;

SELECT rename_duplicate_names('concept_set_name', 'concept_set_id', 'concept_set');
SELECT rename_duplicate_names('name', 'id', 'cohort_definition');
SELECT rename_duplicate_names('name', 'id', 'cohort_characterization');
SELECT rename_duplicate_names('name', 'id', 'fe_analysis');
SELECT rename_duplicate_names('name', 'id', 'pathway_analysis');
SELECT rename_duplicate_names('name', 'id', 'ir_analysis');
SELECT rename_duplicate_names('name', 'estimation_id', 'estimation');
SELECT rename_duplicate_names('name', 'prediction_id', 'prediction');

DROP FUNCTION ${ohdsiSchema}.rename_duplicate_names(name_title VARCHAR(20), id_title VARCHAR(15), table_title VARCHAR(30));