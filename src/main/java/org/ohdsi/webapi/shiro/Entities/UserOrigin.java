package org.ohdsi.webapi.shiro.Entities;

import org.ohdsi.webapi.user.importer.model.LdapProviderType;

public enum UserOrigin {
    SYSTEM, AD, LDAP;

    public static UserOrigin getFrom(LdapProviderType ldapProviderType) {
        switch (ldapProviderType) {
            case LDAP: return LDAP;
            case ACTIVE_DIRECTORY: return AD;
        }
        return SYSTEM;
    }
}
