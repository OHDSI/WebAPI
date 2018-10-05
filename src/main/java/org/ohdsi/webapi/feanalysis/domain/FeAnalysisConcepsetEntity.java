package org.ohdsi.webapi.feanalysis.domain;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.annotations.Type;
import org.ohdsi.circe.cohortdefinition.ConceptSet;

import javax.persistence.*;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "fe_analysis_conceptset")
public class FeAnalysisConcepsetEntity {
  @Id
  @SequenceGenerator(name = "fe_conceptset_sequence", sequenceName = "fe_conceptset_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fe_conceptset_sequence")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fe_analysis_id")
  private FeAnalysisWithCriteriaEntity featureAnalysis;

  @Lob
  @Column(name = "expression")
  @Type(type = "org.hibernate.type.TextType")
  private String rawExpression;

  public FeAnalysisConcepsetEntity() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public FeAnalysisWithCriteriaEntity getFeatureAnalysis() {
    return featureAnalysis;
  }

  public void setFeatureAnalysis(FeAnalysisWithCriteriaEntity featureAnalysis) {
    this.featureAnalysis = featureAnalysis;
  }

  public String getRawExpression() {
    return rawExpression;
  }

  public void setRawExpression(String rawExpression) {
    this.rawExpression = rawExpression;
  }

  public List<ConceptSet> getConceptSets() {
    final ObjectMapper objectMapper = new ObjectMapper();
    try {
      JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, ConceptSet.class);
      return Objects.nonNull(this.rawExpression) ? objectMapper.readValue(this.rawExpression, type) : null;
    } catch (IOException e) {
      throw new IllegalArgumentException("Concept set cannot be parsed", e);
    }
  }
}
