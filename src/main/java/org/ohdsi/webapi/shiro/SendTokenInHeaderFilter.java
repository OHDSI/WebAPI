package org.ohdsi.webapi.shiro;

import static org.ohdsi.webapi.shiro.management.AtlasSecurity.PERMISSIONS_ATTRIBUTE;
import static org.ohdsi.webapi.shiro.management.AtlasSecurity.TOKEN_ATTRIBUTE;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
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

  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) {
    String jwt = (String)request.getAttribute(TOKEN_ATTRIBUTE);
    String permissions = (String)request.getAttribute(PERMISSIONS_ATTRIBUTE);

    HttpServletResponse httpResponse = WebUtils.toHttp(response);
    httpResponse.setHeader(TOKEN_HEADER_NAME, jwt);
    httpResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    httpResponse.setStatus(HttpServletResponse.SC_OK);

    try (final PrintWriter responseWriter = response.getWriter()) {
      responseWriter.print(new Gson().toJson(new PermissionsDTO(permissions)));
    } catch (IOException e) {
      LOGGER.error(ERROR_WRITING_PERMISSIONS_TO_RESPONSE_LOG, e);
    }
    return false;
  }

  public static class PermissionsDTO {

    private PermissionsDTO(String permissions) {

      this.permissions = permissions;
    }

    public final String permissions;
  }
}
