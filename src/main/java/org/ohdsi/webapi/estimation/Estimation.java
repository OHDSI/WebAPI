package org.ohdsi.webapi.estimation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.analysis.estimation.design.EstimationTypeEnum;
import org.ohdsi.webapi.model.CommonEntity;

@Entity(name = "Estimation")
@Table(name="estimation")
public class Estimation extends CommonEntity<Integer> {
    @Id
    @GenericGenerator(
        name = "estimation_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "estimation_seq"),
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "estimation_generator")
    @Column(name = "estimation_id")
    private Integer id;

    @Column(name = "name")
    private String name;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "description")
    private String description;
    
    @Lob
    private String specification;

    /**
     * @return the id
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the type
     */
    public EstimationTypeEnum getType() {
        return EstimationTypeEnum.fromValue(type);
    }

    /**
     * @param type the type to set
     */
    public void setType(EstimationTypeEnum type) {
        this.type = type.toString();
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the specification
     */
    public String getSpecification() {
        return specification;
    }

    /**
     * @param specification the specification to set
     */
    public void setSpecification(String specification) {
        this.specification = specification;
    }
    
}
