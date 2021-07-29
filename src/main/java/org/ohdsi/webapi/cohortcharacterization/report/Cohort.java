package org.ohdsi.webapi.cohortcharacterization.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Cohort {
  private Integer cohortId;
  private String cohortName;

  @JsonCreator
  public Cohort(@JsonProperty("cohortId") Integer cohortId,
                @JsonProperty("cohortName") String cohortName) {
    this.cohortId = cohortId;
    this.cohortName = cohortName;
  }

  public Integer getCohortId() {
    return cohortId;
  }

  public String getCohortName() {
    return cohortName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Cohort)) return false;
    Cohort cohort = (Cohort) o;
    return Objects.equals(cohortId, cohort.cohortId) &&
            Objects.equals(cohortName, cohort.cohortName);
  }

  @Override
  public int hashCode() {

    return Objects.hash(cohortId, cohortName);
  }
}
