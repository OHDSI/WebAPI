package org.ohdsi.webapi.cohortcharacterization.domain;

import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterizationStrata;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;

import javax.persistence.*;

@Entity
@Table(name = "cc_strata")
public class CcStrataEntity implements CohortCharacterizationStrata {

  @Id
  @SequenceGenerator(name = "cc_strata_pk_sequence", sequenceName = "cc_strata_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cc_strata_pk_sequence")
  private Long id;
  @Column(name = "name")
  private String name;
  @Column(name = "expression")
  private String expressionString;
  @ManyToOne(optional = false, targetEntity = CohortCharacterizationEntity.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "cohort_characterization_id")
  private CohortCharacterizationEntity cohortCharacterization;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public CriteriaGroup getCriteria() {
    return Utils.deserialize(expressionString, CriteriaGroup.class);
  }

  public String getExpressionString() {
    return expressionString;
  }

  public void setExpressionString(String expressionString) {
    this.expressionString = expressionString;
  }

  public CohortCharacterizationEntity getCohortCharacterization() {
    return cohortCharacterization;
  }

  public void setCohortCharacterization(CohortCharacterizationEntity cohortCharacterization) {
    this.cohortCharacterization = cohortCharacterization;
  }
}
