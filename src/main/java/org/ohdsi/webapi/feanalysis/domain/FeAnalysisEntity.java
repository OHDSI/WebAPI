package org.ohdsi.webapi.feanalysis.domain;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.DiscriminatorFormula;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.FeatureAnalysis;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.cohortcharacterization.CcResultType;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;

@Entity
@Table(name = "fe_analyses")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("type")
public class 
FeAnalysisEntity implements FeatureAnalysis, Comparable<FeAnalysisEntity> {

    public FeAnalysisEntity() {
    }

    public FeAnalysisEntity(final FeAnalysisEntity entityForCopy) {
        this.id = entityForCopy.id;
        this.type = entityForCopy.type;
        this.name = entityForCopy.name;
        this.value = entityForCopy.value;
        this.domain = entityForCopy.domain;
        this.descr = entityForCopy.descr;
        this.isLocked = entityForCopy.isLocked;
    }
    
    @Id
    @SequenceGenerator(name = "fe_analyses_pk_sequence", sequenceName = "fe_analyses_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fe_analyses_pk_sequence")
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    private StandardFeatureAnalysisType type;

    @Column
    private String name;

    @Column
    private String value;
    
    @Column
    @Enumerated(EnumType.STRING)
    private StandardFeatureAnalysisDomain domain;

    @Column
    private String descr;
    
    @Column(name = "is_locked")
    private Boolean isLocked;

    @ManyToMany(targetEntity = CohortCharacterizationEntity.class, fetch = FetchType.LAZY)
    @JoinTable(name = "cc_analyses",
            joinColumns = @JoinColumn(name = "fe_analysis_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "cohort_characterization_id", referencedColumnName = "id"))
    private Set<CohortCharacterizationEntity> cohortCharacterizations = new HashSet<>();

    @Column(name = "stat_type")
    @Enumerated(value = EnumType.STRING)
    private CcResultType statType;
    
    public Long getId() {
        return id;
    }

    @Override
    public StandardFeatureAnalysisType getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public StandardFeatureAnalysisDomain getDomain() {
        return domain;
    }

    @Override
    public String getDescr() {
        return descr;
    }

    @Override
    public <T> T getDesign() {
        return null;
    }
    
    public boolean isPreset() {
        return this.type == StandardFeatureAnalysisType.PRESET;
    }
    
    public boolean isCustom() {
        return this.type == StandardFeatureAnalysisType.CUSTOM_FE;
    }
    
    public boolean isCriteria() {
        return this.type == StandardFeatureAnalysisType.CRITERIA_SET;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setType(final StandardFeatureAnalysisType type) {
        this.type = type;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDomain(final StandardFeatureAnalysisDomain domain) {
        this.domain = domain;
    }

    public void setDescr(final String descr) {
        this.descr = descr;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FeAnalysisEntity)) return false;
        final FeAnalysisEntity that = (FeAnalysisEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), super.hashCode());
    }

    public Boolean getLocked() {
        return isLocked;
    }

    public void setLocked(final Boolean locked) {
        isLocked = locked;
    }

    public Set<CohortCharacterizationEntity> getCohortCharacterizations() {

        return cohortCharacterizations;
    }

    public void setCohortCharacterizations(final Set<CohortCharacterizationEntity> cohortCharacterizations) {

        this.cohortCharacterizations = cohortCharacterizations;
    }

    @Override
    public int compareTo(final FeAnalysisEntity o) {
        return this.name.compareTo(o.name);
    }

    public CcResultType getStatType() {

        return statType;
    }

    public void setStatType(final CcResultType statType) {

        this.statType = statType;
    }
}

