package org.ohdsi.webapi.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author gennadiy.anisimov
 */
@Provider
public class HttpUnauthorizedException extends Exception implements ExceptionMapper<HttpUnauthorizedException> {
  
  public HttpUnauthorizedException() {
    super("Authentication failed. Please verify that account exists and credentials are correct.");
  }

  public HttpUnauthorizedException(String message) {
    super(message);
  }

  @Override
  public Response toResponse(HttpUnauthorizedException e) {
    return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).type("application/json").build();
  }
}
