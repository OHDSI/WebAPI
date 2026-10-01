package org.ohdsi.webapi.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Utilities for working with temporary files and directories
 */
public class TempFileUtils {

	/**
	 * Copy a resource from classpath to a temporary file
	 *
	 * @param resource the resource path
	 * @param prefix temp file prefix
	 * @param suffix temp file suffix
	 * @return the temporary file
	 * @throws IOException if copying fails
	 */
	public static File copyResourceToTempFile(String resource, String prefix, String suffix) throws IOException {
		File tempFile = File.createTempFile(prefix, suffix);
		try (InputStream in = TempFileUtils.class.getResourceAsStream(resource)) {
			try (OutputStream out = Files.newOutputStream(tempFile.toPath())) {
				if (in == null) {
					throw new IOException("Resource not found: " + resource);
				}
				IOUtils.copy(in, out);
			}
		}
		return tempFile;
	}

	/**
	 * Execute an action in a temporary directory that is automatically cleaned up
	 *
	 * @param action the action to execute
	 * @param <T> the return type
	 * @return the result of the action
	 */
	public static <T> T doInDirectory(Function<Path, T> action) {
		return doInDirectory("temp-", action);
	}

	/**
	 * Execute an action in a temporary directory with custom prefix
	 *
	 * @param prefix the directory prefix
	 * @param action the action to execute
	 * @param <T> the return type
	 * @return the result of the action
	 */
	public static <T> T doInDirectory(String prefix, Function<Path, T> action) {
		try {
			Path tempDir = Files.createTempDirectory(prefix);
			try {
				return action.apply(tempDir);
			} finally {
				FileUtils.deleteQuietly(tempDir.toFile());
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to create temp directory: " + e.getMessage(), e);
		}
	}
}