package com.jnj.honeur.webapi.shiro;

import com.jnj.honeur.webapi.liferay.LiferayApiClient;
import com.jnj.honeur.webapi.liferay.model.Organization;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.ohdsi.webapi.helper.Guard;
import org.ohdsi.webapi.shiro.Entities.*;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
public class LiferayPermissionManager extends PermissionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiferayPermissionManager.class);

    @Autowired
    private LiferayApiClient liferayApiClient;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Iterable<RoleEntity> getRoles(boolean includePersonalRoles) {
        Iterable<RoleEntity> roles = this.roleRepository.findAll();
        if (includePersonalRoles) {
            return roles;
        }

        List<String> organizationNames = liferayApiClient.getOrganizations().stream()
                .map(organization -> organization.getName())
                .collect(Collectors.toList());

        List<String> liferayRoles = liferayApiClient.getRoleNames().stream()
                .filter(role -> role.startsWith("Atlas "))
                .map(role -> {
                    String result = role;
                    if(role.startsWith("Atlas ")){
                        result = result.substring(6, role.length());
                    }
                    return result;
                })
                .collect(Collectors.toList());
        HashSet<RoleEntity> filteredRoles = new HashSet<>();
        for (RoleEntity role : roles) {
            if (liferayRoles.contains(role.getName()) || organizationNames.contains(role.getName())) {
                filteredRoles.add(role);
            }
        }

        return filteredRoles;
    }

    @Override
    public AuthorizationInfo getAuthorizationInfo(final String login) {
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        final UserEntity userEntity = liferayApiClient.findUserByLogin(login);

        if(userEntity == null) {
            throw new UnknownAccountException(String.format("Account %s does not exist in Liferay", login));
        }

        Set<UserRoleEntity> userRoleEntities = getUserRoleEntities(userEntity);

        userEntity.setUserRoles(userRoleEntities);

        final Set<String> permissionNames = new LinkedHashSet<>();
        final Set<PermissionEntity> permissions = this.getUserPermissions(userEntity);

        for (PermissionEntity permission : permissions) {
            permissionNames.add(permission.getValue());
        }

        info.setStringPermissions(permissionNames);
        return info;
    }

    public Set<PermissionEntity> getUserPermissions(String userName) {
        UserEntity user = this.liferayApiClient.findUserByLogin(userName);
        Set<PermissionEntity> permissions = this.getUserPermissions(user);
        return permissions;
    }

    @Transactional
    @Override
    public UserEntity registerUser(final String login, final Set<String> defaultRoles) throws Exception {
        Guard.checkNotEmpty(login);

        LOGGER.info("registerUser: " + login);

        UserEntity user = liferayApiClient.findUserByLogin(login);

        if(user == null) {
            LOGGER.warn(String.format("No user with login %s found in Liferay!", login));
            return null;
        }

        Set<UserRoleEntity> userRoleEntities = getUserRoleEntities(user);

        user.setUserRoles(userRoleEntities);
        if(this.roleRepository.findByName(login) == null){
            RoleEntity personalRole = this.addRole(login, false);
            this.addUser(user, personalRole, null);
        }
        return user;
    }

    @Override
    public void removeUser(String login) {
    }

    @Override
    public Iterable<UserEntity> getUsers() {
        return null;
    }

    private UserEntity getUserById(Long userId) {
        UserEntity user = liferayApiClient.findUserById(userId);
        if (user == null) {
            String errorMessage = String.format("No user with id %s found in Liferay!", userId);
            LOGGER.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        return user;
    }

    private UserEntity getUserByLogin(final String login) {
        final UserEntity user = liferayApiClient.findUserByLogin(login);
        if (user == null) {
            String errorMessage = String.format("No user with login %s found in Liferay!", login);
            LOGGER.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        Set<UserRoleEntity> userRoleEntities = getUserRoleEntities(user);
        user.setUserRoles(userRoleEntities);

        return user;
    }

    private RoleEntity addRole(String roleName, boolean addToLiferay) {
        return addRole(roleName, addToLiferay, true);
    }

    private RoleEntity addRole(String roleName, boolean addToLiferay, boolean prefix) {
        RoleEntity roleEntity;
        try {
            roleEntity = super.addRole(roleName);
        } catch (Exception e) {
            LOGGER.info(e.getMessage(), e);
            roleEntity = this.roleRepository.findByName(roleName);
        }
        if(addToLiferay && roleEntity != null) {
            liferayApiClient.addRole(roleEntity, prefix);
        }
        return roleEntity;
    }

    @Override
    public RoleEntity addRole(String roleName) {
        try {
            return addRole(roleName, true);
        } catch (Exception e){
            return null;
        }
    }

    @Override
    public void removeRole(Long roleId) {
        liferayApiClient.deleteRole(roleRepository.findById(roleId));
        super.removeRole(roleId);
    }

    @Override
    public RoleEntity updateRole(RoleEntity roleEntity) {
        RoleEntity role = super.updateRole(roleEntity);
        liferayApiClient.updateRole(role);
        return role;
    }

    @Override
    public void removeUserFromRole(String roleName, String login) {
        Guard.checkNotEmpty(roleName);
        Guard.checkNotEmpty(login);

        if (roleName.equalsIgnoreCase(login))
            throw new RuntimeException("Can't remove user from personal role");

        RoleEntity role = this.getRoleByName(roleName);
        UserEntity user = this.getUserByLogin(login);

        this.liferayApiClient.deleteUserRole(user, role);
    }

    @Override
    public void removeUser(Long userId, Long roleId) {
        removeUserFromRole(roleRepository.findById(roleId).getName(), this.liferayApiClient.findUserById(userId).getLogin());
    }

    public RoleEntity addOrganizationRole(String name) {
        return addRole(name, false, false);
    }

    private UserRoleEntity addUser(final UserEntity user, final RoleEntity role, final String status) {
//        super.addUser(user, role, status);
        UserRoleEntity relation = this.liferayApiClient.addUserRole(user, role);
        return relation;
    }

//    @Override
//    public void addOrganization(List<Organization> orgs) {
//        this.liferayApiClient.addOrganization(orgs);
//    }

    private Set<RoleEntity> getUserRoles(UserEntity user) {
        if(user == null) {
            return Collections.emptySet();
        }

        Set<String> roleNames = this.liferayApiClient.getRoleNamesOfUser(user);
        Set<RoleEntity> roles = new LinkedHashSet<>();

        List<String> organizationNames = this.liferayApiClient.getOrganizations()
                .stream()
                .map(Organization::getName).collect(Collectors.toList());

        for(String role: roleNames){
            if (role.startsWith("Atlas ") || organizationNames.contains(role)) {
                if (role.startsWith("Atlas ")) {
                    role = role.substring(6, role.length());
                }
                RoleEntity entity = roleRepository.findByName(role);
                if (entity != null) {
                    roles.add(entity);
                }
            }
        }

        RoleEntity personalRole = roleRepository.findByName(user.getLogin());
        if(personalRole != null) {
            roles.add(personalRole);
        }

        return roles;
    }

    private Set<UserRoleEntity> getUserRoleEntities(UserEntity user) {
        Set<UserRoleEntity> userRoleEntities = new HashSet<>();

        for(RoleEntity role: getUserRoles(user)) {
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setRole(role);
            userRoleEntity.setUser(user);
            userRoleEntities.add(userRoleEntity);
        }
        return userRoleEntities;
    }


    private RoleEntity getRoleByName(String roleName) {
        final RoleEntity roleEntity = this.roleRepository.findByName(roleName);
        if (roleEntity == null)
            throw new RuntimeException(String.format("Role %s doesn't exist", roleName));

        return roleEntity;
    }

    private Set<PermissionEntity> getUserPermissions(UserEntity user) {
        Set<RoleEntity> roles = this.getUserRoles(user);
        Set<PermissionEntity> permissions = new LinkedHashSet<>();

        for (RoleEntity role : roles) {
            permissions.addAll(this.getRolePermissions(role));
        }

        return permissions;
    }

    private Set<PermissionEntity> getRolePermissions(RoleEntity role) {
        Set<PermissionEntity> permissions = new LinkedHashSet<>();

        Set<RolePermissionEntity> rolePermissions = role.getRolePermissions();
        for (RolePermissionEntity rolePermission : rolePermissions) {
            if (isRelationAllowed(rolePermission.getStatus())) {
                PermissionEntity permission = rolePermission.getPermission();
                permissions.add(permission);
            }
        }

        return permissions;
    }

    private boolean isRelationAllowed(final String relationStatus) {
        return relationStatus == null || relationStatus == RequestStatus.APPROVED;
    }

    public PermissionEntity getPermissionByValue(String value) {
        final PermissionEntity permission = this.permissionRepository.findByValueIgnoreCase(value);
        if (permission == null ) {
            try {
                return addPermission(value, "Retrieve Cohort Definition with ID = "+value.split(":")[1]);
            } catch (Exception e){
                return null;
            }
        }

        return permission;
    }
}
