package com.jnj.honeur.webapi.service;

import com.jnj.honeur.security.SecurityUtils2;
import com.jnj.honeur.webapi.hss.StorageServiceClient;
import com.jnj.honeur.webapi.hssserviceuser.HSSServiceUserEntity;
import com.jnj.honeur.webapi.hssserviceuser.HSSServiceUserRepository;
import org.apache.shiro.authz.AuthorizationInfo;
import org.ohdsi.webapi.service.UserService;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.management.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Path("/")
@Component
@ConditionalOnProperty(name = "datasource.honeur.enabled", havingValue = "true")
public class HoneurUserServiceExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoneurUserServiceExtension.class);

    @Autowired
    private PermissionManager authorizer;
    @Autowired
    private Security security;
    @Autowired
    private HSSServiceUserRepository hssServiceUserRepository;
    @Autowired
    private StorageServiceClient storageServiceClient;

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

    @GET
    @Path("user/permission")
    @Produces(MediaType.APPLICATION_JSON)
    public Iterable<String> getPermissions(@HeaderParam("Authorization") String token) throws Exception{
        String login = SecurityUtils2.getSubject(token.replace("Bearer ", ""));
        AuthorizationInfo authorizationInfo = this.authorizer.getAuthorizationInfo(login);
        Collection<String> permissionEntities = authorizationInfo.getStringPermissions();
        return permissionEntities;
    }

    @POST
    @Path("hss/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void changeServiceUser(HSSServiceUserEntity hssServiceUserEntity) {
        hssServiceUserRepository.deleteAll();
        hssServiceUserRepository.save(hssServiceUserEntity);
    }

    @GET
    @Path("hss/token")
    @Produces(MediaType.APPLICATION_JSON)
    public Map getHSSToken() {
        String token = storageServiceClient.getStorageServiceToken();
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        return map;
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
