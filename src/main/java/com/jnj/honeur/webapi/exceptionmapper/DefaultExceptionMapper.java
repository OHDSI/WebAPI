package com.jnj.honeur.webapi.exceptionmapper;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception e) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("type", e.getClass().toString());
        node.put("message", e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(node).build();
    }
}
