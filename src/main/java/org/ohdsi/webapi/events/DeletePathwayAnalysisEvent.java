package org.ohdsi.webapi.events;

import org.springframework.context.ApplicationEvent;

public class DeletePathwayAnalysisEvent extends ApplicationEvent {
    private final Integer id;

    public DeletePathwayAnalysisEvent(Object source, Integer id) {
        super(source);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
