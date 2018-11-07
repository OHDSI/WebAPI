package org.ohdsi.webapi;

import com.google.common.collect.ImmutableMap;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.springframework.batch.core.ExitStatus;

import java.util.Map;

public interface Constants {
  String GENERATE_COHORT = "generateCohort";
  String GENERATE_COHORT_CHARACTERIZATION = "generateCohortCharacterization";
  String GENERATE_PATHWAY_ANALYSIS = "generatePathwayAnalysis";
  String WARM_CACHE = "warmCache";
  String USERS_IMPORT = "usersImport";

  String FAILED = ExitStatus.FAILED.getExitCode();
  String CANCELED = "CANCELED";

  interface Params {

    String VOCABULARY_DATABASE_SCHEMA = "vocabulary_database_schema";
    String COHORT_DEFINITION_ID = "cohort_definition_id";
    String COHORT_CHARACTERIZATION_ID = "cohort_characterization_id";
    String PATHWAY_ANALYSIS_ID = "pathway_analysis_id";
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
  }

  Map<StandardFeatureAnalysisDomain, String> DOMAIN_TABLES = ImmutableMap.<StandardFeatureAnalysisDomain, String>builder()
          .put(StandardFeatureAnalysisDomain.CONDITION, "condition_occurrence")
          .put(StandardFeatureAnalysisDomain.DEVICE, "device_exposure")
          .put(StandardFeatureAnalysisDomain.DRUG, "drug_exposure")
          .put(StandardFeatureAnalysisDomain.MEASUREMENT, "measurement")
          .put(StandardFeatureAnalysisDomain.OBSERVATION, "observation")
          .put(StandardFeatureAnalysisDomain.PROCEDURE, "procedure_occurrence")
          .put(StandardFeatureAnalysisDomain.VISIT, "visit_occurrence")
          .build();

  Map<StandardFeatureAnalysisDomain, String> DOMAIN_ID_FIELDS = ImmutableMap.<StandardFeatureAnalysisDomain, String>builder()
          .put(StandardFeatureAnalysisDomain.CONDITION, "condition_occurrence_id")
          .put(StandardFeatureAnalysisDomain.DEVICE, "device_exposure_id")
          .put(StandardFeatureAnalysisDomain.DRUG, "drug_exposure_id")
          .put(StandardFeatureAnalysisDomain.MEASUREMENT, "measurement_id")
          .put(StandardFeatureAnalysisDomain.OBSERVATION, "observation_id")
          .put(StandardFeatureAnalysisDomain.PROCEDURE, "procedure_occurrence_id")
          .put(StandardFeatureAnalysisDomain.VISIT, "visit_occurrence_id")
          .build();
}
