package org.ohdsi.webapi.exampleapplication.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 *
 */
@Entity(name = "EXAMPLEAPP_WIDGET")
public class Widget implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GenericGenerator(
        name = "exampleapp_widget_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "increment_size", value = "1")
        }
    )
    @GeneratedValue(generator = "exampleapp_widget_generator")
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return this.id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return String.format("Widget Id=%s Name=%s", getId(), getName());
    }
}
