package org.ohdsi.webapi.security.authz;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WildcardPermission {

  private final String raw;
  private final String[][] parts;

  public WildcardPermission(String permission) {
    if (permission == null || permission.isBlank()) {
      throw new IllegalArgumentException("Permission cannot be null or blank");
    }
    this.raw = permission.trim();
    this.parts = Arrays.stream(this.raw.split(":"))
        .map(part -> part.split(","))
        .toArray(String[][]::new);
  }

  /**
   * Checks if this granted permission implies the requested permission.
   * 
   * @param requested the requested permission
   * @return true if this permission covers the requested permission
   */
  public boolean implies(WildcardPermission requested) {
    String[][] requestedParts = requested.parts;

    for (int i = 0; i < requestedParts.length; i++) {
      // If granted has fewer parts, treat missing parts as '*'
      if (i >= parts.length) {
        return true;
      }

      Set<String> grantedPartSet = new HashSet<>(Arrays.asList(parts[i]));
      Set<String> requestedPartSet = new HashSet<>(Arrays.asList(requestedParts[i]));

      if (grantedPartSet.contains("*")) {
        continue; // wildcard matches everything in this part
      }

      if (!grantedPartSet.containsAll(requestedPartSet)) {
        return false;
      }
    }

    // extra granted parts are fine
    return true;
  }

  @Override
  public String toString() {
    return raw;
  }

  // Utility helper for multiple granted permissions
  public static boolean impliesAny(Set<String> grantedPermissions, String requested) {
    WildcardPermission req = new WildcardPermission(requested);
    for (String g : grantedPermissions) {
      WildcardPermission granted = new WildcardPermission(g);
      if (granted.implies(req)) {
        return true;
      }
    }
    return false;
  }
}