package org.ohdsi.webapi.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class JoinUtils {

  public static String join(Iterable<?> parts) {

    return join(parts, ",");
  }

  public static String join(Iterable<?> parts, String separator) {

    return join(parts, separator, "");
  }

  public static String join(Iterable<?> parts, String separator, String defaultValue) {

    return Optional.ofNullable(StringUtils.join(parts, separator)).orElse(defaultValue);
  }
}
