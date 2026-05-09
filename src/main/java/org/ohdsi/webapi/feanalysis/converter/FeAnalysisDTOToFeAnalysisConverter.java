package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithCriteriaEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType.CRITERIA_SET;

@Component
public class FeAnalysisDTOToFeAnalysisConverter extends BaseFeAnalysisDTOToFeAnalysisConverter<FeAnalysisDTO, FeAnalysisEntity> {

    @Autowired
    private ConversionService conversionService;

    @Override
    public FeAnalysisEntity convert(final FeAnalysisDTO source) {
        return super.convert(source);
    }

    @Override
    protected FeAnalysisEntity createResultObject(final FeAnalysisDTO source) {
        if (Objects.equals(source.getType(), CRITERIA_SET)) {
            return conversionService.convert(source, FeAnalysisWithCriteriaEntity.class);
        } else {
            return conversionService.convert(source, FeAnalysisWithStringEntity.class);
        }
    }


}

