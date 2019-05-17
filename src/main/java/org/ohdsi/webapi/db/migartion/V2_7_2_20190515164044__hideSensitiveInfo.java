package org.ohdsi.webapi.db.migartion;

import com.odysseusinc.arachne.commons.config.flyway.ApplicationContextAwareSpringMigration;
import org.apache.commons.collections.map.HashedMap;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContent;
import org.ohdsi.webapi.executionengine.service.AnalysisResultFileContentSensitiveInfoService;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.ohdsi.webapi.Constants.Variables.SOURCE;

@Component
public class V2_7_2_20190515164044__hideSensitiveInfo implements ApplicationContextAwareSpringMigration {
    private final Logger log = LoggerFactory.getLogger(V2_7_2_20190515164044__hideSensitiveInfo.class);
    private final static String SQL_PATH = "/db/migration/java/V2_7_2_20190515164044__hideSensitiveInfo/";

//    @Autowired
//    private HideSensitiveInfoMigration hideSensitiveInfoMigration;

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
            List<OutputFile> outputFiles = migrationDAO.getOutputFiles(webAPISchema);

            for (OutputFile outputFile : outputFiles) {
                Source source = sourceMap.get(outputFile.executionId);
                // Variables must contain field "sourceName". See sensitive_filters.csv
                Map<String, Object> variables = Collections.singletonMap(SOURCE, source);
                byte[] content = migrationDAO.getFileContent(webAPISchema, outputFile.id);
                // Before changing implementaion of AnalysisResultFile or AnalysisResultFileContent check which fields are used in
                // sensitive info filter (AnalysisResultFileContentSensitiveInfoServiceImpl)
                AnalysisResultFile resultFile = new AnalysisResultFile();
                resultFile.setFileName(outputFile.filename);
                AnalysisResultFileContent resultFileContent = new AnalysisResultFileContent();
                resultFileContent.setAnalysisResultFile(resultFile);
                resultFileContent.setContents(content);

                try {
                    resultFileContent = sensitiveInfoService.filterSensitiveInfo(resultFileContent, variables);
                } catch (Exception e) {
                    log.error("Error filtering sensitive info for output file with id:{}", outputFile.id, e);
                }
                migrationDAO.updateFileContent(webAPISchema, outputFile.id, resultFileContent.getContents());
            }
        } catch (Exception e) {
            log.error("Error migrating file content", e);
        }
    }

    @Service
    public static class MigrationDAO extends AbstractDaoService{
        public List<OutputFile> getOutputFiles(String webAPISchema) {
            String[] params = new String[]{"webapi_schema"};
            String[] values = new String[]{webAPISchema};

            String generatedDesignSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(SQL_PATH + "getOutputFilesData.sql"), params, values);
            String translatedSql = SqlTranslate.translateSingleStatementSql(generatedDesignSql, getDialect());

            return getJdbcTemplate().query(translatedSql, rs -> {
                List<OutputFile> result = new ArrayList<>();
                while (rs.next()) {
                    OutputFile outputFile = new OutputFile();
                    outputFile.filename = rs.getString("file_name");
                    outputFile.id = rs.getLong("id");
                    outputFile.executionId = rs.getInt("execution_id");
                    result.add(outputFile);
                }
                return result;
            });
        }

        public Map<Integer, Source> getSourceData(String webAPISchema) {
            String[] params = new String[]{"webapi_schema", "webapi_schema", "webapi_schema"};
            String[] values = new String[]{webAPISchema, webAPISchema, webAPISchema};

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
    }

    private static class OutputFile {
        public long id;
        public int executionId;
        public String filename;
    }

    private static class Source {
        public String sourceName;
    }
}
