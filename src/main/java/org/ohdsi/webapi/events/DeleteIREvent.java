package org.ohdsi.webapi.events;

import org.springframework.context.ApplicationEvent;

public class DeleteIREvent extends ApplicationEvent {
    private final Integer id;

    public DeleteIREvent(Object source, Integer id) {
        super(source);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
