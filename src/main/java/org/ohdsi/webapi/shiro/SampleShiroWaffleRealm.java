package org.ohdsi.webapi.shiro;

import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import waffle.shiro.AbstractWaffleRealm;
import waffle.shiro.WaffleFqnPrincipal;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by GMalikov on 08.09.2015.
 */
public class SampleShiroWaffleRealm extends AbstractWaffleRealm{

    @Autowired
    private UserRepository userRepository;

    @Override
    protected AuthorizationInfo buildAuthorizationInfo(WaffleFqnPrincipal waffleFqnPrincipal) {
//        String login = (String) waffleFqnPrincipal.getFqn();
        final UserEntity userEntity = userRepository.findByLogin("admin");
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
}
