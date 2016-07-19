/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

  private final static String CONCEPT_SET_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/conceptSetQuery.sql");
  private final static String CONCEPT_SET_DESCENDANTS_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/conceptSetDescendants.sql");
  private final static String CONCEPT_SET_MAPPED_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/conceptSetMapped.sql");
  private final static String CONCEPT_SET_INCLUDE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/conceptSetInclude.sql");
  private final static String CONCEPT_SET_EXCLUDE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/conceptSetExclude.sql");


  private ArrayList<Long> getConceptIds(ArrayList<Concept> concepts)
  {
    ArrayList<Long> conceptIdList = new ArrayList<>();
    for (Concept concept : concepts) {
      conceptIdList.add(concept.conceptId);
    }
    return conceptIdList;     
  }
  
  
 
  private String buildConceptSetSubQuery (
          ArrayList<Concept> concepts,
          ArrayList<Concept> descendantConcepts
  )
  {
    ArrayList<String> queries = new ArrayList<>();
    if (concepts.size() > 0) {
      queries.add(StringUtils.replace(CONCEPT_SET_QUERY_TEMPLATE, "@conceptIds", StringUtils.join(getConceptIds(concepts), ",")));
    }
    if (descendantConcepts.size() > 0) {
      queries.add(StringUtils.replace(CONCEPT_SET_DESCENDANTS_TEMPLATE, "@conceptIds", StringUtils.join(getConceptIds(descendantConcepts), ",")));
    }
    
    return StringUtils.join(queries, "UNION");
    
  }
  
  private String buildConceptSetMappedQuery (
          ArrayList<Concept> mappedConcepts,
          ArrayList<Concept> mappedDescendantConcepts
  ) {
    String conceptSetQuery = buildConceptSetSubQuery(mappedConcepts, mappedDescendantConcepts);
    return StringUtils.replace(CONCEPT_SET_MAPPED_TEMPLATE, "@conceptsetQuery", conceptSetQuery);
  }
  
  private String buildConceptSetQuery(
          ArrayList<Concept> concepts,
          ArrayList<Concept> descendantConcepts,
          ArrayList<Concept> mappedConcepts,
          ArrayList<Concept> mappedDesandantConcepts)
  {
    if (concepts.size() == 0)
    {
      return "select concept_id from @cdm_database_schema.CONCEPT where 0=1";
    }
    
    String conceptSetQuery = buildConceptSetSubQuery(concepts, descendantConcepts);

    if (mappedConcepts.size() > 0 || mappedDesandantConcepts.size() > 0)
    {
      buildConceptSetMappedQuery(mappedConcepts,mappedDesandantConcepts);
      conceptSetQuery += "UNION\n" + buildConceptSetMappedQuery(mappedConcepts,mappedDesandantConcepts);
    }
    return conceptSetQuery;
  }
  
  public String buildExpressionQuery(ConceptSetExpression expression)
  {
    // handle included concepts.
    ArrayList<Concept> includeConcepts = new ArrayList<>();
    ArrayList<Concept> includeDescendantConcepts = new ArrayList<>();
    ArrayList<Concept> includeMappedConcepts = new ArrayList<>();
    ArrayList<Concept> includeMappedDescendantConcepts = new ArrayList<>();
 
    ArrayList<Concept> excludeConcepts = new ArrayList<>();
    ArrayList<Concept> excludeDescendantConcepts = new ArrayList<>();
    ArrayList<Concept> excludeMappedConcepts = new ArrayList<>();
    ArrayList<Concept> excludeMappedDescendantConcepts = new ArrayList<>();
    
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
    
    String conceptSetQuery = StringUtils.replace(CONCEPT_SET_INCLUDE_TEMPLATE,"@includeQuery", buildConceptSetQuery(includeConcepts, includeDescendantConcepts, includeMappedConcepts, includeMappedDescendantConcepts));
    
    if (excludeConcepts.size() > 0){
      String excludeConceptsQuery = StringUtils.replace(CONCEPT_SET_EXCLUDE_TEMPLATE, "@excludeQuery", buildConceptSetQuery(excludeConcepts, excludeDescendantConcepts, excludeMappedConcepts, excludeMappedDescendantConcepts));
      conceptSetQuery += excludeConceptsQuery;
    }
    
    return conceptSetQuery;
  }
}
