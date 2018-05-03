package org.ohdsi.webapi.source;


import org.springframework.context.ApplicationEvent;

public class EncryptorPasswordUpdatedEvent extends ApplicationEvent {

    public EncryptorPasswordUpdatedEvent(Object source) {
        super(source);
    }
}
