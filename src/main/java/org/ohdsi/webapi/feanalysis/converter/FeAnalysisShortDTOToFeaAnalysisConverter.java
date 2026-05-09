package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.analysis.cohortcharacterization.design.CcResultType;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.feanalysis.domain.*;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class FeAnalysisShortDTOToFeaAnalysisConverter extends BaseFeAnalysisDTOToFeAnalysisConverter<FeAnalysisShortDTO, FeAnalysisEntity> {

  @Override
  protected FeAnalysisEntity createResultObject(FeAnalysisShortDTO dto) {
    return Objects.equals(dto.getType(), StandardFeatureAnalysisType.CRITERIA_SET) ?
            Objects.equals(dto.getStatType(), CcResultType.PREVALENCE) ? new FeAnalysisWithPrevalenceCriteriaEntity() : new FeAnalysisWithDistributionCriteriaEntity()
            : new FeAnalysisWithStringEntity();
  }
}
