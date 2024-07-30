package org.ohdsi.webapi.service.converters;

import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.KerberosAuthMechanism;
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
        source.setUsername(request.getUsername());
        source.setPassword(request.getPassword());
        source.setSourceDialect(request.getDialect());
        source.setSourceKey(request.getKey());
        source.setDaimons(request.getDaimons());
        source.getDaimons().forEach(d -> d.setSource(source));
        source.setKeyfileName(request.getKeyfileName());
        source.setKrbAdminServer(request.getKrbAdminServer());
        source.setKrbAuthMethod(KerberosAuthMechanism.getByName(request.getKrbAuthMethod()));
        if (request.isCheckConnection() != null) {
            source.setCheckConnection(request.isCheckConnection());
        }
        return source;
    }
}
