
package org.ohdsi.webapi.converter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;

public abstract class BaseConversionServiceAwareConverter<From, To> implements Converter<From, To>, InitializingBean {

    protected To createResultObject() {

        return null;
    }

    protected To createResultObject(From from) {

        return createResultObject();
    }

    @Autowired
    protected GenericConversionService conversionService;

    @Override
    public void afterPropertiesSet() throws Exception {

        conversionService.addConverter(this);
    }

    protected void proceedAdditionalFields(To to, final From from) {}
}
