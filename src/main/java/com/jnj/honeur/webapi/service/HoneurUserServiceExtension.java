package com.jnj.honeur.webapi.service;

import com.jnj.honeur.webapi.liferay.LiferayApiClient;
import com.jnj.honeur.webapi.shiro.LiferayPermissionManager;
import org.ohdsi.webapi.service.UserService;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Path("/")
@Component
@ConditionalOnProperty(name = "datasource.honeur.enabled", havingValue = "true")
public class HoneurUserServiceExtension {

    @Autowired
    private LiferayPermissionManager authorizer;
    @Autowired
    private Security security;

    @POST
    @Path("permission")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PermissionEntity addPermissions(PermissionEntity permissionEntity) throws Exception {
        return this.authorizer.addPermission(permissionEntity.getValue(), permissionEntity.getDescription());
    }

    @GET
    @Path("user/permission")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Iterable<UserService.Permission> getPermissions() throws Exception{
        Set<PermissionEntity> permissionEntities = this.authorizer.getUserPermissions(security.getSubject());
        List<UserService.Permission> permissions = convertPermissions(permissionEntities);
        return permissions;
    }

    private ArrayList<UserService.Permission> convertPermissions(final Iterable<PermissionEntity> permissionEntities) {
        ArrayList<UserService.Permission> permissions = new ArrayList<UserService.Permission>();
        for (PermissionEntity permissionEntity : permissionEntities) {
            UserService.Permission permission = new UserService.Permission(permissionEntity);
            permissions.add(permission);
        }

        return permissions;
    }
}
