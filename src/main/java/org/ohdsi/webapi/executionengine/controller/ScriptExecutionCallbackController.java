package org.ohdsi.webapi.executionengine.controller;

import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.AnalysisExecutionStatusDTO;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.AnalysisResultDTO;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.AnalysisResultStatusDTO;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.executionengine.entity.AnalysisExecution;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.executionengine.exception.ScriptCallbackException;
import org.ohdsi.webapi.executionengine.repository.AnalysisExecutionRepository;
import org.ohdsi.webapi.executionengine.repository.OutputFileRepository;
import org.ohdsi.webapi.executionengine.service.AnalysisResultFileSensitiveInfoService;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;

import static org.ohdsi.webapi.Constants.Variables.SOURCE;
import static org.ohdsi.webapi.executionengine.entity.AnalysisExecution.Status.*;

@Controller
@Path("/executionservice/callbacks")
public class ScriptExecutionCallbackController {

    private static final Logger log = LoggerFactory.getLogger(ScriptExecutionCallbackController.class);
    private static final String EXECUTION_NOT_FOUND = "Analysis execution with id {%d} not found";

    private final AnalysisExecutionRepository analysisExecutionRepository;

    private final OutputFileRepository outputFileRepository;

    @Autowired
    public ScriptExecutionCallbackController(AnalysisExecutionRepository analysisExecutionRepository,
                                             OutputFileRepository outputFileRepository) {

        this.analysisExecutionRepository = analysisExecutionRepository;
        this.outputFileRepository = outputFileRepository;
    }

    @Path(value = "submission/{id}/status/update/{password}")
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Transactional
    public void statusUpdate(@PathParam("id") Long id,
                             @PathParam("password") String password,
                             AnalysisExecutionStatusDTO status) {

        log.info("Accepted an updateSubmission request. ID:{}, Update date:{} Log: {}",
                        status.getId(), status.getStdoutDate(), status.getStdout());
        AnalysisExecution analysisExecution = analysisExecutionRepository.findByJobExecutionId(id)
                .orElseThrow(() -> new ScriptCallbackException(String.format(EXECUTION_NOT_FOUND, id)));
        if (Objects.equals(password, analysisExecution.getUpdatePassword())
                && ( analysisExecution.getExecutionStatus().equals(STARTED)
                || analysisExecution.getExecutionStatus().equals(RUNNING))
                ) {

            analysisExecution.setExecutionStatus(RUNNING);
            analysisExecutionRepository.saveAndFlush(analysisExecution);
        }
    }

    @Path(value = "submission/{id}/result/{password}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @POST
    @Transactional
    public void analysisResult(@PathParam("id") Long id,
                               @PathParam("password") String password,
                               FormDataMultiPart multiPart) {

        log.info("Accepted an analysisResult request. ID:{}", id);
        AnalysisExecution analysisExecution = analysisExecutionRepository.findByJobExecutionId(id)
                .orElseThrow(() -> new ScriptCallbackException(String.format(EXECUTION_NOT_FOUND, id)));
        if (Objects.equals(password, analysisExecution.getUpdatePassword())) {

            AnalysisResultDTO analysisResultDTO =
                    multiPart.getField("analysisResult").getValueAs(AnalysisResultDTO.class);
            AnalysisResultStatusDTO status = analysisResultDTO.getStatus();

            if (status == AnalysisResultStatusDTO.EXECUTED) {
                analysisExecution.setExecutionStatus(AnalysisExecution.Status.COMPLETED);
            } else if (status == AnalysisResultStatusDTO.FAILED) {
                analysisExecution.setExecutionStatus(AnalysisExecution.Status.FAILED);
            }
            Date timestamp = new Date();
            int seconds = (int) ((timestamp.getTime() - analysisExecution.getExecuted().getTime()) / 1000);
            analysisExecution.setDuration(seconds);
            analysisExecutionRepository.saveAndFlush(analysisExecution);

            try {
                saveFiles(multiPart, analysisExecution, analysisResultDTO);
            }catch (Exception e){
                log.warn("Failed to save files for execution ID:{}", id, e);
            }
        } else {
            log.error("Update password not matched for execution ID:{}", id);
        }
    }

    private Iterable<AnalysisResultFile> saveFiles(
            FormDataMultiPart multiPart,
            AnalysisExecution analysisExecution,
            AnalysisResultDTO analysisResultDTO) {

        List<AnalysisResultFile> files = new ArrayList<>();
        List<FormDataBodyPart> bodyParts = multiPart.getFields("file");
        if (bodyParts != null) {
            Map<String,Integer> duplicates = new HashMap<>();
            for (FormDataBodyPart bodyPart : bodyParts) {
                BodyPartEntity bodyPartEntity =
                        (BodyPartEntity) bodyPart.getEntity();
                String fileName = bodyPart.getContentDisposition().getFileName();
                String extension = FilenameUtils.getExtension(fileName);
                int count = duplicates.getOrDefault(fileName, 0) + 1;
                duplicates.put(fileName, count);
                if (count > 1) {
                    fileName = FilenameUtils.getBaseName(fileName) + " (" + count + ")." + extension;
                }
                try {
                    byte[] contents = IOUtils.toByteArray(bodyPartEntity.getInputStream());
                    files.add(new AnalysisResultFile(analysisExecution, fileName,
                            bodyPart.getMediaType().getType(), contents));
                } catch (IOException e) {
                    throw new ScriptCallbackException("Unable to read result " + "files");
                }
            }
        }
        files.add(new AnalysisResultFile(analysisExecution, "stdout.txt", MediaType.TEXT_PLAIN,
                analysisResultDTO.getStdout().getBytes()));
        return outputFileRepository.save(files);
    }

}
