/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: Vitaly Koulakov
 *
 */
package org.ohdsi.webapi.shiro.realms;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.ohdsi.webapi.shiro.Entities.UserPrincipal;
import org.ohdsi.webapi.shiro.mapper.UserMapper;
import org.ohdsi.webapi.shiro.tokens.ActiveDirectoryUsernamePasswordToken;
import org.ohdsi.webapi.shiro.tokens.LdapUsernamePasswordToken;
import org.ohdsi.webapi.shiro.tokens.SpnegoToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

public class LdapRealm extends JndiLdapRealm {
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapRealm.class);

    private String searchString;

    private UserMapper userMapper;

    private String ldapSearchBase;

    public LdapRealm(String ldapSearchString, String ldapSearchBase, UserMapper userMapper) {
        this.searchString = ldapSearchString;
        this.userMapper = userMapper;
        this.ldapSearchBase = ldapSearchBase;
    }

    @Override
    public boolean supports(AuthenticationToken token) {

        return token != null && token.getClass() == LdapUsernamePasswordToken.class;
    }

    @Override
    protected AuthenticationInfo queryForAuthenticationInfo(AuthenticationToken token,
                                                            LdapContextFactory ldapContextFactory)
            throws NamingException {

        Object principal = token.getPrincipal();
        Object credentials = token.getCredentials();

        LOGGER.debug("Authenticating user '{}' through LDAP", principal);

        principal = getLdapPrincipal(token);

        LdapContext ctx = null;
        try {
            ctx = ldapContextFactory.getLdapContext(principal, credentials);
            UserPrincipal userPrincipal = searchForUser(ctx, token);
            return createAuthenticationInfo(token, userPrincipal, credentials, ctx);
        } finally {
            LdapUtils.closeContext(ctx);
        }
    }
    @Override
    protected AuthenticationInfo createAuthenticationInfo(AuthenticationToken token, Object ldapPrincipal,
                                                          Object ldapCredentials, LdapContext ldapContext) {
        return new SimpleAuthenticationInfo(ldapPrincipal, token.getCredentials(), getName());
    }

    private UserPrincipal searchForUser(LdapContext ctx,  AuthenticationToken token) throws NamingException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        Object[] searchArguments = new Object[]{token.getPrincipal()};
        NamingEnumeration results = ctx.search(ldapSearchBase, searchString, searchArguments, searchCtls);
        boolean processSingleRecord = false;
        UserPrincipal userPrincipal = null;
        while (results.hasMore()) {
            if (processSingleRecord) {
                LOGGER.error("Multiple results found for {}", token.getPrincipal());
                throw new RuntimeException("Multiple results found for " + token.getPrincipal());
            }
            processSingleRecord = true;
            SearchResult searchResult = (SearchResult) results.next();
            Attributes attributes = searchResult.getAttributes();
            userPrincipal = userMapper.mapFromAttributes(attributes);
        }
        return userPrincipal;
    }
}
