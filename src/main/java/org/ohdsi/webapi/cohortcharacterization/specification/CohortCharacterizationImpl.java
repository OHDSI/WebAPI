package org.ohdsi.webapi.cohortcharacterization.specification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.analysis.hydra.design.SkeletonTypeEnum;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CohortCharacterizationImpl extends CcExportDTO implements CohortCharacterization {

  @JsonProperty
  private SkeletonTypeEnum skeletonType = SkeletonTypeEnum.COHORT_CHARACTERIZATION;

  @JsonProperty
  private String skeletonVersion = "v0.0.1";

  @JsonProperty
  private String packageName = null;

  @JsonProperty
  private String organizationName = null;

  public SkeletonTypeEnum getSkeletonType() {
    return skeletonType;
  }

  public void setSkeletonType(SkeletonTypeEnum skeletonType) {
    this.skeletonType = skeletonType;
  }

  public String getSkeletonVersion() {
    return skeletonVersion;
  }

  public void setSkeletonVersion(String skeletonVersion) {
    this.skeletonVersion = skeletonVersion;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getOrganizationName() {
    return organizationName;
  }

  public void setOrganizationName(String organizationName) {
    this.organizationName = organizationName;
  }
}
