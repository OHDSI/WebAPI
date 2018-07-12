package org.ohdsi.webapi.vocabulary;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.circe.vocabulary.Concept;

import java.util.List;

public class ConceptAncestors extends Concept {
  @JsonProperty("ANCESTORS")
  public List<Concept> ancestors;

  @JsonProperty("RECORD_COUNT")
  public Long recordCount;

  @JsonProperty("DESCENDANT_RECORD_COUNT")
  public Long descendantRecordCount;

  public ConceptAncestors() {
  }

  public ConceptAncestors(Concept concept) {
    this.conceptClassId = concept.conceptClassId;
    this.conceptCode = concept.conceptCode;
    this.conceptId = concept.conceptId;
    this.conceptName = concept.conceptName;
    this.domainId = concept.domainId;
    this.invalidReason = concept.invalidReason;
    this.standardConcept = concept.standardConcept;
    this.vocabularyId = concept.vocabularyId;
  }
}
