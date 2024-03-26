package org.ohdsi.webapi.conceptset.criteria;

import org.ohdsi.webapi.conceptset.ConceptSet;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.MapsId;

@Entity
@Table(name = "concept_set_criteria", schema = "webapi")
public class ConceptSetCriterion {

    @Id
    @Column(name = "concept_set_id", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concept_set_id", nullable = false)
    private ConceptSet conceptSet;

    @Column(name = "criteria")
    private String criteria;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public ConceptSet getConceptSet() {
        return conceptSet;
    }
    public void setConceptSet(ConceptSet conceptSet) {
        this.conceptSet = conceptSet;
    }
    public String getCriteria() {
        return criteria;
    }
    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }
}