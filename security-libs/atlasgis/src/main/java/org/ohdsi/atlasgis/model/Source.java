package org.ohdsi.atlasgis.model;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.KerberosAuthMechanism;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Source {

    private String key;
    private String sourceName;
    private DBMSType dialect;
    private String connectionString;
    private String username;
    private String password;
    private KerberosAuthMechanism krbAuthMethod;
    private String keyfileName;
    private byte[] keyfile;
    private String krbAdminServer;
    private String cdmSchema;
    private String resultsSchema;
    private String tempSchema;
}
