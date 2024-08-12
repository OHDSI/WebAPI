package org.ohdsi.webapi.service.annotations;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class SearchDataTransformer {

    private static final String FILTER_DATA = "filterData";
    private static final String TITLE = "title";
    private static final String VALUE = "value";
    private static final String FILTER_SOURCE = "filterSource";
    private static final String FILTER_SOURCE_LABEL = "Filter Source";
    private static final String SEARCH_TEXT = "searchText";
    private static final String SEARCH_TEXT_LABEL = "Search Text";
    private static final String FILTER_COLUMNS = "filterColumns";
    private static final String QUOTE = "\"";

    private static final String DELIMITER = ", ";

    private static final String ENTRY_FORMAT = "%s: \"%s\"";

    public String convertJsonToReadableFormat(String jsonInput) {
        JSONObject searchObject = new JSONObject(jsonInput);
        StringBuilder result = new StringBuilder();

        String filterDataResult = extractFilterData(searchObject);
        appendCommaSeparated(result, filterDataResult);

        String filterSourceResult = processFilterSource(searchObject);
        appendCommaSeparated(result, filterSourceResult);

        return result.toString();
    }

    private String extractFilterData(JSONObject jsonObject) {
        JSONObject filterData = jsonObject.optJSONObject(FILTER_DATA);
        if (filterData != null) {
            String searchText = extractSearchText(filterData);
            String filterColumns = extractFilterColumns(filterData);
            return Stream.of(searchText, filterColumns)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.joining(DELIMITER));
        }
        JSONArray filterDataArray = jsonObject.optJSONArray(FILTER_DATA);
        if (filterDataArray != null) {
            return formatKeyValuePairs(filterDataArray);
        }
        return "";
    }

    private String extractFilterColumns(JSONObject filterData) {
        JSONArray filterColumns = filterData.optJSONArray(FILTER_COLUMNS);
        if (filterColumns != null) {
            return formatKeyValuePairs(filterColumns);
        }
        return "";
    }

    private String processFilterSource(JSONObject jsonObject) {
        String filterSource = jsonObject.optString(FILTER_SOURCE, "");
        if (!filterSource.isEmpty()) {
            return String.format(ENTRY_FORMAT, FILTER_SOURCE_LABEL, filterSource);
        }
        return "";
    }

    private String extractSearchText(JSONObject jsonObject) {
        String searchText = jsonObject.optString(SEARCH_TEXT, "");
        if (!searchText.isEmpty()) {
            return String.format(ENTRY_FORMAT, SEARCH_TEXT_LABEL, searchText);
        }
        return "";
    }

    private String formatKeyValuePairs(JSONArray filterColumns) {
        return IntStream.range(0, filterColumns.length())
                .mapToObj(index -> {
                    JSONObject item = filterColumns.getJSONObject(index);
                    String title = optString(item, TITLE);
                    String value = StringUtils.unwrap(optString(item, VALUE), QUOTE);
                    return String.format(ENTRY_FORMAT, title, value);
                })
                .collect(Collectors.joining(DELIMITER));
    }

    private void appendCommaSeparated(StringBuilder resultBuilder, String part) {
        if (!part.isEmpty()) {
            if (resultBuilder.length() > 0) {
                resultBuilder.append(DELIMITER);
            }
            resultBuilder.append(part);
        }
    }

    private String optString(JSONObject item, String key) {
        return item.optString(key, "");
    }
}
