package org.ohdsi.webapi.shiny;

import java.nio.file.Path;
import java.util.function.Function;

public interface PackagingStrategy extends Function<Path, Path> {
}
