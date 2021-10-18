package org.ohdsi.webapi.reusable.converter;

import org.ohdsi.webapi.reusable.domain.Reusable;
import org.ohdsi.webapi.reusable.dto.ReusableDTO;
import org.ohdsi.webapi.service.converters.BaseCommonDTOExtToEntityExtConverter;
import org.springframework.stereotype.Component;

@Component
public class ReusableDTOToReusableConverter extends BaseCommonDTOExtToEntityExtConverter<ReusableDTO, Reusable> {
    protected Reusable createResultObject() {
        return new Reusable();
    }

    @Override
    protected void doConvert(ReusableDTO source, Reusable target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setData(source.getData());
    }
}
