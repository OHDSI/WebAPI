package org.ohdsi.webapi.common;

import java.util.Arrays;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionService;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.authz.UserEntity;
import org.ohdsi.webapi.security.authz.UserRepository;
import org.ohdsi.webapi.util.NameUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import jakarta.annotation.Nullable;
import javax.cache.Cache;
import javax.cache.CacheManager;
import org.ohdsi.webapi.analysis.AnalysisConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.conceptset.ConceptSetService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.springframework.core.convert.ConversionService;

@Service
public class DesignImportService {
    private final AuthorizationService authorizationService;
    private final UserRepository userRepository;
    private final CohortDefinitionRepository cohortRepository;
    private final CohortDefinitionDetailsRepository detailsRepository;
    private final ConversionService conversionService;
    private final ConceptSetService conceptSetService;
    private final CohortDefinitionService cohortDefinitionService;
    private final CacheManager cacheManager;

    public DesignImportService(AuthorizationService authorizationService, 
                               UserRepository userRepository, CohortDefinitionRepository cohortRepository, 
                               CohortDefinitionDetailsRepository detailsRepository, ConceptSetService conceptSetService, 
                               @Qualifier("conversionService") ConversionService conversionService, CohortDefinitionService cohortDefinitionService,
                               @Nullable CacheManager cacheManager) {
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
        this.cohortRepository = cohortRepository;
        this.detailsRepository = detailsRepository;
        this.conceptSetService = conceptSetService;
        this.conversionService = conversionService;
        this.cohortDefinitionService = cohortDefinitionService;
        this.cacheManager = cacheManager;
    }
    
    public ConceptSetDTO persistConceptSet(final AnalysisConceptSet analysisConceptSet) {
        ConceptSetDTO cs = conversionService.convert(analysisConceptSet, ConceptSetDTO.class);
        cs.setName(NameUtils.getNameWithSuffix(cs.getName(), this::getCsNamesLike));
        cs = conceptSetService.createConceptSet(cs);
        final Integer conceptSetId = cs.getId();
        List<ConceptSetItem> csi = Arrays.stream(analysisConceptSet.expression.items).map(i -> conversionService.convert(i, ConceptSetItem.class)).collect(Collectors.toList());
        csi.forEach(n -> n.setConceptSetId(conceptSetId));
        conceptSetService.saveConceptSetItems(cs.getId(), csi.stream().toArray(ConceptSetItem[]::new));
        return cs;
    }

    public CohortDefinitionEntity persistCohortOrGetExisting(final CohortDefinitionEntity cohort) {
        return this.persistCohortOrGetExisting(cohort, false);
    }
    
    public CohortDefinitionEntity persistCohortOrGetExisting(final CohortDefinitionEntity cohort, final Boolean includeCohortNameInComparison) {
        final CohortDefinitionDetailsEntity details = cohort.getDetails();
        Optional<CohortDefinitionEntity> findCohortResult = includeCohortNameInComparison ? this.findCohortByExpressionHashcodeAndName(details, cohort.getName()) : this.findCohortByExpressionHashcode(details);
        return findCohortResult.orElseGet(() -> {
            final UserEntity user = userRepository.findById(authorizationService.getAuthenticatedPrincipal().getUserId()).orElseThrow();
            cohort.setId(null);
            cohort.setCreatedBy(user);
            cohort.setCreatedDate(new Date());
            cohort.setDetails(details);
            details.setCohortDefinition(cohort);            
            cohort.setName(NameUtils.getNameWithSuffix(cohort.getName(), this::getCdNamesLike));
            final CohortDefinitionEntity savedCohort = cohortRepository.save(cohort);
            detailsRepository.save(details);

            // if this is new, we will need to decache the cohort definition list
            if (this.cacheManager != null) {
              Cache cohortDefCache = cacheManager.getCache(CohortDefinitionService.CachingSetup.COHORT_DEFINITION_LIST_CACHE);
              if (cohortDefCache != null) {
                cohortDefCache.clear(); // wipes all entries in cohort definition list cache cache
              }
            }
            // permission caching is handled via the EntityInsertEventListener and EntityPermissionSchema.onInsert

            return savedCohort;
        });
    }

    private List<String> getCsNamesLike(String name) {
        return conceptSetService.getNamesLike(name);
    }

    private List<String> getCdNamesLike(String name) {
        return cohortDefinitionService.getNamesLike(name);
    }

    private Optional<CohortDefinitionEntity> findCohortByExpressionHashcode(final CohortDefinitionDetailsEntity details) {
        return this.findCohortByExpressionHashcodeAndPredicate(details, (c -> true));
    }
    
    private Optional<CohortDefinitionEntity> findCohortByExpressionHashcodeAndName(final CohortDefinitionDetailsEntity details, final String cohortName) {
        return this.findCohortByExpressionHashcodeAndPredicate(details, c -> Objects.equals(c.getName(), cohortName));
    }
    
    private Optional<CohortDefinitionEntity> findCohortByExpressionHashcodeAndPredicate(final CohortDefinitionDetailsEntity details, final Predicate<CohortDefinitionEntity> c) {
        List<CohortDefinitionDetailsEntity> detailsFromDb = detailsRepository.findByHashCode(details.calculateHashCode());
        return detailsFromDb
                .stream()
                .filter(v -> Objects.equals(v.getStandardizedExpression(), details.getStandardizedExpression()))
                .map(CohortDefinitionDetailsEntity::getCohortDefinition)
                .filter(c)
                .findFirst();
    }
}