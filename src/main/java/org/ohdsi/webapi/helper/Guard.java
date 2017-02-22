package org.ohdsi.webapi.helper;

/**
 *
 * @author gennadiy.anisimov
 */
public class Guard {
  
  public static Boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty();
  }
  
  public static void checkNotEmpty(String value) {
    if (isNullOrEmpty(value))
      throw new IllegalArgumentException("Value should not be empty");
  }
}
