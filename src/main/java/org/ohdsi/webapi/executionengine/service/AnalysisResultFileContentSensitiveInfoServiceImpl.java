package org.ohdsi.webapi.executionengine.service;

import com.odysseusinc.arachne.execution_engine_common.util.CommonFileUtils;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ohdsi.webapi.common.sensitiveinfo.AbstractSensitiveInfoService;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFileContent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.io.Files.createTempDir;

@Service
public class AnalysisResultFileContentSensitiveInfoServiceImpl extends AbstractSensitiveInfoService implements AnalysisResultFileContentSensitiveInfoService {
    private final String EXTENSION_ALL = "*";
    private final String EXTENSION_EMPTY = "-";
    private final String EXTENSION_ZIP = "zip";

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
    public AnalysisResultFileContent filterSensitiveInfo(AnalysisResultFileContent source, Map<String, Object> variables, boolean isAdmin) {
        File temporaryDir = createTempDir();
        try {
            Path path = new File(temporaryDir, source.getAnalysisResultFile().getFileName()).toPath();
            Files.write(path, source.getContents(), StandardOpenOption.CREATE_NEW);

            processFile(path, variables);

            byte[] content = Files.readAllBytes(path);
            source.setContents(content);
        } catch (IOException e) {
            LOGGER.error("File writing error", e);
        } finally {
            FileUtils.deleteQuietly(temporaryDir);
        }
        return source;
    }

    @Override
    public boolean isAdmin() {
        return false;
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

    private boolean isArchive(String filename) {
        String extension = FilenameUtils.getExtension(filename);
        return EXTENSION_ZIP.equalsIgnoreCase(extension);
    }

    private void processArchive(Path zipPath, Map<String, Object> variables) {
        File temporaryDir = createTempDir();
        try {
            CommonFileUtils.unzipFiles(zipPath.toFile(), temporaryDir);
            zipPath.toFile().delete();

            Files.list(temporaryDir.toPath()).forEach(path -> {
                try{
                    process(path, variables);
                } catch (IOException e) {
                    LOGGER.error("File processing error: {}", path.getFileName().toString(), e);
                }
            });
            CommonFileUtils.compressAndSplit(temporaryDir, zipPath.toFile(), null);
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
            if (isArchive(path.getFileName().toString())) {
                // If file is archive - decompress it first
                processArchive(path, variables);
            } else {
                doFilterSensitiveInfo(path, variables);
            }
        } catch (IOException e) {
            LOGGER.error("File filtering error: '{}'", path.getFileName().toString(), e);
        }
    }
}
