package com.jnj.honeur.webapi.service;

import com.jnj.honeur.webapi.HoneurHealthEndpoint;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/actuator/health")
@Produces({ MediaType.APPLICATION_JSON })
@Component
public class ActuatorService {

    private final HoneurHealthEndpoint health;

    public ActuatorService(HoneurHealthEndpoint health) {
        this.health = health;
    }

    @GET
    @Path("/liveness")
    public Object getHealthLiveness() {
        return health.invoke(false);
    }

    @GET
    @Path("/readyness")
    public Object getHealthReadyNess() {
        return health.invoke(true);
    }
}