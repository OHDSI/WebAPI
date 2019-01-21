package org.ohdsi.webapi.events;

import org.springframework.context.ApplicationEvent;

public class DeleteConceptSetEvent extends ApplicationEvent {
    private final Integer id;

    public DeleteConceptSetEvent(Object source, Integer id) {
        super(source);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
