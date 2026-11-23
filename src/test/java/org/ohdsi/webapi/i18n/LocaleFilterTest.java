package org.ohdsi.webapi.i18n;

import org.junit.After;
import org.junit.Test;
import org.ohdsi.webapi.Constants;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LocaleFilterTest {
    @After
    public void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    public void usesEnglishByDefault() {
        check(null, null, null, "en");
    }

    @Test
    public void honorsAcceptLanguageWithoutMvc() {
        check(null, null, "fr", "fr");
    }

    @Test
    public void queryParameterTakesPrecedenceOverAcceptLanguage() {
        check(null, "de", "fr", "de");
    }

    @Test
    public void userLanguageTakesPrecedenceOverQueryAndHeader() {
        check("pt", "de", "fr", "pt");
    }

    private void check(String userLanguage, String queryLanguage, String acceptLanguage, String expected) {
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        UriInfo uri = mock(UriInfo.class);
        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
        if (queryLanguage != null) query.putSingle("lang", queryLanguage);
        when(request.getUriInfo()).thenReturn(uri);
        when(uri.getQueryParameters()).thenReturn(query);
        when(request.getHeaderString(Constants.Headers.USER_LANGAUGE)).thenReturn(userLanguage);
        when(request.getHeaderString("Accept-Language")).thenReturn(acceptLanguage);
        new LocaleFilter().filter(request);
        Locale locale = Locale.forLanguageTag(expected);
        verify(request).setProperty("language", locale);
        assertEquals(locale, LocaleContextHolder.getLocale());
    }
}
