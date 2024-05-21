package org.ohdsi.webapi.cohortcharacterization.domain;

import java.util.*;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
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
