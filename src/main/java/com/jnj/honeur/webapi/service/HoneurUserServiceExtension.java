package com.jnj.honeur.webapi.service;

import com.jnj.honeur.security.SecurityUtils2;
import org.apache.shiro.authz.AuthorizationInfo;
import org.ohdsi.webapi.service.UserService;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;

@Path("/")
@Component
@ConditionalOnExpression("${datasource.honeur.enabled} and ${webapi.central}")
public class HoneurUserServiceExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoneurUserServiceExtension.class);

    @Autowired
    private PermissionManager authorizer;

    public HoneurUserServiceExtension() {
        LOGGER.info("new HoneurUserServiceExtension");
    }

    @POST
    @Path("permission")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PermissionEntity addPermissions(PermissionEntity permissionEntity) throws Exception {
        return this.authorizer.addPermission(permissionEntity.getValue(), permissionEntity.getDescription());
    }

//    @GET
//    @Path("user/permission")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Iterable<String> getPermissions(@HeaderParam("Authorization") String token) {
//        String login = SecurityUtils2.getSubject(token.replace("Bearer ", ""));
//        AuthorizationInfo authorizationInfo = this.authorizer.getAuthorizationInfo(login);
//        Collection<String> permissionEntities = authorizationInfo.getStringPermissions();
//        return permissionEntities;
//    }
//
//    private ArrayList<UserService.Permission> convertPermissions(final Iterable<PermissionEntity> permissionEntities) {
//        ArrayList<UserService.Permission> permissions = new ArrayList<UserService.Permission>();
//        for (PermissionEntity permissionEntity : permissionEntities) {
//            UserService.Permission permission = new UserService.Permission(permissionEntity);
//            permissions.add(permission);
//        }
//
//        return permissions;
//    }
}
