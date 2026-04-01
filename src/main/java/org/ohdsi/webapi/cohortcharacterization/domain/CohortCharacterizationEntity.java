package org.ohdsi.webapi.cohortcharacterization.domain;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.model.CommonEntityExt;
import org.ohdsi.webapi.tag.domain.Tag;

@Entity
@Table(name = "cohort_characterization")
public class CohortCharacterizationEntity extends CommonEntityExt<Long> implements CohortCharacterization {

    @Id
    @GenericGenerator(
        name = "cohort_characterization_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "cohort_characterization_seq"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "cohort_characterization_generator")
    private Long id;
    
    @Column
    private String name;

    @Column
    private String description;
    
    @ManyToMany(targetEntity = CohortDefinitionEntity.class, fetch = FetchType.LAZY)
    @JoinTable(name = "cc_cohort",
            joinColumns = @JoinColumn(name = "cohort_characterization_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "cohort_id", referencedColumnName = "id"))
    private Set<CohortDefinitionEntity> cohortDefinitions = new HashSet<>();
    
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "cohort_characterization_id", insertable = false, updatable = false, nullable = false)
    private Set<CcFeAnalysisEntity> featureAnalyses = new HashSet<>();
    
    @OneToMany(mappedBy = "cohortCharacterization", fetch = FetchType.LAZY, targetEntity = CcParamEntity.class)
    private Set<CcParamEntity> parameters = new HashSet<>();

    @OneToMany(mappedBy = "cohortCharacterization", fetch = FetchType.LAZY, targetEntity = CcStrataEntity.class)
    private Set<CcStrataEntity> stratas = new HashSet<>();

    @Column(name = "stratified_by")
    private String stratifiedBy;

    @Column(name = "strata_only")
    private Boolean strataOnly;

    @OneToOne(mappedBy = "cohortCharacterization", cascade = CascadeType.ALL)
    private CcStrataConceptSetEntity conceptSetEntity;
    
    @Column(name = "hash_code")
    private Integer hashCode;

    @ManyToMany(targetEntity = Tag.class, fetch = FetchType.LAZY)
    @JoinTable(name = "cohort_characterization_tag",
            joinColumns = @JoinColumn(name = "asset_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"))
    private Set<Tag> tags;
    
    @Override
    public Set<CohortDefinitionEntity> getCohorts() {
        return cohortDefinitions;
    }

    @Override
    public Set<FeAnalysisEntity> getFeatureAnalyses() {
        return featureAnalyses != null ?
                featureAnalyses.stream().map(CcFeAnalysisEntity::getFeatureAnalysis).collect(Collectors.toSet()) :
                Collections.emptySet();
    }

    public Set<CcFeAnalysisEntity> getCcFeatureAnalyses() {
        return featureAnalyses;
    }

    @Override
    public Set<CcParamEntity> getParameters() {
        return parameters;
    }

    public void setFeatureAnalyses(Set<CcFeAnalysisEntity> featureAnalyses) {
        this.featureAnalyses = featureAnalyses;
    }

    @Override
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setParameters(final Set<CcParamEntity> parameters) {
        this.parameters = parameters;
    }

    public Set<CohortDefinitionEntity> getCohortDefinitions() {
        return cohortDefinitions;
    }

    public void setCohortDefinitions(final Set<CohortDefinitionEntity> cohortDefinitions) {
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

    public Boolean getStrataOnly() {
        return Objects.nonNull(strataOnly) ? strataOnly : false;
    }

    public void setStrataOnly(Boolean strataOnly) {
        this.strataOnly = strataOnly;
    }

    public CcStrataConceptSetEntity getConceptSetEntity() {
        return conceptSetEntity;
    }

    public void setConceptSetEntity(CcStrataConceptSetEntity conceptSetEntity) {
        this.conceptSetEntity = conceptSetEntity;
    }

    @Override
    public Collection<ConceptSet> getStrataConceptSets() {
        return Objects.nonNull(this.conceptSetEntity) ? this.conceptSetEntity.getConceptSets() : Collections.emptyList();
    }

    public Integer getHashCode() {
        return hashCode;
    }

    public void setHashCode(final Integer hashCode) {
        this.hashCode = hashCode;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
}
