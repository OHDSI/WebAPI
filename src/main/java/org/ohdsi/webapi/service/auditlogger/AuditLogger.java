package org.ohdsi.webapi.service.auditlogger;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static java.util.Arrays.asList;

public class AuditLogger {

    private AuditLogger() {}

    @NotNull
    private static final Logger logger = LoggerFactory.getLogger(AuditLogger.class);

    public static void log(@NotNull String userId, @NotNull String action, @NotNull Object[] arguments, @NotNull String[] argumentNames) {
        logger.info("{} {} [{}]", userId, action, argumentsToString(arguments, argumentNames));
    }

    private static String argumentsToString(Object[] arguments, String[]argumentNames) {
        if (arguments.length != argumentNames.length) {
            return asList(arguments)
                    .stream()
                    .map(arg -> getObjectValue(arg))
                    .collect(Collectors.joining());
        }

        List<String> argumentList = new ArrayList<String>();
        IntStream.range(0, arguments.length)
                .forEach(idx -> argumentList.add(argumentNames[idx] + ":" + getObjectValue(arguments[idx])));

        return String.join(",", argumentList);
    }

    private static String getObjectValue(Object object) {
        if (object == null) {
            return "";
        }

        if ( object instanceof String) {
            return object.toString();
        }

        return ToStringBuilder.reflectionToString(object, new CustomRecursiveToStringStyleForLogging());
    }

}
