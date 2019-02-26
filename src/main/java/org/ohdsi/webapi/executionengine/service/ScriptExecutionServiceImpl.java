package org.ohdsi.webapi.executionengine.service;

import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.AnalysisRequestDTO;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.AnalysisRequestStatusDTO;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;
import jersey.repackaged.com.google.common.collect.ImmutableList;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineGenerationEntity;
import org.ohdsi.webapi.executionengine.repository.AnalysisExecutionRepository;
import org.ohdsi.webapi.executionengine.repository.ExecutionEngineGenerationRepository;
import org.ohdsi.webapi.executionengine.repository.InputFileRepository;
import org.ohdsi.webapi.executionengine.repository.OutputFileRepository;
import org.ohdsi.webapi.service.HttpClient;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.shiro.management.datasource.SourceAccessor;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.DataSourceDTOParser;
import org.ohdsi.webapi.util.SourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.ohdsi.webapi.Constants.Variables.SOURCE;

@Service
@Transactional
class ScriptExecutionServiceImpl implements ScriptExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(ScriptExecutionServiceImpl.class);

    private List<ExecutionEngineAnalysisStatus.Status> INVALIDATE_STATUSES = new ArrayList<>();

    @Autowired
    private HttpClient client;

    @Value("${executionengine.url}")
    private String executionEngineURL;
    @Value("${executionengine.token}")
    private String executionEngineToken;
    @Value("${executionengine.resultCallback}")
    private String resultCallback;
    @Value("${executionengine.updateStatusCallback}")
    private String updateStatusCallback;
    @Autowired
    private OutputFileRepository outputFileRepository;

    private List<ExecutionEngineAnalysisStatus.Status> FINAL_STATUES = ImmutableList.of(ExecutionEngineAnalysisStatus.Status.COMPLETED, ExecutionEngineAnalysisStatus.Status.COMPLETED);

    @Autowired
    private SourceService sourceService;
    @Autowired
    private InputFileRepository inputFileRepository;

    @Autowired
    private JobExplorer jobExplorer;
    @Autowired
    private AnalysisExecutionRepository analysisExecutionRepository;

    @Autowired
    private AnalysisResultFileSensitiveInfoService sensitiveInfoService;

    @Autowired
    private ExecutionEngineGenerationRepository executionEngineGenerationRepository;

    @Autowired
    private SourceAccessor sourceAccessor;

    ScriptExecutionServiceImpl() throws KeyManagementException, NoSuchAlgorithmException {

        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        INVALIDATE_STATUSES.add(ExecutionEngineAnalysisStatus.Status.RUNNING);
        INVALIDATE_STATUSES.add(ExecutionEngineAnalysisStatus.Status.STARTED);
        INVALIDATE_STATUSES.add(ExecutionEngineAnalysisStatus.Status.PENDING);
    }

    @Override
    public void runScript(Long executionId, Source source, List<AnalysisFile> files, String updatePassword,
                          String executableFilename, String targetTable) {

        DataSourceUnsecuredDTO dataSourceData = DataSourceDTOParser.parseDTO(source);
        dataSourceData.setCohortTargetTable(targetTable);
        dataSourceData.setTargetSchema(SourceUtils.getTempQualifier(source));

        final String analysisExecutionUrl = "/analyze";
        WebTarget webTarget = client.target(executionEngineURL + analysisExecutionUrl);
        MultiPart multiPart = buildRequest(buildAnalysisRequest(executionId, dataSourceData, updatePassword, executableFilename), files);

        webTarget
            .request(MediaType.MULTIPART_FORM_DATA_TYPE)
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", executionEngineToken)
            .post(Entity.entity(multiPart, multiPart.getMediaType()),
                    AnalysisRequestStatusDTO.class);
    }

    @Override
    public Source findSourceByKey(final String key) {

        return sourceService.findBySourceKey(key);
    }

    private MultiPart buildRequest(AnalysisRequestDTO analysisRequestDTO, List<AnalysisFile> files) {

        MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

        files.forEach(file -> {
            StreamDataBodyPart filePart = new StreamDataBodyPart("file",
                    new ByteArrayInputStream(file.getContents()),
                    file.getFileName());
            multiPart.bodyPart(filePart);
        });

        multiPart.bodyPart(
                new FormDataBodyPart("analysisRequest", analysisRequestDTO,
                        MediaType.APPLICATION_JSON_TYPE));
        return multiPart;
    }

    private AnalysisRequestDTO buildAnalysisRequest(Long executionId, DataSourceUnsecuredDTO dataSourceData, String password,
                                                    String executableFileName) {

        AnalysisRequestDTO analysisRequestDTO = new AnalysisRequestDTO();
        analysisRequestDTO.setId(executionId);
        analysisRequestDTO.setDataSource(dataSourceData);
        analysisRequestDTO.setCallbackPassword(password);
        analysisRequestDTO.setRequested(new Date());
        analysisRequestDTO.setExecutableFileName(executableFileName);
        analysisRequestDTO.setResultCallback(
                StrSubstitutor.replace(resultCallback,
                        ImmutableMap.of("id", executionId,
                                "password", password
                        ),
                        "{", "}"));
        analysisRequestDTO.setUpdateStatusCallback(
                StrSubstitutor.replace(updateStatusCallback,
                        ImmutableMap.of("id", executionId,
                                "password", password
                        ),
                        "{", "}"));
        return analysisRequestDTO;
    }

    @Override
    public ExecutionEngineAnalysisStatus createAnalysisExecution(Long jobId, Source source, String password, List<AnalysisFile> analysisFiles) {

        ExecutionEngineGenerationEntity executionEngineGenerationEntity = executionEngineGenerationRepository.findOne(jobId);
        ExecutionEngineAnalysisStatus execution = new ExecutionEngineAnalysisStatus();
        execution.setExecutionStatus(ExecutionEngineAnalysisStatus.Status.STARTED);
        execution.setExecutionEngineGeneration(executionEngineGenerationEntity);
        ExecutionEngineAnalysisStatus saved = analysisExecutionRepository.saveAndFlush(execution);
        if (Objects.nonNull(analysisFiles)) {
            analysisFiles.forEach(file -> file.setAnalysisExecution(saved));
            inputFileRepository.save(analysisFiles);
        }
        return saved;
    }

    @Override
    public String getExecutionStatus(Long executionId) {

        String status;
        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if (execution.getExecutionContext().containsKey("engineExecutionId")) {
            Long execId = execution.getExecutionContext().getLong("engineExecutionId");

            ExecutionEngineAnalysisStatus analysisExecution = analysisExecutionRepository.findOne(execId.intValue());
            if (analysisExecution == null) {
                throw new NotFoundException(String.format("Execution with id=%d was not found", executionId));
            }
            status = analysisExecution.getExecutionStatus().name();
        } else {
            status = ExecutionEngineAnalysisStatus.Status.PENDING.name();
        }
        return status;
    }

    @Override
    public void updateAnalysisStatus(ExecutionEngineAnalysisStatus analysisExecution, ExecutionEngineAnalysisStatus.Status status) {

        if (FINAL_STATUES.stream().noneMatch(s -> Objects.equals(s, status))) {
            analysisExecution.setExecutionStatus(status);
            analysisExecutionRepository.saveAndFlush(analysisExecution);
        }
    }

    @PostConstruct
    public void invalidateOutdatedAnalyses() {

        logger.info("Invalidating execution engine based analyses");
        List<ExecutionEngineAnalysisStatus> outdateExecutions = analysisExecutionRepository.findByExecutionStatusIn(INVALIDATE_STATUSES);
        outdateExecutions.forEach(ee -> ee.setExecutionStatus(ExecutionEngineAnalysisStatus.Status.FAILED));
    }

    @Override
    public File getExecutionResult(Long executionId) throws IOException {

        ExecutionEngineGenerationEntity executionEngineGeneration = executionEngineGenerationRepository.findById(executionId)
                .orElseThrow(NotFoundException::new);
        sourceAccessor.checkAccess(executionEngineGeneration.getSource());
        ExecutionEngineAnalysisStatus analysisExecution = executionEngineGeneration.getAnalysisExecution();

        java.nio.file.Path tempDirectory = Files.createTempDirectory("atlas_ee_arch");
        String fileName = "execution_" + executionId + "_result.zip";
        File archive = tempDirectory.resolve(fileName).toFile();
        archive.deleteOnExit();
        Map<String, Object> variables = Collections.singletonMap(SOURCE, analysisExecution.getExecutionEngineGeneration().getSource());

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archive))) {
            List<AnalysisResultFile> outputFiles = analysisExecution.getResultFiles(); //outputFileRepository.findByExecutionId(analysisExecution.getId());
            for (AnalysisResultFile resultFile : outputFiles) {
                ZipEntry entry = new ZipEntry(resultFile.getFileName());
                entry.setSize(sensitiveInfoService.filterSensitiveInfo(resultFile, variables).getContents().length);
                zos.putNextEntry(entry);
                zos.write(resultFile.getContents());
                zos.closeEntry();
            }
        }

        return archive;
    }
}
