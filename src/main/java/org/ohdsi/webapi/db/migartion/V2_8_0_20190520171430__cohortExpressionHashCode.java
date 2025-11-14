package org.ohdsi.webapi.db.migartion;

import org.ohdsi.webapi.arachne.commons.config.flyway.ApplicationContextAwareSpringMigration;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;

import java.util.List;

/**
 * Flyway Java migration to update cohort expression hash codes.
 *
 * Note: NOT a @Component - Flyway discovers this via classpath scanning.
 * Dependencies are retrieved from ApplicationContext to avoid circular dependency issues.
 */
public class V2_8_0_20190520171430__cohortExpressionHashCode extends ApplicationContextAwareSpringMigration {

    @Override
    public void migrate() throws Exception {
        // Get repository from Spring ApplicationContext (set by Flyway before migration runs)
        CohortDefinitionDetailsRepository detailsRepository =
            applicationContext.getBean(CohortDefinitionDetailsRepository.class);

        List<CohortDefinitionDetails> allDetails = detailsRepository.findAll();
        for (CohortDefinitionDetails details: allDetails) {
            //after deserialization the field "cdmVersionRange" is added and default value for it is set
            CohortExpression expression = Utils.deserialize(details.getExpression(), CohortExpression.class);
            details.setExpression(Utils.serialize(expression));
            details.updateHashCode();
            detailsRepository.save(details);
        }
    }
}
