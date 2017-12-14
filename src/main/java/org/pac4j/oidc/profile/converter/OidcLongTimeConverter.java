package org.pac4j.oidc.profile.converter;

import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.FormattedDate;
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
            return new FormattedDate(new Date(seconds * 1000L), "yyyy-MM-dd'T'HH:mm:ss'z'", Locale.getDefault());
        } else if (attribute instanceof String) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");

            try {
                return new FormattedDate(sdf.parse((String)attribute), "yyyy-MM-dd'T'HH:mm:ssz", Locale.getDefault());
            } catch (ParseException var4) {
                throw new TechnicalException(var4);
            }
        } else {
            return attribute instanceof FormattedDate ? (Date)attribute : null;
        }
    }
}