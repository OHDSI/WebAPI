package org.ohdsi.webapi.events;

import org.springframework.context.ApplicationEvent;

public class DeleteEntityEvent extends ApplicationEvent {
    private final Integer id;
    private final EntityName entityName;

    public DeleteEntityEvent(Object source, Integer id, EntityName entityName) {
        super(source);
        this.id = id;
        this.entityName = entityName;
    }

    public Integer getId() {
        return id;
    }

    public EntityName getEntityName() {
        return entityName;
    }
}
