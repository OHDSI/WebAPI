package org.ohdsi.webapi.security.authc;

import org.ohdsi.webapi.security.provisioning.model.LdapProviderType;

public enum UserOrigin {
    SYSTEM, AD, LDAP, WINDOWS, KERBEROS, GOOGLE, FACEBOOK, DATABASE;

    public static UserOrigin getFrom(LdapProviderType ldapProviderType) {
        switch (ldapProviderType) {
            case LDAP: return LDAP;
            case ACTIVE_DIRECTORY: return AD;
        }
        return SYSTEM;
    }
}
