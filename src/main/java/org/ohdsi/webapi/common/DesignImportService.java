package org.ohdsi.webapi.common;

import java.util.Arrays;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.util.NameUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.ohdsi.webapi.analysis.AnalysisConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.springframework.core.convert.ConversionService;

@Service
public class DesignImportService {
    private final Security security;
    private final UserRepository userRepository;
    private final CohortDefinitionRepository cohortRepository;
    private final CohortDefinitionDetailsRepository detailsRepository;
    private final ConversionService conversionService;
    private final ConceptSetService conceptSetService;
    private final CohortDefinitionService cohortDefinitionService;

    public DesignImportService(Security security, UserRepository userRepository, CohortDefinitionRepository cohortRepository, 
                               CohortDefinitionDetailsRepository detailsRepository, ConceptSetService conceptSetService, 
                               ConversionService conversionService, CohortDefinitionService cohortDefinitionService) {
        this.security = security;
        this.userRepository = userRepository;
        this.cohortRepository = cohortRepository;
        this.detailsRepository = detailsRepository;
        this.conceptSetService = conceptSetService;
        this.conversionService = conversionService;
        this.cohortDefinitionService = cohortDefinitionService;
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

    public CohortDefinition persistCohortOrGetExisting(final CohortDefinition cohort) {
        return this.persistCohortOrGetExisting(cohort, false);
    }
    
    public CohortDefinition persistCohortOrGetExisting(final CohortDefinition cohort, final Boolean includeCohortNameInComparison) {
        final CohortDefinitionDetails details = cohort.getDetails();
        Optional<CohortDefinition> findCohortResult = includeCohortNameInComparison ? this.findCohortByExpressionHashcodeAndName(details, cohort.getName()) : this.findCohortByExpressionHashcode(details);
        return findCohortResult.orElseGet(() -> {
            final UserEntity user = userRepository.findByLogin(security.getSubject());
            cohort.setId(null);
            cohort.setCreatedBy(user);
            cohort.setCreatedDate(new Date());
            cohort.setDetails(details);
            details.setCohortDefinition(cohort);            
            cohort.setName(NameUtils.getNameWithSuffix(cohort.getName(), this::getCdNamesLike));
            final CohortDefinition savedCohort = cohortRepository.save(cohort);
            detailsRepository.save(details);
            return savedCohort;
        });
    }

    private List<String> getCsNamesLike(String name) {
        return conceptSetService.getNamesLike(name);
    }

    private List<String> getCdNamesLike(String name) {
        return cohortDefinitionService.getNamesLike(name);
    }

    private Optional<CohortDefinition> findCohortByExpressionHashcode(final CohortDefinitionDetails details) {
        return this.findCohortByExpressionHashcodeAndPredicate(details, (c -> true));
    }
    
    private Optional<CohortDefinition> findCohortByExpressionHashcodeAndName(final CohortDefinitionDetails details, final String cohortName) {
        return this.findCohortByExpressionHashcodeAndPredicate(details, c -> Objects.equals(c.getName(), cohortName));
    }
    
    private Optional<CohortDefinition> findCohortByExpressionHashcodeAndPredicate(final CohortDefinitionDetails details, final Predicate<CohortDefinition> c) {
        List<CohortDefinitionDetails> detailsFromDb = detailsRepository.findByHashCode(details.calculateHashCode());
        return detailsFromDb
                .stream()
                .filter(v -> Objects.equals(v.getStandardizedExpression(), details.getStandardizedExpression()))
                .map(CohortDefinitionDetails::getCohortDefinition)
                .filter(c)
                .findFirst();
    }
}