package org.ohdsi.webapi.arachne.commons.converter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;

public abstract class BaseConvertionServiceAwareConverter<S, T> implements Converter<S, T>, InitializingBean {

    @Autowired
    protected GenericConversionService conversionService;

    @Override
    public void afterPropertiesSet() throws Exception {
        conversionService.addConverter(this);
    }

    @Override
    public T convert(S s) {

        T target = createResultObject(s);
        convert(s, target);
        return target;
    }

    protected abstract T createResultObject(S source);

    protected abstract void convert(S source, T target);
}
