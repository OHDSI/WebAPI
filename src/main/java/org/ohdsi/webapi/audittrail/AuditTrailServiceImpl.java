package org.ohdsi.webapi.audittrail;

import org.ohdsi.webapi.cohortsample.dto.CohortSampleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class AuditTrailServiceImpl implements AuditTrailService {
    private final Logger AUDIT_LOGGER = LoggerFactory.getLogger("audit");

    @Override
    public void log(final AuditTrailEntry entry) {

        final StringBuilder additionalInfo = new StringBuilder();
        if (entry.getReturnedObject() instanceof CohortSampleDTO) {
            final CohortSampleDTO sampleDto = (CohortSampleDTO) entry.getReturnedObject();
            additionalInfo.append("returned patients ids: ");
            sampleDto.getElements().forEach((e) -> {
                additionalInfo.append(e.getPersonId()).append(" ");
            });
        }

        AUDIT_LOGGER.info((entry.getCurrentUser() != null ? entry.getCurrentUser().getId() : "anonymous") + " - " +
                (entry.getActionLocation() != null ? entry.getActionLocation() : "NO-LOCATION") + " - " +
                entry.getRequestUri() + " - " +
                additionalInfo);
    }
}
