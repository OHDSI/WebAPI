package org.ohdsi.webapi.db.migartion;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.ohdsi.webapi.arachne.commons.config.flyway.ApplicationContextAwareSpringMigration;
import java.util.List;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;

/**
 * Flyway Java migration to update hash codes for cohort definition details.
 *
 * Note: NOT a @Component - Flyway discovers this via classpath scanning.
 * Dependencies are retrieved from ApplicationContext to avoid circular dependency issues.
 */
public class V2_6_0_20180807192421__cohortDetailsHashcodes extends ApplicationContextAwareSpringMigration {

    @Override
    public void migrate() throws JsonProcessingException {
        // Get repository from Spring ApplicationContext (set by Flyway before migration runs)
        CohortDefinitionDetailsRepository detailsRepository =
            applicationContext.getBean(CohortDefinitionDetailsRepository.class);

        final List<CohortDefinitionDetails> allDetails = detailsRepository.findAll();
        for (CohortDefinitionDetails details: allDetails) {
            details.updateHashCode();
            detailsRepository.save(details);
        }
    }
}