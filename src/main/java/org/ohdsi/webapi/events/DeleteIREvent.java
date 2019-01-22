package org.ohdsi.webapi.events;

public class DeleteIREvent extends DeleteEntityEvent {

    public DeleteIREvent(Object source, Integer id) {
        super(source, id);
    }
}