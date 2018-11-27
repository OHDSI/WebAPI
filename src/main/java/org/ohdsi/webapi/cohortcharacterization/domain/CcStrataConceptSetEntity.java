package org.ohdsi.webapi.cohortcharacterization.domain;

import org.ohdsi.webapi.common.CommonConceptSetEntity;

import javax.persistence.*;

@Entity
@Table(name = "cc_strata_conceptset")
public class CcStrataConceptSetEntity extends CommonConceptSetEntity {
  @Id
  @SequenceGenerator(name = "cc_strata_conceptset_sequence", sequenceName = "cc_strata_conceptset_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cc_strata_conceptset_sequence")
  private Long id;

  @OneToOne(optional = false, targetEntity = CohortCharacterizationEntity.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "cohort_characterization_id")
  private CohortCharacterizationEntity cohortCharacterization;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public CohortCharacterizationEntity getCohortCharacterization() {
    return cohortCharacterization;
  }

  public void setCohortCharacterization(CohortCharacterizationEntity cohortCharacterization) {
    this.cohortCharacterization = cohortCharacterization;
  }
}
