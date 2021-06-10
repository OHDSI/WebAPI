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
    private static final String DEFAULT_DESIGN_NAME = "Design";

    private NameUtils(){}

    public static String getNameForCopy(String dtoName, Function<String, List<String>> getNamesLike, Optional<?> existingObject) {
        String name = dtoName != null ? dtoName : DEFAULT_DESIGN_NAME;
        String nameWithPrefix = String.format(ENTITY_COPY_PREFIX, name);
        return existingObject.map(o -> getNameWithSuffix(nameWithPrefix, getNamesLike)).orElse(name);
    }

    public static String getNameWithSuffix(String dtoName, Function<String, List<String>> getNamesLike){
        String name = dtoName != null ? dtoName : DEFAULT_DESIGN_NAME;
        StringBuilder builder = new StringBuilder(name);

        List<String> nameList = getNamesLike.apply(formatNameForLikeSearch(name) + "%");
        Pattern p = Pattern.compile(Pattern.quote(name) + " \\(([0-9]+)\\)");
        nameList.stream()
                .map(n -> {
                    if (n.equalsIgnoreCase(name)) {
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
    
    public static String formatNameForLikeSearch(String name) {
        return name.replace("[", "\\[").replace("]", "\\]").replace("%", "\\%").replace("_", "\\_");
    }
}
