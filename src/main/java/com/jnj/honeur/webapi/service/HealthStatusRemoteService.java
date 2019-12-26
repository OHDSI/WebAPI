package com.jnj.honeur.webapi.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component("healthStatusRemoteController")
@Path("/health-status-remote")
@ConditionalOnProperty(name = "webapi.central", havingValue = "false")
public class HealthStatusRemoteService {

    private final Log log = LogFactory.getLog(getClass());

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHealthStatus() {
        return Response.ok().build();
    }


    @PostConstruct
    public void initIt() throws Exception {
        log.info("HEALTH STATUS REMOTE CONTROLLER CREATED");
    }
}
