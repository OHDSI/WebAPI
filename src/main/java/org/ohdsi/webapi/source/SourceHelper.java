package org.ohdsi.webapi.source;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.util.BigQueryUtils;
import com.odysseusinc.logging.event.ChangeDataSourceEvent;
import org.apache.commons.io.IOUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SourceHelper {

  private ConcurrentHashMap<Source, String> sourcesConnections = new ConcurrentHashMap<>();

  public String getSourceConnectionString(Source source) {
    return sourcesConnections.computeIfAbsent(source, s -> {
      if (Objects.equals(DBMSType.BIGQUERY.getOhdsiDB(), s.getSourceDialect())) {
        return getBQConnectionString(s);
      } else {
        return s.getSourceConnection();
      }
    });
  }

  private String getBQConnectionString(Source source) {

    String connectionString = source.getSourceConnection();
    if (BigQueryUtils.getBigQueryKeyPath(connectionString) == null) {
      byte[] keyFileData = source.getKeyfile();
      if (Objects.nonNull(keyFileData)) {
        try {
          File keyFile = java.nio.file.Files.createTempFile("", ".json").toFile();
          try (OutputStream out = new FileOutputStream(keyFile)) {
            IOUtils.write(keyFileData, out);
          }
          String filePath = keyFile.getAbsolutePath();
          connectionString = BigQueryUtils.replaceBigQueryKeyPath(connectionString, filePath);
        } catch (IOException ex) {
          throw new UncheckedIOException(ex);
        }
      }
    }

    return connectionString;
  }

  @EventListener
  public void onSourceUpdate(ChangeDataSourceEvent changeDataSourceEvent) {

    Source s = new Source();
    s.setSourceId(changeDataSourceEvent.getId());
    sourcesConnections.remove(s);
  }
}
