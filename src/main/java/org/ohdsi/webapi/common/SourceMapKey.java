package org.ohdsi.webapi.common;

import org.ohdsi.webapi.source.SourceInfo;

import java.util.function.Function;

public final class SourceMapKey<T> {
  public static SourceMapKey<String> BY_SOURCE_KEY = new SourceMapKey<>(i -> i.sourceKey);
  public static SourceMapKey<Integer> BY_SOURCE_ID = new SourceMapKey<>(i -> i.sourceId);

  private Function<SourceInfo, T> keyFunc;

  private SourceMapKey(Function<SourceInfo, T> keyFunc) {
    this.keyFunc = keyFunc;
  }

  public Function<SourceInfo, T> getKeyFunc() {
    return keyFunc;
  }
}
