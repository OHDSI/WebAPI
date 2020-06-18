package org.ohdsi.webapi.shiro.filters;

import static org.ohdsi.webapi.events.EntityName.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.events.DeleteEntityEvent;
import org.ohdsi.webapi.events.EntityName;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.InternalServerErrorException;
import java.util.HashMap;
import java.util.Map;

public class ProcessResponseContentFilterImpl extends ProcessResponseContentFilter {
    
    private PermissionManager authorizer;
    private ApplicationEventPublisher eventPublisher;
    private Map<String, String> template;
    private EntityName entityName;
    private String httpMethod;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private String identityField;
    private final List<EntityName> nestedEntities = Arrays.asList(CONCEPT_SET, COHORT);
    private final List<EntityName> parentEntities = Arrays.asList(COHORT_CHARACTERIZATION, PATHWAY_ANALYSIS, INCIDENCE_RATE, COMPARATIVE_COHORT_ANALYSIS, PATIENT_LEVEL_PREDICTION);
    public ProcessResponseContentFilterImpl(Map<String, String> template, EntityName entityName, PermissionManager authorizer,
                                            ApplicationEventPublisher eventPublisher, String httpMethod) {
        this(template, entityName, authorizer, eventPublisher, httpMethod, "id");
    }

    public ProcessResponseContentFilterImpl(Map<String, String> template, EntityName entityName, PermissionManager authorizer, 
                                            ApplicationEventPublisher eventPublisher, String httpMethod, String identityField) {
        this.template = new HashMap<>(template);
        this.entityName = entityName;
        this.authorizer = authorizer;
        this.eventPublisher = eventPublisher;
        this.httpMethod = httpMethod;
        this.identityField = identityField;
    }

    @Override
    public void doProcessResponseContent(String content) throws Exception {

        Optional<EntityName> parentEntityName = getParentEntity(content);
        if (parentEntityName.isPresent() && nestedEntities.contains(entityName)) {
            addPermissionsToNestedEntities(content, parentEntityName.get());
        } else {
            String id = parseJsonField(content, identityField);
            addPermissions(id);
        }        
    }
    
    private Optional<EntityName> getParentEntity(String content) throws IOException {

        String entityType = parseJsonField(content, "entityName");
        return parentEntities.stream()
                .filter(e -> e.toString().equalsIgnoreCase(entityType))
                .findFirst();
    }

    private void addPermissionsToNestedEntities(String content, EntityName parentEntityName) throws IOException {
        
        List<String> ids = new ArrayList<>();
        switch (parentEntityName) {
            case COHORT_CHARACTERIZATION: ids = getNestedIdsFromCC(content); break;
            case PATHWAY_ANALYSIS: ids = getNestedIdsFromPathway(content); break;
            case INCIDENCE_RATE: ids = getNestedIdsFromIr(content); break;
            case PATIENT_LEVEL_PREDICTION:
            case COMPARATIVE_COHORT_ANALYSIS: ids = getNestedIdsFromPlpPle(content); break;
        }        
        ids.forEach(this::addPermissions);
    }

    private List<String> getNestedIdsFromIr(String content) throws IOException {

        List<String> ids = new ArrayList<>();
        String expression = parseJsonField(content, "expression");
        if (hasJsonField(expression, "targetIds") && hasJsonField(expression, "outcomeIds") && entityName.equals(COHORT)) {
            ids.addAll(parseNestedJsonField(expression, "targetIds", null));
            ids.addAll(parseNestedJsonField(expression, "outcomeIds", null));
        }
        return ids;        
    }
    
    private List<String> getNestedIdsFromPathway(String content) throws IOException {

        List<String> ids = new ArrayList<>();
        if (hasJsonField(content, "targetCohorts") && hasJsonField(content, "eventCohorts") && entityName.equals(COHORT)) {
            ids.addAll(parseNestedJsonField(content, "targetCohorts", identityField));
            ids.addAll(parseNestedJsonField(content, "eventCohorts", identityField));
        }
        return ids;
    }
    
    private List<String> getNestedIdsFromCC(String content) throws IOException {

        List<String> ids = new ArrayList<>();
        if (hasJsonField(content, "cohorts") && entityName.equals(COHORT)) {
            ids = parseNestedJsonField(content, "cohorts", identityField);
        }
        return ids;
    }
    
    private List<String> getNestedIdsFromPlpPle(String content) throws IOException {

        List<String> ids = new ArrayList<>();
        String specification = parseJsonField(content, "specification");
        if (hasJsonField(specification, "conceptSets") && entityName.equals(CONCEPT_SET))  {
            ids = parseNestedJsonField(specification, "conceptSets", identityField);
        } else if (hasJsonField(specification, "cohortDefinitions") && entityName.equals(COHORT)) {
            ids = parseNestedJsonField(specification, "cohortDefinitions", identityField);
        }
        return ids;
    }

    private void addPermissions(String id) {

        try {
            authorizer.addPermissionsFromTemplate(template, id);
        } catch (Exception ex) {
            eventPublisher.publishEvent(new DeleteEntityEvent(this, Integer.parseInt(id), entityName));
            log.error("Failed to add permissions to " + entityName.getName() + " with id = " + id, ex);
            throw new InternalServerErrorException();
        }
    }

    @Override
    protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
        
        return httpMethod.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
    }
}
