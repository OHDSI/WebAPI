package org.ohdsi.webapi.executionengine.controller;

import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.AnalysisExecutionStatusDTO;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.AnalysisResultDTO;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.AnalysisResultStatusDTO;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisExecutionRepository;
import org.ohdsi.webapi.executionengine.entity.AnalysisExecution;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.executionengine.exception.ScriptCallbackException;
import org.ohdsi.webapi.executionengine.repository.AnalysisExecutionRepository;
import org.ohdsi.webapi.executionengine.repository.OutputFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("/executionservice/callbacks")
public class ScriptExecutionCallbackController {

    private static final Log log = LogFactory.getLog(ScriptExecutionCallbackController.class);

    private final ComparativeCohortAnalysisExecutionRepository comparativeCohortAnalysisExecutionRepository;

    private final AnalysisExecutionRepository analysisExecutionRepository;

    private final OutputFileRepository outputFileRepository;


    @Autowired
    public ScriptExecutionCallbackController(ComparativeCohortAnalysisExecutionRepository
                                                     comparativeCohortAnalysisExecutionRepository,
                                             AnalysisExecutionRepository analysisExecutionRepository,
                                             OutputFileRepository outputFileRepository) {

        this.comparativeCohortAnalysisExecutionRepository = comparativeCohortAnalysisExecutionRepository;
        this.analysisExecutionRepository = analysisExecutionRepository;
        this.outputFileRepository = outputFileRepository;
    }

    @Path(value = "submission/{id}/status/update/{password}")
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Transactional
    public void statusUpdate(@PathParam("id") Integer id,
                             @PathParam("password") String password,
                             AnalysisExecutionStatusDTO status) {

        log.info(MessageFormat
                .format("Accepted an updateSubmission request. \n "
                                + "ID:{0}, Update date:{1} Log:\n" + "{2}",
                        status.getId(), status.getStdoutDate(),
                        status.getStdout()));
            AnalysisExecution analysisExecution = analysisExecutionRepository.findOne(id);
            if (analysisExecution != null
                    && Objects.equals(password, analysisExecution.getUpdatePassword())
                    && ( analysisExecution.getExecutionStatus().equals(AnalysisExecution.Status.STARTED)
                    || analysisExecution.getExecutionStatus().equals(AnalysisExecution.Status.RUNNING))
                    ) {
                analysisExecution.setExecutionStatus(AnalysisExecution.Status.RUNNING);
            }
    }

    @Path(value = "submission/{id}/result/{password}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @POST
    @Transactional
    public void analysisResult(@PathParam("id") Integer id,
                               @PathParam("password") String password,
                               FormDataMultiPart multiPart) {

        log.info(MessageFormat.format("Accepted an analysisResult request. \n " + "ID:{0}", id));
        AnalysisExecution analysisExecution = analysisExecutionRepository.findOne(id);
        if (Objects.nonNull(analysisExecution) && Objects.equals(password, analysisExecution.getUpdatePassword())) {
            Date timestamp = new Date();
            int seconds = (int) ((timestamp.getTime() - analysisExecution.getExecuted().getTime()) / 1000);
            analysisExecution.setDuration(seconds);

            AnalysisResultDTO analysisResultDTO =
                    multiPart.getField("analysisResult").getValueAs(AnalysisResultDTO.class);
            try {
                saveFiles(multiPart, analysisExecution, analysisResultDTO);
            }catch (Exception e){
                log.warn("Failed to save files for execution id: " + id, e);
            }

            AnalysisResultStatusDTO status = analysisResultDTO.getStatus();

            if (status == AnalysisResultStatusDTO.EXECUTED) {
                analysisExecution.setExecutionStatus(AnalysisExecution.Status.COMPLETED);
            } else if (status == AnalysisResultStatusDTO.FAILED) {
                analysisExecution.setExecutionStatus(AnalysisExecution.Status.FAILED);
            }
            analysisExecutionRepository.save(analysisExecution);
        } else {
            log.error(String.format("Update password not matched for execution id=%d", id));
        }
    }

    private Iterable<AnalysisResultFile> saveFiles(
            FormDataMultiPart multiPart,
            AnalysisExecution analysisExecution,
            AnalysisResultDTO analysisResultDTO) {

        List<AnalysisResultFile> files = new ArrayList<>();
        List<FormDataBodyPart> bodyParts = multiPart.getFields("file");
        if (bodyParts != null) {
            for (FormDataBodyPart bodyPart : bodyParts) {
                BodyPartEntity bodyPartEntity =
                        (BodyPartEntity) bodyPart.getEntity();
                String fileName = bodyPart.getContentDisposition().getFileName();
                try {
                    byte[] contents = IOUtils.toByteArray(bodyPartEntity.getInputStream());
                    String extension = FilenameUtils.getExtension(fileName);
                    if ("R".equalsIgnoreCase(extension)) {
                        contents = filterCredentials(contents);
                    }
                    files.add(new AnalysisResultFile(analysisExecution, fileName, contents));
                } catch (IOException e) {
                    throw new ScriptCallbackException("Unable to read result " + "files");
                }
            }
        }
        files.add(new AnalysisResultFile(analysisExecution, "stdout.txt", analysisResultDTO.getStdout().getBytes()));
        return outputFileRepository.save(files);
    }

    private byte[] filterCredentials(byte[] contents) throws IOException {

        String script = IOUtils.toString(contents, "UTF-8");
        return script
                .replaceAll("user\\s*=\\s*\".*\"", "user = \"database_user\"")
                .replaceAll("password\\s*=\\s*\".*\"","password = \"database_password\"")
                .getBytes();
    }
}
