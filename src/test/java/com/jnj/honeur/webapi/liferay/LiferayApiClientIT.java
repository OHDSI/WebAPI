package com.jnj.honeur.webapi.liferay;

import com.jnj.honeur.webapi.liferay.model.Organization;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRoleEntity;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class LiferayApiClientIT {

    private LiferayApiClient liferayApiClient;

    @Before
    public void setup() {
        liferayApiClient = new LiferayApiClient();
        liferayApiClient.setLiferayBaseUrl("https://portaldev71.honeur.org");
        liferayApiClient.setLiferayServiceUser("test@liferay.com");
        liferayApiClient.setLiferayServicePassword("test");
        liferayApiClient.initializeLiferayUrl();
    }

    @Test
    public void findUserByLogin() {
        UserEntity userEntity = liferayApiClient.findUserByLogin("pmoorth1@its.jnj.com");
        assertNotNull(userEntity);
        assertNotNull(userEntity.getId());
        assertEquals("pmoorth1@its.jnj.com", userEntity.getLogin());
        assertEquals("Peter", userEntity.getName());
        assertNotNull(userEntity.getUserRoles());
        assertEquals(0, userEntity.getUserRoles().size());
    }

    @Test
    public void findUserById() {
        UserEntity userEntityByLogin = liferayApiClient.findUserByLogin("pmoorth1@its.jnj.com");
        assertNotNull(userEntityByLogin);
        assertNotNull(userEntityByLogin.getId());

        UserEntity userEntity = liferayApiClient.findUserById(userEntityByLogin.getId());
        assertNotNull(userEntity);
        assertNotNull(userEntity.getId());
        assertEquals(userEntityByLogin.getId(), userEntity.getId());
        assertEquals("pmoorth1@its.jnj.com", userEntity.getLogin());
        assertEquals("Peter", userEntity.getName());
        assertNotNull(userEntity.getUserRoles());
        assertEquals(0, userEntity.getUserRoles().size());
    }

    @Test
    public void getOrganizations() {
        List<Organization> organizations = liferayApiClient.getOrganizations();
        assertNotNull(organizations);
        assertTrue(organizations.size() > 0);
    }

    @Test
    public void addDeleteRole() {
        RoleEntity role = new RoleEntity();
        role.setName("IT_ROLE");
        assertTrue(liferayApiClient.addRole(role));
        String externalRoleId = liferayApiClient.getRoleId(role);
        assertNotNull(externalRoleId);
        assertTrue(liferayApiClient.deleteRole(role));
    }

    @Test
    public void updateRole() {
        RoleEntity role = new RoleEntity();
        role.setName("IT_ROLE");

        assertTrue(liferayApiClient.addRole(role));
        assertTrue(liferayApiClient.updateRole(role));
        assertTrue(liferayApiClient.deleteRole(role));
    }

    @Test
    public void addDeleteUserRole() {
        UserEntity user = liferayApiClient.findUserByLogin("pmoorth1@its.jnj.com");
        assertNotNull(user);

        RoleEntity role = new RoleEntity();
        role.setName("IT_ROLE");
        assertTrue(liferayApiClient.addRole(role));

        UserRoleEntity userRoleEntity = liferayApiClient.addUserRole(user, role);
        assertNotNull(userRoleEntity);

        // Cleanup
        assertTrue(liferayApiClient.deleteUserRole(user, role));
        assertTrue(liferayApiClient.deleteRole(role));
    }

    @Test
    public void getRoleNamesOfUser() {
        UserEntity user = liferayApiClient.findUserByLogin("pmoorth1@its.jnj.com");
        assertNotNull(user);

        Set<String> roleNames = liferayApiClient.getRoleNamesOfUser(user);
        assertNotNull(roleNames);
        assertTrue(roleNames.size() > 0);
        assertTrue(roleNames.stream().anyMatch("User"::equals));
    }

    @Test
    public void getRoleNames() {
        List<String> roleNames = liferayApiClient.getRoleNames();
        assertNotNull(roleNames);
        assertTrue(roleNames.size() > 0);
        assertTrue(roleNames.stream().anyMatch("Guest"::equals));
        assertTrue(roleNames.stream().anyMatch("User"::equals));
        assertTrue(roleNames.stream().anyMatch("Administrator"::equals));
    }
}