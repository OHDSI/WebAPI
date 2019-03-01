package org.ohdsi.webapi.common;

import java.util.Arrays;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.Security;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DesignImportService {

    private final Security security;
    private final UserRepository userRepository;
    private final CohortDefinitionRepository cohortRepository;
    private final CohortDefinitionDetailsRepository detailsRepository;

    public DesignImportService(Security security, UserRepository userRepository, CohortDefinitionRepository cohortRepository, CohortDefinitionDetailsRepository detailsRepository) {

        this.security = security;
        this.userRepository = userRepository;
        this.cohortRepository = cohortRepository;
        this.detailsRepository = detailsRepository;
    }

    public CohortDefinition persistCohortOrGetExisting(final CohortDefinition cohort) {
        return this.persistCohortOrGetExisting(cohort, false);
    }
    
    public CohortDefinition persistCohortOrGetExisting(final CohortDefinition cohort, final Boolean includeCohortNameInComparison) {
        final CohortDefinitionDetails details = cohort.getDetails();
        Optional<CohortDefinition> findCohortResult = includeCohortNameInComparison ? this.findCohortByExpressionHashcodeAndName(details, cohort.getName()) : this.findCohortByExpressionHashcode(details);
        return findCohortResult.orElseGet(() -> {
            final UserEntity user = userRepository.findByLogin(security.getSubject());
            cohort.setCreatedBy(user);
            cohort.setCreatedDate(new Date());
            cohort.setDetails(null);
            final CohortDefinition savedCohort = cohortRepository.save(cohort);
            details.setCohortDefinition(savedCohort);
            savedCohort.setDetails(detailsRepository.save(details));
            return savedCohort;
        });
    }

    private Optional<CohortDefinition> findCohortByExpressionHashcode(final CohortDefinitionDetails details) {
        List<CohortDefinitionDetails> detailsFromDb = detailsRepository.findByHashCode(details.calculateHashCode());
        return detailsFromDb
                .stream()
                .filter(v -> Objects.equals(v.getStandardizedExpression(), details.getStandardizedExpression()))
                .findFirst()
                .map(CohortDefinitionDetails::getCohortDefinition);
    }
    
    private Optional<CohortDefinition> findCohortByExpressionHashcodeAndName(final CohortDefinitionDetails details, final String cohortName) {
        List<CohortDefinitionDetails> detailsFromDb = detailsRepository.findByHashCode(details.calculateHashCode());
        return detailsFromDb
                .stream()
                .filter(v -> Objects.equals(v.getStandardizedExpression(), details.getStandardizedExpression()))
                .map(CohortDefinitionDetails::getCohortDefinition)
                .filter(c -> c.getName().equals(cohortName))
                .findFirst();
    }
}