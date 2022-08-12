package org.ohdsi.webapi.tag.domain;

public interface HasTags<T extends Number> {
    void assignTag(T id, int tagId);

    void unassignTag(T id, int tagId);
}
