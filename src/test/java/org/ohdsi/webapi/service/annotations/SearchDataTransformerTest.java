package org.ohdsi.webapi.service.annotations;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;

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
        assertThat(result, is("Search Text: \"testSearch\""));
    }

    @Test
    public void shouldHandleFilterSource() {
        String input = "{\"filterSource\":\"Search\"}";
        String result = sut.convertJsonToReadableFormat(input);
        assertThat(result, is("Filter Source: \"Search\""));
    }

    @Test
    public void shouldHandleFilterColumns() {
        String input = "{\"filterData\":{\"filterColumns\":[{\"title\":\"Domain\",\"value\":\"Drug\"}]} }";
        String result = sut.convertJsonToReadableFormat(input);
        assertThat(result, is("Domain: \"Drug\""));
    }

    @Test
    public void shouldCombineFilterDataAndFilterSource() {
        String input = "{\"filterData\":{\"searchText\":\"testSearch\",\"filterColumns\":[{\"title\":\"Domain\",\"value\":\"Drug\"}]},\"filterSource\":\"Search\"}";
        String result = sut.convertJsonToReadableFormat(input);
        String expected = "Search Text: \"testSearch\", Domain: \"Drug\", Filter Source: \"Search\"";
        assertThat(result, is(expected));
    }

    @Test
    public void shouldHandleMultipleFilterColumns() {
        String input = "{\"filterData\":{\"filterColumns\":[{\"title\":\"Domain\",\"value\":\"Drug\"},{\"title\":\"Class\",\"value\":\"Medication\"}]}}";
        String result = sut.convertJsonToReadableFormat(input);
        String expected = "Domain: \"Drug\", Class: \"Medication\"";
        assertThat(result, is(expected));
    }

    @Test
    public void shouldIgnoreEmptyFilterColumnsAndSearchText() {
        String input = "{\"filterData\":{\"searchText\":\"\",\"filterColumns\":[]}, \"filterSource\":\"\"}";
        String result = sut.convertJsonToReadableFormat(input);
        assertThat(result, isEmptyString());
    }

    @Test
    public void shouldHandleNullValuesGracefully() {
        String input = "{\"filterData\":{\"filterColumns\":[{\"title\":null,\"value\":null}], \"searchText\":null}, \"filterSource\":null}";
        String result = sut.convertJsonToReadableFormat(input);
        assertThat(result, not(containsString("null")));
    }
}