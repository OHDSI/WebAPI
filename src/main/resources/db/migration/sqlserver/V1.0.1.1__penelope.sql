CREATE TABLE [${ohdsiSchema}].COHORT_CONCEPT_MAP (
    cohort_definition_id int NULL,
    cohort_definition_name varchar(255) NULL,
    concept_id int NULL
);

CREATE TABLE [${ohdsiSchema}].[COHORT_STUDY](
        [cohort_study_id] [int] IDENTITY(1,1) NOT NULL,
        [cohort_definition_id] [int] NULL,
        [study_type] [int] NULL,
        [study_name] [varchar](1000) NULL,
        [study_URL] [varchar](1000) NULL,
 CONSTRAINT [PK_COHORT_STUDY] PRIMARY KEY CLUSTERED 
    (
        [cohort_study_id] ASC
    )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
);

CREATE TABLE [${ohdsiSchema}].[CONCEPT_OF_INTEREST](
        [id] [int] IDENTITY(1,1) NOT NULL,
        [concept_id] [int] NULL,
        [concept_of_interest_id] [int] NULL,
 CONSTRAINT [PK_CONCEPT_OF_INTEREST] PRIMARY KEY CLUSTERED 
  (
    [id] ASC
  ) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
);

CREATE TABLE [${ohdsiSchema}].[drug_labels](
        [drug_label_id] [bigint] NOT NULL,
        [search_name] [varchar](255) NULL,
        [ingredient_concept_id] [bigint] NULL,
        [ingredient_concept_name] [varchar](255) NULL,
        [setid] [varchar](255) NULL,
        [Date] [datetime] NULL,
        [cohort_id] [int] NULL,
        [image_url] [varchar](255) NULL,
 CONSTRAINT [PK_drug_labels] PRIMARY KEY CLUSTERED 
  (
    [drug_label_id] ASC
  ) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
);
