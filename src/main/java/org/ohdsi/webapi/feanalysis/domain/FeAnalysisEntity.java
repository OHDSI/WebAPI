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
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.ohdsi.analysis.cohortcharacterization.design.CcResultType;
import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysis;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.model.CommonEntity;

@Entity
@Table(name = "fe_analysis")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
        "CASE WHEN type = 'CRITERIA_SET' THEN CONCAT(CONCAT(type,'_'),stat_type) " +
          "ELSE type END"
)
public abstract class FeAnalysisEntity<T> extends CommonEntity<Integer> implements FeatureAnalysis<T, Integer>, Comparable<FeAnalysisEntity<String>> {

    public FeAnalysisEntity() {
    }

    public FeAnalysisEntity(final FeAnalysisEntity<T> entityForCopy) {
        this.id = entityForCopy.id;
        this.type = entityForCopy.type;
        this.name = entityForCopy.name;
        this.setDesign(entityForCopy.getDesign());
        this.domain = entityForCopy.domain;
        this.descr = entityForCopy.descr;
        this.isLocked = entityForCopy.isLocked;
        this.statType = entityForCopy.statType;
    }
    
    @Id
    @GenericGenerator(
        name = "fe_analysis_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "fe_analysis_sequence"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "fe_analysis_generator")
    private Integer id;

    @Column
    @Enumerated(EnumType.STRING)
    private StandardFeatureAnalysisType type;

    @Column
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "design", insertable = false, updatable = false)
    private String rawDesign;
    
    @Column
    @Enumerated(EnumType.STRING)
    private StandardFeatureAnalysisDomain domain;

    @Column
    private String descr;
    
    @Column(name = "is_locked")
    private Boolean isLocked;

    @ManyToMany(targetEntity = CohortCharacterizationEntity.class, fetch = FetchType.LAZY, mappedBy = "featureAnalyses")
    private Set<CohortCharacterizationEntity> cohortCharacterizations = new HashSet<>();

    @Column(name = "stat_type")
    @Enumerated(value = EnumType.STRING)
    private CcResultType statType;

    @Override
    public Integer getId() {
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
    public abstract T getDesign();

    public abstract void setDesign(T design);
    
    public boolean isPreset() {
        return this.type == StandardFeatureAnalysisType.PRESET;
    }
    
    public boolean isCustom() {
        return this.type == StandardFeatureAnalysisType.CUSTOM_FE;
    }
    
    public boolean isCriteria() {
        return this.type == StandardFeatureAnalysisType.CRITERIA_SET;
    }

    public void setId(final Integer id) {
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

    public String getRawDesign() {
        return rawDesign;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FeAnalysisEntity)) return false;
        final FeAnalysisEntity that = (FeAnalysisEntity) o;
        if (getId() != null && that.getId() != null) {
            return Objects.equals(getId(), that.getId());
        } else {
            return Objects.equals(getType(), that.getType()) && Objects.equals(getDesign(), that.getDesign());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
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
        return ObjectUtils.compare(this.name, o.name);
    }

    public CcResultType getStatType() {

        return statType;
    }

    public void setStatType(final CcResultType statType) {

        this.statType = statType;
    }
}

