package org.ohdsi.webapi.db.migartion;

import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;
import org.ohdsi.webapi.configuration.flyway.ApplicationContextAwareSpringMigration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class V2_8_0_20190520171430__cohortExpressionHashCode implements ApplicationContextAwareSpringMigration {

    private CohortDefinitionDetailsRepository detailsRepository;

    @Autowired
    public V2_8_0_20190520171430__cohortExpressionHashCode(CohortDefinitionDetailsRepository detailsRepository){
        this.detailsRepository = detailsRepository;
    }

    @Override
    public void migrate() throws Exception {
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
