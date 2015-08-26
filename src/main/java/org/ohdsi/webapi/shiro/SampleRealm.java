package org.ohdsi.webapi.shiro;


import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by GMalikov on 20.08.2015.
 */

@Component
public class SampleRealm extends AuthorizingRealm{

    @Autowired
    private UserRepository userRepository;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        final String login = (String) principalCollection.getPrimaryPrincipal();
        final UserEntity userEntity = userRepository.findByLogin(login);
        if(userEntity == null){
            throw new UnknownAccountException("Account does not exist");
        }
        final int totalRoles = userEntity.getRoles().size();
        final Set<String> roleNames = new LinkedHashSet<>(totalRoles);
        final Set<String> permissionNames = new LinkedHashSet<>();
        if (totalRoles > 0){
            for (RoleEntity roleEntity : userEntity.getRoles()){
                roleNames.add(roleEntity.getName());
                for (PermissionEntity permissionEntity : roleEntity.getPermissions()){
                    permissionNames.add(permissionEntity.getValue());
                }
            }
        }
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames);
        info.setStringPermissions(permissionNames);
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        final UsernamePasswordToken credentials = (UsernamePasswordToken) authenticationToken;
        final String login = credentials.getUsername();
        if (login == null){
            throw new UnknownAccountException("Login not provided");
        }

        final UserEntity userEntity = userRepository.findByLogin(login);
        if (userEntity == null){
            throw new UnknownAccountException("Account does not exist");
        }
        return new SimpleAuthenticationInfo(login, userEntity.getPassword(),getName());
//        return new SimpleAuthenticationInfo(login,userEntity.getPassword().toCharArray(),
//                ByteSource.Util.bytes(login), getName());
    }
}
