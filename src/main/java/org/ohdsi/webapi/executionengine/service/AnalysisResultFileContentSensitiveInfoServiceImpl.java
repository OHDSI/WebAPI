package org.ohdsi.webapi.executionengine.service;

import com.odysseusinc.arachne.execution_engine_common.util.CommonFileUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ohdsi.webapi.common.sensitiveinfo.AbstractSensitiveInfoService;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContent;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContentList;
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

    private Set<String> sensitiveExtensions;

    @Value("${sensitiveinfo.analysis.extensions}")
    private String[] sensitiveAnalysisExtensions;

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
    // IMPORTANT: All volumes of multivolume archives will be merged into one volume
    public AnalysisResultFileContentList filterSensitiveInfo(AnalysisResultFileContentList source, Map<String, Object> variables, boolean isAdmin) {
        File temporaryDir = createTempDir();
        try {
            // Save all files to be able to process multivolume archives
            Map<AnalysisResultFileContent, Path> paths = saveFiles(temporaryDir, source.getFiles());
            paths.forEach((file, path) -> {
                // Archive volumes will be processed as entire archive
                if(!AnalysisZipUtils.isArchiveVolume(path)) {
                    processFile(path, variables);
                }
            });
            for(Iterator<Map.Entry<AnalysisResultFileContent, Path>> iter = paths.entrySet().iterator(); iter.hasNext();) {
                Map.Entry<AnalysisResultFileContent, Path> entry = iter.next();
                AnalysisResultFileContent fileContent = entry.getKey();
                Path path = entry.getValue();
                // If file does not exist then it was a part of multivolume archive and was deleted
                if(path.toFile().exists()) {
                    byte[] content = Files.readAllBytes(path);
                    fileContent.setContents(content);
                } else {
                    // Path contains information about archive volume, must be deleted
                    // because we create new archive without volumes
                    iter.remove();
                }
            }
            source.getFiles().retainAll(paths.keySet());
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

    private Map<AnalysisResultFileContent, Path> saveFiles(File tempDir, List<AnalysisResultFileContent> files) throws Exception{
        Map<AnalysisResultFileContent, Path> paths = new HashedMap();
        for (AnalysisResultFileContent file : files) {
            try {
                AnalysisResultFile analysisResultFile = file.getAnalysisResultFile();
                Path path = new File(tempDir, analysisResultFile.getFileName()).toPath();
                paths.put(file, path);
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

    private boolean isFilteringRequired(Path path) {
        return checkExtension(FilenameUtils.getExtension(path.getFileName().toString()));
    }

    private boolean checkExtension(String extension) {
        if(sensitiveExtensions.contains(EXTENSION_ALL)) {
            return true;
        }
        if (extension == null || extension.isEmpty()) {
            return sensitiveExtensions.contains(EXTENSION_EMPTY);
        } else {
            return sensitiveExtensions.contains(extension);
        }
    }

    private void processArchive(Path zipPath, Map<String, Object> variables) {
        File temporaryDir = createTempDir();
        try {
            CommonFileUtils.unzipFiles(zipPath.toFile(), temporaryDir);

            AnalysisZipUtils.deleteZipWithVolumes(zipPath);

            Files.list(temporaryDir.toPath()).forEach(path -> {
                try {
                    process(path, variables);
                } catch (IOException e) {
                    LOGGER.error("File processing error: {}", path.getFileName().toString(), e);
                }
            });
            CommonFileUtils.compressAndSplit(temporaryDir, zipPath.toFile(), null);
        } catch (ZipException e) {
            LOGGER.error("Error unzipping file", e);
        } catch (IOException e) {
            LOGGER.error("File writing error", e);
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

            if (AnalysisZipUtils.isArchive(path.getFileName().toString())) {
                // If file is archive - decompress it first
                processArchive(path, variables);
            } else if (!AnalysisZipUtils.isArchiveVolume(path)) {
                doFilterSensitiveInfo(path, variables);
            }
        } catch (IOException e) {
            LOGGER.error("File filtering error: '{}'", path.getFileName().toString(), e);
        }
    }
}
