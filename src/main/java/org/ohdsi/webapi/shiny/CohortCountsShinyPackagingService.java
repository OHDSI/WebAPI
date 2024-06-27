package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.databind.JsonNode;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.stream.Stream;

@Service
@ConditionalOnBean(ShinyService.class)
public class CohortCountsShinyPackagingService implements ShinyPackagingService {

    private static final Logger log = LoggerFactory.getLogger(CohortCountsShinyPackagingService.class);
    private static final String SHINY_COHORT_COUNTS = "/shiny/shiny-cohortCounts.zip";

    @Autowired
    private CohortDefinitionService cohortDefinitionService;
    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;
    @Autowired
    private FileWriter fileWriter;
    @Autowired
    private ManifestUtils manifestUtils;

    @Value("${shiny.atlas.url}")
    private String atlasUrl;

    @Override
    public CommonAnalysisType getType() {
        return CommonAnalysisType.COHORT;
    }

    @Override
    public TemporaryFile packageApp(Integer cohortId, String sourceKey, PackagingStrategy packaging) {
        return TempFileUtils.doInDirectory(path -> {
            CohortDefinition cohort = cohortDefinitionRepository.findOne(cohortId);
            ExceptionUtils.throwNotFoundExceptionIfNull(cohort, String.format("There is no cohort definition with id = %d.", cohortId));
            try {
                File templateArchive = TempFileUtils.copyResourceToTempFile(SHINY_COHORT_COUNTS, "shiny", ".zip");
                CommonFileUtils.unzipFiles(templateArchive, path.toFile());
                Path manifestPath = path.resolve("manifest.json");
                if (!Files.exists(manifestPath)) {
                    throw new PositConnectClientException("manifest.json is not found in the Shiny Application");
                }
                JsonNode manifest = manifestUtils.parseManifest(manifestPath);

                InclusionRuleReport byEventReport = cohortDefinitionService.getInclusionRuleReport(cohortId, sourceKey, 0); //by event
                InclusionRuleReport byPersonReport = cohortDefinitionService.getInclusionRuleReport(cohortId, sourceKey, 1); //by person
                Path dataDir = path.resolve("data");
                Files.createDirectory(dataDir);
                Stream.of(
                        fileWriter.writeObjectAsJsonFile(dataDir, byEventReport, sourceKey + "_by_event.json"),
                        fileWriter.writeObjectAsJsonFile(dataDir, byPersonReport, sourceKey + "_by_person.json"),
                        fileWriter.writeTextFile(dataDir.resolve("cohort_link.txt"), pw -> pw.printf("%s/#/cohortdefinition/%s", atlasUrl, cohortId)),
                        fileWriter.writeTextFile(dataDir.resolve("cohort_name.txt"), pw -> pw.print(cohort.getName())),
                        fileWriter.writeTextFile(dataDir.resolve("datasource.txt"), pw -> pw.print(sourceKey))
                ).forEach(manifestUtils.addDataToManifest(manifest, path));
                fileWriter.writeJsonNodeToFile(manifest, manifestPath);
                Path appArchive = packaging.apply(path);
                return new TemporaryFile(String.format("Cohort_%s_%s.zip", cohortId, sourceKey), appArchive);
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
        brief.setTitle(String.format("%s (%s)", cohort.getName(), sourceKey));
        brief.setDescription(cohort.getDescription());
        return brief;
    }
}
