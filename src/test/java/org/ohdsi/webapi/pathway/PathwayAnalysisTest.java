/*
 * Copyright 2020 cknoll1.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.pathway;

import com.github.mjeanroy.dbunit.core.dataset.DataSetFactory;
import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.KerberosAuthMechanism;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.DatabaseOperation;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.pathway.converter.SerializedPathwayAnalysisToPathwayAnalysisConverter;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGenerationEntity;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author cknoll1
 */
public class PathwayAnalysisTest extends AbstractDatabaseTest {

  private static final Collection<String> CDM_DDL_FILE_PATHS = Arrays.asList("/cdm-postgresql-ddl.sql");
  private static final String CDM_SCHEMA_NAME = "cdm";
  private static boolean isSourceInitialized = false;
  private static final String SOURCE_KEY = "Embedded_PG";
  private static Integer SOURCE_ID;
  private static final String RESULT_SCHEMA_NAME = "results";
  private static final Collection<String> RESULTS_DDL_FILE_PATHS = Arrays.asList(
          "/ddl/results/cohort.sql",
          "/ddl/results/cohort_cache.sql",
          "/ddl/results/cohort_inclusion.sql",
          "/ddl/results/cohort_inclusion_result.sql",
          "/ddl/results/cohort_inclusion_stats.sql",
          "/ddl/results/cohort_inclusion_result_cache.sql",
          "/ddl/results/cohort_inclusion_stats_cache.sql",
          "/ddl/results/cohort_summary_stats.sql",
          "/ddl/results/cohort_summary_stats_cache.sql",
          "/ddl/results/cohort_censor_stats.sql",
          "/ddl/results/cohort_censor_stats_cache.sql",
          "/ddl/results/pathway_analysis_codes.sql",
          "/ddl/results/pathway_analysis_events.sql",
          "/ddl/results/pathway_analysis_paths.sql",
          "/ddl/results/pathway_analysis_stats.sql"
  );
  
  @Autowired
  private SourceRepository sourceRepository;

  @Autowired
  private PathwayService pathwayService;

  @Value("${datasource.ohdsi.schema}")
  private String ohdsiSchema;

  private static SerializedPathwayAnalysisToPathwayAnalysisConverter converter =  new SerializedPathwayAnalysisToPathwayAnalysisConverter();

  
  @Before
  public void setUp() throws Exception {
    if (!isSourceInitialized) {
      // one-time setup of CDM source
      truncateTable(String.format("%s.%s", ohdsiSchema, "source"));
      resetSequence(String.format("%s.%s", ohdsiSchema, "source_sequence"));
      Source s = sourceRepository.saveAndFlush(getCdmSource());
      SOURCE_ID = s.getSourceId();
      isSourceInitialized = true;
    }
    // perform the following before each test
    truncateTable(String.format("%s.%s", ohdsiSchema, "cohort_definition"));
    resetSequence(String.format("%s.%s", ohdsiSchema, "cohort_definition_sequence"));
    truncateTable(String.format("%s.%s", ohdsiSchema, "pathway_analysis"));
    resetSequence(String.format("%s.%s", ohdsiSchema, "pathway_analysis_sequence"));
    truncateTable(String.format("%s.%s", ohdsiSchema, "generation_cache"));
    prepareCdmSchema();
    prepareResultSchema();    
  }

  @After
  public void tearDownDB() {
    truncateTable(String.format("%s.%s", ohdsiSchema, "cohort_definition"));
    resetSequence(String.format("%s.%s", ohdsiSchema, "cohort_definition_sequence"));
    truncateTable(String.format("%s.%s", ohdsiSchema, "pathway_analysis"));
    resetSequence(String.format("%s.%s", ohdsiSchema, "pathway_analysis_sequence"));
  }

  private static void prepareResultSchema() {
    prepareSchema(RESULT_SCHEMA_NAME, "results_schema", RESULTS_DDL_FILE_PATHS);
  }

  private static void prepareCdmSchema() {
    prepareSchema(CDM_SCHEMA_NAME, "cdm_database_schema", CDM_DDL_FILE_PATHS);
  }
  
