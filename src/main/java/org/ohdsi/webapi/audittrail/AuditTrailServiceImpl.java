package org.ohdsi.webapi.audittrail;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.cohortsample.dto.CohortSampleDTO;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.util.Collection;
import java.util.Iterator;

@Component
class AuditTrailServiceImpl implements AuditTrailService {
    private final Logger AUDIT_LOGGER = LoggerFactory.getLogger("audit");

    private static final String USER_LOGIN_SUCCESS_TEMPLATE = "User successfully logged in: %s";
    private static final String USER_LOGIN_FAILURE_TEMPLATE = "User login failed: %s";
    private static final String USER_LOGOUT_SUCCESS_TEMPLATE = "User successfully logged out: %s";
    private static final String USER_LOGOUT_FAILURE_TEMPLATE = "User logout failed: %s";

    private static final String JOB_STARTED_TEMPLATE = "%s - Job %s execution started: %s";
    private static final String JOB_COMPLETED_TEMPLATE = "%s - Job %s execution completed successfully: %s";
    private static final String JOB_FAILED_TEMPLATE = "%s - Job %s execution failed: %s";

    private static final String PATIENT_IDS_TEMPLATE = " Patient IDs (%s): ";
    private static final String PATIENT_ID_TEMPLATE = " Patient ID: %s";

    private static final String LIST_OF = "list of %s objects %s";

    private static final String FIELD_DIVIDER = " - ";
    private static final String ANONYMOUS = "anonymous";
    private static final String NO_LOCATION = "NO_LOCATION";

    @Autowired
    private UserRepository userRepository;

    @Override
    public void logSuccessfulLogin(final String login) {
        AUDIT_LOGGER.info(String.format(USER_LOGIN_SUCCESS_TEMPLATE, getUserIdByLogin(login)));
    }

    @Override
    public void logFailedLogin(final String login) {
        AUDIT_LOGGER.info(String.format(USER_LOGIN_FAILURE_TEMPLATE, getUserIdByLogin(login)));
    }

    @Override
    public void logSuccessfulLogout(final String login) {
        AUDIT_LOGGER.info(String.format(USER_LOGOUT_SUCCESS_TEMPLATE, getUserIdByLogin(login)));
    }

    @Override
    public void logFailedLogout(final String login) {
        AUDIT_LOGGER.info(String.format(USER_LOGOUT_FAILURE_TEMPLATE, getUserIdByLogin(login)));
    }

    @Override
    public void logRestCall(final AuditTrailEntry entry) {
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

    @Override
    public void logJobStart(final JobExecution jobExecution) {
        logJob(jobExecution, JOB_STARTED_TEMPLATE);
    }

    @Override
    public void logJobCompleted(final JobExecution jobExecution) {
        logJob(jobExecution, JOB_COMPLETED_TEMPLATE);
    }

    @Override
    public void logJobFailed(JobExecution jobExecution) {
        logJob(jobExecution, JOB_FAILED_TEMPLATE);
    }

    private String getCurrentUserField(final AuditTrailEntry entry) {
        return (entry.getCurrentUser() != null ? String.valueOf(entry.getCurrentUser().getId()) : ANONYMOUS);
    }

    private String getActionLocationField(final AuditTrailEntry entry) {
        return entry.getActionLocation() != null ? entry.getActionLocation() : NO_LOCATION;
    }

    private String getRestCallField(final AuditTrailEntry entry) {
        final StringBuilder sb = new StringBuilder();
        sb.append(entry.getRequestMethod())
                .append(" ")
                .append(entry.getRequestUri());
        if (!StringUtils.isBlank(entry.getQueryString())) {
            sb.append("?").append(entry.getQueryString());
        }
        return  sb.toString();
    }

    private String getAdditionalInfo(final AuditTrailEntry entry) {
        final StringBuilder additionalInfo = new StringBuilder();

        final String returnedObjectFields = getReturnedObjectFields(entry.getReturnedObject());
        if (!StringUtils.isBlank(returnedObjectFields)) {
            additionalInfo.append(returnedObjectFields);
        }

        if (entry.getReturnedObject() instanceof CohortSampleDTO) {
            final CohortSampleDTO sampleDto = (CohortSampleDTO) entry.getReturnedObject();
            if (sampleDto.getElements().isEmpty()) {
                additionalInfo.append(String.format(PATIENT_IDS_TEMPLATE, 0)).append("none");
            } else {
                additionalInfo.append(String.format(PATIENT_IDS_TEMPLATE, sampleDto.getElements().size()));
                sampleDto.getElements().forEach((e) -> {
                    additionalInfo.append(e.getPersonId()).append(" ");
                });
            }
        }
        return additionalInfo.toString();
    }

    private String getReturnedObjectFields(final Object returnedObject) {
        if (returnedObject instanceof Collection) {
            final Collection<?> c = (Collection<?>) returnedObject;
            if (!c.isEmpty()) {
                final String fields = collectClassFieldNames(c.iterator().next().getClass());
                return fields != null ? String.format(LIST_OF, c.size(), fields) : null;
            }
            return null;
        } else {
            return collectClassFieldNames(returnedObject.getClass());
        }
    }

    private String collectClassFieldNames(final Class<?> klass) {
        if (!klass.getPackage().getName().startsWith("org.ohdsi.")) {
            return null;
        }

        // collect only first level field names
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        ReflectionUtils.doWithFields(klass, field -> {
            sb.append(field.getName()).append("::").append(field.getType().getSimpleName()).append(",");
        });
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
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

    private void logJob(final JobExecution jobExecution, final String template) {
        final JobParameters jobParameters = jobExecution.getJobParameters();
        final String author = jobParameters.getString(Constants.Params.JOB_AUTHOR);
        if (author.equals("anonymous")) { // system jobs
            return;
        }

        final String jobName = jobParameters.getString(Constants.Params.JOB_NAME);
        final Long userId = getUserIdByLogin(author);
        AUDIT_LOGGER.info(String.format(template, userId, jobExecution.getJobId(), jobName));
    }
}
