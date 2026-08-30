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

package org.ohdsi.webapi.arachne.commons.utils;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.apache.commons.io.IOUtils;

public class TemplateUtils {

    // Seems to be thread-safe
    private static final Handlebars handlebars = new Handlebars();

    public static Template loadTemplate(String path) {

        InputStream inputStream = TemplateUtils.class.getResourceAsStream(path);
        return loadTemplate(inputStream);
    }

    public static Template loadTemplate(InputStream inputStream) {

        try (Reader r = new InputStreamReader(inputStream)) {
            return handlebars.compileInline(IOUtils.toString(r));
        } catch (IOException e) {
            String message = "Failed to initailize template";
            throw new IllegalArgumentException(message);
        }
    }
}
