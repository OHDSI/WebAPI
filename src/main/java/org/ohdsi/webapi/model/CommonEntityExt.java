package org.ohdsi.webapi.model;

import org.ohdsi.webapi.tag.domain.Tag;

import javax.persistence.MappedSuperclass;
import java.util.List;
import java.util.Set;

@MappedSuperclass
public abstract class CommonEntityExt<T extends Number> extends CommonEntity<T> {
    public abstract Set<Tag> getTags();

    public abstract void setTags(Set<Tag> tags);
}