  private static void prepareSchema(final String schemaName, final String schemaToken, final Collection<String> schemaPaths) {
    StringBuilder ddl = new StringBuilder();
    
    ddl.append(String.format("DROP SCHEMA IF EXISTS %s CASCADE;", schemaName));
    ddl.append(String.format("CREATE SCHEMA %s;", schemaName));
    schemaPaths.forEach(sqlPath -> ddl.append(ResourceHelper.GetResourceAsString(sqlPath)).append("\n"));
    String resultSql = SqlRender.renderSql(ddl.toString(), new String[]{schemaToken}, new String[]{schemaName});
    String ddlSql = SqlTranslate.translateSql(resultSql, DBMSType.POSTGRESQL.getOhdsiDB());    
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(ddlSql));
  }  

  private Source getCdmSource() throws SQLException {
    Source source = new Source();
    source.setSourceName("Embedded PG");
    source.setSourceKey(SOURCE_KEY);
    source.setSourceDialect(DBMSType.POSTGRESQL.getOhdsiDB());
    source.setSourceConnection(getDataSource().getConnection().getMetaData().getURL());
    source.setUsername("postgres");
    source.setPassword("postgres");
    source.setKrbAuthMethod(KerberosAuthMechanism.PASSWORD);

    SourceDaimon cdmDaimon = new SourceDaimon();
    cdmDaimon.setPriority(1);
    cdmDaimon.setDaimonType(SourceDaimon.DaimonType.CDM);
    cdmDaimon.setTableQualifier(CDM_SCHEMA_NAME);
    cdmDaimon.setSource(source);

    SourceDaimon vocabDaimon = new SourceDaimon();
    vocabDaimon.setPriority(1);
    vocabDaimon.setDaimonType(SourceDaimon.DaimonType.Vocabulary);
    vocabDaimon.setTableQualifier(CDM_SCHEMA_NAME);
    vocabDaimon.setSource(source);

    SourceDaimon resultsDaimon = new SourceDaimon();
    resultsDaimon.setPriority(1);
    resultsDaimon.setDaimonType(SourceDaimon.DaimonType.Results);
    resultsDaimon.setTableQualifier(RESULT_SCHEMA_NAME);
    resultsDaimon.setSource(source);

    source.setDaimons(Arrays.asList(cdmDaimon, vocabDaimon, resultsDaimon));

    return source;
  }
  
  private void generateAnalysis(PathwayAnalysisEntity entity) throws Exception {
    JobExecutionResource executionResource = pathwayService.generatePathways(entity.getId(), SOURCE_ID);
    PathwayAnalysisGenerationEntity generationEntity;
    while (true) {
        generationEntity = pathwayService.getGeneration(executionResource.getExecutionId());
        if (generationEntity.getStatus().equals("FAILED") || generationEntity.getStatus().equals("COMPLETED")) {
            break;
        }
        Thread.sleep(2000L);
    }
    assertEquals("COMPLETED", generationEntity.getStatus());
  }

  /**
   * Basic test that defines a pathway analysis with a Target cohort of the observation period start-end, 
   * and 2 event cohorts that are constructed from the drug eras of 2 distinct drug concepts: 
   * Child 1 [Parent 1] (ID=2) and Child 2 [Parent 1] (ID=3) creating the following overlapping periods:
   * <pre>{@code
   * Target:  |-------------------------------------------------|
   *    EC1:  |---|  |------|
   *    EC2:    |-----------| |---| |-------|
   * 
   * Final pathway: EC1 -> EC1+EC2 -> EC2
   *      combo_id:  1        3        2
   * 
   * }</pre>
   * 
   * References:
   * /pathway/vocabulary.json: simple vocabulary with 1 parent, 5 children (of parent 1)
   * /pathway/simpleTest_PREP.json: the person, drug_era, and observation_period data needed to produce the above cohorts.
   * 
   * @throws Exception 
   */
  @Test
  public void test01_simplePathway() throws Exception {
    final String[] testDataSetsPaths = new String[] { "/pathway/vocabulary.json", "/pathway/simpleTest_PREP.json" };
     
    loadPrepData(testDataSetsPaths);
    
    // CDM data loaded, generate pathways
    PathwayAnalysisEntity entity = converter.convertToEntityAttribute(ResourceHelper.GetResourceAsString("/pathway/simpleTest_design.json"));      
    entity = pathwayService.importAnalysis(entity);
    
    generateAnalysis(entity);
    
    // Validate results
    // Load actual records from cohort table
    final IDatabaseConnection dbUnitCon = getConnection();
    final ITable pathwayCodes = dbUnitCon.createQueryTable(RESULT_SCHEMA_NAME + ".pathway_analysis_codes", 
            String.format("SELECT code, name, is_combo from %s ORDER BY code, name, is_combo", RESULT_SCHEMA_NAME + ".pathway_analysis_codes"));
    final ITable pathwayPaths = dbUnitCon.createQueryTable(RESULT_SCHEMA_NAME + ".pathway_analysis_paths", 
            String.format("SELECT target_cohort_id, step_1, step_2, step_3, step_4, step_5, step_6, step_7, step_8, step_9, step_10, count_value from %s ORDER BY target_cohort_id, step_1, step_2, step_3, step_4, step_5", RESULT_SCHEMA_NAME + ".pathway_analysis_paths"));
    final ITable pathwayStats = dbUnitCon.createQueryTable(RESULT_SCHEMA_NAME + ".pathway_analysis_stats", 
            String.format("SELECT target_cohort_id, target_cohort_count, pathways_count from %s ORDER BY target_cohort_id", RESULT_SCHEMA_NAME + ".pathway_analysis_stats"));
    
    final IDataSet actualDataSet = new CompositeDataSet(new ITable[] {pathwayCodes, pathwayPaths, pathwayStats});

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/pathway/simpleTest_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);     
    
    
  }
