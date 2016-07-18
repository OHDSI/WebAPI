package org.ohdsi.webapi.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.stereotype.Component;

/**
 *
 * @author gennadiy.anisimov based on sample service created by GMalikov
 */

@Path("user")
@Component
public class UserService {

  @GET
  @Path("test/{val}")
  @Produces(MediaType.APPLICATION_JSON)
  public String test(@PathParam("val") String val) {
    return val;
  }  
}
