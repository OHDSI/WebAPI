package org.ohdsi.webapi.tag.domain;

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
import java.util.List;
import java.util.Objects;

public interface TagCount {
//    @Id
//    @GenericGenerator(
//            name = "tags_generator",
//            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
//            parameters = {
//                    @Parameter(name = "sequence_name", value = "tags_seq"),
//                    @Parameter(name = "increment_size", value = "1")
//            }
//    )
//    @GeneratedValue(generator = "tags_generator")
//    private int id;
//
//    @ManyToMany(targetEntity = TagCount.class, fetch = FetchType.LAZY)
//    @JoinTable(name = "tag_groups",
//            joinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"),
//            inverseJoinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id"))
//    private List<TagCount> groups;
//
//    @Column
//    private String name;
//
//    @Column
//    private TagType type;
//
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public TagType getType() {
//        return type;
//    }
//
//    public void setType(TagType type) {
//        this.type = type;
//    }
//
//    public List<TagCount> getGroups() {
//        return groups;
//    }
//
//    public void setGroups(List<TagCount> groups) {
//        this.groups = groups;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        TagCount tag = (TagCount) o;
//        return name.equals(tag.name);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(name);
//    }
}