/**
   * A more advanced test that defines a pathway analysis with a Target cohort of the observation period start-end, 
   * and 2 event cohorts that are constructed from the drug eras of 2 distinct drug concepts, with a gap window of 15d: 
   * Child 1 [Parent 1] (ID=2) and Child 2 [Parent 1] (ID=3) creating the following overlapping periods:
   * <pre>{@code
   * Target:  |-------------------------------------------------|
   *    EC1:  |---|  |------|
   *    EC2:    |-----------| |---| |-------|
   * 
   * With 15d collapse (and re-arranged to show the overlapping periods:
   * Target:  |-------------------------------------------------|
   *    EC1:  |---|
   *    EC1:  |-------------|
   *    EC2:  |-------------|     
   *    EC2:                |---|
   *    EC2:                    |----------|
   * 
   * Note: EC1 gets collapse to cause an overlap between the first EC1 episode and second EC2 Episode
   * Final pathway: EC1+EC2 -> EC2
   *      combo_id:  3          2 
   * 
   * }</pre>
   * 
   * References:
   * /pathway/vocabulary.json: simple vocabulary with 1 parent, 5 children (of parent 1)
   * /pathway/collapseTest_PREP.json: the person, drug_era, and observation_period data needed to produce the above cohorts.
   * 
   * @throws Exception 
   */
  @Test
  public void test02_collapseWindow() throws Exception {
    final String[] testDataSetsPaths = new String[] { "/pathway/vocabulary.json", "/pathway/collapseTest_PREP.json" };
     
    loadPrepData(testDataSetsPaths);
        
    // CDM data loaded, generate pathways
    PathwayAnalysisEntity entity = converter.convertToEntityAttribute(ResourceHelper.GetResourceAsString("/pathway/collapseTest_design.json"));      
    entity = pathwayService.importAnalysis(entity);

    generateAnalysis(entity);

    // Validate results
    // Load actual records from cohort table
    final IDatabaseConnection dbUnitCon = getConnection();
    final ITable pathwayCodes = dbUnitCon.createQueryTable(RESULT_SCHEMA_NAME + ".pathway_analysis_codes", 
            String.format("SELECT code, name, is_combo from %s ORDER BY code, name, is_combo", RESULT_SCHEMA_NAME + ".pathway_analysis_codes"));
    final ITable pathwayPaths = dbUnitCon.createQueryTable(RESULT_SCHEMA_NAME + ".pathway_analysis_paths", 
            String.format("SELECT target_cohort_id, step_1, step_2, step_3, step_4, step_5, step_6, step_7, step_8, step_9, step_10, count_value from %s ORDER BY target_cohort_id, step_1, step_2, step_3, step_4, step_5", RESULT_SCHEMA_NAME + ".pathway_analysis_paths"));
    final ITable pathwayStats = dbUnitCon.createQueryTable(RESULT_SCHEMA_NAME + ".pathway_analysis_stats", 
            String.format("SELECT target_cohort_id, target_cohort_count, pathways_count from %s ORDER BY target_cohort_id", RESULT_SCHEMA_NAME + ".pathway_analysis_stats"));
    
    final IDataSet actualDataSet = new CompositeDataSet(new ITable[] {pathwayCodes, pathwayPaths, pathwayStats});

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/pathway/collapseTest_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);     
    
  }
  
}
