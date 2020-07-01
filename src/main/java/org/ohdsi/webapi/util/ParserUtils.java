package org.ohdsi.webapi.util;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.ohdsi.webapi.shiro.exception.NullJsonNodeException;

public class ParserUtils {

    public static String parseJsonField(String json, String field) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readValue(json, JsonNode.class);
        JsonNode fieldNode = rootNode.get(field);
        if (Objects.isNull(fieldNode)) {
            throw new NullJsonNodeException(format("Json node '%s' is null", field));
        }
        String fieldValue = fieldNode.asText();
        return fieldValue;
    }

    public static List<String> parseNestedJsonField(String parentJson, String childArrayName, String childField) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode parentNode = mapper.readValue(parentJson, JsonNode.class);
        List<String> results = new ArrayList<>();
        if (parentNode.has(childArrayName) && parentNode.get(childArrayName).isArray()) {
            JsonNode childNode = parentNode.withArray(childArrayName);
            for (JsonNode child : childNode) {
                if (childField != null && child.has(childField)) {
                    results.add(child.get(childField).asText());
                } else {
                    results.add(child.asText());
                }
            }
        }
        return results;
    }
}
