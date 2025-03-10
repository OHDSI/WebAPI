package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.execution_engine_common.util.CommonFileUtils;
import org.ohdsi.webapi.report.CDMDashboard;
import org.ohdsi.webapi.service.CDMResultsService;
import org.ohdsi.webapi.shiny.posit.PositConnectClientException;
import org.ohdsi.webapi.shiny.summary.DataSourceSummary;
import org.ohdsi.webapi.shiny.summary.DataSourceSummaryConverter;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.TempFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CommonShinyPackagingService {
    private static final Logger LOG = LoggerFactory.getLogger(CommonShinyPackagingService.class);
    protected final String atlasUrl;
    protected String repoLink;
    protected final FileWriter fileWriter;
    protected final ManifestUtils manifestUtils;
    protected final ObjectMapper objectMapper;
    protected final SourceRepository sourceRepository;
    protected final CDMResultsService cdmResultsService;
    protected final DataSourceSummaryConverter dataSourceSummaryConverter;

    public CommonShinyPackagingService(String atlasUrl, String repoLink, FileWriter fileWriter, ManifestUtils manifestUtils, ObjectMapper objectMapper, SourceRepository sourceRepository, CDMResultsService cdmResultsService, DataSourceSummaryConverter dataSourceSummaryConverter) {
        this.atlasUrl = atlasUrl;
        this.repoLink = repoLink;
        this.fileWriter = fileWriter;
        this.manifestUtils = manifestUtils;
        this.objectMapper = objectMapper;
        this.sourceRepository = sourceRepository;
        this.cdmResultsService = cdmResultsService;
        this.dataSourceSummaryConverter = dataSourceSummaryConverter;
    }

    public abstract CommonAnalysisType getType();


    public abstract ApplicationBrief getBrief(Integer generationId, String sourceKey);

    public abstract String getAppTemplateFilePath();

    public abstract void populateAppData(
            Integer generationId,
            String sourceKey,
            ShinyAppDataConsumers shinyAppDataConsumers
    );

    public String getAtlasUrl() {
        return atlasUrl;
    }

    public String getRepoLink() {
        return repoLink;
    }

    public void setRepoLink(String repoLink) {
        this.repoLink = repoLink;
    }

    public FileWriter getFileWriter() {
        return fileWriter;
    }

    public ManifestUtils getManifestUtils() {
        return manifestUtils;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public SourceRepository getSourceRepository() {
        return sourceRepository;
    }

    public CDMResultsService getCdmResultsService() {
        return cdmResultsService;
    }

    public DataSourceSummaryConverter getDataSourceSummaryConverter() {
        return dataSourceSummaryConverter;
    }


    class ShinyAppDataConsumers {
        private final Map<String, String> applicationProperties = new HashMap<>();
        private final Map<String, Object> jsonObjectsToSave = new HashMap<>();
        private final Map<String, String> textFilesToSave = new HashMap<>();
        private final BiConsumer<String, String> appPropertiesConsumer = applicationProperties::put;
        private final BiConsumer<String, String> textFilesConsumer = textFilesToSave::put;
        private final BiConsumer<String, Object> jsonObjectsConsumer = jsonObjectsToSave::put;

        public BiConsumer<String, String> getAppProperties() {
            return appPropertiesConsumer;
        }

        public BiConsumer<String, String> getTextFiles() {
            return textFilesConsumer;
        }

        public BiConsumer<String, Object> getJsonObjects() {
            return jsonObjectsConsumer;
        }
    }

    public final TemporaryFile packageApp(Integer generationId, String sourceKey, PackagingStrategy packaging) {
        return TempFileUtils.doInDirectory(path -> {
            try {
                File templateArchive = TempFileUtils.copyResourceToTempFile(getAppTemplateFilePath(), "shiny", ".zip");
                CommonFileUtils.unzipFiles(templateArchive, path.toFile());
                Path manifestPath = path.resolve("manifest.json");
                if (!Files.exists(manifestPath)) {
                    throw new PositConnectClientException("manifest.json is not found in the Shiny Application");
                }
                JsonNode manifest = getManifestUtils().parseManifest(manifestPath);

                Path dataDir = path.resolve("data");
                Files.createDirectory(dataDir);

                Source source = getSourceRepository().findBySourceKey(sourceKey);

                ShinyAppDataConsumers shinyAppDataConsumers = new ShinyAppDataConsumers();

                //Default properties common for each shiny app
                shinyAppDataConsumers.applicationProperties.put(ShinyConstants.PROPERTY_NAME_REPO_LINK.getValue(), getRepoLink());
                shinyAppDataConsumers.applicationProperties.put(ShinyConstants.PROPERTY_NAME_ATLAS_URL.getValue(), getAtlasUrl());
                shinyAppDataConsumers.applicationProperties.put(ShinyConstants.PROPERTY_NAME_DATASOURCE_KEY.getValue(), sourceKey);
                shinyAppDataConsumers.applicationProperties.put(ShinyConstants.PROPERTY_NAME_DATASOURCE_NAME.getValue(), source.getSourceName());

                populateCDMDataSourceSummaryIfPresent(source, shinyAppDataConsumers);

                populateAppData(generationId, sourceKey, shinyAppDataConsumers);

                Stream<Path> textFilesPaths = shinyAppDataConsumers.textFilesToSave.entrySet()
                        .stream()
                        .map(entry -> getFileWriter().writeTextFile(dataDir.resolve(entry.getKey()), pw -> pw.print(entry.getValue())));

                Stream<Path> jsonFilesPaths = shinyAppDataConsumers.jsonObjectsToSave.entrySet()
                        .stream()
                        .map(entry -> getFileWriter().writeObjectAsJsonFile(dataDir, entry.getValue(), entry.getKey()));

                Stream<Path> appPropertiesFilePath = Stream.of(
                        getFileWriter().writeTextFile(dataDir.resolve("app.properties"), pw -> pw.print(convertAppPropertiesToString(shinyAppDataConsumers.applicationProperties)))
                );

                Stream.of(textFilesPaths, jsonFilesPaths, appPropertiesFilePath)
                        .flatMap(Function.identity())
                        .forEach(getManifestUtils().addDataToManifest(manifest, path));

                getFileWriter().writeJsonNodeToFile(manifest, manifestPath);
                Path appArchive = packaging.apply(path);
                ApplicationBrief applicationBrief = getBrief(generationId, sourceKey);
                return new TemporaryFile(String.format("%s.zip", applicationBrief.getTitle()), appArchive);
            } catch (IOException e) {
                LOG.error("Failed to prepare Shiny application", e);
                throw new InternalServerErrorException();
            }
        });
    }

    private void populateCDMDataSourceSummaryIfPresent(Source source, ShinyAppDataConsumers shinyAppDataConsumers) {
        DataSourceSummary dataSourceSummary;
        try {
            CDMDashboard cdmDashboard = getCdmResultsService().getDashboard(source.getSourceKey());
            dataSourceSummary = getDataSourceSummaryConverter().convert(cdmDashboard);
        } catch (Exception e) {
            LOG.warn("Could not populate datasource summary", e);
            dataSourceSummary = getDataSourceSummaryConverter().emptySummary(source.getSourceName());
        }
        shinyAppDataConsumers.jsonObjectsToSave.put("datasource_summary.json", dataSourceSummary);
    }

    private String convertAppPropertiesToString(Map<String, String> appProperties) {
        return appProperties.entrySet().stream()
                .map(entry -> String.format("%s=%s\n", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining());
    }

    protected String dateToString(Date date) {
        if (date == null) return null;
        DateFormat df = new SimpleDateFormat(ShinyConstants.DATE_TIME_FORMAT.getValue());
        return df.format(date);
    }
}
