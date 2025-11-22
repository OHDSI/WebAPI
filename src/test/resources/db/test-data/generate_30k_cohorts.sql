-- Test Data Generator: 30,000 Cohort Definitions
-- Purpose: Generate large dataset for performance testing
-- Feature: 001-cohort-performance
-- Usage: psql -U postgres -d webapi < generate_30k_cohorts.sql

-- Ensure cohort_definition and cohort_definition_details tables exist
-- This script generates 30,000 cohorts with realistic metadata and large JSON expressions

DO $$
DECLARE
    i INTEGER;
    expression_json TEXT;
BEGIN
    -- Create a realistic cohort expression template (~10KB)
    expression_json := '{
        "ConceptSets": [
            {
                "id": 0,
                "name": "Test Concept Set",
                "expression": {
                    "items": [
                        {"concept": {"CONCEPT_ID": 201826, "CONCEPT_NAME": "Type 2 diabetes mellitus", "STANDARD_CONCEPT": "S", "DOMAIN_ID": "Condition"}},
                        {"concept": {"CONCEPT_ID": 443238, "CONCEPT_NAME": "Diabetes mellitus", "STANDARD_CONCEPT": "S", "DOMAIN_ID": "Condition"}}
                    ]
                }
            }
        ],
        "PrimaryCriteria": {
            "CriteriaList": [
                {
                    "ConditionOccurrence": {
                        "CodesetId": 0,
                        "ConditionTypeExclude": false
                    }
                }
            ],
            "ObservationWindow": {
                "PriorDays": 0,
                "PostDays": 0
            },
            "PrimaryCriteriaLimit": {
                "Type": "First"
            }
        },
        "QualifiedLimit": {
            "Type": "First"
        },
        "ExpressionLimit": {
            "Type": "First"
        },
        "InclusionRules": [],
        "CensoringCriteria": [],
        "CollapseSettings": {
            "CollapseType": "ERA",
            "EraPad": 0
        },
        "CensorWindow": {}
    }';

    RAISE NOTICE 'Starting generation of 30,000 cohort definitions...';
    RAISE NOTICE 'This may take 5-10 minutes. Progress will be reported every 1,000 cohorts.';

    FOR i IN 1..30000 LOOP
        -- Insert cohort definition metadata
        INSERT INTO cohort_definition (id, name, description, created_by_id, created_date, modified_date)
        VALUES (
            i,
            'Performance Test Cohort ' || i,
            'Auto-generated cohort for performance testing - ID ' || i,
            1,  -- Assumes user ID 1 exists (admin/demo user)
            NOW() - (random() * INTERVAL '365 days'),
            NOW() - (random() * INTERVAL '180 days')
        )
        ON CONFLICT (id) DO NOTHING;  -- Skip if already exists

        -- Insert cohort definition details (large JSON expression)
        INSERT INTO cohort_definition_details (id, expression)
        VALUES (
            i,
            expression_json
        )
        ON CONFLICT (id) DO NOTHING;  -- Skip if already exists

        -- Report progress every 1,000 records
        IF i % 1000 = 0 THEN
            RAISE NOTICE '  Generated % / 30,000 cohorts (% complete)', i, ROUND((i::NUMERIC / 30000) * 100, 1);
        END IF;

        -- Commit every 5,000 records to avoid transaction log bloat
        IF i % 5000 = 0 THEN
            COMMIT;
        END IF;
    END LOOP;

    RAISE NOTICE 'Successfully generated 30,000 cohort definitions!';
    RAISE NOTICE 'Total cohorts in database: %', (SELECT COUNT(*) FROM cohort_definition);
END $$;

-- Verify data was created
SELECT
    COUNT(*) as total_cohorts,
    MIN(id) as min_id,
    MAX(id) as max_id
FROM cohort_definition;

SELECT
    COUNT(*) as total_details,
    AVG(LENGTH(expression::TEXT)) as avg_expression_size_bytes
FROM cohort_definition_details;
