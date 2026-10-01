-- Create a join table for the many-to-many relationship between compare jobs and authors
CREATE SEQUENCE ${ohdsiSchema}.CONCEPT_SET_COMPARE_JOB_AUTHOR_SEQUENCE
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 9223372036854775807
    NO CYCLE;

CREATE TABLE ${ohdsiSchema}.CONCEPT_SET_COMPARE_JOB_AUTHOR (
    ID INTEGER NOT NULL PRIMARY KEY DEFAULT NEXTVAL('${ohdsiSchema}.CONCEPT_SET_COMPARE_JOB_AUTHOR_SEQUENCE'),
    COMPARE_JOB_ID INTEGER NOT NULL,
    USER_ID INTEGER NOT NULL,
    CONSTRAINT FK_COMPARE_JOB_AUTHOR_JOB FOREIGN KEY (COMPARE_JOB_ID) 
        REFERENCES ${ohdsiSchema}.CONCEPT_SET_COMPARE_JOB(ID) ON DELETE CASCADE,
    CONSTRAINT FK_COMPARE_JOB_AUTHOR_USER FOREIGN KEY (USER_ID) 
        REFERENCES ${ohdsiSchema}.SEC_USER(ID) ON DELETE CASCADE
);

-- Create index for better query performance
CREATE INDEX idx_cs_compare_job_author_job_id 
    ON ${ohdsiSchema}.CONCEPT_SET_COMPARE_JOB_AUTHOR(COMPARE_JOB_ID);

CREATE INDEX idx_cs_compare_job_author_user_id 
    ON ${ohdsiSchema}.CONCEPT_SET_COMPARE_JOB_AUTHOR(USER_ID);

-- Remove the old author column from concept_set_compare_job
ALTER TABLE ${ohdsiSchema}.concept_set_compare_job DROP COLUMN author;