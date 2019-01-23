package org.ohdsi.webapi.shiro.management;

import org.ohdsi.webapi.events.EntityName;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.filters.ProcessResponseContentFilterImpl;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashMap;
import java.util.Map;

public class ResponseContentFilterBuilder {
    
    private PermissionManager permissionManager;
    private ApplicationEventPublisher eventPublisher;
    private Map<String, String> template;
    private EntityName entityName;
    private boolean shouldHttpMethodCheck;

    private ResponseContentFilterBuilder() {
    }

    public static ResponseContentFilterBuilder builder() {
        return new ResponseContentFilterBuilder();
    }
    
    public ResponseContentFilterBuilder withPermissionManager(PermissionManager permissionManager){
        this.permissionManager = permissionManager;
        return this;
    }

    public ResponseContentFilterBuilder withEventPublisher(ApplicationEventPublisher eventPublisher){
        this.eventPublisher = eventPublisher;
        return this;
    }

    public ResponseContentFilterBuilder withTemplate(Map<String, String> template) {
        this.template = new HashMap<>(template);
        return this;
    }

    public ResponseContentFilterBuilder withEntityName(EntityName entityName) {
        this.entityName = entityName;
        return this;
    }

    public ResponseContentFilterBuilder withHttpMethodCheck() {
        this.shouldHttpMethodCheck = true;
        return this;
    }

    public ProcessResponseContentFilterImpl build() {
        return new ProcessResponseContentFilterImpl(template, entityName, shouldHttpMethodCheck, permissionManager, eventPublisher);
    }
}
