package org.ohdsi.webapi.executionengine.service;

import org.apache.commons.io.FilenameUtils;
import org.ohdsi.webapi.common.sensitiveinfo.AbstractSensitiveInfoService;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisResultFileSensitiveInfoServiceImpl extends AbstractSensitiveInfoService implements AnalysisResultFileSensitiveInfoService {
    private final String EXTENSION_ALL = "*";
    private final String EXTENSION_EMPTY = "-";

    private List<String> sensitiveExtensions;

    @Value("${sensitiveinfo.analysis.extensions}")
    private String sensitiveAnalysisExtensions;

    @PostConstruct
    public void init() {
        super.init();
        sensitiveExtensions = new ArrayList<>();
        if (sensitiveAnalysisExtensions != null && !sensitiveAnalysisExtensions.isEmpty()) {
            String[] values = sensitiveAnalysisExtensions.split(",");
            // If there is "*" symbol - ignore other values
            for (String value : values) {
                if (EXTENSION_ALL.equals(value)) {
                    sensitiveExtensions = new ArrayList<>();
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
        if (isNeedFiltering(source)) {
            final String value = filterSensitiveInfo(new String(source.getContents()), variables, isAdmin);
            source.setContents(value.getBytes());
        }
        return source;
    }

    private boolean isNeedFiltering(AnalysisResultFile source) {
        return MediaType.TEXT_PLAIN.equals(source.getMediaType()) || "text".equals(source.getMediaType()) || checkExtension(source);
    }

    private boolean checkExtension(AnalysisResultFile source) {
        String extension = FilenameUtils.getExtension(source.getFileName());
        for (String value : sensitiveExtensions) {
            if (EXTENSION_ALL.equals(value)) {
                return true;
            }
            if (extension == null || extension.isEmpty()) {
                if (EXTENSION_EMPTY.equals(value)) {
                    return true;
                }
            } else {
                if (extension.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }
}
