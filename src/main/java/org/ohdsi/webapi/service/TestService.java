package org.ohdsi.webapi.service;

import org.springframework.stereotype.Component;

import javax.annotation.Generated;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by GMalikov on 20.08.2015.
 */
@Path("/test/")
@Component
public class TestService {

    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public String getTestResponse(){

        return "Test response!";
    }
}
