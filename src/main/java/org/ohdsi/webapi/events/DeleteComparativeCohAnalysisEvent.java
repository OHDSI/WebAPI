package org.ohdsi.webapi.events;

import org.springframework.context.ApplicationEvent;

public class DeleteComparativeCohAnalysisEvent extends ApplicationEvent {
    private final Integer id;

    public DeleteComparativeCohAnalysisEvent(Object source, Integer id) {
        super(source);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
