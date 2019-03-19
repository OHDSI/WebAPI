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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.KerberosAuthMechanism;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;
import org.ohdsi.webapi.source.SourceDaimon.DaimonType;

/**
 *
 * @author fdefalco
 */
@Entity(name = "Source")
@Table(name="source")
@SQLDelete(sql = "UPDATE {h-schema}source SET deleted_date = current_timestamp WHERE SOURCE_ID = ?")
@Where(clause = "deleted_date IS NULL")
public class Source implements Serializable {

  public static final String MASQUERADED_USERNAME = "<username>";
  public static final String MASQUERADED_PASSWORD = "<password>";
  public static final String IMPALA_DATASOURCE = "impala";

  @Id
  @GenericGenerator(
    name = "source_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
      @Parameter(name = "sequence_name", value = "source_sequence"),
      @Parameter(name = "increment_size", value = "1")
    }
  )
  @GeneratedValue(generator = "source_generator")
  @Column(name="SOURCE_ID")
  private int sourceId;

  @OneToMany(fetch= FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "source")
  @Where(clause = "priority >= 0")
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

  @Column(name = "deleted_date")
  private Date deletedDate;

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

  @Override
  public boolean equals(Object o) {

    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Source source = (Source) o;
    return sourceId == source.sourceId;
  }

  @Override
  public int hashCode() {

    return Objects.hash(sourceId);
  }

  @Override
  public String toString() {
    String source = "sourceId=" + sourceId +
                    ", daimons=" + daimons +
                    ", sourceName='" + sourceName + '\'' +
                    ", sourceDialect='" + sourceDialect + '\'' +
                    ", sourceKey='" + sourceKey;
    if (IMPALA_DATASOURCE.equalsIgnoreCase(sourceDialect)){
      source += '\'' +
              ", krbAdminServer='" + krbAdminServer + '\'' +
              ", krbAuthMethod=" + krbAuthMethod;
    }
    return source;
  }
}
