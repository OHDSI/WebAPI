package org.ohdsi.webapi.i18n;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.circe.helper.ResourceHelper;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Path("/i18n/")
@Controller
public class I18nController {

  private String defaultLocale = "en";

  private List<LocaleDTO> availableLocales;

  @PostConstruct
  public void init() throws IOException {

    String json = ResourceHelper.GetResourceAsString("/i18n/locales.json");
    ObjectMapper objectMapper = new ObjectMapper();
    JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, LocaleDTO.class);
    availableLocales = objectMapper.readValue(json, type);
  }

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getResources(@Context ContainerRequestContext requestContext) {

    Locale locale = (Locale) requestContext.getProperty("language");
    if (locale == null || !isLocaleSupported(locale.getLanguage())) {
      locale = Locale.forLanguageTag(defaultLocale);
    }
    String resourcePath = String.format("/i18n/messages_%s.json", locale.getLanguage());
    URL resourceURL = this.getClass().getResource(resourcePath);
    String messages = "";
    if (resourceURL != null) {
      messages = ResourceHelper.GetResourceAsString(resourcePath);
    }
    return Response.ok(messages).build();
  }

  private boolean isLocaleSupported(String code) {

    return availableLocales.stream().anyMatch(l -> Objects.equals(code, l.getCode()));
  }

  @GET
  @Path("/locales")
  @Produces(MediaType.APPLICATION_JSON)
  public List<LocaleDTO> getAvailableLocales() {

    return availableLocales;
  }
}
