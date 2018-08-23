package org.ohdsi.webapi.db.migartion;

import com.odysseusinc.arachne.commons.config.flyway.ApplicationContextAwareSpringMigration;
import java.util.List;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class V2_5_0_20180807192421__cohortDetailsHashcodes implements ApplicationContextAwareSpringMigration {

    private CohortDefinitionDetailsRepository detailsRepository;
    
    @Autowired
    public V2_5_0_20180807192421__cohortDetailsHashcodes(final CohortDefinitionDetailsRepository detailsRepository) {
        this.detailsRepository = detailsRepository;
    }

    @Override
    public void migrate() {

        final List<CohortDefinitionDetails> allDetails = detailsRepository.findAll();
        allDetails
                .stream()
                .peek(v -> v.setHashCode(v.getExpression().hashCode()))
                .forEach(detailsRepository::save);
    }
}