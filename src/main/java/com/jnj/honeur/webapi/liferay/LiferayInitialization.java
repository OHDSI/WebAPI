package com.jnj.honeur.webapi.liferay;

import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.RoleRepository;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class LiferayInitialization implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private LiferayApiClient liferayApiClient;
    @Autowired
    private PermissionManager authorizer;
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Iterable<RoleEntity> roleEntityList = roleRepository.findAll();
        for(RoleEntity role: roleEntityList){
            if(liferayApiClient.findUserByLogin(role.getName()) == null){
                liferayApiClient.addRole(role);
            }
        }
    }
}
