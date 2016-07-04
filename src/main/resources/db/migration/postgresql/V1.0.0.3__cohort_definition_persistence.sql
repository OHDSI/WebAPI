CREATE TABLE ${ohdsiSchema}.cohort_definition(
	id int NOT NULL,
	name varchar(255) NOT NULL,
	description varchar(1000) NULL,
	expression_type varchar(50) NULL,
	created_by varchar(255) NULL,
	created_date Timestamp(3) NULL,
	modified_by varchar(255) NULL,
	modified_date Timestamp(3) NULL,
	CONSTRAINT PK_cohort_definition PRIMARY KEY (id) 
);

CREATE TABLE ${ohdsiSchema}.cohort_definition_details(
	id int,
	expression Text NOT NULL,
	CONSTRAINT PK_cohort_definition_details PRIMARY KEY (id),
	CONSTRAINT FK_cohort_definition_details_cohort_definition 
		FOREIGN KEY (id)
		REFERENCES ${ohdsiSchema}.cohort_definition(id)
);

