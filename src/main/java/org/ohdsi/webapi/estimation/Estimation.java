package org.ohdsi.webapi.estimation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;

@Entity(name = "Estimation")
@Table(name="estimation")
public class Estimation extends CommonEntity {
    @Id
    @SequenceGenerator(name = "estimation_seq", sequenceName = "estimation_seq", allocationSize = 1)
    @GeneratedValue(generator = "estimation_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "estimation_id")
    private Integer id;

    @Column(name = "name")
    private String name;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "description")
    private String description;
    
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String specification;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", updatable = false)
    private UserEntity createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by_id")
    private UserEntity modifiedBy;

    /**
     * @return the id
     */
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
    public EstimationType getType() {
        return EstimationType.valueOf(type);
    }

    /**
     * @param type the type to set
     */
    public void setType(EstimationType type) {
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

    /**
     * @return the createdBy
     */
    public UserEntity getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the modifiedBy
     */
    public UserEntity getModifiedBy() {
        return modifiedBy;
    }

    /**
     * @param modifiedBy the modifiedBy to set
     */
    public void setModifiedBy(UserEntity modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
    
}
