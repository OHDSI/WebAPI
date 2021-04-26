package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.ohdsi.webapi.service.converters.BaseCommonEntityToDTOConverter;

public abstract class BaseFeAnalysisEntityToFeAnalysisDTOConverter<T extends FeAnalysisShortDTO> extends
        BaseCommonEntityToDTOConverter<FeAnalysisEntity<?>, T> {

    @Override
    public void doConvert(FeAnalysisEntity<?> source, T target) {
        target.setType(source.getType());
        target.setName(source.getName());
        target.setId(source.getId());
        target.setDomain(source.getDomain());
        target.setDescription(source.getDescr());
        target.setStatType(source.getStatType());
    }
}
