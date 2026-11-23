/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: August 25, 2017
 *
 */

package com.odysseusinc.arachne.commons.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class TemplateUtils {

    /** Loads only a classpath resource, decoded explicitly as UTF-8. */
    public static ScalarTemplate loadTemplate(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Template resource path is required");
        }
        InputStream inputStream = TemplateUtils.class.getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalArgumentException("Template resource not found: " + path);
        }
        try (Reader r = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            StringBuilder source = new StringBuilder();
            char[] buffer = new char[2048];
            int count;
            while ((count = r.read(buffer)) != -1) {
                source.append(buffer, 0, count);
            }
            return new ScalarTemplate(source.toString());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to initialize template: " + path, e);
        }
    }
}
