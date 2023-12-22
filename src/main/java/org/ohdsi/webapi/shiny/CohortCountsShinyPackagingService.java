package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.utils.CommonFilenameUtils;
import com.odysseusinc.arachne.commons.utils.ZipUtils;
import com.odysseusinc.arachne.execution_engine_common.util.CommonFileUtils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.InclusionRuleReport;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.service.ShinyService;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.TempFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import javax.ws.rs.InternalServerErrorException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.function.Consumer;

@Service
@ConditionalOnBean(ShinyService.class)
public class CohortCountsShinyPackagingService implements ShinyPackagingService {

    private static final Logger log = LoggerFactory.getLogger(CohortCountsShinyPackagingService.class);
    private static String SHINY_COHORT_COUNTS = "/shiny/shiny-cohortCounts.zip";
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private CohortDefinitionService cohortDefinitionService;
    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;
    @Value("${shiny.atlas.url}")
    private String atlasUrl;

    @Override
    public CommonAnalysisType getType() {
        return CommonAnalysisType.COHORT;
    }

    @Override
    public TemporaryFile packageApp(Integer cohortId, String sourceKey) {
        return TempFileUtils.doInDirectory(path -> {
            CohortDefinition cohort = cohortDefinitionRepository.findOne(cohortId);
            ExceptionUtils.throwNotFoundExceptionIfNull(cohort, String.format("There is no cohort definition with id = %d.", cohortId));
            try {
                File templateArchive = TempFileUtils.copyResourceToTempFile(SHINY_COHORT_COUNTS, "shiny", ".zip");
                CommonFileUtils.unzipFiles(templateArchive, path.toFile());
                InclusionRuleReport byEventReport = cohortDefinitionService.getInclusionRuleReport(cohortId, sourceKey, 0); //by event
                InclusionRuleReport byPersonReport = cohortDefinitionService.getInclusionRuleReport(cohortId, sourceKey, 1); //by person
                Path dataDir = path.resolve("data");
                Files.createDirectory(dataDir);
                writeInclusionRuleReport(dataDir, byEventReport, sourceKey + "_by_event.json");
                writeInclusionRuleReport(dataDir, byPersonReport, sourceKey + "_by_person.json");
                writeTextFile(dataDir.resolve("cohort_link.txt"), pw -> pw.printf("%s/#/cohortdefinition/%s", atlasUrl, cohortId));
                writeTextFile(dataDir.resolve("cohort_name.txt"), pw -> pw.print(cohort.getName()));
                Path appArchive = Files.createTempFile("shinyapp_", ".zip");
                ZipUtils.zipDirectory(appArchive, path);
                return new TemporaryFile(String.format("%s_cohortCounts_shinyApp.zip", CommonFilenameUtils.sanitizeFilename(cohort.getName())), appArchive);
            } catch (IOException e) {
                log.error("Failed to prepare Shiny application", e);
                throw new InternalServerErrorException();
            }
        });
    }

    @Override
    public ApplicationBrief getBrief(Integer cohortId, String sourceKey) {
        CohortDefinition cohort = cohortDefinitionRepository.findOne(cohortId);
        ApplicationBrief brief = new ApplicationBrief();
        brief.setName(MessageFormat.format("cohort_{0}_{1}", cohort.getId(), sourceKey));
        brief.setTitle(cohort.getName());
        brief.setDescription(cohort.getDescription());
        return brief;
    }

    private void writeTextFile(Path path, Consumer<PrintWriter> writer) {
        try(OutputStream out = Files.newOutputStream(path); PrintWriter printWriter = new PrintWriter(out)) {
            writer.accept(printWriter);
        } catch (IOException e) {
            log.error("Filed to write file", e);
            throw new InternalServerErrorException();
        }
    }

    private void writeInclusionRuleReport(Path parentDir, InclusionRuleReport report, String filename) {
        try(OutputStream out = Files.newOutputStream(Files.createFile(parentDir.resolve(filename)))) {
            objectMapper.writeValue(out, report);
        } catch (IOException e) {
            log.error("Failed to package Cohort Counts Shiny application", e);
            throw new InternalServerErrorException();
        }
    }
}
