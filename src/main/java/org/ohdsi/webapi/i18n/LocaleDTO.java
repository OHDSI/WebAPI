package org.ohdsi.webapi.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LocaleDTO {
  private String code;
  private String name;
  @JsonProperty("default")
  private boolean defaultLocale;

  @JsonCreator
  public LocaleDTO(@JsonProperty("code") String code, @JsonProperty("name") String name,
                   @JsonProperty("default") boolean defaultLocale) {
    this.code = code;
    this.name = name;
    this.defaultLocale = defaultLocale;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public boolean isDefaultLocale() {
    return defaultLocale;
  }
}
