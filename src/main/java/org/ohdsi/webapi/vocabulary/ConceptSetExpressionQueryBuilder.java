/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.vocabulary;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.helper.ResourceHelper;

/**
 *
 * Unit tests for CocneptSetExpressionQueryBuilder.
 */
public class ConceptSetExpressionQueryBuilder {

  private final static String CONCEPT_SET_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/conceptSetExpression.sql");
  private final static String CONCEPT_SET_EXCLUDE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/conceptSetExclude.sql");
  private final static String CONCEPT_SET_DESCENDANTS_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/conceptSetDescendants.sql");
  private final static String CONCEPT_SET_MAPPED_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/conceptSetMapped.sql");


  private ArrayList<Long> getConceptIds(ArrayList<ConceptSetExpression.Concept> concepts)
  {
    ArrayList<Long> conceptIdList = new ArrayList<>();
    for (ConceptSetExpression.Concept concept : concepts) {
      conceptIdList.add(concept.conceptId);
    }
    return conceptIdList;     
  }
  
  private String buildConceptSetQuery(
          ArrayList<ConceptSetExpression.Concept> concepts,
          ArrayList<ConceptSetExpression.Concept> descendantConcepts,
          ArrayList<ConceptSetExpression.Concept> excludeConcepts,
          ArrayList<ConceptSetExpression.Concept> excludeDescendantConcepts)
  {
    String conceptSetQuery = StringUtils.replace(CONCEPT_SET_QUERY_TEMPLATE, "@conceptIds",StringUtils.join(getConceptIds(concepts), ","));
    if (descendantConcepts.size() > 0) {
      String includeDescendantQuery = StringUtils.replace(CONCEPT_SET_DESCENDANTS_TEMPLATE, "@conceptIds", StringUtils.join(getConceptIds(descendantConcepts), ","));
      conceptSetQuery = StringUtils.replace(conceptSetQuery,"@descendantQuery", includeDescendantQuery);
    } else {
      conceptSetQuery = StringUtils.replace(conceptSetQuery, "@descendantQuery", "");
    }
    if (excludeConcepts.size() > 0)
    {
      String excludeClause = StringUtils.replace(CONCEPT_SET_EXCLUDE_TEMPLATE,"@conceptIds", StringUtils.join(getConceptIds(excludeConcepts),","));
      if (excludeDescendantConcepts.size() > 0){
        String excludeClauseDescendantQuery = StringUtils.replace(CONCEPT_SET_DESCENDANTS_TEMPLATE, "@conceptIds", StringUtils.join(getConceptIds(excludeDescendantConcepts), ","));
        excludeClause = StringUtils.replace(excludeClause, "@descendantQuery", excludeClauseDescendantQuery);
      } else {
        excludeClause = StringUtils.replace(excludeClause, "@descendantQuery", "");
      }
      conceptSetQuery += excludeClause;
    }
    
    return conceptSetQuery;
  }
  
  public String buildExpressionQuery(ConceptSetExpression expression)
  {
    // handle included concepts.
    ArrayList<ConceptSetExpression.Concept> includeConcepts = new ArrayList<>();
    ArrayList<ConceptSetExpression.Concept> includeDescendantConcepts = new ArrayList<>();
    ArrayList<ConceptSetExpression.Concept> excludeConcepts = new ArrayList<>();
    ArrayList<ConceptSetExpression.Concept> excludeDescendantConcepts = new ArrayList<>();
    
    ArrayList<ConceptSetExpression.Concept> includeMappedConcepts = new ArrayList<>();
    ArrayList<ConceptSetExpression.Concept> includeMappedDescendantConcepts = new ArrayList<>();
    ArrayList<ConceptSetExpression.Concept> excludeMappedConcepts = new ArrayList<>();
    ArrayList<ConceptSetExpression.Concept> excludeMappedDescendantConcepts = new ArrayList<>();
    
    // populate each sub-set of cocnepts from the flags set in each concept set item
    for (ConceptSetExpression.ConceptSetItem item : expression.items)
    {
      if (!item.isExcluded)
      {
        includeConcepts.add(item.concept);

        if (item.includeDescendants)
          includeDescendantConcepts.add(item.concept);

        if (item.includeMapped)
        {
          includeMappedConcepts.add(item.concept);
          if (item.includeDescendants)
            includeMappedDescendantConcepts.add(item.concept);
        }
      } else {
        excludeConcepts.add(item.concept);
        if (item.includeDescendants)
          excludeDescendantConcepts.add(item.concept);
        if (item.includeMapped)
        {
          excludeMappedConcepts.add(item.concept);
          if (item.includeDescendants)
            excludeMappedDescendantConcepts.add(item.concept);
        }
      }
    }
    
    // each ArrayList contains the concepts that are used in the sub-query of the codeset expression query
    
    // sanity check: if there are no included concepts, throw exception
    if (includeConcepts.isEmpty())
      throw new RuntimeException("Codeset Expression contained zero included concepts.  A codeset expression must contain at least 1 concept that is not excluded.");
    
    String conceptSetQuery = buildConceptSetQuery(includeConcepts, includeDescendantConcepts, excludeConcepts, excludeDescendantConcepts);
    
    if (includeMappedConcepts.size() > 0){
      String mappedConceptsQuery = buildConceptSetQuery(includeMappedConcepts, includeMappedDescendantConcepts, excludeMappedConcepts, excludeMappedDescendantConcepts);
      conceptSetQuery += "\nUNION\n" + StringUtils.replace(CONCEPT_SET_MAPPED_TEMPLATE, "@conceptsetQuery", mappedConceptsQuery);
    }
    
    return conceptSetQuery;
  }
}
