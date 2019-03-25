package org.ohdsi.webapi;

import org.ohdsi.webapi.source.Source;
import com.google.common.collect.ImmutableList;
import com.odysseusinc.arachne.commons.types.DBMSType;
import org.springframework.batch.core.ExitStatus;

import java.util.List;

public interface Constants {
  String GENERATE_COHORT = "generateCohort";
  String GENERATE_COHORT_CHARACTERIZATION = "generateCohortCharacterization";
  String GENERATE_PATHWAY_ANALYSIS = "generatePathwayAnalysis";
  String GENERATE_IR_ANALYSIS = "irAnalysis";
  String GENERATE_PREDICTION_ANALYSIS = "generatePredictionAnalysis";
  String GENERATE_ESTIMATION_ANALYSIS = "generateEstimationAnalysis";
  String WARM_CACHE = "warmCache";
  String USERS_IMPORT = "usersImport";

  String FAILED = ExitStatus.FAILED.getExitCode();
  String CANCELED = "CANCELED";

  String TEMP_COHORT_TABLE_PREFIX = "temp_cohort_";

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
    String GENERATE_STATS = "generate_stats";
    String JOB_START_TIME = "time";
    String USER_ROLES = "userRoles";
    String LDAP_PROVIDER = "provider";
    String ROLE_GROUP_MAPPING = "roleGroupMapping";
    String PRESERVE_ROLES = "preserveRoles";
    String SESSION_ID = "sessionId";
    String PACKAGE_NAME = "packageName";
    String PACKAGE_FILE_NAME = "packageFilename";
    String EXECUTABLE_FILE_NAME = "executableFilename";
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
}
