package org.ohdsi.webapi.i18n;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.circe.helper.ResourceHelper;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Component
public class I18nServiceImpl implements I18nService {

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
      throw new InternalServerErrorException(e);
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
}
