package org.ohdsi.webapi.events;

public class DeleteSourceEvent extends DeleteEntityEvent {

    public DeleteSourceEvent(Object source, Integer id) {
        super(source, id);
    }
}
