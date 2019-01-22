package org.ohdsi.webapi.events;

public class DeletePLPEvent extends DeleteEntityEvent {

    public DeletePLPEvent(Object source, Integer id) {
        super(source, id);
    }
}
