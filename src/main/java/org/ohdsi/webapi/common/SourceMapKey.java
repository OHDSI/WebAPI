package org.ohdsi.webapi.common;

import org.ohdsi.webapi.source.Source;

import java.util.function.Function;

public final class SourceMapKey<T> {
  public static SourceMapKey<String> BY_SOURCE_KEY = new SourceMapKey<>(Source::getSourceKey);
  public static SourceMapKey<Integer> BY_SOURCE_ID = new SourceMapKey<>(Source::getSourceId);

  private Function<Source, T> keyFunc;

  private SourceMapKey(Function<Source, T> keyFunc) {
    this.keyFunc = keyFunc;
  }

  public Function<Source, T> getKeyFunc() {
    return keyFunc;
  }
}
