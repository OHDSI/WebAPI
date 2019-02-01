DELETE FROM ${ohdsiSchema}.CONCEPT_SET_GENERATION_INFO;

DROP TABLE [${ohdsiSchema}].[CONCEPT_SET_NEGATIVE_CONTROLS];

DROP SEQUENCE ${ohdsiSchema}.negative_controls_sequence;

CREATE SEQUENCE ${ohdsiSchema}.negative_controls_sequence;

CREATE TABLE ${ohdsiSchema}.CONCEPT_SET_NEGATIVE_CONTROLS (
    id int NOT NULL DEFAULT (NEXT VALUE FOR ${ohdsiSchema}.negative_controls_sequence),
    evidence_job_id BIGINT NOT NULL,
    source_id INTEGER NOT NULL,
    concept_set_id INTEGER NOT NULL
    CONSTRAINT [PK_CONCEPT_SET_NC] PRIMARY KEY CLUSTERED 
     (
             [id] ASC
     ) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
);

