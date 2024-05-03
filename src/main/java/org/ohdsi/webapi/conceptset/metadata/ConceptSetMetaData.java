package org.ohdsi.webapi.conceptset.metadata;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.model.CommonEntity;

@Entity(name = "ConceptSetMetaData")
@Table(name = "concept_set_meta_data")
public class ConceptSetMetaData extends CommonEntity<Integer> implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    @Id
    @GenericGenerator(
        name = "concept_set_meta_data_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "concept_set_meta_data_sequence"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "concept_set_meta_data_generator")
    @Column(name="concept_set_meta_data_id")
    private Integer id;
    
    @Column(name = "concept_set_id", nullable = false)
    private Integer conceptSetId;
    
    @Column(name = "concept_id")
    private Integer conceptId;
    
    @Column(name = "metadata")
    private String metadata;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getConceptSetId() {
        return conceptSetId;
    }
    
    public void setConceptSetId(Integer conceptSetId) {
        this.conceptSetId = conceptSetId;
    }
    
    public Integer getConceptId() {
        return conceptId;
    }
    
    public void setConceptId(Integer conceptId) {
        this.conceptId = conceptId;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
