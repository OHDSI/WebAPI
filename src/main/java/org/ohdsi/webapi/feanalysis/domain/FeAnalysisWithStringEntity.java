package org.ohdsi.webapi.feanalysis.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@DiscriminatorValue("not null")
public class FeAnalysisWithStringEntity extends FeAnalysisEntity<String> {
    public FeAnalysisWithStringEntity() {
        super();
    }

    public FeAnalysisWithStringEntity(final FeAnalysisWithStringEntity analysis) {
        super(analysis);
    }
    
    @Lob
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private String design;

    @Override
    public String getDesign() {

        return design;
    }

    public void setDesign(final String design) {

        this.design = design;
    }
}
