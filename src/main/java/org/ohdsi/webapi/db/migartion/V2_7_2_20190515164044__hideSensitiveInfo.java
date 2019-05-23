package org.ohdsi.webapi.db.migartion;

import com.odysseusinc.arachne.commons.config.flyway.ApplicationContextAwareSpringMigration;
import org.apache.commons.collections.map.HashedMap;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContent;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContentList;
import org.ohdsi.webapi.executionengine.service.AnalysisResultFileContentSensitiveInfoService;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Variables.SOURCE;

@Component
public class V2_7_2_20190515164044__hideSensitiveInfo implements ApplicationContextAwareSpringMigration {
    private final Logger log = LoggerFactory.getLogger(V2_7_2_20190515164044__hideSensitiveInfo.class);
    private final static String SQL_PATH = "/db/migration/java/V2_7_2_20190515164044__hideSensitiveInfo/";

    @Autowired
    private Environment env;

    @Autowired
    private MigrationDAO migrationDAO;

    @Autowired
    private AnalysisResultFileContentSensitiveInfoService sensitiveInfoService;

    @Override
    public void migrate() throws Exception {
        String webAPISchema = this.env.getProperty("spring.jpa.properties.hibernate.default_schema");

        try {
            Map<Integer, Source> sourceMap = migrationDAO.getSourceData(webAPISchema);
            List<ExecutionData> executions = migrationDAO.getExecutions(webAPISchema);

            for (ExecutionData execution : executions) {
                Source source = sourceMap.get(execution.executionId);
                // Variables must contain field "sourceName". See sensitive_filters.csv
                Map<String, Object> variables = Collections.singletonMap(SOURCE, source);

                AnalysisResultFileContentList contentList = new AnalysisResultFileContentList();
                for(OutputFile outputFile: execution.files) {
                    byte[] content = migrationDAO.getFileContent(webAPISchema, outputFile.id);
                    // Before changing implementaion of AnalysisResultFile or AnalysisResultFileContent check which fields are used in
                    // sensitive info filter (AnalysisResultFileContentSensitiveInfoServiceImpl)
                    AnalysisResultFile resultFile = new AnalysisResultFile();
                    resultFile.setFileName(outputFile.filename);
                    resultFile.setId(outputFile.id);
                    AnalysisResultFileContent resultFileContent = new AnalysisResultFileContent();
                    resultFileContent.setAnalysisResultFile(resultFile);
                    resultFileContent.setContents(content);
                    contentList.getFiles().add(resultFileContent);
                }

                // We have to filter all files for current execution because of possibility of archives split into volumes
                // Volumes will be removed during decompressing and compressing
                contentList = sensitiveInfoService.filterSensitiveInfo(contentList, variables);

                // Update content of files only if all files were processed successfully
                if(contentList.isSuccessfullyFiltered()) {
                    for (AnalysisResultFileContent resultFileContent : contentList.getFiles()) {
                        try {
                            migrationDAO.updateFileContent(webAPISchema, resultFileContent.getAnalysisResultFile().getId(), resultFileContent.getContents());
                        } catch (Exception e) {
                            log.error("Error updating file content for file with id: {}", resultFileContent.getAnalysisResultFile().getId(), e);
                        }
                    }
                    // Get list of ids of files (archive volumes) that are not used anymore
                    // and delete them from database
                    Set<Long> rowIds = contentList.getFiles().stream()
                            .map(file -> file.getAnalysisResultFile().getId())
                            .collect(Collectors.toSet());
                    execution.files.stream()
                            .filter(file -> !rowIds.contains(file.id))
                            .forEach(file -> {
                                try {
                                    migrationDAO.deleteFileAndContent(webAPISchema, file.id);
                                } catch (Exception e) {
                                    log.error("Error deleting file content for file with id: {}", file.id, e);
                                }
                            });
                } else {
                    log.error("Error migrating file content. See errors above");
                }
            }
        } catch (Exception e) {
            log.error("Error migrating file content", e);
        }
    }

