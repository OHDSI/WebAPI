package org.ohdsi.webapi.i18n;

import java.util.List;
import java.util.Locale;

public interface I18nService {
  List<LocaleDTO> getAvailableLocales();

  String translate(String key);
  String translate(String key, String defaultValue);

  String getLocaleResource(Locale locale);
}
