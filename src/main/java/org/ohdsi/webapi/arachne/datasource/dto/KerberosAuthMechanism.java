package org.ohdsi.webapi.arachne.datasource.dto;

public enum KerberosAuthMechanism {
    PASSWORD, KEYTAB, DEFAULT;

    public static KerberosAuthMechanism getByName(String name) {

        for (KerberosAuthMechanism auth : values()) {
            if (auth.toString().equals(name.toUpperCase())) {
                return auth;
            }
        }
        return DEFAULT;
    }
}
