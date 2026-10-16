package org.ohdsi.webapi.source;

public record SourcePermission(
  int sourceId,
  String sourceKey,
  boolean canRead,
  boolean canWrite
) {};
