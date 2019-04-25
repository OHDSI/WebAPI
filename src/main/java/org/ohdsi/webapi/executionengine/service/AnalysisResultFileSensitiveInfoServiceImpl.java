package org.ohdsi.webapi.executionengine.service;

import org.apache.commons.io.FilenameUtils;
import org.ohdsi.webapi.common.sensitiveinfo.AbstractSensitiveInfoService;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class AnalysisResultFileSensitiveInfoServiceImpl extends AbstractSensitiveInfoService implements AnalysisResultFileSensitiveInfoService {
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
    public AnalysisResultFile filterSensitiveInfo(AnalysisResultFile source, Map<String, Object> variables, boolean isAdmin) {
        // some txt files have media type "text" instead of MediaType.TEXT_PLAIN
        if (isFilteringRequired(source)) {
            final String value = filterSensitiveInfo(new String(source.getContents()), variables, isAdmin);
            source.setContents(value.getBytes());
        }
        return source;
    }

    private boolean isFilteringRequired(AnalysisResultFile source) {
        return MediaType.TEXT_PLAIN.equals(source.getMediaType()) || "text".equals(source.getMediaType()) || checkExtension(source);
    }

    private boolean checkExtension(AnalysisResultFile source) {
        String extension = FilenameUtils.getExtension(source.getFileName());

        if(sensitiveExtensions.contains(EXTENSION_ALL)) {
            return true;
        }
        if (extension == null || extension.isEmpty()) {
            if (sensitiveExtensions.contains(EXTENSION_EMPTY)) {
                return true;
            }
        } else {
            return sensitiveExtensions.contains(extension);
        }
        return false;
    }
}
