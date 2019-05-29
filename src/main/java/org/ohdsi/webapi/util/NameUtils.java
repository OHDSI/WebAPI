package org.ohdsi.webapi.util;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ohdsi.webapi.Constants.Templates.ENTITY_COPY_PREFIX;

public final class NameUtils {

    private NameUtils(){}

    public static String getNameForCopy(String dtoName, Function<String, List<String>> getNamesLike, Optional<?> existingObject) {
        String nameWithPrefix = String.format(ENTITY_COPY_PREFIX, dtoName);
        return existingObject.map(o -> getNameWithSuffix(nameWithPrefix, getNamesLike)).orElse(dtoName);
    }

    public static String getNameWithSuffix(String dtoName, Function<String, List<String>> getNamesLike){
        StringBuilder builder = new StringBuilder(dtoName);

        List<String> nameList = getNamesLike.apply(dtoName);
        Pattern p = Pattern.compile(Pattern.quote(dtoName) + " \\(([0-9]+)\\)");
        nameList.stream()
                .map(n -> {
                    if (Objects.equals(n, dtoName)) {
                        return "0";
                    } else {
                        Matcher m = p.matcher(n);
                        return m.find() ? m.group(1) : null;
                    }
                })
                .filter(Objects::nonNull)
                .map(Integer::parseInt)
                .max(Comparator.naturalOrder())
                .ifPresent(cnt -> builder.append(" (").append(cnt + 1).append(")"));

        return builder.toString();
    }
}
