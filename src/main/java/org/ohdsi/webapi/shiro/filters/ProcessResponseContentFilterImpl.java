package org.ohdsi.webapi.shiro.filters;

import static org.ohdsi.webapi.events.EntityName.*;
import static org.ohdsi.webapi.util.ParserUtils.parseJsonField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.cohortcharacterization.CcService;
import org.ohdsi.webapi.estimation.EstimationService;
import org.ohdsi.webapi.events.DeleteEntityEvent;
import org.ohdsi.webapi.events.EntityName;
import org.ohdsi.webapi.pathway.PathwayService;
import org.ohdsi.webapi.service.IRAnalysisService;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.exception.PermissionAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.InternalServerErrorException;
import java.util.HashMap;
import java.util.Map;

public class ProcessResponseContentFilterImpl extends ProcessResponseContentFilter {
    private static final boolean RAISE_ERROR_IF_PERMISSION_EXISTS = true;
    private static final boolean IGNORE_ERROR_IF_PERMISSION_EXISTS = false;
    
    private PermissionManager authorizer;
    private ApplicationEventPublisher eventPublisher;
    private Map<String, String> template;
    private EntityName entityName;
    private String httpMethod;
    private IRAnalysisService irAnalysisService;
    private PathwayService pathwayService;
    private CcService ccService;
    private EstimationService estimationService;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private String identityField;
    private final List<EntityName> nestedEntities = Arrays.asList(CONCEPT_SET, COHORT);
    private final List<EntityName> parentEntities = Arrays.asList(COHORT_CHARACTERIZATION, PATHWAY_ANALYSIS, INCIDENCE_RATE, COMPARATIVE_COHORT_ANALYSIS, PATIENT_LEVEL_PREDICTION);
    public ProcessResponseContentFilterImpl(Map<String, String> template, EntityName entityName, PermissionManager authorizer,
                                            ApplicationEventPublisher eventPublisher, String httpMethod,
                                            IRAnalysisService irAnalysisService, PathwayService pathwayService, CcService ccService,
                                            EstimationService estimationService) {
        this(template, entityName, authorizer, eventPublisher, httpMethod, "id", irAnalysisService, pathwayService, ccService,
                estimationService);
    }

    public ProcessResponseContentFilterImpl(Map<String, String> template, EntityName entityName, PermissionManager authorizer, 
                                            ApplicationEventPublisher eventPublisher, String httpMethod, String identityField,
                                            IRAnalysisService irAnalysisService, PathwayService pathwayService, CcService ccService,
                                            EstimationService estimationService) {
        this.template = new HashMap<>(template);
        this.entityName = entityName;
        this.authorizer = authorizer;
        this.eventPublisher = eventPublisher;
        this.httpMethod = httpMethod;
        this.identityField = identityField;
        this.irAnalysisService = irAnalysisService;
        this.pathwayService = pathwayService;
        this.ccService = ccService;
        this.estimationService = estimationService;
    }

    @Override
    public void doProcessResponseContent(String content) throws Exception {

        Optional<EntityName> parentEntityName = getParentEntity(content);
        if (parentEntityName.isPresent() && nestedEntities.contains(entityName)) {
            addPermissionsToNestedEntities(content, parentEntityName.get());
        } else {
            String id = parseJsonField(content, identityField);
            addPermissions(id, RAISE_ERROR_IF_PERMISSION_EXISTS);
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
            case COHORT_CHARACTERIZATION: ids = ccService.getNestedEntitiesIds(content, entityName); break;
            case PATHWAY_ANALYSIS: ids = pathwayService.getNestedEntitiesIds(content, entityName); break;
            case INCIDENCE_RATE: ids = irAnalysisService.getNestedEntitiesIds(content, entityName); break;
            case PATIENT_LEVEL_PREDICTION:
            case COMPARATIVE_COHORT_ANALYSIS: ids = estimationService.getNestedEntitiesIds(content, entityName); break;
        }        
        ids.forEach(id -> addPermissions(id, IGNORE_ERROR_IF_PERMISSION_EXISTS));
    }

    private void addPermissions(String id, boolean raiseErrorIfExists) {

        try {
            authorizer.addPermissionsFromTemplate(template, id);
        } catch (PermissionAlreadyExistsException ex) {
            if (raiseErrorIfExists) {
                processAddPermissionError(id, ex);
            }
        } catch (Exception ex) {
            processAddPermissionError(id, ex);
        }
    }

    private void processAddPermissionError(String id, Exception ex) {
        eventPublisher.publishEvent(new DeleteEntityEvent(this, Integer.parseInt(id), entityName));
        log.error("Failed to add permissions to " + entityName.getName() + " with id = " + id, ex);
        throw new InternalServerErrorException();
    }

    @Override
    protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
        
        return httpMethod.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
    }
}
