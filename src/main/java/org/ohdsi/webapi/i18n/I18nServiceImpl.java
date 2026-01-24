package org.ohdsi.webapi.i18n;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.ohdsi.circe.helper.ResourceHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Spring MVC version of I18nController
 *
 * Migration Status: Replaces /i18n/I18nController.java (Jersey)
 * Endpoints: 2 GET endpoints
 * Complexity: Simple - i18n resource handling
 */
@RestController
@RequestMapping("/i18n")
public class I18nServiceImpl implements I18nService {

  @Value("${i18n.enabled}")
  private boolean i18nEnabled = true;

  @Value("${i18n.defaultLocale}")
  private String defaultLocale = "en";

  private List<LocaleDTO> availableLocales;

  @PostConstruct
  public void init() throws IOException {

    String json = ResourceHelper.GetResourceAsString("/i18n/locales.json");
    ObjectMapper objectMapper = new ObjectMapper();
    JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, LocaleDTO.class);
    availableLocales = objectMapper.readValue(json, type);
  }

  @Override
  public List<LocaleDTO> getAvailableLocales() {

    return Collections.unmodifiableList(availableLocales);
  }

  @Override
  public String translate(String key) {

    return translate(key, key);
  }

  @Override
  public String translate(String key, String defaultValue) {

    try {
      Locale locale = LocaleContextHolder.getLocale();
      String messages = getLocaleResource(locale);
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(messages);
      String pointer = "/" + key.replaceAll("\\.", "/");
      JsonNode node = root.at(pointer);
      return node.isValueNode() ? node.asText() : defaultValue;
    }catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @Override
  public String getLocaleResource(Locale locale) {

    String resourcePath = String.format("/i18n/messages_%s.json", locale.getLanguage());
    URL resourceURL = this.getClass().getResource(resourcePath);
    String messages = "";
    if (resourceURL != null) {
      messages = ResourceHelper.GetResourceAsString(resourcePath);
    }
    return messages;
  }

  // REST Endpoints

  /**
   * Get i18n resources for current locale
   *
   * Note: Locale is resolved by LocaleInterceptor and stored in LocaleContextHolder
   */
  @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  public String getResources() {
    // Get locale from LocaleContextHolder (set by LocaleInterceptor)
    Locale locale = LocaleContextHolder.getLocale();

    if (!this.i18nEnabled || locale == null || !isLocaleSupported(locale.getLanguage())) {
      locale = Locale.forLanguageTag(defaultLocale);
    }

    return getLocaleResource(locale);
  }

  /**
   * Get list of available locales
   */
  @GetMapping(value = "/locales", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<LocaleDTO> getAvailableLocalesEndpoint() {
    if (this.i18nEnabled) {
      return getAvailableLocales();
    }

    // if i18n is disabled, then return only default locale
    return ImmutableList.of(new LocaleDTO(this.defaultLocale, null, true));
  }

  private boolean isLocaleSupported(String code) {
    return getAvailableLocales().stream().anyMatch(l -> Objects.equals(code, l.getCode()));
  }
}
