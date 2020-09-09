package org.ohdsi.webapi.shiro;

import java.util.Collection;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.realm.Realm;
import org.ohdsi.webapi.shiro.lockout.LockoutPolicy;
import org.ohdsi.webapi.shiro.lockout.LockoutWebSecurityManager;

/**
 * WebSecurityManger uses the same collection of the realms for authentication and authorization by default.
 * In our case only jwtAuthRealm make a real authorization, so for this we create ModularRealmAuthenticator and ModularRealmAuthorizer by hand,
 * and override afterRealmsSet, to prevent reassign realms for authenticator and authorizer.
 *
 */
public class AtlasWebSecurityManager extends LockoutWebSecurityManager {

    public AtlasWebSecurityManager(LockoutPolicy lockoutPolicy, ModularRealmAuthenticator authenticator, Collection<Realm> authenticationRealms, Collection<Realm> authorizationRealms) {
        super(lockoutPolicy);

        ModularRealmAuthorizer authorizer = new ModularRealmAuthorizer();
        Collection realms = CollectionUtils.union(authenticationRealms, authorizationRealms);
        if (realms != null && !realms.isEmpty()) {
            this.setRealms(realms);
        }

        authenticator.setRealms(authenticationRealms);
        authorizer.setRealms(authorizationRealms);

        this.setAuthenticator(authenticator);
        this.setAuthorizer(authorizer);
    }

    @Override
    protected void afterRealmsSet() {
        //by default this method override realms for authenticator and authorizer. we don't want this.
        //do nothing
    }

}
