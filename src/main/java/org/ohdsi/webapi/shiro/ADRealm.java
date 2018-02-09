package org.ohdsi.webapi.shiro;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADRealm extends ActiveDirectoryRealm {
    private static final Logger LOGGER = LoggerFactory.getLogger(ADRealm.class);

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        try {
            return super.doGetAuthorizationInfo(principals);
        } catch (AuthorizationException e) {
            LOGGER.warn(e.getMessage());
            return null;
        }
    }
}
