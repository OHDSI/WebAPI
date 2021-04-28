package org.ohdsi.webapi.feanalysis.converter;

import com.odysseusinc.arachne.commons.converter.BaseConvertionServiceAwareConverter;
import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysisAggregate;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisAggregateEntity;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisAggregateDTO;
import org.springframework.stereotype.Component;

@Component
public class FeatureAnalysisAggregateToDTOConverter extends BaseConvertionServiceAwareConverter<FeatureAnalysisAggregate, FeAnalysisAggregateDTO> {

  @Override
  protected FeAnalysisAggregateDTO createResultObject(FeatureAnalysisAggregate featureAnalysisAggregate) {

    return new FeAnalysisAggregateDTO();
  }

  @Override
  protected void convert(FeatureAnalysisAggregate source, FeAnalysisAggregateDTO dto) {

    if (source instanceof FeAnalysisAggregateEntity) {
      dto.setId(source.getId());
    }
    dto.setDomain((StandardFeatureAnalysisDomain) source.getDomain());
    dto.setName(source.getName());
    dto.setExpression(source.getExpression());
    dto.setFunction(source.getFunction());
    dto.setJoinTable(source.getJoinTable());
    dto.setJoinType(source.getJoinType());
    dto.setJoinCondition(source.getJoinCondition());
    dto.setAdditionalColumns(source.getAdditionalColumns());
    if (source instanceof FeAnalysisAggregateEntity) {
      dto.setDefault(((FeAnalysisAggregateEntity) source).isDefault());
    }
    dto.setMissingMeansZero(source.isMissingMeansZero());
  }
}
