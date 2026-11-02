package org.ohdsi.webapi.util;

import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.CoreConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Custom Logback converter that filters out framework packages from stack traces
 * to make them more readable by showing only application-level code.
 * 
 * Configuration: Pass comma-separated package prefixes as option from application.yaml.
 * Example in logback-spring.xml: %fex{org.springframework,org.apache,jakarta.servlet}
 * 
 * Note: Logback's built-in %ex converter does NOT support package filtering (unlike Log4j2).
 * This custom converter is required for Spring Boot 3.x / Logback 1.5.x.
 * 
 * Extends ThrowableHandlingConverter (not ThrowableProxyConverter) to avoid the parent's
 * option parsing which expects integers for stack trace depth limiting.
 */
public class PackageFilteringThrowableProxyConverter extends ThrowableHandlingConverter {

    private List<String> ignoredPackages = Collections.emptyList();

    @Override
    public void start() {
        // Read option from pattern (configured via application.yaml -> logback-spring.xml)
        List<String> options = getOptionList();
        if (options != null && !options.isEmpty()) {
            ignoredPackages = options;
        }
        super.start();
    }

    @Override
    public String convert(ILoggingEvent event) {
        IThrowableProxy tp = event.getThrowableProxy();
        if (tp == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(2048);
        recursiveAppendFiltered(sb, "", tp);
        return sb.toString();
    }

    private void recursiveAppendFiltered(StringBuilder sb, String prefix, IThrowableProxy tp) {
        if (tp == null) {
            return;
        }
        
        StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
        int commonFrames = tp.getCommonFrames();
        
        int filteredCount = 0;
        
        // Print stack trace elements, filtering framework packages
        for (int i = 0; i < stepArray.length - commonFrames; i++) {
            StackTraceElementProxy step = stepArray[i];
            String className = step.getStackTraceElement().getClassName();
            
            boolean shouldIgnore = ignoredPackages.stream()
                .anyMatch(className::startsWith);
            
            if (!shouldIgnore) {
                sb.append(prefix);
                sb.append("\tat ");
                sb.append(step.getStackTraceElement().toString());
                sb.append(CoreConstants.LINE_SEPARATOR);
            } else {
                filteredCount++;
            }
        }
        
        // Show how many frames were filtered (parenthetical to indicate omission, not continuation)
        if (filteredCount > 0) {
            sb.append(prefix);
            sb.append("\t(");
            sb.append(filteredCount);
            sb.append(" frames filtered)");
            sb.append(CoreConstants.LINE_SEPARATOR);
        }
        
        if (commonFrames > 0) {
            sb.append(prefix);
            sb.append("\t... ");
            sb.append(commonFrames);
            sb.append(" common frames omitted");
            sb.append(CoreConstants.LINE_SEPARATOR);
        }
        
        // Handle cause
        IThrowableProxy cause = tp.getCause();
        if (cause != null) {
            sb.append(prefix);
            sb.append("Caused by: ");
            sb.append(cause.getClassName());
            sb.append(": ");
            sb.append(cause.getMessage());
            sb.append(CoreConstants.LINE_SEPARATOR);
            recursiveAppendFiltered(sb, prefix, cause);
        }
        
        // Handle suppressed exceptions
        IThrowableProxy[] suppressed = tp.getSuppressed();
        if (suppressed != null) {
            for (IThrowableProxy suppressedEx : suppressed) {
                sb.append(prefix);
                sb.append("Suppressed: ");
                sb.append(suppressedEx.getClassName());
                sb.append(": ");
                sb.append(suppressedEx.getMessage());
                sb.append(CoreConstants.LINE_SEPARATOR);
                recursiveAppendFiltered(sb, prefix + "\t", suppressedEx);
            }
        }
    }
}
