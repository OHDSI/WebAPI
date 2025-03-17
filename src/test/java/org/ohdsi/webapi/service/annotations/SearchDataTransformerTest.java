package org.ohdsi.webapi.service.annotations;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;

public class SearchDataTransformerTest {

    private SearchDataTransformer sut;

    @Before
    public void setUp() {
        sut = new SearchDataTransformer();
    }

    @Test
    public void shouldReturnEmptyStringWhenInputIsEmpty() {
        JSONObject emptyJson = new JSONObject();
        String transformed = sut.convertJsonToReadableFormat(emptyJson.toString());
        assertThat(transformed, isEmptyString());
    }

    @Test
    public void shouldHandleSearchText() {
        String input = "{\"filterData\":{\"searchText\":\"testSearch\"}}";
        String result = sut.convertJsonToReadableFormat(input);
        assertThat(result, is("Search, \"testSearch\""));
    }

    @Test
    public void shouldHandleFilterSource() {
        String input = "{\"filterSource\":\"Search\"}";
        String result = sut.convertJsonToReadableFormat(input);
        assertThat(result, is("Search"));
    }

    @Test
    public void shouldHandleFilterColumns() {
        String input = "{\"filterData\":{\"filterColumns\":[{\"title\":\"Domain\",\"key\":\"Drug\"}]} }";
        String result = sut.convertJsonToReadableFormat(input);
        assertThat(result, is("Search, \"\", Filtered By: \"Domain: \"Drug\"\""));
    }

    @Test
    public void shouldCombineFilterDataAndFilterSource() {
        String input = "{\"filterData\":{\"searchText\":\"testSearch\",\"filterColumns\":[{\"title\":\"Domain\",\"key\":\"Drug\"}]},\"filterSource\":\"Search\"}";
        String result = sut.convertJsonToReadableFormat(input);
        String expected = "Search, \"testSearch\", Filtered By: \"Domain: \"Drug\"\"";
        assertThat(result, is(expected));
    }

    @Test
    public void shouldHandleMultipleFilterColumns() {
        String input = "{\"filterData\":{\"filterColumns\":[{\"title\":\"Domain\",\"key\":\"Drug\"},{\"title\":\"Class\",\"key\":\"Medication\"}]}}";
        String result = sut.convertJsonToReadableFormat(input);
        String expected = "Search, \"\", Filtered By: \"Domain: \"Drug\"" + ", Class: \"Medication\"\"";
        assertThat(result, is(expected));
    }

    @Test
    public void shouldHandleNullValuesGracefully() {
        String input = "{\"filterData\":{\"filterColumns\":[{\"title\":null,\"key\":null}], \"searchText\":null}, \"filterSource\":null}";
        String result = sut.convertJsonToReadableFormat(input);
        assertThat(result, is("Search, \"\", Filtered By: \": \"\"\""));
    }
}