package org.ohdsi.webapi.audittrail;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.cohortsample.dto.CohortSampleDTO;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class AuditTrailServiceImpl implements AuditTrailService {
    private final Logger AUDIT_LOGGER = LoggerFactory.getLogger("audit");

    private static final String FIELD_DIVIDER = " - ";
    private static final String ANONYMOUS = "anonymous";
    private static final String NO_LOCATION = "NO_LOCATION";

    @Autowired
    private UserRepository userRepository;

    @Override
    public void logSuccessfulLogin(final String login) {
        AUDIT_LOGGER.info("User successfully logged in: " + getUserIdByLogin(login));
    }

    @Override
    public void logFailedLogin(final String login) {
        AUDIT_LOGGER.info("User login failed: " + getUserIdByLogin(login));
    }

    @Override
    public void logSuccessfulLogout(final String login) {
        AUDIT_LOGGER.info("User successfully logged out: " + getUserIdByLogin(login));
    }

    @Override
    public void logFailedLogout(final String login) {
        AUDIT_LOGGER.info("User logout failed: " + getUserIdByLogin(login));
    }

    @Override
    public void log(final AuditTrailEntry entry) {
        final StringBuilder logEntry = new StringBuilder();

        logEntry.append(getCurrentUserField(entry))
                .append(FIELD_DIVIDER)
                .append(getActionLocationField(entry))
                .append(FIELD_DIVIDER)
                .append(getRestCallField(entry));

        final String additionalInfo = getAdditionalInfo(entry);
        if (!StringUtils.isBlank(additionalInfo)) {
            logEntry.append(FIELD_DIVIDER).append(additionalInfo);
        }

        AUDIT_LOGGER.info(logEntry.toString());
    }

    private String getCurrentUserField(final AuditTrailEntry entry) {
        return (entry.getCurrentUser() != null ? String.valueOf(entry.getCurrentUser().getId()) : ANONYMOUS);
    }

    private String getActionLocationField(final AuditTrailEntry entry) {
        return entry.getActionLocation() != null ? entry.getActionLocation() : NO_LOCATION;
    }

    private String getRestCallField(final AuditTrailEntry entry) {
        return entry.getRequestMethod() + " " + entry.getRequestUri();
    }

    private String getAdditionalInfo(final AuditTrailEntry entry) {
        final StringBuilder additionalInfo = new StringBuilder();
        if (entry.getReturnedObject() instanceof CohortSampleDTO) {
            final CohortSampleDTO sampleDto = (CohortSampleDTO) entry.getReturnedObject();
            additionalInfo.append("patients IDs: ");
            if (sampleDto.getElements().isEmpty()) {
                additionalInfo.append("none");
            } else {
                sampleDto.getElements().forEach((e) -> {
                    additionalInfo.append(e.getPersonId()).append(" ");
                });
            }
        }
        return additionalInfo.toString();
    }

    private Long getUserIdByLogin(final String login) {
        try {
            final UserEntity user = userRepository.findByLogin(login);
            if (user != null) {
                return user.getId();
            } else {
                return null;
            }
        } catch(final Exception e) {
            return null;
        }
    }
}
