/*
 * Copyright 2015 fdefalco.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.source;

import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.KerberosAuthMechanism;
import org.hibernate.annotations.Type;
import org.ohdsi.webapi.source.SourceDaimon.DaimonType;
import java.io.Serializable;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;
import org.ohdsi.webapi.source.SourceDaimon.DaimonType;

/**
 *
 * @author fdefalco
 */
@Entity(name = "Source")
@Table(name="source")
public class Source implements Serializable {

  public static final String MASQUERADED_USERNAME = "<username>";
  public static final String MASQUERADED_PASSWORD = "<password>";

  @Id
  @GeneratedValue
  @Column(name="SOURCE_ID")
  private int sourceId;

  @OneToMany(fetch= FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "source")
  private Collection<SourceDaimon> daimons;

  @Column(name="SOURCE_NAME")
  private String sourceName;

  @Column(name="SOURCE_DIALECT")
  private String sourceDialect;

  @Column(name="SOURCE_CONNECTION")
  private String sourceConnection;

  @Column(name="SOURCE_KEY")
  private String sourceKey;

  @Column
  @Type(type = "encryptedString")
  private String username;

  @Column
  @Type(type = "encryptedString")
  private String password;

    @Column(name = "krb_keytab")
    private byte[] krbKeytab;

    @Column(name = "keytab_name")
    private String keytabName;

    @Column(name = "krb_admin_server")
    private String krbAdminServer;

    @Column(name = "krb_auth_method")
    @Enumerated(EnumType.STRING)
    private KerberosAuthMechanism krbAuthMethod;

  public String getTableQualifier(DaimonType daimonType) {
		String result = getTableQualifierOrNull(daimonType);
		if (result == null)
			throw new RuntimeException("DaimonType (" + daimonType + ") not found in Source");
		return result;
  }

	  public String getTableQualifierOrNull(DaimonType daimonType) {
    for (SourceDaimon sourceDaimon : this.getDaimons()) {
      if (sourceDaimon.getDaimonType() == daimonType) {
        return sourceDaimon.getTableQualifier();
      }
    }
		return null;
  }

  public String getSourceKey() {
    return sourceKey;
  }

  public Collection<SourceDaimon> getDaimons() {
    return daimons;
  }

  public void setDaimons(Collection<SourceDaimon> daimons) {
    this.daimons = daimons;
  }

  public void setSourceKey(String sourceKey) {
    this.sourceKey = sourceKey;
  }

  public int getSourceId() {
    return sourceId;
  }

  public void setSourceId(int sourceId) {
    this.sourceId = sourceId;
  }

  public String getSourceName() {
    return sourceName;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  public String getSourceDialect() {
    return sourceDialect;
  }

  public void setSourceDialect(String sourceDialect) {
    this.sourceDialect = sourceDialect;
  }

  public String getSourceConnection() {
    return sourceConnection;
  }

  public void setSourceConnection(String sourceConnection) {
    this.sourceConnection = sourceConnection;
  }

  public SourceInfo getSourceInfo() {
    return new SourceInfo(this);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

    public byte[] getKrbKeytab() {
        return krbKeytab;
    }

    public void setKrbKeytab(byte[] krbKeytab) {
        this.krbKeytab = krbKeytab;
    }

    public String getKeytabName() {
        return keytabName;
    }

    public void setKeytabName(String keytabName) {
        this.keytabName = keytabName;
    }

    public KerberosAuthMechanism getKrbAuthMethod() {
        return krbAuthMethod;
    }

    public void setKrbAuthMethod(KerberosAuthMechanism krbAuthMethod) {
        this.krbAuthMethod = krbAuthMethod;
    }

    public String getKrbAdminServer() {
        return krbAdminServer;
    }

    public void setKrbAdminServer(String krbAdminServer) {
        this.krbAdminServer = krbAdminServer;
    }
}
