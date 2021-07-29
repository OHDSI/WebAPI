package org.ohdsi.webapi.conceptset.converter;

import com.odysseusinc.arachne.commons.converter.BaseConvertionServiceAwareConverter;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.springframework.stereotype.Component;

@Component
public class ConceptSetToExportConverter extends BaseConvertionServiceAwareConverter<ConceptSet, ConceptSetExport> {
  @Override
  protected ConceptSetExport createResultObject(ConceptSet conceptSet) {

    return new ConceptSetExport();
  }

  @Override
  protected void convert(ConceptSet conceptSet, ConceptSetExport export) {
    export.ConceptSetId = conceptSet.id;
    export.ConceptSetName = conceptSet.name;
    export.csExpression = conceptSet.expression;
  }
}
