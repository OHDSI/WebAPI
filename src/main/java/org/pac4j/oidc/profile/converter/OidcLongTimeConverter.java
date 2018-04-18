/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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
 * Authors: Mikhail Mironov
 *
 */
package org.pac4j.oidc.profile.converter;

import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.converter.DateConverter;
import org.pac4j.core.profile.converter.AttributeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class OidcLongTimeConverter implements AttributeConverter<Date> {
    public OidcLongTimeConverter() {
    }

    public Date convert(Object attribute) {
        if (attribute instanceof Long) {
            long seconds = ((Long)attribute).longValue();
            DateConverter converter = new DateConverter("yyyy-MM-dd'T'HH:mm:ss'z'", Locale.getDefault());
            return converter.convert(new Date(seconds * 1000L));
        } else if (attribute instanceof String) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");

            try {
                DateConverter converter = new DateConverter("yyyy-MM-dd'T'HH:mm:ssz", Locale.getDefault());
                return converter.convert(sdf.parse((String)attribute));
            } catch (ParseException var4) {
                throw new TechnicalException(var4);
            }
        } else {
            return attribute instanceof DateConverter ? (Date)attribute : null;
        }
    }
}