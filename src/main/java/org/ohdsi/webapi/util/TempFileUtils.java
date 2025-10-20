package org.ohdsi.webapi.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class TempFileUtils {

  public static File copyResourceToTempFile(String resource, String prefix, String suffix) throws IOException {

    File tempFile = File.createTempFile(prefix, suffix);
    try(InputStream in = TempFileUtils.class.getResourceAsStream(resource)) {
      try(OutputStream out = Files.newOutputStream(tempFile.toPath())) {
        if(in == null) {
          throw new IOException("File not found: " + resource);
        }
        IOUtils.copy(in, out);
      }
    }
    return tempFile;
  }

  public static  <F> F doInDirectory(Function<Path, F> action) {
    try {
      Path tempDir = Files.createTempDirectory("webapi-");
      try {
        return action.apply(tempDir);
      } finally {
        FileUtils.deleteQuietly(tempDir.toFile());
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to create temp directory, " + e.getMessage());
    }
  }
}
