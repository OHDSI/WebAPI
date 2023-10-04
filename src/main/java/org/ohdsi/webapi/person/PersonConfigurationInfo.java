package org.ohdsi.webapi.person;

import org.ohdsi.info.ConfigurationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PersonConfigurationInfo extends ConfigurationInfo {

    private static final String KEY = "person";

    public PersonConfigurationInfo(@Value("${person.viewDates}") Boolean viewDatesPermitted) {

        properties.put("viewDatesPermitted", viewDatesPermitted);
    }

    @Override
    public String getKey() {

        return KEY;
    }
}
