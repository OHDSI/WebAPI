package com.jnj.honeur.webapi.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Component("healthStatusController")
@Path("/health-status")
@ConditionalOnProperty(name = "webapi.central", havingValue = "false")
public class HealthStatusRemoteService {

    @GetMapping
    @Path("/")
    public Response getHealthStatus() {
        return Response.ok().build();
    }
}
