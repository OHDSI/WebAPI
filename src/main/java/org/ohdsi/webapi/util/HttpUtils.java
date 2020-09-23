package org.ohdsi.webapi.util;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.OutputStream;

public class HttpUtils {

  public static Response respondBinary(OutputStream stream, String filename) {

    return  Response
            .ok(stream)
            .type(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", String.format("attachment; filename=\"%s\"", filename))
            .build();
  }
}
