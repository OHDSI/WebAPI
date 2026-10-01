package org.ohdsi.webapi.util.archive;

import com.odysseusinc.arachne.commons.utils.ZipUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory for creating various archive packaging strategies
 */
public class ArchiveStrategies {

	/**
	 * Create a ZIP packaging strategy
	 *
	 * @return strategy that creates ZIP archives
	 */
	public static ArchiveStrategy zip() {
		return new ZipArchiveStrategy();
	}

	/**
	 * Create a ZIP packaging strategy with custom prefix and suffix
	 *
	 * @param prefix the temp file prefix
	 * @param suffix the temp file suffix
	 * @return strategy that creates ZIP archives
	 */
	public static ArchiveStrategy zip(String prefix, String suffix) {
		return new ZipArchiveStrategy(prefix, suffix);
	}

	/**
	 * Create a TAR.GZ packaging strategy
	 *
	 * @return strategy that creates TAR.GZ archives
	 */
	public static ArchiveStrategy targz() {
		return new TarGzArchiveStrategy();
	}

	/**
	 * Create a TAR.GZ packaging strategy with custom prefix and suffix
	 *
	 * @param prefix the temp file prefix
	 * @param suffix the temp file suffix
	 * @return strategy that creates TAR.GZ archives
	 */
	public static ArchiveStrategy targz(String prefix, String suffix) {
		return new TarGzArchiveStrategy(prefix, suffix);
	}

	/**
	 * ZIP archive strategy implementation
	 */
	private static class ZipArchiveStrategy implements ArchiveStrategy {
		private final String prefix;
		private final String suffix;

		public ZipArchiveStrategy() {
			this("archive_", ".zip");
		}

		public ZipArchiveStrategy(String prefix, String suffix) {
			this.prefix = prefix;
			this.suffix = suffix;
		}

		@Override
		public Path apply(Path path) {
			try {
				Path archive = Files.createTempFile(prefix, suffix);
				ZipUtils.zipDirectory(archive, path);
				return archive;
			} catch (IOException e) {
				throw new RuntimeException("Failed to create ZIP archive: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * TAR.GZ archive strategy implementation
	 */
	private static class TarGzArchiveStrategy implements ArchiveStrategy {
		private final String prefix;
		private final String suffix;

		public TarGzArchiveStrategy() {
			this("archive_", ".tar.gz");
		}

		public TarGzArchiveStrategy(String prefix, String suffix) {
			this.prefix = prefix;
			this.suffix = suffix;
		}

		@Override
		public Path apply(Path path) {
			try {
				Path archive = Files.createTempFile(prefix, suffix);
				try (OutputStream out = Files.newOutputStream(archive);
						 OutputStream gzout = new GzipCompressorOutputStream(out);
						 ArchiveOutputStream arch = new TarArchiveOutputStream(gzout)) {
					packDirectoryFiles(path, arch);
				}
				return archive;
			} catch (IOException e) {
				throw new RuntimeException("Failed to create TAR.GZ archive: " + e.getMessage(), e);
			}
		}

		private void packDirectoryFiles(Path path, ArchiveOutputStream arch) throws IOException {
			packDirectoryFiles(path, null, arch);
		}

		private void packDirectoryFiles(Path path, String parentDir, ArchiveOutputStream arch) throws IOException {
			try (Stream<Path> files = Files.list(path)) {
				files.forEach(p -> {
					try {
						File file = p.toFile();
						String filePath = Stream.of(parentDir, p.getFileName().toString())
							.filter(Objects::nonNull)
							.collect(Collectors.joining("/"));
						ArchiveEntry entry = arch.createArchiveEntry(file, filePath);
						arch.putArchiveEntry(entry);
						if (file.isFile()) {
							try (InputStream in = Files.newInputStream(p)) {
								IOUtils.copy(in, arch);
							}
						}
						arch.closeArchiveEntry();
						if (file.isDirectory()) {
							packDirectoryFiles(p, filePath, arch);
						}
					} catch (IOException e) {
						throw new RuntimeException("Failed to pack file: " + p, e);
					}
				});
			}
		}
	}
}