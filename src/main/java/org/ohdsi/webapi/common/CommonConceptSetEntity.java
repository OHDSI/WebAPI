package org.ohdsi.webapi.common;

import org.hibernate.annotations.Type;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.ConceptSet;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;
import java.util.Objects;

@MappedSuperclass
public class CommonConceptSetEntity {
  @Lob
  @Column(name = "expression")
  @JdbcTypeCode(SqlTypes.VARCHAR) // TODO: Custom type "org.hibernate.type.TextType" needs Hibernate 6 migration
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
