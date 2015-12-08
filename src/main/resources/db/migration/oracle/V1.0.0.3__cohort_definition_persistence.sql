CREATE TABLE ${ohdsiSchema}.cohort_definition(
	id Number(10) NOT NULL ,
	name Varchar2(255) NOT NULL,
	description Varchar2(1000) NULL,
	expression_type Varchar2(50) NULL,
	created_by Varchar2(255) NULL,
	created_date Timestamp(3) NULL,
	modified_by Varchar2(255) NULL,
	modified_date Timestamp(3) NULL,
	CONSTRAINT PK_cohort_definition PRIMARY KEY (id) 
);

CREATE TABLE ${ohdsiSchema}.cohort_definition_details(
	id Number(10),
	expression CLOB NOT NULL,
	CONSTRAINT PK_cohort_def_details PRIMARY KEY (id),
	CONSTRAINT FK_cdd_cd 
          FOREIGN KEY (id)
          REFERENCES cohort_definition(id)
);
