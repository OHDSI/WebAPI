package org.ohdsi.webapi.shiny;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class CohortCharacterizationAnalysisHeaderToFieldMapper {

    private static final Logger LOG = LoggerFactory.getLogger(CohortCharacterizationAnalysisHeaderToFieldMapper.class);
    private final Map<String, String> headerFieldMapping;

    public CohortCharacterizationAnalysisHeaderToFieldMapper(@Value("classpath:shiny/cc-header-field-mapping.csv") Resource resource) throws IOException {
        this.headerFieldMapping = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2); // Split line into two parts
                if (parts.length >= 2) { // Ensure that line has header and field
                    String header = parts[0];
                    String field = parts[1];
                    headerFieldMapping.put(header, field);
                } else {
                    LOG.warn("ignoring a line due to unexpected count of parameters (!=2): " + line);
                }
            }
        }
    }

    public Map<String, String> getHeaderFieldMapping() {
        return headerFieldMapping;
    }

}
