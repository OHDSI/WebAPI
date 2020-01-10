package org.ohdsi.webapi;

import org.springframework.batch.core.ExitStatus;

public interface Constants {
  String DEFAULT_DIALECT = "sql server";
  String GENERATE_COHORT = "generateCohort";
  String GENERATE_COHORT_CHARACTERIZATION = "generateCohortCharacterization";
  String GENERATE_PATHWAY_ANALYSIS = "generatePathwayAnalysis";
  String GENERATE_IR_ANALYSIS = "irAnalysis";
  String GENERATE_PREDICTION_ANALYSIS = "generatePredictionAnalysis";
  String GENERATE_ESTIMATION_ANALYSIS = "generateEstimationAnalysis";
  String WARM_CACHE = "warmCache";
  String WARM_CACHE_BY_USER = "warmCacheByUser";
  String USERS_IMPORT = "usersImport";
  String JOB_IS_ALREADY_SCHEDULED = "Job for provider %s is already scheduled";

  String FAILED = ExitStatus.FAILED.getExitCode();
  String CANCELED = "CANCELED";

  String TEMP_COHORT_TABLE_PREFIX = "temp_cohort_";

  interface SqlSchemaPlaceholders {
    String CDM_DATABASE_SCHEMA_PLACEHOLDER = "@cdm_database_schema";
    String RESULTS_DATABASE_SCHEMA_PLACEHOLDER = "@results_database_schema";
    String VOCABULARY_DATABASE_SCHEMA_PLACEHOLDER = "@vocabulary_database_schema";
    String TEMP_DATABASE_SCHEMA_PLACEHOLDER = "@temp_database_schema";
  }

  interface Params {

    String VOCABULARY_DATABASE_SCHEMA = "vocabulary_database_schema";
    String COHORT_DEFINITION_ID = "cohort_definition_id";
    String COHORT_CHARACTERIZATION_ID = "cohort_characterization_id";
    String PATHWAY_ANALYSIS_ID = "pathway_analysis_id";
    String PREDICTION_ANALYSIS_ID = "prediction_analysis_id";
    String ESTIMATION_ANALYSIS_ID = "estimation_analysis_id";
    String UPDATE_PASSWORD = "update_password";
    String SOURCE_ID = "source_id";
    String SOURCE_KEY = "source_key";
    String ANALYSIS_ID = "analysis_id";
    String CDM_DATABASE_SCHEMA = "cdm_database_schema";
    String JOB_NAME = "jobName";
    String JOB_AUTHOR = "jobAuthor";
    String RESULTS_DATABASE_SCHEMA = "results_database_schema";
    String TARGET_DATABASE_SCHEMA = "target_database_schema";
    String TEMP_DATABASE_SCHEMA = "temp_database_schema";
    String TARGET_DIALECT = "target_dialect";
    String TARGET_TABLE = "target_table";
    String COHORT_ID_FIELD_NAME = "cohort_id_field_name";
    String TARGET_COHORT_ID = "target_cohort_id";
    String GENERATE_STATS = "generate_stats";
    String JOB_START_TIME = "time";
    String USER_IMPORT_ID = "user_import_id";
    String USER_ROLES = "userRoles";
    String SESSION_ID = "sessionId";
    String PACKAGE_NAME = "packageName";
    String PACKAGE_FILE_NAME = "packageFilename";
    String EXECUTABLE_FILE_NAME = "executableFilename";
    String GENERATION_ID = "generation_id";
    String DESIGN_HASH = "design_hash";
  }

  interface Variables {
    String SOURCE = "source";
  }

  interface Headers {
    String AUTH_PROVIDER = "x-auth-provider";
  }

  interface SecurityProviders {
    String DISABLED = "DisabledSecurity";
    String REGULAR = "AtlasRegularSecurity";
    String GOOGLE = "AtlasGoogleSecurity";
  }

  interface Templates {

    String ENTITY_COPY_PREFIX = "COPY OF: %s";
  }

  interface Tables {
    String COHORT_CACHE = "cohort_cache";
    String COHORT_INCLUSION_RESULT_CACHE = "cohort_inclusion_result_cache";
    String COHORT_INCLUSION_STATS_CACHE = "cohort_inclusion_stats_cache";
    String COHORT_SUMMARY_STATS_CACHE = "cohort_summary_stats_cache";
    String COHORT_CENSOR_STATS_CACHE = "cohort_censor_stats";
  }
}
