package org.ohdsi.webapi.check.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Path {
    private static final String DEFAULT_SEPARATOR = " :: ";

    private final List<String> items;

    private Path(List<String> items) {
        this.items = items;
    }

    public static Path createPath(Path path, String item) {
        List<String> tempItems;
        if (Objects.isNull(path)) {
            tempItems = new ArrayList<>();
        } else {
            tempItems = new ArrayList<>(path.items);
        }
        if (item != null) {
            tempItems.add(item);
        }
        return new Path(tempItems);
    }

    public static Path createPath(String item) {
        return createPath(null, item);
    }

    public static Path createPath() {
        return createPath(null);
    }

    public String getPath() {
        return getPath(DEFAULT_SEPARATOR);
    }

    public String getPath(String separator) {
        return StringUtils.join(this.items, separator);
    }
}
