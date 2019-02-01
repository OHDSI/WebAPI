package org.ohdsi.webapi.cohortcharacterization.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.*;

import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
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
    private List<CohortDefinition> cohortDefinitions = new ArrayList<>();
    
    @ManyToMany(targetEntity = FeAnalysisEntity.class, fetch = FetchType.LAZY)
    @JoinTable(name = "cc_analysis",
            joinColumns = @JoinColumn(name = "cohort_characterization_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "fe_analysis_id", referencedColumnName = "id"))
    private Set<FeAnalysisEntity> featureAnalyses = new HashSet<>();
    
    @OneToMany(mappedBy = "cohortCharacterization", fetch = FetchType.LAZY, targetEntity = CcParamEntity.class)
    private Set<CcParamEntity> parameters = new HashSet<>();
    
    @Column(name = "hash_code")
    private Integer hashCode;
    
    @Override
    public List<CohortDefinition> getCohorts() {
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

    public List<CohortDefinition> getCohortDefinitions() {
        return cohortDefinitions;
    }

    public void setCohortDefinitions(final List<CohortDefinition> cohortDefinitions) {
        this.cohortDefinitions = cohortDefinitions;
    }

    public Integer getHashCode() {
        return hashCode;
    }

    public void setHashCode(final Integer hashCode) {
        this.hashCode = hashCode;
    }
}
