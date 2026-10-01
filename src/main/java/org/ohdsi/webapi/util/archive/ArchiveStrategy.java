package org.ohdsi.webapi.util.archive;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * Strategy interface for packaging directories into archive files
 */
public interface ArchiveStrategy extends Function<Path, Path> {
	/**
	 * Package the directory at the given path into an archive
	 *
	 * @param sourcePath the directory to package
	 * @return the path to the created archive file
	 */
	@Override
	Path apply(Path sourcePath);
}