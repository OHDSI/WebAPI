package com.jnj.honeur.webapi.liferay;

import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.RoleRepository;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(value="webapi.central", matchIfMissing = true)
public class LiferayInitialization implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiferayInitialization.class);

    @Autowired
    private LiferayApiClient liferayApiClient;
    @Autowired
    private PermissionManager authorizer;
    @Autowired
    private RoleRepository roleRepository;

    /**
     * WebAPI inserts roles into liferay prefixed with 'Atlas '.
     * Roles that are also organizations in Liferay can also be imported into liferay, without prefix.
     *
     * @param applicationReadyEvent
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Iterable<RoleEntity> roleEntityList = roleRepository.findAll();
        List<String> organizationNames = liferayApiClient.getOrganizations().stream()
                .map(organization -> organization.getName()).collect(Collectors.toList());
        for(RoleEntity role: roleEntityList) {
            if(liferayApiClient.findUserByLogin(role.getName()) == null && !organizationNames.contains(role.getName())){
                liferayApiClient.addRole(role);
            } else {
                LOGGER.info("Don't sync personal role or organization '{}' to Liferay", role.getName());
            }
        }
    }
}
