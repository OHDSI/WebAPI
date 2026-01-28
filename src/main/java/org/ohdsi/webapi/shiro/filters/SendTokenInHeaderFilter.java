package org.ohdsi.webapi.shiro.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.ohdsi.webapi.shiro.management.AtlasSecurity.PERMISSIONS_ATTRIBUTE;
import static org.ohdsi.webapi.shiro.management.AtlasSecurity.TOKEN_ATTRIBUTE;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.ohdsi.webapi.shiro.ServletBridge;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

/**
 *
 * @author gennadiy.anisimov
 */
public class SendTokenInHeaderFilter extends AdviceFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(SendTokenInHeaderFilter.class);
  private static final String ERROR_WRITING_PERMISSIONS_TO_RESPONSE_LOG = "Error writing permissions to response";
  private static final String TOKEN_HEADER_NAME = "Bearer";

  private final ObjectMapper objectMapper;
  
  public SendTokenInHeaderFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  
  
  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) {
    String jwt = (String)request.getAttribute(TOKEN_ATTRIBUTE);
    PermissionManager.PermissionsDTO permissions = (PermissionManager.PermissionsDTO)request.getAttribute(PERMISSIONS_ATTRIBUTE);

    HttpServletResponse httpResponse = ServletBridge.toHttp(response);
    httpResponse.setHeader(TOKEN_HEADER_NAME, jwt);
    httpResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    httpResponse.setStatus(HttpServletResponse.SC_OK);

    try {
      final PrintWriter responseWriter = response.getWriter();
      responseWriter.print(objectMapper.writeValueAsString(permissions));
      httpResponse.flushBuffer(); // Commit the response
    } catch (IOException e) {
      LOGGER.error(ERROR_WRITING_PERMISSIONS_TO_RESPONSE_LOG, e);
    }
    return false;
  }
}
