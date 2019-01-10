package com.jnj.honeur.webapi.service;

import com.jnj.honeur.security.TokenContext;
import com.jnj.honeur.webapi.hss.StorageServiceClient;
import com.jnj.honeur.webapi.hssserviceuser.HSSServiceUserEntity;
import com.jnj.honeur.webapi.hssserviceuser.HSSServiceUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/hss")
@Component
@ConditionalOnExpression("${datasource.honeur.enabled} and !${webapi.central}")
public class HoneurHSSService {

    @Autowired
    private HSSServiceUserRepository hssServiceUserRepository;
    @Autowired
    private StorageServiceClient storageServiceClient;


    @POST
    @Path("/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void changeServiceUser(HSSServiceUserEntity hssServiceUserEntity) {
        hssServiceUserRepository.deleteAll();
        hssServiceUserRepository.save(hssServiceUserEntity);
    }

    @GET
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)
    public Map getHSSToken() {
        TokenContext tokenContext = storageServiceClient.getStorageServiceToken();
        Map<String, String> map = new HashMap<>();
        map.put("token", tokenContext.getToken());
        map.put("userFingerprint", tokenContext.getFingerprint());
        return map;
    }
}
