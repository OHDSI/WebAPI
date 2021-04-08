package org.ohdsi.webapi.i18n;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.Constants;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.util.Locale;

@Provider
public class LocaleFilter implements ContainerRequestFilter {

  private String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
  private String LANG_PARAM = "lang";

  private String defaultLocale = "en";

  @Override
  public void filter(ContainerRequestContext requestContext) {

    Locale locale = Locale.forLanguageTag(defaultLocale);
    String userHeader = requestContext.getHeaderString(Constants.Headers.USER_LANGAUGE);
    if (StringUtils.isNotBlank(userHeader)) {
      locale = Locale.forLanguageTag(userHeader);
    } else if (requestContext.getUriInfo().getQueryParameters().containsKey(LANG_PARAM)) {
      locale = Locale.forLanguageTag(requestContext.getUriInfo().getQueryParameters().getFirst(LANG_PARAM));
    } else if (requestContext.getHeaderString(ACCEPT_LANGUAGE_HEADER) != null) {
      locale = Locale.forLanguageTag(requestContext.getHeaderString(ACCEPT_LANGUAGE_HEADER));
    }
    requestContext.setProperty("language", locale);
    LocaleContextHolder.setLocale(locale);
  }
}
