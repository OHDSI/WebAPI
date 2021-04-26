package org.ohdsi.webapi.model;

import org.ohdsi.analysis.WithId;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.tag.Tag;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@MappedSuperclass
public abstract class CommonEntityExt<T extends Number> extends CommonEntity<T> {
    public abstract Set<Tag> getTags();

    public abstract void setTags(Set<Tag> tags);
}
