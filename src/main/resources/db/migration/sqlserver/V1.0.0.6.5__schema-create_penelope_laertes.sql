CREATE TABLE [${ohdsiSchema}].penelope_laertes_universe (
    id bigint,
    condition_concept_id bigint NULL,
    condition_concept_name varchar(200) NULL,
    ingredient_concept_id bigint NULL,
    ingredient_concept_name varchar(200) NULL,
    evidence_type varchar(200) NULL,
    supports char(1) NULL,
    statistic_value real NULL,
    evidence_linkouts varchar(max) NULL,
 CONSTRAINT [PK_PENELOPE_LAERTES_UNIVERSE] PRIMARY KEY CLUSTERED 
    (
        [id] ASC
    )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
);

CREATE TABLE [${ohdsiSchema}].penelope_laertes_uni_pivot(
    condition_concept_id bigint NULL,
    condition_concept_name varchar(255) NULL,
    ingredient_concept_id bigint NULL,
    ingredient_concept_name varchar(255) NULL,
    medline_ct bigint NULL,
    medline_case bigint NULL,
    medline_other bigint NULL,
    semmeddb_ct_t bigint NULL,
    semmeddb_case_t bigint NULL,
    semmeddb_other_t bigint NULL,
    semmeddb_ct_f bigint NULL,
    semmeddb_case_f bigint NULL,
    semmeddb_other_f bigint NULL,
    eu_spc bigint NULL,
    spl_adr bigint NULL,
    aers bigint NULL,
    aers_prr float NULL,
    aers_prr_original float NULL
) ON [PRIMARY]