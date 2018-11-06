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

  Map<StandardFeatureAnalysisDomain, DomainMetadata> DOMAIN_METADATA = ImmutableMap.<StandardFeatureAnalysisDomain, DomainMetadata>builder()
          .put(StandardFeatureAnalysisDomain.CONDITION, new DomainMetadata("condition_occurrence", "condition_occurrence_id", "condition_start_date", "condition_end_date"))
          .put(StandardFeatureAnalysisDomain.DEVICE, new DomainMetadata("device_exposure", "device_exposure_id", "device_exposure_start_date", "device_exposure_end_date"))
          .put(StandardFeatureAnalysisDomain.DRUG, new DomainMetadata("drug_exposure", "drug_exposure_id", "drug_exposure_start_date", "drug_exposure_end_date"))
          .put(StandardFeatureAnalysisDomain.MEASUREMENT, new DomainMetadata("measurement", "measurement_id", "measurement_date", "NULL"))
          .put(StandardFeatureAnalysisDomain.OBSERVATION, new DomainMetadata("observation", "observation_id", "observation_date", "NULL"))
          .put(StandardFeatureAnalysisDomain.PROCEDURE, new DomainMetadata("procedure_occurrence", "procedure_occurrence_id", "procedure_date", "NULL"))
          .put(StandardFeatureAnalysisDomain.VISIT, new DomainMetadata("visit_occurrence", "visit_occurrence_id", "visit_start_date", "visit_end_date"))
          .build();

  public static class DomainMetadata {
    private String tableName;
    private String idField;
    private String startDateField;
    private String endDateField;

    public DomainMetadata(String tableName, String idField, String startDateField, String endDateField) {
      this.tableName = tableName;
      this.idField = idField;
      this.startDateField = startDateField;
      this.endDateField = endDateField;
    }

    public String getTableName() {
      return tableName;
    }

    public String getIdField() {
      return idField;
    }

    public String getStartDateField() {
      return startDateField;
    }

    public String getEndDateField() {
      return endDateField;
    }
  }
}
