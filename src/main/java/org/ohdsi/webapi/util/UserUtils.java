package org.ohdsi.webapi.util;

import java.util.Objects;

public class UserUtils {

  public static String toLowerCase(String input) {

    return Objects.nonNull(input) ? input.toLowerCase() : input;
  }
}
