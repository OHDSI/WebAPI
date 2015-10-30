CREATE TABLE COHORT_CONCEPT_MAP (
    cohort_definition_id int NULL,
    cohort_definition_name varchar(255) NULL,
    concept_id int NULL
);

CREATE TABLE COHORT_STUDY(
    cohort_study_id SERIAL NOT NULL,
    cohort_definition_id int NULL,
    study_type int NULL,
    study_name varchar(1000) NULL,
    study_URL varchar(1000) NULL,
    CONSTRAINT PK_COHORT_STUDY PRIMARY KEY (cohort_study_id)
 );

CREATE TABLE CONCEPT_OF_INTEREST(
        id SERIAL NOT NULL,
        concept_id int NULL,
        concept_of_interest_id int NULL,
        CONSTRAINT PK_CONCEPT_OF_INTEREST PRIMARY KEY (id)
 );

CREATE TABLE drug_labels(
        drug_label_id bigint NOT NULL,
        search_name varchar(255) NULL,
        ingredient_concept_id bigint NULL,
        ingredient_concept_name varchar(255) NULL,
        setid varchar(255) NULL,
        Date Timestamp(3) NULL,
        cohort_id int NULL,
        image_url varchar(255) NULL,
        CONSTRAINT PK_drug_labels PRIMARY KEY (drug_label_id)
);

