package org.ohdsi.webapi.cohortcharacterization.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.common.CommonConceptSetEntity;

import javax.persistence.*;

@Entity
@Table(name = "cc_strata_conceptset")
public class CcStrataConceptSetEntity extends CommonConceptSetEntity {
  @Id
  @GenericGenerator(
    name = "cc_strata_conceptset_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
      @Parameter(name = "sequence_name", value = "cc_strata_conceptset_seq"),
      @Parameter(name = "increment_size", value = "1")
    }
  )
  @GeneratedValue(generator = "cc_strata_conceptset_generator")
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
