package org.ohdsi.webapi.util;

import java.util.Objects;

public class QuoteUtils {

  public static String dequote(String val) {

    return Objects.nonNull(val) ? val.replaceAll("(^\"|\"$|^'|'$)", "") : val;
  }

  public static String escapeSql(String val) {

    return Objects.nonNull(val) ? val.replaceAll("'", "''") : val;
  }
}
