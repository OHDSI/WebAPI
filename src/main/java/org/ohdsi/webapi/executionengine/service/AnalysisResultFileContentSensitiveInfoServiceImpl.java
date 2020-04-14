package org.ohdsi.webapi.executionengine.service;

import com.odysseusinc.arachne.execution_engine_common.util.CommonFileUtils;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ohdsi.webapi.common.sensitiveinfo.AbstractSensitiveInfoService;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContent;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContentList;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.io.Files.createTempDir;

@Service
public class AnalysisResultFileContentSensitiveInfoServiceImpl extends AbstractSensitiveInfoService implements AnalysisResultFileContentSensitiveInfoService {
    private final String EXTENSION_ALL = "*";
    private final String EXTENSION_EMPTY = "-";

    private final String EXTENSION_ZIP = "zip";
    private static final String ZIP_VOLUME_EXT_PATTERN = "z[0-9]+$";

    private Set<String> sensitiveExtensions;

    @Value("${sensitiveinfo.analysis.extensions}")
    private String[] sensitiveAnalysisExtensions;

    @Value("${sensitiveinfo.analysis.zip-chunk-size-mb}")
    private int zipChunkSizeMb;

    @PostConstruct
    public void init() {

        super.init();
        sensitiveExtensions = new HashSet<>();
        if (sensitiveAnalysisExtensions != null && sensitiveAnalysisExtensions.length > 0) {
            // If there is "*" symbol - ignore other values
            for (String value : sensitiveAnalysisExtensions) {
                if (EXTENSION_ALL.equals(value)) {
                    sensitiveExtensions.clear();
                    sensitiveExtensions.add(EXTENSION_ALL);
                    break;
                } else {
                    sensitiveExtensions.add(value.trim());
                }
            }
        }
    }

    @Override
    /**
     * This method not only filters sensitive info but also repackages zip archives to the multivalue archive with defined volume size (zipChunkSizeMb)
     */
    public AnalysisResultFileContentList filterSensitiveInfo(AnalysisResultFileContentList source, Map<String, Object> variables, boolean isAdmin) {

        if (CollectionUtils.isEmpty(source.getFiles())) {
            return source;
        }

        File temporaryDir = createTempDir();
        try {
            Map<Path, AnalysisResultFileContent> savedFiles = saveFilesToProcessMultivolumeArchives(temporaryDir, source.getFiles());

            savedFiles.keySet().stream()
                    .filter(path -> !isArchiveVolume(path))
                    .forEach(path -> processFile(path, variables));

            source.getFiles().clear();

            for (File file : temporaryDir.listFiles()) {
                Path path = file.toPath();
                AnalysisResultFileContent analysisResultFileContent = getAnalysisResultFileOrCreatNewForZipVolume(path, savedFiles);
                if (analysisResultFileContent != null) {
                    analysisResultFileContent.setContents(Files.readAllBytes(path));
                    source.getFiles().add(analysisResultFileContent);
                }
            }

        } catch (Exception e) {
            LOGGER.error("Files filtering error", e);
            source.setHasErrors(true);
        } finally {
            FileUtils.deleteQuietly(temporaryDir);
        }
        return source;
    }

    @Override
    public boolean isAdmin() {

        return false;
    }

    public void setZipChunkSizeMb(int zipChunkSizeMb) {

        this.zipChunkSizeMb = zipChunkSizeMb;
    }

    private AnalysisResultFileContent getAnalysisResultFileOrCreatNewForZipVolume(Path path, Map<Path, AnalysisResultFileContent> filePathWithResultFileContent) {

        if (isArchiveVolume(path)) {

            ExecutionEngineAnalysisStatus execution = filePathWithResultFileContent.values().stream().map(f -> f.getAnalysisResultFile().getExecution()).findAny().orElse(null);

            AnalysisResultFileContent analysisResultFileContent = new AnalysisResultFileContent();
            AnalysisResultFile analysisResultFile = new AnalysisResultFile();
            analysisResultFile.setFileName(path.getFileName().toString());
            analysisResultFile.setMediaType("application");
            analysisResultFile.setExecution(execution);
            analysisResultFileContent.setAnalysisResultFile(analysisResultFile);
            return analysisResultFileContent;
        }
        return filePathWithResultFileContent.get(path);
    }

