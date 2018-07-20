package org.ohdsi.webapi;

public interface Constants {
  String GENERATE_COHORT = "generateCohort";
  String WARM_CACHE = "warmCache";

  interface Params {

    String VOCABULARY_DATABASE_SCHEMA = "vocabulary_database_schema";
    String COHORT_DEFINITION_ID = "cohort_definition_id";
    String SOURCE_ID = "source_id";
    String ANALYSIS_ID = "analysis_id";
    String CDM_DATABASE_SCHEMA = "cdm_database_schema";
    String JOB_NAME = "jobName";
    String JOB_AUTHOR = "jobAuthor";
    String RESULTS_DATABASE_SCHEMA = "results_database_schema";
    String TARGET_DATABASE_SCHEMA = "target_database_schema";
    String TARGET_DIALECT = "target_dialect";
    String TARGET_TABLE = "target_table";
    String GENERATE_STATS = "generate_stats";
    String JOB_START_TIME = "time";
  }
}
