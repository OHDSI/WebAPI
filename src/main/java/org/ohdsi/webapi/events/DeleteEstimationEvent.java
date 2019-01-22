package org.ohdsi.webapi.events;

public class DeleteEstimationEvent extends DeleteEntityEvent {

    public DeleteEstimationEvent(Object source, Integer id) {
        super(source, id);
    }
}
