/*
 * Copyright 2026 OHDSI.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.odysseusinc.arachne.commons.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Immutable, single-pass scalar substitution; no helpers, paths, or executable syntax. */
public final class ScalarTemplate {
    private final List<String> literals = new ArrayList<>();
    private final List<String> names = new ArrayList<>();

    public ScalarTemplate(String source) {
        if (source == null) {
            throw new IllegalArgumentException("Template source is required");
        }
        int offset = 0;
        while (offset < source.length()) {
            int open = source.indexOf("{{", offset);
            int close = source.indexOf("}}", offset);
            if (close >= 0 && (open < 0 || close < open)) {
                throw new IllegalArgumentException("Unmatched template closing delimiter");
            }
            if (open < 0) {
                break;
            }
            close = source.indexOf("}}", open + 2);
            if (close < 0) {
                throw new IllegalArgumentException("Unclosed template placeholder");
            }
            String name = source.substring(open + 2, close).trim();
            if (!name.matches("[A-Za-z_][A-Za-z0-9_]*")
                    || (close + 2 < source.length() && source.charAt(close + 2) == '}')) {
                throw new IllegalArgumentException("Unsupported template placeholder: " + name);
            }
            literals.add(source.substring(offset, open));
            names.add(name);
            offset = close + 2;
        }
        literals.add(source.substring(offset));
    }

    public String apply(Map<String, ?> context) throws IOException {
        return render(context);
    }

    /** Compatibility overload: only null or Map contexts are accepted. */
    public String apply(Object context) throws IOException {
        if (context != null && !(context instanceof Map)) {
            throw new IllegalArgumentException("Template context must be null or a Map");
        }
        return render((Map<?, ?>) context);
    }

    private String render(Map<?, ?> context) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            result.append(literals.get(i));
            Object value = context == null ? null : context.get(names.get(i));
            if (value != null) {
                escape(result, value.toString());
            }
        }
        return result.append(literals.get(literals.size() - 1)).toString();
    }

    private static void escape(StringBuilder result, String value) {
        for (int i = 0; i < value.length(); i++) {
            switch (value.charAt(i)) {
                case '<': result.append("&lt;"); break;
                case '>': result.append("&gt;"); break;
                case '"': result.append("&quot;"); break;
                case '\'': result.append("&#x27;"); break;
                case '`': result.append("&#x60;"); break;
                case '&': result.append("&amp;"); break;
                case '=': result.append("&#x3D;"); break;
                default: result.append(value.charAt(i));
            }
        }
    }
}
