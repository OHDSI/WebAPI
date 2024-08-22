package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.databind.JsonNode;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.execution_engine_common.util.CommonFileUtils;
import org.ohdsi.webapi.pathway.PathwayService;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayPopulationResultsDTO;
import org.ohdsi.webapi.service.ShinyService;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.TempFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CohortPathwaysShinyPackagingService implements ShinyPackagingService {

    private static final Logger log = LoggerFactory.getLogger(CohortPathwaysShinyPackagingService.class);
    private static final String SHINY_COHORT_PATHWAYS = "/shiny/shiny-cohortPathways.zip";

    private static final String APP_NAME_FORMAT = "Pathway_%s_gv%s_%s";
    @Autowired
    private PathwayService pathwayService;
    @Autowired
    private FileWriter fileWriter;
    @Autowired
    private ManifestUtils manifestUtils;

    @Override
    public CommonAnalysisType getType() {
        return CommonAnalysisType.COHORT_PATHWAY;
    }

    @Override
    public TemporaryFile packageApp(Integer generationId, String sourceKey, PackagingStrategy packaging) {
        return TempFileUtils.doInDirectory(path -> {
            PathwayAnalysisDTO pathwayAnalysis = pathwayService.getByGenerationId(generationId);
            PathwayPopulationResultsDTO pathwayPopulationResultsDTO = pathwayService.getGenerationResults(generationId.longValue());
            ExceptionUtils.throwNotFoundExceptionIfNull(pathwayAnalysis, String.format("There is no pathway analysis definition with generation id = %d.", generationId));
            ExceptionUtils.throwNotFoundExceptionIfNull(pathwayPopulationResultsDTO, String.format("There is no pathway population result with generation id = %d.", generationId));
            try {
                File templateArchive = TempFileUtils.copyResourceToTempFile(SHINY_COHORT_PATHWAYS, "shiny", ".zip");
                CommonFileUtils.unzipFiles(templateArchive, path.toFile());
                Path manifestPath = path.resolve("manifest.json");
                if (!Files.exists(manifestPath)) {
                    throw new PositConnectClientException("manifest.json is not found in the Shiny Application");
                }
                JsonNode manifest = manifestUtils.parseManifest(manifestPath);

                Path dataDir = path.resolve("data");
                Files.createDirectory(dataDir);
                Stream.of(
                        fileWriter.writeObjectAsJsonFile(dataDir, pathwayAnalysis, "PathwayDefinitionsMetaData.json"),
                        fileWriter.writeObjectAsJsonFile(dataDir, pathwayPopulationResultsDTO, "PathwayAnalysisDTOs.json"),
                        fileWriter.writeTextFile(dataDir.resolve("datasource.txt"), pw -> pw.print(sourceKey))
                ).forEach(manifestUtils.addDataToManifest(manifest, path));
                fileWriter.writeJsonNodeToFile(manifest, manifestPath);
                Path appArchive = packaging.apply(path);
                return new TemporaryFile(String.format("%s.zip", prepareAppTitle(pathwayAnalysis.getId(), generationId, sourceKey)), appArchive);
            } catch (IOException e) {
                log.error("Failed to prepare Shiny application", e);
                throw new InternalServerErrorException();
            }
        });
    }

    @Override
    public ApplicationBrief getBrief(Integer generationId, String sourceKey) {
        PathwayAnalysisDTO pathwayAnalysis = pathwayService.getByGenerationId(generationId);
        ApplicationBrief applicationBrief = new ApplicationBrief();
        applicationBrief.setName(MessageFormat.format("cohort_pathways_analysis_{0}_{1}", generationId, sourceKey));
        applicationBrief.setTitle(prepareAppTitle(pathwayAnalysis.getId(), generationId, sourceKey));
        applicationBrief.setDescription(pathwayAnalysis.getDescription());
        return applicationBrief;
    }

    private String prepareAppTitle(Integer studyAssetId, Integer generationId, String sourceKey) {
        return String.format(APP_NAME_FORMAT, studyAssetId, generationId, sourceKey);
    }
}
