package org.ohdsi.webapi.events;

import org.springframework.context.ApplicationEvent;

public class DeleteEntityEvent extends ApplicationEvent {
    private final Integer id;

    public DeleteEntityEvent(Object source, Integer id) {
        super(source);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
