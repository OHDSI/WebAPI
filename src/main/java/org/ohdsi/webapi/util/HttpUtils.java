package org.ohdsi.webapi.util;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import java.io.OutputStream;

public class HttpUtils {

  public static ResponseEntity<OutputStream> respondBinary(OutputStream stream, String filename) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.set(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", filename));

    return ResponseEntity.ok()
            .headers(headers)
            .body(stream);
  }
}
