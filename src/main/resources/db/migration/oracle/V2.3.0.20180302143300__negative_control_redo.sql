DROP SEQUENCE ${ohdsiSchema}.negative_controls_sequence;

CREATE SEQUENCE ${ohdsiSchema}.negative_controls_sequence START WITH 1 MAXVALUE 9223372036854775807 NOCYCLE;

DROP TABLE ${ohdsiSchema}.CONCEPT_SET_NEGATIVE_CONTROLS;

CREATE TABLE ${ohdsiSchema}.CONCEPT_SET_NEGATIVE_CONTROLS (
    id INTEGER NOT NULL,
		evidence_job_id NUMBER(19) NOT NULL,
    source_id INTEGER NOT NULL,
    concept_set_id INTEGER NOT NULL,
    concept_set_name varchar(255) NOT NULL,
		negative_control INTEGER NOT NULL,
    concept_id INTEGER NOT NULL,
    concept_name varchar(255) NOT NULL,
    domain_id varchar(255) NOT NULL,
    sort_order NUMBER(19),
    descendant_pmid_count INTEGER,
    exact_pmid_count INTEGER,
    parent_pmid_count INTEGER,
    ancestor_pmid_count INTEGER,
    ind_ci INTEGER,
    too_broad INTEGER,
    drug_induced INTEGER,
    pregnancy INTEGER,
    descendant_splicer_cnt NUMBER(19),
    exact_splicer_cnt NUMBER(19),
    parent_splicer_cnt NUMBER(19),
    ancestor_splicer_cnt NUMBER(19),
    descendant_faers_cnt NUMBER(19),
    exact_faers_cnt NUMBER(19),
    parent_faers_cnt NUMBER(19),
    ancestor_faers_cnt NUMBER(19),
    user_excluded INTEGER,
    user_included INTEGER,
    optimized_out INTEGER,
    not_prevalent INTEGER,
    CONSTRAINT PK_CONCEPT_SET_NC PRIMARY KEY (id)
);