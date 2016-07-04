package org.ohdsi.webapi.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.ohdsi.webapi.exceptions.HttpUnauthorizedException;
import org.ohdsi.webapi.shiro.SimpleAuthToken;
import org.ohdsi.webapi.shiro.SimpleAuthorizer;
import org.springframework.stereotype.Component;
import org.ohdsi.webapi.shiro.TokenManager;
import org.ohdsi.webapi.shiro.WindowsAuthToken;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author gennadiy.anisimov based on sample service created by GMalikov
 */

@Path("user")
@Component
public class UserService {
  
  @Autowired
  private SimpleAuthorizer authorizer;

  @POST
  @Path("login/win")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String winLogin(@FormParam("login") String login, @FormParam("password") String password) throws HttpUnauthorizedException {
    SimpleAuthToken simpleToken = new SimpleAuthToken(login);           // to check if user is registred in the system
    WindowsAuthToken winToken = new WindowsAuthToken(login, password);  // for windows authentication
    Subject user = SecurityUtils.getSubject();
    try {
        user.login(simpleToken);
        user.login(winToken);
    } 
    catch (Exception ex) {
        throw new HttpUnauthorizedException();
    }
    
    return TokenManager.createJsonWebToken(login);
  } 
  
  @POST
  @Path("register/win")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String winRegister(@FormParam("login") String login, @FormParam("password") String password) throws Exception {
    
    WindowsAuthToken winToken = new WindowsAuthToken(login, password);
    Subject user = SecurityUtils.getSubject();
    try {
        user.login(winToken);
    } 
    catch (Exception ex) {
        throw new HttpUnauthorizedException();
    }
        
    authorizer.registerUser(login);
    return TokenManager.createJsonWebToken(login);
  } 
  
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
  @RequiresPermissions("read")
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
