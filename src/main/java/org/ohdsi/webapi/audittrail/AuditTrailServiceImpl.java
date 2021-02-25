package org.ohdsi.webapi.audittrail;

import org.apache.commons.lang3.StringUtils;
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

import java.util.Iterator;

@Component
class AuditTrailServiceImpl implements AuditTrailService {
    private final Logger AUDIT_LOGGER = LoggerFactory.getLogger("audit");

    private static final String USER_LOGIN_SUCCESS_TEMPLATE = "User successfully logged in: %s";
    private static final String USER_LOGIN_FAILURE_TEMPLATE = "User login failed: %s";
    private static final String USER_LOGOUT_SUCCESS_TEMPLATE = "User successfully logged out: %s";
    private static final String USER_LOGOUT_FAILURE_TEMPLATE = "User logout failed: %s";

    private static final String JOB_STARTED_TEMPLATE = "%s - Job execution started: %s";
    private static final String JOB_COMPLETED_TEMPLATE = "%s - Job execution completed successfully: %s";
    private static final String JOB_FAILED_TEMPLATE = "%s - Job execution failed: %s";

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
        logJob(jobExecution.getJobParameters(), JOB_STARTED_TEMPLATE);
    }

    @Override
    public void logJobCompleted(final JobExecution jobExecution) {
        logJob(jobExecution.getJobParameters(), JOB_COMPLETED_TEMPLATE);
    }

    @Override
    public void logJobFailed(JobExecution jobExecution) {
        logJob(jobExecution.getJobParameters(), JOB_FAILED_TEMPLATE);
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

    private String getReturnedObjectFields(final Object returnedObject) {
        if (returnedObject instanceof Iterable) {
            final Iterator<?> i = ((Iterable<?>) returnedObject).iterator();
            if (i.hasNext()) {
                final String fields = collectClassFieldNames(i.next().getClass());
                return fields != null ? "list of " + fields : null;
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
            sb.append(field.getName()).append(",");
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

    private void logJob(final JobParameters jobParameters, final String template) {
        try {
            final String author = jobParameters.getString("jobAuthor");
            if (author.equals("anonymous")) { // system jobs
                return;
            }

            final String jobName = jobParameters.getString("jobName");
            final Long userId = getUserIdByLogin(author);
            AUDIT_LOGGER.info(String.format(template, userId, jobName));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
