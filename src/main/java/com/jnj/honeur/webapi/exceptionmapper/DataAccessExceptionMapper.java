package com.jnj.honeur.webapi.exceptionmapper;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.dao.DataAccessException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class DataAccessExceptionMapper implements ExceptionMapper<DataAccessException> {

    @Override
    public Response toResponse(DataAccessException e) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("type", e.getClass().toString());
        node.put("message", "Data access error: " + e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(node).build();
    }
}
