package org.ohdsi.webapi.common;

import org.hibernate.annotations.Type;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.ConceptSet;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import java.util.List;
import java.util.Objects;

@MappedSuperclass
public class CommonConceptSetEntity {
  @Lob
  @Column(name = "expression")
  @Type(type = "org.hibernate.type.TextType")
  private String rawExpression;

  public String getRawExpression() {
    return rawExpression;
  }

  public void setRawExpression(String rawExpression) {
    this.rawExpression = rawExpression;
  }

  public List<ConceptSet> getConceptSets() {
    return Objects.nonNull(this.rawExpression) ?
            Utils.deserialize(this.rawExpression, typeFactory -> typeFactory.constructCollectionType(List.class, ConceptSet.class)) : null;
  }
}
