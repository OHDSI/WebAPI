package org.ohdsi.webapi.events;

import org.springframework.context.ApplicationEvent;

public class DeleteCcEvent extends ApplicationEvent {
    private final Integer id;

    public DeleteCcEvent(Object source, Integer id) {
        super(source);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
