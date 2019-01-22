package org.ohdsi.webapi.events;

public class DeleteCohortEvent extends DeleteEntityEvent {

    public DeleteCohortEvent(Object source, Integer id) {
        super(source, id);
    }
}
