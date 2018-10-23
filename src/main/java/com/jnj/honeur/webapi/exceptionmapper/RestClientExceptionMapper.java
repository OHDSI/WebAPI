package com.jnj.honeur.webapi.exceptionmapper;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.client.RestClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class RestClientExceptionMapper implements ExceptionMapper<RestClientException> {

    @Override
    public Response toResponse(RestClientException e) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("type", e.getClass().toString());
        node.put("message", "REST service error: " + e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(node).build();
    }
}
