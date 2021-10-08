package org.ohdsi.webapi.reusable.converter;

import org.ohdsi.webapi.reusable.domain.Reusable;
import org.ohdsi.webapi.reusable.dto.ReusableDTO;
import org.ohdsi.webapi.service.converters.BaseCommonEntityExtToDTOExtConverter;
import org.ohdsi.webapi.service.converters.BaseCommonEntityToDTOConverter;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ReusableToReusableDTOConverter extends BaseCommonEntityExtToDTOExtConverter<Reusable, ReusableDTO> {
    @Override
    protected void doConvert(Reusable source, ReusableDTO target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setData(source.getData());
    }

    protected ReusableDTO createResultObject() {
        return new ReusableDTO();
    }
}
