package org.ohdsi.webapi.service;

import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.source.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;

import javax.ws.rs.ForbiddenException;
import java.util.Objects;

public abstract class AbstractVocabularyService extends AbstractDaoService {

  @Autowired
  protected VocabularyService vocabService;
  @Autowired
  protected GenericConversionService conversionService;
  @Autowired
  protected SourceService sourceService;

  public Source getPriorityVocabularySource() {

    Source source = sourceService.getPriorityVocabularySource();
    if (Objects.isNull(source)) {
      throw new ForbiddenException();
    }
    return source;
  }

  protected ConceptSetExport exportConceptSet(ConceptSet conceptSet, SourceInfo vocabSource) {

    ConceptSetExport export = conversionService.convert(conceptSet, ConceptSetExport.class);
    // Lookup the identifiers
    export.identifierConcepts = vocabService.executeIncludedConceptLookup(vocabSource.sourceKey, conceptSet.expression);
    // Lookup the mapped items
    export.mappedConcepts = vocabService.executeMappedLookup(vocabSource.sourceKey, conceptSet.expression);
    return export;
  }
}
