package org.ohdsi.webapi.events;

public class DeletePathwayAnalysisEvent extends DeleteEntityEvent {

    public DeletePathwayAnalysisEvent(Object source, Integer id) {
        super(source, id);
    }
}
