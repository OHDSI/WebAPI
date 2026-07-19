package org.ohdsi.webapi.util.archive;

import java.nio.file.Path;

/**
 * Represents a temporary archive file with metadata
 */
public class TemporaryArchive {
	private final String filename;
	private final Path archivePath;

	public TemporaryArchive(String filename, Path archivePath) {
		this.filename = filename;
		this.archivePath = archivePath;
	}

	public String getFilename() {
		return filename;
	}

	public Path getArchivePath() {
		return archivePath;
	}

	public Path getPath() {
		return archivePath;
	}
}