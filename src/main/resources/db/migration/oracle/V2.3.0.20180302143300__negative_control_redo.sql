BEGIN
   EXECUTE IMMEDIATE '${ohdsiSchema}.CONCEPT_SET_NEGATIVE_CONTROLS';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;

BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE ' || ${ohdsiSchema}.negative_controls_sequence;
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -2289 THEN
      RAISE;
    END IF;
END;

CREATE SEQUENCE ${ohdsiSchema}.negative_controls_sequence START WITH 1 MAXVALUE 9223372036854775807 NO CYCLE;
CREATE TABLE ${ohdsiSchema}.CONCEPT_SET_NEGATIVE_CONTROLS (
    id INTEGER NOT NULL DEFAULT NEXTVAL('negative_controls_sequence'),
		evidence_job_id BIGINT NOT NULL,
    source_id INTEGER NOT NULL,
    concept_set_id INTEGER NOT NULL,
    concept_set_name varchar(255) NOT NULL,
		negative_control INTEGER NOT NULL,
    concept_id INTEGER NOT NULL,
    concept_name varchar(255) NOT NULL,
    domain_id varchar(255) NOT NULL,
    sort_order bigint,
    descendant_pmid_count INTEGER,
    exact_pmid_count INTEGER,
    parent_pmid_count INTEGER,
    ancestor_pmid_count INTEGER,
    ind_ci INTEGER,
    too_broad INTEGER,
    drug_induced INTEGER,
    pregnancy INTEGER,
    descendant_splicer_cnt BIGINT,
    exact_splicer_cnt BIGINT,
    parent_splicer_cnt BIGINT,
    ancestor_splicer_cnt BIGINT,
    descendant_faers_cnt BIGINT,
    exact_faers_cnt BIGINT,
    parent_faers_cnt BIGINT,
    ancestor_faers_cnt BIGINT,
    user_excluded INTEGER,
    user_included INTEGER,
    optimized_out INTEGER,
    not_prevalent INTEGER,
    CONSTRAINT PK_CONCEPT_SET_NC PRIMARY KEY (id)
);