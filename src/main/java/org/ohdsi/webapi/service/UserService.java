package org.ohdsi.webapi.service;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Component;

/**
 *
 * @author gennadiy.anisimov based on sample service created by GMalikov
 */

@Path("user")
@Component
public class UserService {
  
  @POST
  @Path("logout")
  @Produces(MediaType.APPLICATION_JSON)
  public String logout() {
    Subject user = SecurityUtils.getSubject();
    
    if (user.isAuthenticated()) {
      user.logout();
      return "User logged out";
    }
    
    return "User is not authenticated. Unable to logout";
  }
  
  @GET
  @Path("test")
  @Produces(MediaType.APPLICATION_JSON)
  public String test() {
    Subject user = SecurityUtils.getSubject();
    
    if (user.isAuthenticated()) {
      return "User is authenticated.";
    }
    else {
      return "User has no access!";
    }
  }  
}
