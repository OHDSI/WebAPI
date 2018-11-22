package org.ohdsi.webapi.cohortcharacterization.domain;

import java.util.*;
import javax.persistence.*;

import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.model.CommonEntity;

@Entity
@Table(name = "cohort_characterization")
public class CohortCharacterizationEntity extends CommonEntity implements CohortCharacterization {

    @Id
    @SequenceGenerator(name = "cohort_characterization_pk_sequence", sequenceName = "cohort_characterization_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cohort_characterization_pk_sequence")
    private Long id;
    
    @Column
    private String name;
    
    @ManyToMany(targetEntity = CohortDefinition.class, fetch = FetchType.LAZY)
    @JoinTable(name = "cc_cohort",
            joinColumns = @JoinColumn(name = "cohort_characterization_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "cohort_id", referencedColumnName = "id"))
    private Set<CohortDefinition> cohortDefinitions = new HashSet<>();
    
    @ManyToMany(targetEntity = FeAnalysisEntity.class, fetch = FetchType.LAZY)
    @JoinTable(name = "cc_analysis",
            joinColumns = @JoinColumn(name = "cohort_characterization_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "fe_analysis_id", referencedColumnName = "id"))
    private Set<FeAnalysisEntity> featureAnalyses = new HashSet<>();
    
    @OneToMany(mappedBy = "cohortCharacterization", fetch = FetchType.LAZY, targetEntity = CcParamEntity.class)
    private Set<CcParamEntity> parameters = new HashSet<>();

    @OneToMany(mappedBy = "cohortCharacterization", fetch = FetchType.LAZY, targetEntity = CcStrataEntity.class)
    private Set<CcStrataEntity> stratas = new HashSet<>();

    @Column(name = "stratified_by")
    private String stratifiedBy;

    @OneToOne(mappedBy = "cohortCharacterization", cascade = CascadeType.ALL)
    private CcConceptSetEntity conceptSetEntity;
    
    @Column(name = "hash_code")
    private Integer hashCode;
    
    @Override
    public Set<CohortDefinition> getCohorts() {
        return cohortDefinitions;
    }

    @Override
    public Set<FeAnalysisEntity> getFeatureAnalyses() {
        return featureAnalyses;
    }

    @Override
    public Set<CcParamEntity> getParameters() {
        return parameters;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setParameters(final Set<CcParamEntity> parameters) {
        this.parameters = parameters;
    }

    public void setFeatureAnalyses(final Set<FeAnalysisEntity> featureAnalyses) {
        this.featureAnalyses = featureAnalyses;
    }

    public Set<CohortDefinition> getCohortDefinitions() {
        return cohortDefinitions;
    }

    public void setCohortDefinitions(final Set<CohortDefinition> cohortDefinitions) {
        this.cohortDefinitions = cohortDefinitions;
    }

    @Override
    public Set<CcStrataEntity> getStratas() {
        return stratas;
    }

    public void setStratas(Set<CcStrataEntity> stratas) {
        this.stratas = stratas;
    }

    public String getStratifiedBy() {
        return stratifiedBy;
    }

    public void setStratifiedBy(String stratifiedBy) {
        this.stratifiedBy = stratifiedBy;
    }

    public CcConceptSetEntity getConceptSetEntity() {
        return conceptSetEntity;
    }

    public void setConceptSetEntity(CcConceptSetEntity conceptSetEntity) {
        this.conceptSetEntity = conceptSetEntity;
    }

    public List<ConceptSet> getConceptSets() {
        return Objects.nonNull(this.conceptSetEntity) ? this.conceptSetEntity.getConceptSets() : Collections.emptyList();
    }

    public Integer getHashCode() {
        return hashCode;
    }

    public void setHashCode(final Integer hashCode) {
        this.hashCode = hashCode;
    }
}
