package org.ohdsi.webapi.util;

import org.apache.commons.io.IOUtils;

import java.io.*;

public class TempFileUtils {

  public static File copyResourceToTempFile(String resource, String prefix, String suffix) throws IOException {

    File tempFile = File.createTempFile(prefix, suffix);
    try(InputStream in = TempFileUtils.class.getResourceAsStream(resource)) {
      try(OutputStream out = new FileOutputStream(tempFile)) {
        IOUtils.copy(in, out);
      }
    }
    return tempFile;
  }
}
