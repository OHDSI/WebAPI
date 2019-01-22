package org.ohdsi.webapi.events;

public class DeleteFeatureAnalysisEvent extends DeleteEntityEvent {

    public DeleteFeatureAnalysisEvent(Object source, Integer id) {
        super(source, id);
    }
}
