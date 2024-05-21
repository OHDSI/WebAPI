package org.ohdsi.webapi.executionengine.service;

import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.AnalysisRequestDTO;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.AnalysisRequestStatusDTO;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;
import com.odysseusinc.arachne.execution_engine_common.util.CommonFileUtils;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.ohdsi.webapi.JobInvalidator;
import org.ohdsi.webapi.exception.AtlasException;
import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineGenerationEntity;
import org.ohdsi.webapi.executionengine.repository.AnalysisExecutionRepository;
import org.ohdsi.webapi.executionengine.repository.ExecutionEngineGenerationRepository;
import org.ohdsi.webapi.executionengine.repository.InputFileRepository;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.HttpClient;
import org.ohdsi.webapi.source.SourceService;
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
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.ohdsi.webapi.executionengine.service.AnalysisZipUtils.getHeadersForFilesThatWillBeAddedToZip;

@Service
@Transactional
class ScriptExecutionServiceImpl extends AbstractDaoService implements ScriptExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(ScriptExecutionServiceImpl.class);
    private static final String REQUEST_FILENAME = "request.zip";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ARACHNE_COMPRESSED_HEADER = "arachne-compressed";
    private static final String ARACHNE_WAITING_COMPRESSED_RESULT_HEADER = "arachne-waiting-compressed-result";
    private static final String TEMPDIR_PREFIX = "webapi-exec";

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
    @Value("${executionengine.resultExclusions}")
    private String resultExclusions;

    private static List<ExecutionEngineAnalysisStatus.Status> INVALIDATE_STATUSES = ImmutableList.of(
            ExecutionEngineAnalysisStatus.Status.RUNNING,
            ExecutionEngineAnalysisStatus.Status.STARTED,
            ExecutionEngineAnalysisStatus.Status.PENDING
    );


    @Autowired
    private SourceService sourceService;
    @Autowired
    private InputFileRepository inputFileRepository;

    @Autowired
    private JobExplorer jobExplorer;
    @Autowired
    private JobInvalidator jobInvalidator;

    @Autowired
    private AnalysisExecutionRepository analysisExecutionRepository;

    @Autowired
    private ExecutionEngineGenerationRepository executionEngineGenerationRepository;

    @Autowired
    private SourceAccessor sourceAccessor;

    ScriptExecutionServiceImpl() throws KeyManagementException, NoSuchAlgorithmException {

        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }

    @Override
    public void runScript(Long executionId, Source source, List<AnalysisFile> files, String updatePassword,
                          String executableFilename, String targetTable) {

        DataSourceUnsecuredDTO dataSourceData = DataSourceDTOParser.parseDTO(source);
        dataSourceData.setCohortTargetTable(targetTable);
        dataSourceData.setTargetSchema(SourceUtils.getTempQualifier(source));

        final String analysisExecutionUrl = "/analyze";
        WebTarget webTarget = client.target(executionEngineURL + analysisExecutionUrl);
        try{
            File tempDir = Files.createTempDirectory(TEMPDIR_PREFIX).toFile();
            try{
                saveFilesToTempDir(tempDir, files);
                try(MultiPart multiPart = buildRequest(buildAnalysisRequest(executionId, dataSourceData, updatePassword, executableFilename), tempDir)) {

                    File zipFile = new File(tempDir, REQUEST_FILENAME);
                    CommonFileUtils.compressAndSplit(tempDir, zipFile, null);
                    try(InputStream in = new FileInputStream(zipFile)) {
                        StreamDataBodyPart filePart = new StreamDataBodyPart("file", in, zipFile.getName());
                        multiPart.bodyPart(filePart);

                        webTarget
                                .request(MediaType.MULTIPART_FORM_DATA_TYPE)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(AUTHORIZATION_HEADER, executionEngineToken)
                                .header(ARACHNE_COMPRESSED_HEADER, "true")
                                .header(ARACHNE_WAITING_COMPRESSED_RESULT_HEADER, "true")
                                .post(Entity.entity(multiPart, multiPart.getMediaType()),
                                        AnalysisRequestStatusDTO.class);
                    }
                }
            }finally {
                FileUtils.deleteQuietly(tempDir);
            }
        }catch (IOException e) {
            log.error("Failed to compress request files", e);
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Source findSourceByKey(final String key) {

        return sourceService.findBySourceKey(key);
    }

    private void saveFilesToTempDir(File tempDir, List<AnalysisFile> files) {

        files.forEach(file -> {
            try(OutputStream out = new FileOutputStream(new File(tempDir, file.getFileName()))) {
                IOUtils.write(file.getContents(), out);
            }catch (IOException e) {
                log.error("Cannot build request to ExecutionEngine", e);
                throw new InternalServerErrorException();
            }
        });
    }

    private MultiPart buildRequest(AnalysisRequestDTO analysisRequestDTO, File tempDir) throws ZipException, IOException {

        MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

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
        analysisRequestDTO.setResultExclusions(resultExclusions);
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
    public void invalidateExecutions(Date invalidateDate) {

        getTransactionTemplateRequiresNew().execute(status -> {
            logger.info("Invalidating execution engine based analyses");
            List<ExecutionEngineAnalysisStatus> executions = analysisExecutionRepository.findAllInvalidAnalysis(invalidateDate, ScriptExecutionServiceImpl.INVALIDATE_STATUSES);

            executions.forEach(exec -> {
                exec.setExecutionStatus(ExecutionEngineAnalysisStatus.Status.FAILED);
                jobInvalidator.invalidateJobExecutionById(exec);
            });
            analysisExecutionRepository.save(executions);
            return null;
        });
    }

    @PostConstruct
    public void invalidateOutdatedAnalyses() {

        invalidateExecutions(new Date());
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

        try {
            ZipFile resultZip = new ZipFile(archive);

            List<AnalysisResultFile> zipFiles = analysisExecution.getResultFiles().stream()
                    .filter(resultFile ->
                            AnalysisZipUtils.isResultArchive(resultFile.getFileName()) ||
                            AnalysisZipUtils.isResultArchiveVolume(resultFile.getFileName()))
                    .collect(Collectors.toList());

            List<AnalysisResultFile> otherFiles = analysisExecution.getResultFiles().stream()
                    .filter(resultFile -> !zipFiles.contains(resultFile))
                    .collect(Collectors.toList());

            for (AnalysisResultFile resultFile : otherFiles) {
                addFileToZip(resultZip, resultFile);
            }

            copyContentOfOneZipToAnotherZip(zipFiles, resultZip,tempDirectory);
        } catch (ZipException e) {
            throw new AtlasException("Cannot process zip archive result", e);
        }
        return archive;
    }


    private void addFileToZip(ZipFile resultZip, AnalysisResultFile resultFile) throws ZipException {

        resultZip.addStream(
                new ByteArrayInputStream(resultFile.getContents()),
                getHeadersForFilesThatWillBeAddedToZip(resultFile.getFileName())
        );
    }

    private void copyContentOfOneZipToAnotherZip(List<AnalysisResultFile> zipWithMultivolume, ZipFile resultZip, Path tempDirectory) throws IOException, ZipException {

        if (CollectionUtils.isEmpty(zipWithMultivolume)) {
            return;
        }

        Optional<AnalysisResultFile> zipAnalysisFileOpt = zipWithMultivolume.stream()
                .filter(file -> AnalysisZipUtils.isArchive(file.getFileName()))
                .findFirst();

        if (zipAnalysisFileOpt.isPresent()) {

            AnalysisResultFile zipAnalysisFile = zipAnalysisFileOpt.orElse(null);
            File zipFile = saveZipFileToTempDirectory(zipAnalysisFile, tempDirectory);
            saveZipVolumeFilesToTempDirectory(zipWithMultivolume, tempDirectory);
            ZipFile outZipFile = new ZipFile(zipFile);
            //getFileHeaders return not generic List, that is already fixed in the last version of library
            for (FileHeader header : (List< FileHeader>) outZipFile.getFileHeaders()) {
                resultZip.addStream(
                        outZipFile.getInputStream(header),
                        getHeadersForFilesThatWillBeAddedToZip(header.getFileName())
                );
            }
        }
    }

    private void saveZipVolumeFilesToTempDirectory(List<AnalysisResultFile> resultFiles, Path tempDirectory) throws IOException {
        for (AnalysisResultFile resultFile : resultFiles) {
            if (AnalysisZipUtils.isResultArchiveVolume(resultFile.getFileName())) {
                saveZipFileToTempDirectory(resultFile, tempDirectory);
            }
        }
    }

    private File saveZipFileToTempDirectory(AnalysisResultFile resultFile, Path tempDirectory) throws IOException {

        File file = tempDirectory.resolve(resultFile.getFileName()).toFile();
        FileUtils.writeByteArrayToFile(file, resultFile.getContents());
        return file;
    }
}
