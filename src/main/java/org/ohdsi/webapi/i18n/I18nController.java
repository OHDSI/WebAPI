package org.ohdsi.webapi.i18n;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Path("/i18n/")
@Controller
public class I18nController {

  @Value("${i18n.enabled}")
  private boolean i18nEnabled = true;

  @Value("${i18n.defaultLocale}")
  private String defaultLocale = "en";

  @Autowired
  private I18nService i18nService;

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getResources(@Context ContainerRequestContext requestContext) {

    Locale locale = (Locale) requestContext.getProperty("language");
    if (!this.i18nEnabled || locale == null || !isLocaleSupported(locale.getLanguage())) {
      locale = Locale.forLanguageTag(defaultLocale);
    }
    String messages = i18nService.getLocaleResource(locale);
    return Response.ok(messages).build();
  }

  private boolean isLocaleSupported(String code) {

    return i18nService.getAvailableLocales().stream().anyMatch(l -> Objects.equals(code, l.getCode()));
  }

  @GET
  @Path("/locales")
  @Produces(MediaType.APPLICATION_JSON)
  public List<LocaleDTO> getAvailableLocales() {
    if (this.i18nEnabled) {
      return i18nService.getAvailableLocales();
    }

    // if i18n is disabled, then return only default locale
    return ImmutableList.of(new LocaleDTO(this.defaultLocale, null, true));
  }
}
