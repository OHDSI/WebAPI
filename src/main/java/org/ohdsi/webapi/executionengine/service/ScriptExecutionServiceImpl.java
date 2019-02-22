package org.ohdsi.webapi.executionengine.service;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.*;
import com.odysseusinc.arachne.execution_engine_common.util.ConnectionParams;
import jersey.repackaged.com.google.common.collect.ImmutableList;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysis;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisExecutionRepository;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisRepository;
import org.ohdsi.webapi.executionengine.dto.ExecutionRequestDTO;
import org.ohdsi.webapi.executionengine.entity.AnalysisExecution;
import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.executionengine.repository.AnalysisExecutionRepository;
import org.ohdsi.webapi.executionengine.repository.InputFileRepository;
import org.ohdsi.webapi.executionengine.repository.OutputFileRepository;
import org.ohdsi.webapi.executionengine.util.StringGenerationUtil;
import org.ohdsi.webapi.prediction.PatientLevelPredictionAnalysis;
import org.ohdsi.webapi.prediction.PatientLevelPredictionAnalysisRepository;
import org.ohdsi.webapi.service.HttpClient;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.DataSourceDTOParser;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
class ScriptExecutionServiceImpl implements ScriptExecutionService {

    private static final Log logger = LogFactory.getLog(ScriptExecutionServiceImpl.class);
    private static final String IMPALA_DATASOURCE = "impala";

    @Autowired
    private HttpClient client;

    @Autowired
    ComparativeCohortAnalysisExecutionRepository comparativeCohortAnalysisExecutionRepository;
    @Value("${executionengine.url}")
    private String executionEngineURL;
    @Value("${executionengine.token}")
    private String executionEngineToken;
    @Value("${executionengine.resultCallback}")
    private String resultCallback;
    @Value("${executionengine.updateStatusCallback}")
    private String updateStatusCallback;
    @Value("${execution.invalidation.maxage}")
    private int invalidateHours;
    @Autowired
    private OutputFileRepository outputFileRepository;

    private List<DBMSType> DBMS_REQUIRE_DB = ImmutableList.of(DBMSType.POSTGRESQL, DBMSType.REDSHIFT);

    @Autowired
    private SourceService sourceService;
    @Autowired
    private InputFileRepository inputFileRepository;

    @Autowired
    private ComparativeCohortAnalysisRepository comparativeCohortAnalysisRepository;
    @Autowired
    private JobExplorer jobExplorer;
    @Autowired
    private AnalysisExecutionRepository analysisExecutionRepository;
    @Autowired
    private PatientLevelPredictionAnalysisRepository patientLevelPredictionAnalysisRepository;

    private List<AnalysisExecution.Status> INVALIDATE_STATUSES = new ArrayList<>();

    ScriptExecutionServiceImpl() throws KeyManagementException, NoSuchAlgorithmException {

        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        INVALIDATE_STATUSES.add(AnalysisExecution.Status.RUNNING);
        INVALIDATE_STATUSES.add(AnalysisExecution.Status.STARTED);
        INVALIDATE_STATUSES.add(AnalysisExecution.Status.PENDING);
    }

    @Override
    public Long runScript(ExecutionRequestDTO dto, int analysisExecutionId) {

        Source source = findSourceByKey(dto.sourceKey);

        final String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
        final String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Vocabulary);
        if (vocabularyTableQualifier == null) {
            vocabularyTableQualifier = cdmTableQualifier;
        }

        AnalysisExecution execution = analysisExecutionRepository.findOne(analysisExecutionId);

        String name = getAnalysisName(dto);

        //replace var in R-script
        ConnectionParams connectionParams = DataSourceDTOParser.parse(source);
        final String script = processTemplate(dto, cdmTableQualifier, resultsTableQualifier, vocabularyTableQualifier, source.getSourceDialect(), connectionParams);
        AnalysisFile inputFile = new AnalysisFile();
        inputFile.setAnalysisExecution(execution);
        inputFile.setContents(script.getBytes());
        inputFile.setFileName(name + ".r");
        inputFileRepository.save(inputFile);

