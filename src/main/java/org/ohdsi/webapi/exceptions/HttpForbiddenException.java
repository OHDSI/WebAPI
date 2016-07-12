package org.ohdsi.webapi.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author gennadiy.anisimov
 */
 
@Provider
public class HttpForbiddenException extends Exception implements ExceptionMapper<HttpForbiddenException> {

    public HttpForbiddenException() {
        super("Access denied");
    }
    
    public HttpForbiddenException(String message) {
        super(message);
    }
    
    @Override
    public Response toResponse(HttpForbiddenException e) {
        return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).type("application/json").build();
    }

}
