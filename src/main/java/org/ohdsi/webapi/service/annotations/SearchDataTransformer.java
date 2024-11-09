package org.ohdsi.webapi.service.annotations;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SearchDataTransformer {

    private static final String FILTER_DATA = "filterData";
    private static final String TITLE = "title";
    private static final String VALUE = "value";
    private static final String KEY = "key";
    private static final String FILTER_SOURCE = "filterSource";
    private static final String FILTER_SOURCE_LABEL = "Filtered By";
    private static final String SEARCH_TEXT = "searchText";
    private static final String DEFAULT_FILTER_SOURCE = "Search";
    private static final String DELIMITER = ", ";
    private static final String ENTRY_FORMAT = "%s: \"%s\"";

    public String convertJsonToReadableFormat(String jsonInput) {
        JSONObject searchObject = new JSONObject(Optional.ofNullable(jsonInput).orElse("{}"));

        if (searchObject.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        String filterSource = processFilterSource(searchObject);
        append(result, getDefaultOrActual(filterSource, DEFAULT_FILTER_SOURCE));

        JSONObject filterDataObject = searchObject.optJSONObject(FILTER_DATA);
        JSONArray filterDataArray = searchObject.optJSONArray(FILTER_DATA);

        if (filterDataObject != null) {
            Optional.ofNullable(filterDataObject).map(this::processSearchText).ifPresent(searchText -> appendCommaSeparated(result, formatQuoted(searchText)));
            Optional.ofNullable(filterDataObject.optJSONArray("filterColumns")).map(this::formatKeyValuePairs).ifPresent(
                    fdResult -> appendCommaSeparated(result, FILTER_SOURCE_LABEL + ": \"" + fdResult + "\"")
            );
        } else if (filterDataArray != null) {
            String extractedData = formatKeyValuePairs(filterDataArray);
            if (!extractedData.isEmpty()) {
                appendCommaSeparated(result, FILTER_SOURCE_LABEL + ": \"" + extractedData + "\"");
            }
        }

        return result.toString().trim();
    }

    private String processFilterSource(JSONObject jsonObject) {
        return jsonObject.optString(FILTER_SOURCE, "");
    }

    private String processSearchText(JSONObject filterData) {
        return filterData.optString(SEARCH_TEXT, "");
    }

    private String formatKeyValuePairs(JSONArray filterDataArray) {
        return IntStream.range(0, filterDataArray.length())
                .mapToObj(index -> formatEntry(filterDataArray.getJSONObject(index)))
                .collect(Collectors.joining(DELIMITER));
    }

    private String formatEntry(JSONObject item) {
        String title = optString(item, TITLE);
        String key = StringUtils.unwrap(optString(item, KEY), '"');
        return String.format(ENTRY_FORMAT, title, key);
    }

    private void appendCommaSeparated(StringBuilder builder, String part) {
        if (!part.isEmpty()) {
            append(builder, part);
        }
    }

    private void append(StringBuilder builder, String part) {
        if (builder.length() > 0) {
            builder.append(DELIMITER);
        }
        builder.append(part);
    }

    private String optString(JSONObject item, String key) {
        return item.optString(key, "");
    }

    private String getDefaultOrActual(String actual, String defaultVal) {
        return actual.isEmpty() ? defaultVal : actual;
    }

    private String formatQuoted(String text) {
        return String.format("\"%s\"", text);
    }
}