        final String analysisExecutionUrl = "/analyze";
        WebTarget webTarget = client.target(executionEngineURL + analysisExecutionUrl);
        MultiPart multiPart = buildRequest(buildAnalysisRequest(execution, source, execution.getUpdatePassword(), connectionParams), script);
        try {
                webTarget
                    .request(MediaType.MULTIPART_FORM_DATA_TYPE)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", executionEngineToken)
                    .post(Entity.entity(multiPart, multiPart.getMediaType()),
                            AnalysisRequestStatusDTO.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            execution.setExecutionStatus(AnalysisExecution.Status.FAILED);
            analysisExecutionRepository.save(execution);
        }
        return execution.getId().longValue();
    }

    @Override
    public Source findSourceByKey(final String key) {

        return sourceService.findBySourceKey(key);
    }

    private MultiPart buildRequest(AnalysisRequestDTO analysisRequestDTO, String script) {

        MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

        StreamDataBodyPart filePart = new StreamDataBodyPart("file",
                IOUtils.toInputStream(script),
                analysisRequestDTO.getExecutableFileName());
        multiPart.bodyPart(filePart);

        multiPart.bodyPart(
                new FormDataBodyPart("analysisRequest", analysisRequestDTO,
                        MediaType.APPLICATION_JSON_TYPE));
        return multiPart;
    }

