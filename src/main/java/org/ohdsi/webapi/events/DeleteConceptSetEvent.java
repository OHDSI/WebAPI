package org.ohdsi.webapi.events;

public class DeleteConceptSetEvent extends DeleteEntityEvent {

    public DeleteConceptSetEvent(Object source, Integer id) {
        super(source, id);
    }
}