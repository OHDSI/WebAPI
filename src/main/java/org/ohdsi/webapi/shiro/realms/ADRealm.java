package org.ohdsi.webapi.shiro.realms;

import java.util.List;
import java.util.Objects;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

public class ADRealm extends ActiveDirectoryRealm {
    private static final Logger LOGGER = LoggerFactory.getLogger(ADRealm.class);

    private String searchFilter;

    private LdapTemplate ldapTemplate;

    private AttributesMapper<String> dnAttributesMapper = (AttributesMapper<String>) attrs -> (String) attrs.get("distinguishedName").get();

    public ADRealm() {
    }

    public ADRealm(LdapTemplate ldapTemplate, String searchFilter) {
        this.ldapTemplate = ldapTemplate;
        this.searchFilter = searchFilter;
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

        if (Objects.nonNull(ldapTemplate) && StringUtils.isNotBlank(searchFilter)) {
            UsernamePasswordToken upToken = (UsernamePasswordToken) token;
            String userPrincipalName = getUserPrincipalName(upToken.getUsername());

            String userSearch = String.format("(&(objectClass=*)(userPrincipalName=%s))", userPrincipalName);
            List<String> result = ldapTemplate.search("", userSearch, SearchControls.SUBTREE_SCOPE,
                    dnAttributesMapper);

            if (result.size() == 1) {
                String userDn = result.iterator().next();
                List<String> filterResult = ldapTemplate.search("", String.format(searchFilter, userDn), SearchControls.SUBTREE_SCOPE,
                        dnAttributesMapper);
                if (!filterResult.isEmpty()) {
                    return super.queryForAuthenticationInfo(token, ldapContextFactory);
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
