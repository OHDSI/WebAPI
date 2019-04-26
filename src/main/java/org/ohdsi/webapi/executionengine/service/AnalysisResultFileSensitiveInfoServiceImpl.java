package org.ohdsi.webapi.executionengine.service;

import com.odysseusinc.arachne.execution_engine_common.util.CommonFileUtils;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ohdsi.webapi.common.sensitiveinfo.AbstractSensitiveInfoService;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class AnalysisResultFileSensitiveInfoServiceImpl extends AbstractSensitiveInfoService implements AnalysisResultFileSensitiveInfoService {
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
    public AnalysisResultFile filterSensitiveInfo(AnalysisResultFile source, Map<String, Object> variables, boolean isAdmin) {
        if (isArchive(source)) {
            // If file is archive - decompress it first
            return checkZipArchive(source, variables, isAdmin);
        } else {
            return doFilterSensitiveInfo(source, variables, isAdmin);
        }
    }

    private AnalysisResultFile doFilterSensitiveInfo(AnalysisResultFile source, Map<String, Object> variables, boolean isAdmin) {
        if (isFilteringRequired(source)) {
            final String value = filterSensitiveInfo(new String(source.getContents()), variables, isAdmin);
            source.setContents(value.getBytes());
        }
        return source;
    }

    private byte[] doFilterSensitiveInfo(Path path, byte[] bytes, Map<String, Object> variables, boolean isAdmin) {
        if (isFilteringRequired(path)) {
            final String value = filterSensitiveInfo(new String(bytes), variables, isAdmin);
            return value.getBytes();
        }
        return bytes;
    }

    private boolean isFilteringRequired(AnalysisResultFile source) {
        // some txt files have media type "text" instead of MediaType.TEXT_PLAIN
        return MediaType.TEXT_PLAIN.equals(source.getMediaType()) || "text".equals(source.getMediaType()) ||
                checkExtension(FilenameUtils.getExtension(source.getFileName()));
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

    private boolean isArchive(AnalysisResultFile resultFile) {
        String extension = FilenameUtils.getExtension(resultFile.getFileName());
        return EXTENSION_ZIP.equalsIgnoreCase(extension);
    }

    private AnalysisResultFile checkZipArchive(AnalysisResultFile resultFile, Map<String, Object> variables, boolean isAdmin) {
        File temporaryDir = com.google.common.io.Files.createTempDir();

        try {
            File zippedFile = new File(temporaryDir, resultFile.getFileName());
            com.google.common.io.Files.write(resultFile.getContents(), zippedFile);
            CommonFileUtils.unzipFiles(zippedFile, temporaryDir);
            zippedFile.delete();

            Files.list(temporaryDir.toPath()).forEach(path -> {
                try{
                    process(path, isAdmin, variables);
                } catch (IOException e) {
                    LOGGER.error("File processing error: {}", path.getFileName().toString(), e);
                }
            });
            CommonFileUtils.compressAndSplit(temporaryDir, zippedFile, null);
            byte[] content = com.google.common.io.Files.toByteArray(zippedFile);
            resultFile.setContents(content);
        } catch (IOException e) {
            LOGGER.error("File writing error", e);
        } catch (ZipException e) {
            LOGGER.error("Error unzipping file", e);
        } finally {
            try {
                FileUtils.deleteDirectory(temporaryDir);
            } catch (IOException e) {
                LOGGER.warn("Can't delete analysis directory: '{}'", temporaryDir.getAbsolutePath(), e);
            }
        }
        return resultFile;
    }

    private void process(Path path, boolean isAdmin, Map<String, Object> variables) throws IOException {
        if (path.toFile().isDirectory()) {
            Files.list(path).forEach(child -> {
                try {
                    process(child, isAdmin, variables);
                } catch (IOException e) {
                    LOGGER.error("File processing error: {}", child.getFileName().toString(), e);
                }
            });
        } else {
            processFile(path, isAdmin, variables);
        }
    }

    private void processFile(Path path, boolean isAdmin, Map<String, Object> variables) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            byte[] filteredBytes = doFilterSensitiveInfo(path, bytes, variables, isAdmin);
            com.google.common.io.Files.write(filteredBytes, path.toFile());
        } catch (IOException e) {
            LOGGER.error("File filtering error: '{}'", path.getFileName().toString(), e);
        }
    }
}
