package org.ohdsi.webapi;

import org.ohdsi.webapi.arachne.datasource.dto.DataSourceUnsecuredDTO;
import org.ohdsi.webapi.arachne.datasource.util.ConnectionParams;
import org.ohdsi.webapi.source.Source;

public final class KerberosUtils {

    private KerberosUtils(){}

    public static void setKerberosParams(Source source, ConnectionParams connectionParams, DataSourceUnsecuredDTO ds) {
        ds.setUseKerberos(Boolean.TRUE);
        ds.setKrbAuthMethod(source.getKrbAuthMethod());
        ds.setKeyfile(source.getKeyfile());
        if (source.getKrbAdminServer() != null) {
            ds.setKrbAdminFQDN(source.getKrbAdminServer());
        } else {
            ds.setKrbAdminFQDN(connectionParams.getKrbFQDN());
        }
        ds.setKrbFQDN(connectionParams.getKrbFQDN());
        ds.setKrbRealm(connectionParams.getKrbRealm());
        ds.setKrbPassword(connectionParams.getPassword());
        ds.setKrbUser(connectionParams.getUser());
    }
}
