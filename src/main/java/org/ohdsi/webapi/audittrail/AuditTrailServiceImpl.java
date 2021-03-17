package org.ohdsi.webapi.audittrail;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.cohortsample.dto.CohortSampleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
class AuditTrailServiceImpl implements AuditTrailService {
    private final Logger AUDIT_LOGGER = LoggerFactory.getLogger("audit");
    private final Logger AUDIT_EXTRA_LOGGER = LoggerFactory.getLogger("audit-extra");

    private static final int MAX_ENTRY_LENGTH = 2048;
    private static final String EXTRA_LOG_REFERENCE_MESSAGE =
            "... Log entry exceeds 2048 chars length. Please see the whole message in extra log file, entry id = %s";

    private static final String USER_LOGIN_SUCCESS_TEMPLATE = "User successfully logged in: %s, sessionId = %s, remote-host = %s";
    private static final String USER_LOGIN_FAILURE_TEMPLATE = "User login failed: %s, remote-host = %s";
    private static final String USER_LOGOUT_SUCCESS_TEMPLATE = "User successfully logged out: %s";
    private static final String USER_LOGOUT_FAILURE_TEMPLATE = "User logout failed: %s";

    private static final String JOB_STARTED_TEMPLATE = "%s - Job %s execution started: %s";
    private static final String JOB_COMPLETED_TEMPLATE = "%s - Job %s execution completed successfully: %s";
    private static final String JOB_FAILED_TEMPLATE = "%s - Job %s execution failed: %s";

    private static final String LIST_OF_TEMPLATE = "list of %s objects %s";
    private static final String MAP_OF_TEMPLATE = "map (key: %s) of %s objects %s";
    private static final String FILE_TEMPLATE = "file %s (%s bytes)";
    private static final String PATIENT_IDS_TEMPLATE = " Patient IDs (%s): ";

    private static final String FIELD_DIVIDER = " - ";
    private static final String SPACE = " ";
    private static final String FAILURE = "FAILURE (see application log for details)";
    private static final String ANONYMOUS = "anonymous";
    private static final String NO_SESSION = "NO_SESSION";
    private static final String NO_LOCATION = "NO_LOCATION";
    private static final String EMPTY_LIST = "empty list";
    private static final String EMPTY_MAP = "empty map";

    private final AtomicInteger extraLogIdSuffix = new AtomicInteger();

    @Override
    public void logSuccessfulLogin(final String login, final String sessionId, final String remoteHost) {
        log(String.format(USER_LOGIN_SUCCESS_TEMPLATE, login, sessionId, remoteHost));
    }

    @Override
    public void logFailedLogin(final String login, final String remoteHost) {
        log(String.format(USER_LOGIN_FAILURE_TEMPLATE, login, remoteHost));
    }

    @Override
    public void logSuccessfulLogout(final String login) {
        log(String.format(USER_LOGOUT_SUCCESS_TEMPLATE, login));
    }

    @Override
    public void logFailedLogout(final String login) {
        log(String.format(USER_LOGOUT_FAILURE_TEMPLATE, login));
    }

    @Override
    public void logRestCall(final AuditTrailEntry entry, final boolean success) {
        final StringBuilder logEntry = new StringBuilder();

        final String currentUserField = getCurrentUserField(entry);

        logEntry.append(currentUserField).append(SPACE)
                .append(entry.getRemoteHost()).append(SPACE)
                .append(getSessionIdField(entry))
                .append(FIELD_DIVIDER)
                .append(getActionLocationField(entry))
                .append(FIELD_DIVIDER)
                .append(getRestCallField(entry));

        if (success) {
            final String additionalInfo = getAdditionalInfo(entry);
            if (!StringUtils.isBlank(additionalInfo)) {
                logEntry.append(FIELD_DIVIDER).append(additionalInfo);
            }
        } else {
            logEntry.append(FIELD_DIVIDER).append(FAILURE);
        }

        log(logEntry.toString());
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

    private void log(final String message) {
        final SyslogMessage syslogMessage = new SyslogMessage()
                .withFacility(Facility.AUDIT)
                .withSeverity(Severity.INFORMATIONAL)
                .withAppName("Atlas")
                .withMsg(message);
        final StringBuilder logEntry = new StringBuilder(syslogMessage.toRfc5424SyslogMessage());

        if (logEntry.length() >= MAX_ENTRY_LENGTH) {
            final String currentExtraSuffix = String.format("%02d", this.extraLogIdSuffix.getAndIncrement());
            final String entryId = System.currentTimeMillis() + "_" + currentExtraSuffix;
            AUDIT_EXTRA_LOGGER.info(entryId + FIELD_DIVIDER + message);

            final String extraLogReferenceMessage = String.format(EXTRA_LOG_REFERENCE_MESSAGE, entryId);
            logEntry.setLength(MAX_ENTRY_LENGTH - extraLogReferenceMessage.length() - 1);
            logEntry.append(extraLogReferenceMessage);
            AUDIT_LOGGER.info(logEntry.toString());

            if (this.extraLogIdSuffix.get() > 99) {
                this.extraLogIdSuffix.set(0);
            }

        } else {
            AUDIT_LOGGER.info(logEntry.toString());
        }
    }

    private String getSessionIdField(final AuditTrailEntry entry) {
        return entry.getSessionId() != null ? entry.getSessionId() : NO_SESSION;
    }

    private String getCurrentUserField(final AuditTrailEntry entry) {
        return (entry.getCurrentUser() != null ? String.valueOf(entry.getCurrentUser()) : ANONYMOUS);
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

        // File entry log
        if (entry.getReturnedObject() instanceof Response) {
            try {
                final Object entity = ((Response) entry.getReturnedObject()).getEntity();
                if (entity instanceof File) {
                    final File file = (File) entity;
                    return String.format(FILE_TEMPLATE, file.getName(), file.length());
                }
                return null;
            } catch (final Exception e) {
                return null;
            }
        }

        // Patient IDs log
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
        if (returnedObject == null) {
            return null;
        }

        if (returnedObject instanceof Collection) {
            final Collection<?> c = (Collection<?>) returnedObject;
            if (!c.isEmpty()) {
                final String fields = collectClassFieldNames(c.iterator().next().getClass());
                return fields != null ? String.format(LIST_OF_TEMPLATE, c.size(), fields) : null;
            }
            return EMPTY_LIST;
        } if (returnedObject instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>) returnedObject;
            if (!map.isEmpty()) {
                final Map.Entry<?, ?> entry = map.entrySet().iterator().next();
                final Class<?> keyClass = entry.getKey().getClass();
                final Class<?> valueClass = entry.getValue().getClass();
                final String valueFields = collectClassFieldNames(valueClass);
                return valueFields != null ?
                        String.format(MAP_OF_TEMPLATE, keyClass.getSimpleName(), map.size(), valueFields) : null;
            }
            return EMPTY_MAP;
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
        ReflectionUtils.doWithFields(klass,
                field -> sb.append(field.getName()).append("::").append(field.getType().getSimpleName()).append(","));
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }

    private void logJob(final JobExecution jobExecution, final String template) {
        final JobParameters jobParameters = jobExecution.getJobParameters();
        final String author = jobParameters.getString(Constants.Params.JOB_AUTHOR);
        if (author.equals("anonymous")) { // system jobs
            return;
        }

        final String jobName = jobParameters.getString(Constants.Params.JOB_NAME);
        log(String.format(template, author, jobExecution.getJobId(), jobName));
    }
}
