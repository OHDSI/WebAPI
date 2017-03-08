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

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.ohdsi.webapi.source.SourceDaimon.DaimonType;

/**
 *
 * @author fdefalco
 */
@Entity(name = "Source")
@Table(name="source")
public class Source implements Serializable {
  
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
  
  @Column(name="GROUP_KEY")
  private String groupKey;
  
  @Column(name="GROUP_PRIORITY")
  private int groupPriority;
  
  @Column(name="VERSION_ID")
  private int versionId;
  
  @Column(name="VERSION_DESC")
  private String versionDesc;
  
  @Column(name="ACTIVE")
  private int active;

  
  public String getTableQualifier(DaimonType daimonType) {
    for (SourceDaimon sourceDaimon : this.getDaimons()) {
      if (sourceDaimon.getDaimonType() == daimonType) {
        return sourceDaimon.getTableQualifier();
      } 
    }
    
    throw new RuntimeException("DaimonType (" + daimonType + ") not found in Source");
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

    /**
     * @return the active
     */
    public int getActive() {
        return active;
    }

    /**
     * @return the groupKey
     */
    public String getGroupKey() {
        return groupKey;
    }

    /**
     * @return the groupPriority
     */
    public int getGroupPriority() {
        return groupPriority;
    }

    /**
     * @return the versionDesc
     */
    public String getVersionDesc() {
        return versionDesc;
    }

    /**
     * @return the versionId
     */
    public int getVersionId() {
        return versionId;
    }

    /**
     * @param active the active to set
     */
    public void setActive(int active) {
        this.active = active;
    }

    /**
     * @param groupKey the groupKey to set
     */
    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    /**
     * @param groupPriority the groupPriority to set
     */
    public void setGroupPriority(int groupPriority) {
        this.groupPriority = groupPriority;
    }

    /**
     * @param versionDesc the versionDesc to set
     */
    public void setVersionDesc(String versionDesc) {
        this.versionDesc = versionDesc;
    }

    /**
     * @param versionId the versionId to set
     */
    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }
}
