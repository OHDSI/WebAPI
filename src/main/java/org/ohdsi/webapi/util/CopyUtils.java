package org.ohdsi.webapi.util;

import java.util.Optional;
import java.util.function.Function;

import static org.ohdsi.webapi.Constants.Templates.ENTITY_COPY_PREFIX;

public final class CopyUtils {
    
    private CopyUtils(){}

    public static String getNameForCopy(String dtoName, Function<String, Integer> countLikeName, Optional<?> existingObject) {
        String nameWithPrefix = String.format(ENTITY_COPY_PREFIX, dtoName);
        int similar = countLikeName.apply(nameWithPrefix);
        return existingObject.map(c -> similar > 0 ? nameWithPrefix + " (" + similar + ")" : nameWithPrefix)
                .orElse(dtoName);
    }
}
