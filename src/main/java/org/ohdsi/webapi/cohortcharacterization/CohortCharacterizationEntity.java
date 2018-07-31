package org.ohdsi.webapi.cohortcharacterization;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.Cohort;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.feanalysis.FeAnalysisEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;

@Entity
@Table(name = "cohort_characterizations")
public class CohortCharacterizationEntity implements CohortCharacterization {

    @Id
    @SequenceGenerator(name = "cohort_characterizations_pk_sequence", sequenceName = "cohort_characterizations_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cohort_characterizations_pk_sequence")
    private Long id;
    
    @Column
    private String name;
    
    @ManyToOne
    @JoinColumn(name="created_by")
    private UserEntity createdBy;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
    
    @ManyToOne
    @JoinColumn(name="updated_by")
    private UserEntity updatedBy;
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @ManyToMany(targetEntity = CohortDefinition.class, fetch = FetchType.LAZY)
    @JoinTable(name = "cohort_characterizations_cohorts",
            joinColumns = @JoinColumn(name = "cohort_characterization_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "cohort_id", referencedColumnName = "id"))
    private List<CohortDefinition> cohortDefinitions = new ArrayList<>();
    
    @ManyToMany(targetEntity = FeAnalysisEntity.class, fetch = FetchType.LAZY)
    @JoinTable(name = "cohort_characterizations_analyses",
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

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    public UserEntity getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(final UserEntity updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final Date updatedAt) {
        this.updatedAt = updatedAt;
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
