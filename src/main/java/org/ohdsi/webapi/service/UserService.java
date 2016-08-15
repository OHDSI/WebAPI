package org.ohdsi.webapi.service;

import java.util.Set;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.ohdsi.webapi.shiro.Entities.PermissionRequest;
import org.ohdsi.webapi.shiro.Entities.RoleRequest;
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
  
  @PUT
  @Path("permissions/request/{role}/{permission}")
  @Produces(MediaType.APPLICATION_JSON)
  public String requestPermission(@PathParam("role") String role, @PathParam("permission") String permission) {
    return this.requestPermission(role, permission, null);
  }  
  
  @PUT
  @Path("permissions/request/{role}/{permission}/{description}")
  @Produces(MediaType.APPLICATION_JSON)
  public String requestPermission(
          @PathParam("role") String role,
          @PathParam("permission") String permission, 
          @PathParam("description") String description) {
    String status = this.authorizer.requestPermission(role, permission, description);
    return status;
  }  

  @GET
  @Path("permissions/requested")
  @Produces(MediaType.APPLICATION_JSON)
  public PermissionRequest[] getRequestedPermissions () {
    Set<PermissionRequest> permissions = this.authorizer.getRequestedPermissions();
    return permissions.toArray(new PermissionRequest[permissions.size()]);
  }

  @POST
  @Path("permissions/approve/{requestId}")
  @Produces(MediaType.APPLICATION_JSON)
  public String approvePermissionRequest (@PathParam("requestId") Long requestId) {
    String status = this.authorizer.approvePermissionRequest(requestId);
    return status;
  }

  @POST
  @Path("permissions/refuse/{requestId}")
  @Produces(MediaType.APPLICATION_JSON)
  public String refusePermissionRequest (@PathParam("requestId") Long requestId) {
    String status = this.authorizer.refusePermissionRequest(requestId);
    return status;
  }

  @DELETE
  @Path("permissions/remove/{role}/{permission}")
  @Produces(MediaType.APPLICATION_JSON)
  public void removePermission (
          @PathParam("role") String role,
          @PathParam("permission") String permission) {
    this.authorizer.removePermission(role, permission);
  }

  @GET
  @Path("permitted/{permission}")
  @Produces(MediaType.APPLICATION_JSON)
  public boolean isPermitted (@PathParam("permission") String permission) {
    boolean isPermitted = this.authorizer.isPermitted(permission);
    return isPermitted;
  }

  @PUT
  @Path("roles/request/{role}")
  @Produces(MediaType.APPLICATION_JSON)
  public String requestRole(@PathParam("role") String role) {
    String status = this.authorizer.requestRole(role);
    return status;
  }

  @GET
  @Path("roles/requested")
  @Produces(MediaType.APPLICATION_JSON)
  public RoleRequest[] getRequestedRoles() {
    Set<RoleRequest> roles = this.authorizer.getRequestedRoles();
    return roles.toArray(new RoleRequest[roles.size()]);
  }

  @POST
  @Path("roles/approve/{requestId}")
  @Produces(MediaType.APPLICATION_JSON)
  public String approveRoleRequest(@PathParam("requestId") Long requestId) {
    String status = this.authorizer.approveRoleRequest(requestId);
    return status;
  }

  @POST
  @Path("roles/refuse/{requestId}")
  @Produces(MediaType.APPLICATION_JSON)
  public String refuseRoleRequest(@PathParam("requestId") Long requestId) {
    String status = this.authorizer.refuseRoleRequest(requestId);
    return status;
  }

}
