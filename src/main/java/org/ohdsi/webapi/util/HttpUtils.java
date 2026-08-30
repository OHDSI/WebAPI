package org.ohdsi.webapi.util;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.ByteArrayOutputStream;

public class HttpUtils {

  public static ResponseEntity<StreamingResponseBody> respondBinary(ByteArrayOutputStream stream, String filename) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.set(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", filename));

    StreamingResponseBody responseBody = outputStream -> {
      stream.writeTo(outputStream);
      outputStream.flush();
    };

    return ResponseEntity.ok()
            .headers(headers)
            .body(responseBody);
  }
}
