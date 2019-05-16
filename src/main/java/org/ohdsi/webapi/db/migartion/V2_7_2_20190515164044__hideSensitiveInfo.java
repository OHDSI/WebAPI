package org.ohdsi.webapi.db.migartion;

import com.odysseusinc.arachne.commons.config.flyway.ApplicationContextAwareSpringMigration;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContent;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.ohdsi.webapi.executionengine.repository.AnalysisResultFileContentRepository;
import org.ohdsi.webapi.executionengine.service.AnalysisResultFileContentSensitiveInfoService;
import org.ohdsi.webapi.source.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.ohdsi.webapi.Constants.Variables.SOURCE;

@Component
public class V2_7_2_20190515164044__hideSensitiveInfo implements ApplicationContextAwareSpringMigration {
    private final Logger log = LoggerFactory.getLogger(V2_7_2_20190515164044__hideSensitiveInfo.class);

    @Autowired
    private HideSensitiveInfoMigration hideSensitiveInfoMigration;

    @Override
    public void migrate() throws Exception {
        // Logic is moved to inner class because we need session to access
        // lazy fields, but migration class V2_7_2_20190515164044__hideSensitiveInfo can't have @Transactional annotation
        hideSensitiveInfoMigration.filterAllSensitiveInfo();
    }

    @Component
    public class HideSensitiveInfoMigration {
        @Autowired
        private AnalysisResultFileContentSensitiveInfoService sensitiveInfoService;

        @Autowired
        private AnalysisResultFileContentRepository analysisResultFileContentRepository;

        @Transactional
        public void filterAllSensitiveInfo() {
            List<AnalysisResultFileContent> fileContents = analysisResultFileContentRepository.findAll();

            for(AnalysisResultFileContent fileContent: fileContents) {
                ExecutionEngineAnalysisStatus analysisExecution = fileContent.getAnalysisResultFile().getExecution();
                Source source = analysisExecution.getExecutionEngineGeneration().getSource();
                Map<String, Object> variables = Collections.singletonMap(SOURCE, source);

                try {
                    AnalysisResultFileContent resultFileContent = sensitiveInfoService.filterSensitiveInfo(fileContent, variables);
                    analysisResultFileContentRepository.save(resultFileContent);
                } catch (Exception e) {
                    log.error("error filtering sensitive info during migration", e);
                }
            }
            log.info("done filtering sensitive info during migration");
        }
    }
}
