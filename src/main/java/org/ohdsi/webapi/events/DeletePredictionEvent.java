package org.ohdsi.webapi.events;

public class DeletePredictionEvent extends DeleteEntityEvent {

    public DeletePredictionEvent(Object source, Integer id) {
        super(source, id);
    }
}