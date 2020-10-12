package org.ohdsi.webapi.shiro.realms;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.ohdsi.webapi.shiro.Entities.UserPrincipal;
import org.ohdsi.webapi.shiro.mapper.UserMapper;
import org.ohdsi.webapi.shiro.tokens.ActiveDirectoryUsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapContext;
import java.util.List;
import java.util.Objects;

public class ADRealm extends ActiveDirectoryRealm {
    private static final Logger LOGGER = LoggerFactory.getLogger(ADRealm.class);

    private String searchFilter;

    private String searchString;

    private LdapTemplate ldapTemplate;

    private AttributesMapper<String> dnAttributesMapper = attrs -> (String) attrs.get("distinguishedName").get();

    private UserMapper userMapper;

    public ADRealm() {
    }

    public ADRealm(LdapTemplate ldapTemplate, String searchFilter, String searchString, UserMapper userMapper) {
        this.ldapTemplate = ldapTemplate;
        this.searchFilter = searchFilter;
        this.searchString = searchString;
        this.userMapper = userMapper;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && token.getClass() == ActiveDirectoryUsernamePasswordToken.class;
    }

    public void setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        try {
            return super.doGetAuthorizationInfo(principals);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
            return null;
        }
    }

    private String getUserPrincipalName(final String username) {

        return StringUtils.isNotBlank(principalSuffix) ? username + principalSuffix : username;
    }

    @Override
    protected AuthenticationInfo queryForAuthenticationInfo(AuthenticationToken token, LdapContextFactory ldapContextFactory) throws NamingException {

        if (Objects.nonNull(ldapTemplate) && StringUtils.isNotBlank(searchFilter) && StringUtils.isNotBlank(searchString)) {
            UsernamePasswordToken upToken = (UsernamePasswordToken) token;
            String userPrincipalName = getUserPrincipalName(upToken.getUsername());

            String userSearch = String.format(searchString, userPrincipalName);
            List<UserPrincipal> result = ldapTemplate.search("", userSearch, SearchControls.SUBTREE_SCOPE,
                    userMapper);

            if (result.size() == 1) {
                UserPrincipal userPrincipal = result.iterator().next();
                List<String> filterResult = ldapTemplate.search("", String.format(searchFilter, userPrincipal.getUsername()),
                        SearchControls.SUBTREE_SCOPE, dnAttributesMapper);
                if (!filterResult.isEmpty()) {
                    LdapContext ctx = null;
                    try {
                        ctx = ldapContextFactory.getLdapContext(upToken.getUsername(), String.valueOf(upToken.getPassword()));
                    } finally {
                        LdapUtils.closeContext(ctx);
                    }

                    return new SimpleAuthenticationInfo(userPrincipal, upToken.getPassword(), getName());
                }
            } else {
                LOGGER.warn("Multiple results found for {}", userPrincipalName);
            }
        } else {
            return super.queryForAuthenticationInfo(token, ldapContextFactory);
        }
        return null;
    }
}
