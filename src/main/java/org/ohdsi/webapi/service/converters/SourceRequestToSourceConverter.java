package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class SourceRequestToSourceConverter implements Converter<SourceRequest, Source> {

    public SourceRequestToSourceConverter(GenericConversionService conversionService) {
        conversionService.addConverter(this);
    }

    @Override
    public Source convert(SourceRequest request) {

        Source source = new Source();
        source.setSourceName(request.getName());
        source.setSourceConnection(request.getConnectionString());
        source.setSourceDialect(request.getDialect());
        source.setSourceKey(request.getKey());
        source.setDaimons(request.getDaimons());
        source.getDaimons().forEach(d -> d.setSource(source));
        return source;
    }
}
