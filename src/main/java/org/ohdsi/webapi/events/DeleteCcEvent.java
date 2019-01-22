package org.ohdsi.webapi.events;

public class DeleteCcEvent extends DeleteEntityEvent {

    public DeleteCcEvent(Object source, Integer id) {
        super(source, id);
    }
}
