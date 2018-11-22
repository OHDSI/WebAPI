package org.ohdsi.webapi.cohortcharacterization.domain;

import org.ohdsi.webapi.common.CommonConceptSetEntity;

import javax.persistence.*;

@Entity
@Table(name = "cc_conceptset")
public class CcConceptSetEntity extends CommonConceptSetEntity {
  @Id
  @SequenceGenerator(name = "cc_conceptset_sequence", sequenceName = "cc_conceptset_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cc_conceptset_sequence")
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
