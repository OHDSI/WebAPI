package org.ohdsi.webapi.shiro.filters;

import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.events.EntityName;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.ohdsi.webapi.events.DeleteEventMessageFactory.getDeletionEvent;

public class ProcessResponseContentFilterImpl extends ProcessResponseContentFilter {
    
    private PermissionManager authorizer;
    private ApplicationEventPublisher eventPublisher;
    private Map<String, String> template;
    private EntityName entityName;
    private String httpMethod;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public ProcessResponseContentFilterImpl(Map<String, String> template, EntityName entityName, PermissionManager authorizer, 
                                            ApplicationEventPublisher eventPublisher, String httpMethod) {
        this.template = new HashMap<>(template);
        this.entityName = entityName;
        this.authorizer = authorizer;
        this.eventPublisher = eventPublisher;
        this.httpMethod = httpMethod;
    }

    @Override
    public void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, "id");
        try {
            RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
            authorizer.addPermissionsFromTemplate(currentUserPersonalRole, template, id);
        } catch (Exception ex) {
            eventPublisher.publishEvent(getDeletionEvent(this, entityName, Integer.parseInt(id)));
            log.error("Failed to add permissions to " + entityName.getName() + " with id = " + id, ex);
        }
    }

    @Override
    protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
        
        return httpMethod.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
    }
}