    @Service
    public static class MigrationDAO extends AbstractDaoService{
        public List<ExecutionData> getExecutions(String webAPISchema) {
            String[] params = new String[]{"webapi_schema"};
            String[] values = new String[]{webAPISchema};

            String generatedDesignSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(SQL_PATH + "getOutputFilesData.sql"), params, values);
            String translatedSql = SqlTranslate.translateSingleStatementSql(generatedDesignSql, getDialect());

            Map<Integer, ExecutionData> executionMap = new HashMap<>();
            return getJdbcTemplate().query(translatedSql, rs -> {
                while (rs.next()) {
                    OutputFile outputFile = new OutputFile();
                    outputFile.filename = rs.getString("file_name");
                    outputFile.id = rs.getLong("id");

                    int executionId = rs.getInt("execution_id");
                    ExecutionData execution = executionMap.get(executionId);
                    if(execution == null) {
                        execution = new ExecutionData();
                        execution.executionId = executionId;
                        executionMap.put(executionId, execution);
                    }

                    execution.files.add(outputFile);
                }
                return new ArrayList<>(executionMap.values());
            });
        }

        public Map<Integer, Source> getSourceData(String webAPISchema) {
            String[] params = new String[]{"webapi_schema"};
            String[] values = new String[]{webAPISchema};

            String generatedDesignSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(SQL_PATH + "getSourceData.sql"), params, values);
            String translatedSql = SqlTranslate.translateSingleStatementSql(generatedDesignSql, getDialect());

            return getJdbcTemplate().query(translatedSql, rs -> {
                Map<Integer, Source> result = new HashedMap();
                while (rs.next()) {
                    Source source = new Source();
                    source.sourceName = rs.getString("source_name");

                    int executionId = rs.getInt("execution_id");
                    result.put(executionId, source);
                }
                return result;
            });
        }

        public byte[] getFileContent(String webAPISchema, long id) {
            String[] params = new String[]{"webapi_schema", "id"};
            String[] values = new String[]{webAPISchema, String.valueOf(id)};

            String generatedDesignSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(SQL_PATH + "getOutputFileContent.sql"), params, values);
            String translatedSql = SqlTranslate.translateSingleStatementSql(generatedDesignSql, getDialect());

            return getJdbcTemplate().query(translatedSql, rs -> {
                while (rs.next()) {
                    return rs.getBytes("file_contents");
                }
                return null;
            });
        }

        public void updateFileContent(String webAPISchema, long id, byte[] content) {
            String[] params = new String[]{"webapi_schema"};
            String[] values = new String[]{webAPISchema};

            String generatedDesignSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(SQL_PATH + "updateFileContent.sql"), params, values);
            String translatedSql = SqlTranslate.translateSingleStatementSql(generatedDesignSql, getDialect());

            Object[] psValues = new Object[] {content, id};

            getJdbcTemplate().update(translatedSql, psValues);
        }

        public void deleteFileAndContent(String webAPISchema, Long id) {
            String[] params = new String[]{"webapi_schema"};
            String[] values = new String[]{webAPISchema};

            String generatedDesignSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(SQL_PATH + "deleteFileContent.sql"), params, values);
            String translatedSql = SqlTranslate.translateSingleStatementSql(generatedDesignSql, getDialect());

            Object[] psValues = new Object[] {id};

            getJdbcTemplate().update(translatedSql, psValues);

            generatedDesignSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(SQL_PATH + "deleteFile.sql"), params, values);
            translatedSql = SqlTranslate.translateSingleStatementSql(generatedDesignSql, getDialect());

            getJdbcTemplate().update(translatedSql, psValues);
        }
    }

    private static class ExecutionData {
        public int executionId;
        public List<OutputFile> files = new ArrayList<>();
    }

    private static class OutputFile {
        public long id;
        public String filename;
    }

    private static class Source {
        public String sourceName;
    }
}
