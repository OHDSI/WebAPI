package org.ohdsi.webapi.shiny;

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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PackagingStrategies {
    public static PackagingStrategy zip() {
        return path -> {
            try {
                Path appArchive = Files.createTempFile("shinyapp_", ".zip");
                ZipUtils.zipDirectory(appArchive, path);
                return appArchive;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static PackagingStrategy targz() {
        return path -> {
            try {
                Path archive = Files.createTempFile("shinyapp_", ".tar.gz");
                try (OutputStream out = Files.newOutputStream(archive); OutputStream gzout = new GzipCompressorOutputStream(out); ArchiveOutputStream arch = new TarArchiveOutputStream(gzout)) {
                    packDirectoryFiles(path, arch);
                }
                return archive;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static void packDirectoryFiles(Path path, ArchiveOutputStream arch) throws IOException {
        packDirectoryFiles(path, null, arch);
    }

    private static void packDirectoryFiles(Path path, String parentDir, ArchiveOutputStream arch) throws IOException {
        try (Stream<Path> files = Files.list(path)) {
            files.forEach(p -> {
                try {
                    File file = p.toFile();
                    String filePath = Stream.of(parentDir, p.getFileName().toString()).filter(Objects::nonNull).collect(Collectors.joining("/"));
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
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
