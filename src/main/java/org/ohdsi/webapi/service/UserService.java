package org.ohdsi.webapi.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Component;
import org.ohdsi.webapi.shiro.TokenManager;

/**
 *
 * @author gennadiy.anisimov based on sample service created by GMalikov
 */

@Path("user")
@Component
public class UserService {
  
  @POST
  @Path("login")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public String login(@FormParam("login") String login, @FormParam("password") String password) {
    Subject user = SecurityUtils.getSubject();
    
    if (!user.isAuthenticated()) {
      UsernamePasswordToken token = new UsernamePasswordToken(login, password);
      try {
          user.login(token);
      } 
      catch (UnknownAccountException | IncorrectCredentialsException ex) {
          throw new AuthenticationException();
      }
    }
    
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
