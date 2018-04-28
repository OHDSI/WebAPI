IF (EXISTS (SELECT * 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_SCHEMA = '${ohdsiSchema}' 
            AND  TABLE_NAME = 'CONCEPT_SET_NEGATIVE_CONTROLS'))
BEGIN
    DROP TABLE [${ohdsiSchema}].[CONCEPT_SET_NEGATIVE_CONTROLS];
END

IF (EXISTS (SELECT *
						FROM sys.objects
					  WHERE name = 'negative_controls_sequence'))
BEGIN
	DROP SEQUENCE ${ohdsiSchema}.negative_controls_sequence;
END

CREATE SEQUENCE ${ohdsiSchema}.negative_controls_sequence;
CREATE TABLE ${ohdsiSchema}.CONCEPT_SET_NEGATIVE_CONTROLS (
    id int NOT NULL DEFAULT (NEXT VALUE FOR ${ohdsiSchema}.negative_controls_sequence),
		evidence_job_id BIGINT NOT NULL,
    source_id INTEGER NOT NULL,
    concept_set_id INTEGER NOT NULL,
    concept_set_name varchar(255) NOT NULL,
		negative_control INTEGER NOT NULL,
    concept_id INTEGER NOT NULL,
    concept_name varchar(255) NOT NULL,
    domain_id varchar(255) NOT NULL,
    sort_order bigint,
    descendant_pmid_cnt BIGINT,
    exact_pmid_cnt BIGINT,
    parent_pmid_cnt BIGINT,
    ancestor_pmid_cnt BIGINT,
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
    not_prevalent INTEGER
		CONSTRAINT [PK_CONCEPT_SET_NC] PRIMARY KEY CLUSTERED 
		 (
			 [id] ASC
		 ) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
);