    private AnalysisRequestDTO buildAnalysisRequest(AnalysisExecution execution, Source source, String password, ConnectionParams connectionParams) {
        AnalysisRequestDTO analysisRequestDTO = new AnalysisRequestDTO();
        Long executionId = execution.getId().longValue();
        analysisRequestDTO.setId(executionId);
        analysisRequestDTO.setDataSource(makeDataSourceDTO(source, connectionParams));
        analysisRequestDTO.setCallbackPassword(password);
        analysisRequestDTO.setRequested(new Date());
        if (IMPALA_DATASOURCE.equalsIgnoreCase(source.getSourceDialect()) && AuthMethod.KERBEROS == connectionParams.getAuthMethod()) {
            setKerberosParams(source, connectionParams, analysisRequestDTO.getDataSource());
        }
        String executableFileName = StringGenerationUtil.generateFileName(AnalysisRequestTypeDTO.R.name().toLowerCase());
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

    private void setKerberosParams(Source source, ConnectionParams connectionParams, DataSourceUnsecuredDTO ds) {
        ds.setUseKerberos(Boolean.TRUE);
        ds.setKrbAuthMethod(source.getKrbAuthMethod());
        ds.setKrbKeytab(source.getKrbKeytab());
        if (source.getKrbAdminServer() != null) {
            ds.setKrbAdminFQDN(source.getKrbAdminServer());
        } else {
            ds.setKrbAdminFQDN(connectionParams.getKrbFQDN());
        }
        ds.setKrbFQDN(connectionParams.getKrbFQDN());
        ds.setKrbRealm(connectionParams.getKrbRealm());
        ds.setKrbPassword(connectionParams.getPassword());
        ds.setKrbUser(connectionParams.getUser());
    }


    @Override
    public AnalysisExecution createAnalysisExecution(ExecutionRequestDTO dto, Source source, String password) {

        AnalysisExecution execution = new AnalysisExecution();
        execution.setAnalysisId(dto.cohortId);
        execution.setAnalysisType(dto.analysisType);
        execution.setDuration(0);
        execution.setSourceId(source.getSourceId());
        execution.setExecuted(new Date());
        execution.setExecutionStatus(AnalysisExecution.Status.STARTED);
        execution.setUserId(0); //Looks strange
        execution.setUpdatePassword(password);
        analysisExecutionRepository.saveAndFlush(execution);
        return execution;
    }

    private String getAnalysisName(ExecutionRequestDTO dto) {

        String name;

        switch (dto.analysisType){
            case CCA:
                ComparativeCohortAnalysis cca = comparativeCohortAnalysisRepository.findOne(dto.cohortId);
                name = cca.getName();
                break;
            case PLP:
                PatientLevelPredictionAnalysis plp = patientLevelPredictionAnalysisRepository.findOne(dto.cohortId);
                name = plp.getName();
                break;
            default:
                name = "";
                break;
        }
        return name;
    }

    @Override
    public String getExecutionStatus(Long executionId) {

        String status;
        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if (execution.getExecutionContext().containsKey("engineExecutionId")) {
            Long execId = execution.getExecutionContext().getLong("engineExecutionId");

            AnalysisExecution analysisExecution = analysisExecutionRepository.findOne(execId.intValue());
            if (analysisExecution == null) {
                throw new NotFoundException(String.format("Execution with id=%d was not found", executionId));
            }
            status = analysisExecution.getExecutionStatus().name();
        } else {
            status = AnalysisExecution.Status.PENDING.name();
        }
        return status;
    }

    @Override
    public List<AnalysisResultFile> getExecutionResultFiles(Integer executionId) {

        return outputFileRepository.findByExecutionId(executionId);
    }

    @Scheduled(fixedDelayString = "${execution.invalidation.period}")
    public void invalidateExecutions(){

        Date invalidate = DateUtils.addHours(new Date(), -invalidateHours);
        List<AnalysisExecution> executions = analysisExecutionRepository.findByExecutedBeforeAndExecutionStatusIn(invalidate, INVALIDATE_STATUSES);
        executions.forEach(exec -> {
            exec.setExecutionStatus(AnalysisExecution.Status.FAILED);
            analysisExecutionRepository.save(exec);
        });
    }

    private String processTemplate(ExecutionRequestDTO requestDTO,
                                   String cdmTable, String resultsTable,
                                   String vocabularyTable,
                                   String sourceDialect,
                                   ConnectionParams connectionParams) {

        String serverName;
        if (DBMS_REQUIRE_DB.stream().anyMatch(dbms -> dbms.getOhdsiDB().equalsIgnoreCase(sourceDialect))) {
            serverName = connectionParams.getServer() + "/" + connectionParams.getSchema();
        } else {
            serverName = connectionParams.getServer();
        }

        String temp = requestDTO.template
                .replace("dbms = \"postgresql\"", "dbms = \"" + connectionParams.getDbms() + "\"")
                .replace("server = \"localhost/ohdsi\"", "server = \"" + serverName + "\"")
                .replace("user = \"joe\"", "user = \"" + connectionParams.getUser() + "\"")
                .replace("my_cdm_data", cdmTable)
                .replace("my_vocabulary_data", vocabularyTable)
                .replace("my_results", resultsTable)
                .replace("exposure_database_schema", resultsTable)
                .replace("outcome_database_schema", resultsTable)
                .replace("exposure_table", "cohort")
                .replace("outcome_table", "cohort")
                .replace("cohort_table", "cohort")
//                .replace("exposureTable <- \"exposure_table\"", "")
//                .replace("outcomeTable <- \"outcome_table\"", "")

                .replace("cdmVersion <- \"5\"",
                        "cdmVersion <- \"" + requestDTO.cdmVersion + "\"")
                .replace("<insert your " + "directory here>",
                        requestDTO.workFolder);
        if (IMPALA_DATASOURCE.equalsIgnoreCase(sourceDialect)) {
            temp = temp.replace("password = \"supersecret\"", "password = \""
                    + connectionParams.getPassword() + "\", "
                    + "schema=\"" + connectionParams.getSchema() + "\", "
                    + "extraSettings=\"" + connectionParams.getExtraSettings() + "\", "
                    + "pathToDriver=\"/impala/\"");

        } else {
            temp = temp.replace("password = \"supersecret\"", "password = \"" + connectionParams.getPassword() + "\"");
        }

        if (connectionParams.getPort() != null) {
            temp = temp.replace("port = 5432", "port = " + connectionParams.getPort());
        } else {
            temp = temp.replace( "port = 5432,", "");
        }
        //uncommenting package installation
        return temp
                .replace("true", "TRUE")
                .replace("false", "FALSE");
    }

    private DataSourceUnsecuredDTO makeDataSourceDTO(Source source, ConnectionParams connectionParams) {

        DataSourceUnsecuredDTO dto = DataSourceDTOParser.parseDTO(source, connectionParams);
        dto.setCdmSchema(source.getTableQualifier(SourceDaimon.DaimonType.CDM));
        return dto;
    }

}
