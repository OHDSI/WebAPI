package org.ohdsi.webapi.executionengine.service;

import static com.google.common.io.Files.createTempDir;

import com.odysseusinc.arachne.execution_engine_common.util.CommonFileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContent;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ArchiveService {

    public static final String ZIP_VALUE_MEDIA_TYPE = "application";
    private static final String EXTENSION_ZIP = "zip";
    private static final String ZIP_VOLUME_EXT_PATTERN = "z[0-9]+$";
    protected static final Logger LOGGER = LoggerFactory.getLogger(ArchiveService.class);


    public List<AnalysisResultFileContent> splitZipArchivesIntoMultiplyVolumes(List<AnalysisResultFileContent> fileContents, int zipChunkSizeMb) {

        if (CollectionUtils.isEmpty(fileContents)) {
            return Collections.emptyList();
        }

        List<AnalysisResultFileContent> resultFileContents = new ArrayList<>();
        File temporaryDir = createTempDir();
        try {
            Map<Path, AnalysisResultFileContent> savedFiles = saveFilesToProcessArchives(temporaryDir, fileContents);

            savedFiles.keySet().stream()
                    .filter(ArchiveService::isArchive)
                    .forEach(zipPath ->
                            processArchive(zipPath, zipChunkSizeMb)
                    );

            ExecutionEngineAnalysisStatus execution = fileContents.stream().map(f -> f.getAnalysisResultFile().getExecution()).findAny().orElse(null);

            for (File file : temporaryDir.listFiles()) {
                Path path = file.toPath();
                AnalysisResultFileContent analysisResultFileContent = getAnalysisResultFileOrCreatNewForZipVolume(path, savedFiles, execution);
                if (analysisResultFileContent != null) {
                    analysisResultFileContent.setContents(Files.readAllBytes(path));
                    resultFileContents.add(analysisResultFileContent);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Cannot split archives", e);
        } finally {
            FileUtils.deleteQuietly(temporaryDir);
        }
        return resultFileContents;
    }

    public static void deleteZipWithVolumes(Path zipPath) throws ZipException {
        // Delete archive volumes
        ZipFile zipFile = new ZipFile(zipPath.toFile());
        List<String> filenames = zipFile.getSplitZipFiles();
        filenames.forEach(filename -> {
            File file = ArchiveService.fixWrongNameForZ10Volume(filename);
            file.delete();
        });
    }

    public static boolean isArchiveVolume(Path path) {

        String filename = path.getFileName().toString();

        String extension = FilenameUtils.getExtension(filename);
        Pattern pattern = Pattern.compile(ZIP_VOLUME_EXT_PATTERN);
        Matcher matcher = pattern.matcher(extension);
        return matcher.find();
    }

    public static boolean isArchive(Path path) {

        String filename = path.getFileName().toString();
        return isArchive(filename);
    }

    public static boolean isArchive(String filename) {

        String extension = FilenameUtils.getExtension(filename);
        return EXTENSION_ZIP.equalsIgnoreCase(extension);
    }

    //this is known bug for zip4j library https://stackoverflow.com/q/29989451/1167673
    //most likely it already fixed in the latest version of the library
    public static File fixWrongNameForZ10Volume(String filename) {

        if (filename.endsWith("z010")) {
            String fixedVolumePath = StringUtils.replacePattern(filename, ".z010$", ".z10");
            return new File(fixedVolumePath);
        }
        return new File(filename);

    }

    private AnalysisResultFileContent getAnalysisResultFileOrCreatNewForZipVolume(Path path, Map<Path, AnalysisResultFileContent> filePathWithResultFileContent, ExecutionEngineAnalysisStatus execution) {

        if (isArchiveVolume(path)) {

            AnalysisResultFileContent analysisResultFileContent = new AnalysisResultFileContent();
            AnalysisResultFile analysisResultFile = new AnalysisResultFile();
            analysisResultFile.setFileName(path.getFileName().toString());
            analysisResultFile.setMediaType(ZIP_VALUE_MEDIA_TYPE);
            analysisResultFile.setExecution(execution);
            analysisResultFileContent.setAnalysisResultFile(analysisResultFile);
            return analysisResultFileContent;
        }
        return filePathWithResultFileContent.get(path);
    }

    private Map<Path, AnalysisResultFileContent> saveFilesToProcessArchives(File tempDir, List<AnalysisResultFileContent> files) throws Exception {

        Map<Path, AnalysisResultFileContent> paths = new HashMap<>();

        for (AnalysisResultFileContent file : files) {
            try {
                AnalysisResultFile analysisResultFile = file.getAnalysisResultFile();
                Path path = new File(tempDir, analysisResultFile.getFileName()).toPath();
                Files.write(path, file.getContents(), StandardOpenOption.CREATE_NEW);
                paths.put(path, file);
            } catch (Exception e) {
                LOGGER.error("File writing error for file with id: {}", file.getAnalysisResultFile().getId(), e);
                throw e;
            }
        }
        return paths;
    }

    private void processArchive(Path zipPath, int zipChunkSizeMb) {

        File temporaryDir = createTempDir();
        try {
            CommonFileUtils.unzipFiles(zipPath.toFile(), temporaryDir);

            deleteZipWithVolumes(zipPath);

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


}
