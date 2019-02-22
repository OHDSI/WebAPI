package org.ohdsi.webapi.analysis.importer;

import java.util.Date;
import org.ohdsi.webapi.analysis.AnalysisCohortDefinition;
import org.ohdsi.webapi.analysis.converter.AnalysisCohortDefinitionToCohortDefinitionConverter;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.Security;
import org.springframework.stereotype.Service;

@Service
public class AnalysisCohortDefinitionImportService {
    private final Security security;
    private final UserRepository userRepository;
    private final CohortDefinitionRepository cohortRepository;
    private final AnalysisCohortDefinitionToCohortDefinitionConverter conversionService = new AnalysisCohortDefinitionToCohortDefinitionConverter();
    
    public AnalysisCohortDefinitionImportService(Security security, UserRepository userRepository, CohortDefinitionRepository cohortRepository) {
        this.security = security;
        this.userRepository = userRepository;
        this.cohortRepository = cohortRepository;
    }
    
    public CohortDefinition persistCohort(final AnalysisCohortDefinition analysisCohortDefinition) {
        CohortDefinition cohort = conversionService.convert(analysisCohortDefinition);
        final UserEntity user = userRepository.findByLogin(security.getSubject());
        cohort.setCreatedBy(user);
        cohort.setCreatedDate(new Date());
        return cohortRepository.save(cohort);
    }
}