    private Map<Path, AnalysisResultFileContent> saveFilesToProcessMultivolumeArchives(File tempDir, List<AnalysisResultFileContent> files) throws Exception {

        Map<Path, AnalysisResultFileContent> paths = new HashMap<>();

        for (AnalysisResultFileContent file : files) {
            try {
                AnalysisResultFile analysisResultFile = file.getAnalysisResultFile();
                Path path = new File(tempDir, analysisResultFile.getFileName()).toPath();
                paths.put(path, file);
                Files.write(path, file.getContents(), StandardOpenOption.CREATE_NEW);
            } catch (Exception e) {
                LOGGER.error("File writing error for file with id: {}", file.getAnalysisResultFile().getId(), e);
                throw e;
            }
        }
        return paths;
    }

    private Path doFilterSensitiveInfo(Path path, Map<String, Object> variables) throws IOException {

        if (isFilteringRequired(path)) {
            byte[] bytes = Files.readAllBytes(path);
            final String value = filterSensitiveInfo(new String(bytes), variables, isAdmin());
            Files.write(path, value.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        }
        return path;
    }

    private boolean isArchiveVolume(Path path) {

        String filename = path.getFileName().toString();
        return isArchiveVolume(filename);
    }

    private boolean isArchiveVolume(String filename) {

        String extension = FilenameUtils.getExtension(filename);
        Pattern pattern = Pattern.compile(ZIP_VOLUME_EXT_PATTERN);
        Matcher matcher = pattern.matcher(extension);
        return matcher.find();
    }

    private boolean isFilteringRequired(Path path) {

        return checkExtension(FilenameUtils.getExtension(path.getFileName().toString()));
    }

    private boolean checkExtension(String extension) {

        if (sensitiveExtensions.contains(EXTENSION_ALL)) {
            return true;
        }
        if (extension == null || extension.isEmpty()) {
            return sensitiveExtensions.contains(EXTENSION_EMPTY);
        } else {
            return sensitiveExtensions.contains(extension);
        }
    }

    private boolean isArchive(String filename) {

        String extension = FilenameUtils.getExtension(filename);
        return EXTENSION_ZIP.equalsIgnoreCase(extension);
    }

    private void processArchive(Path zipPath, Map<String, Object> variables) {

        File temporaryDir = createTempDir();
        try {
            CommonFileUtils.unzipFiles(zipPath.toFile(), temporaryDir);

            // Delete archive volumes
            ZipFile zipFile = new ZipFile(zipPath.toFile());
            List<String> filenames = zipFile.getSplitZipFiles();
            filenames.forEach(filename -> {
                File file = new File(filename);
                file.delete();
            });

            Files.list(temporaryDir.toPath()).forEach(path -> {
                try {
                    process(path, variables);
                } catch (IOException e) {
                    LOGGER.error("File processing error: {}", path.getFileName().toString(), e);
                }
            });
            long chunkSize = zipChunkSizeMb * 1024 * 1024;
            CommonFileUtils.compressAndSplit(temporaryDir, zipPath.toFile(), chunkSize);
        } catch (IOException e) {
            LOGGER.error("File writing error", e);
        } catch (ZipException e) {
            LOGGER.error("Error unzipping file", e);
        } finally {
            FileUtils.deleteQuietly(temporaryDir);
        }
    }

    private void process(Path path, Map<String, Object> variables) throws IOException {

        if (path.toFile().isDirectory()) {
            Files.list(path).forEach(child -> {
                try {
                    process(child, variables);
                } catch (IOException e) {
                    LOGGER.error("File processing error: {}", child.getFileName().toString(), e);
                }
            });
        } else {
            processFile(path, variables);
        }
    }

    private void processFile(Path path, Map<String, Object> variables) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("File for process: {}", path.toString());
            }

            if (isArchive(path.getFileName().toString())) {
                // If file is archive - decompress it first
                processArchive(path, variables);
            } else if (!isArchiveVolume(path)) {
                doFilterSensitiveInfo(path, variables);
            }

        } catch (IOException e) {
            LOGGER.error("File filtering error: '{}'", path.getFileName().toString(), e);
        }
    }
}
