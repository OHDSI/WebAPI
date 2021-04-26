package org.ohdsi.webapi.tag;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.model.CommonEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "Tag")
@Table(name = "tags")
public class Tag extends CommonEntity<Integer> {
    @Id
    @GenericGenerator(
            name = "tags_generator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "tags_seq"),
                    @Parameter(name = "increment_size", value = "1")
            }
    )
    @GeneratedValue(generator = "tags_generator")
    private int id;

    @ManyToMany(targetEntity = Tag.class, fetch = FetchType.LAZY)
    @JoinTable(name = "tag_groups",
            joinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id"))
    private Set<Tag> groups;

    @Column
    private String name;

    @Column
    private TagType type;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TagType getType() {
        return type;
    }

    public void setType(TagType type) {
        this.type = type;
    }

    public Set<Tag> getGroups() {
        return groups;
    }

    public void setGroups(Set<Tag> groups) {
        this.groups = groups;
    }
}
