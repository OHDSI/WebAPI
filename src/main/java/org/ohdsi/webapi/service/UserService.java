package org.ohdsi.webapi.service;

import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.ohdsi.webapi.shiro.SimpleAuthorizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author gennadiy.anisimov
 */

@Path("user")
@Component
public class UserService {

  @Autowired
  private SimpleAuthorizer authorizer;

  @POST
  @Path("permitted")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public boolean isPermitted (String permission) {
    boolean isPermitted = this.authorizer.isPermitted(permission);
    return isPermitted;
  }

  public static class UserRoleRelation {
    public String login;
    public String role;
  }

  @GET
  @Path("roles")
  @Produces(MediaType.APPLICATION_JSON)
  public String[] getRoles() {
    Set<String> roles = this.authorizer.getRoles();
    return roles.toArray(new String[roles.size()]);
  }

  @PUT
  @Path("roles/add")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void addUserToRole(UserRoleRelation relation) {
    this.authorizer.addUserToRole(relation.role, relation.login);
  }

  @DELETE
  @Path("roles/remove")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void removeUserFromRole(UserRoleRelation relation) {
    this.authorizer.removeUserFromRole(relation.role, relation.login);
  }
}
