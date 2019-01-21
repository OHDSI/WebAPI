package org.ohdsi.webapi.events;

import org.springframework.context.ApplicationEvent;

public class DeleteEstimationEvent extends ApplicationEvent {
    private final Integer id;

    public DeleteEstimationEvent(Object source, Integer id) {
        super(source);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
