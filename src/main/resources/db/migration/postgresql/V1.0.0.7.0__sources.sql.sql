CREATE TABLE source (
    SOURCE_ID int NOT NULL,
    SOURCE_NAME VARCHAR (255) NOT NULL,
    SOURCE_KEY  VARCHAR (50) NOT NULL,
    SOURCE_CONNECTION VARCHAR (8000) NOT NULL,
    SOURCE_DIALECT VARCHAR (255) NOT NULL,
    CONSTRAINT PK_source PRIMARY KEY (source_id) 
);

CREATE TABLE source_daimon (
    source_daimon_id int NOT NULL,
    source_id int NOT NULL,
    daimon_type int NOT NULL,
    table_qualifier  VARCHAR (255) NOT NULL,
    priority int NOT NULL,
    CONSTRAINT PK_source_daimon PRIMARY KEY (source_daimon_id) 
);