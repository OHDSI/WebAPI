package org.ohdsi.webapi.db.migartion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odysseusinc.arachne.commons.config.flyway.ApplicationContextAwareSpringMigration;
import java.util.List;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class V2_6_0_20180807192421__cohortDetailsHashcodes implements ApplicationContextAwareSpringMigration {

    private CohortDefinitionDetailsRepository detailsRepository;

    @Autowired
    public V2_6_0_20180807192421__cohortDetailsHashcodes(final CohortDefinitionDetailsRepository detailsRepository) {
        this.detailsRepository = detailsRepository;
    }

    @Override
    public void migrate() throws JsonProcessingException {

        final List<CohortDefinitionDetails> allDetails = detailsRepository.findAll();
        for (CohortDefinitionDetails details: allDetails) {
            details.updateHashCode();
            detailsRepository.save(details);
        }
    }
}