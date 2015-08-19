/*
 * Copyright 2015 fdefalco.
 *
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
package org.ohdsi.webapi.service;

import java.util.Collection;
import java.util.HashMap;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.vocabulary.Concept;
import org.ohdsi.webapi.vocabulary.ConceptSetExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author fdefalco
 */
@Path("/conceptset/")
@Component
public class ConceptSetService extends AbstractDaoService {
    
  @Autowired 
  private VocabularyService vocabService;
  
  @Autowired
  private SourceService sourceService;
  
  @Path("{id}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ConceptSet getConceptSet(@PathParam("id") final int id) {
    return getConceptSetRepository().findById(id);
  }

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public Iterable<ConceptSet> getConceptSets() {
    return getConceptSetRepository().findAll();
  }

  @GET
  @Path("{id}/items")
  @Produces(MediaType.APPLICATION_JSON)
  public Iterable<ConceptSetItem> getConceptSetItems(@PathParam("id") final int id) {
    return getConceptSetItemRepository().findAllByConceptSetId(id);
  }

  @GET
  @Path("{id}/expression")
  @Produces(MediaType.APPLICATION_JSON)
  public ConceptSetExpression getConceptSetExpression(@PathParam("id") final int id) {
    HashMap<Long, ConceptSetItem> map = new HashMap<>();

    // collect the concept set items so we can lookup their properties later
    for (ConceptSetItem csi : getConceptSetItems(id)) {
      map.put(csi.getConceptId(), csi);
    }

    // create our expression to return
    ConceptSetExpression expression = new ConceptSetExpression();
    expression.items = new ConceptSetExpression.ConceptSetItem[map.size()];

    // lookup the concepts we need information for
    String[] identifiers = new String[map.size()];
    int identifierIndex = 0;
    for (Long identifier : map.keySet()) {
      identifiers[identifierIndex] = identifier.toString();
      identifierIndex++;
    }
    
    // assume we want to resolve using the priority vocabulary provider
    SourceInfo vocabSourceInfo = sourceService.getPriorityVocabularySourceInfo();
    Collection<Concept> concepts = vocabService.executeIdentifierLookup(vocabSourceInfo.sourceKey, identifiers);

    // put the concept information into the expression along with the concept set item information 
    int conceptIndex = 0;
    for (Concept concept : concepts) {
      expression.items[conceptIndex] = new ConceptSetExpression.ConceptSetItem();
      expression.items[conceptIndex].concept = concept;
      
      ConceptSetItem csi = map.get(concept.conceptId);
      expression.items[conceptIndex].includeDescendants = (csi.getIncludeDescendants() == 1);
      expression.items[conceptIndex].includeMapped = (csi.getIncludeMapped() == 1);
      expression.items[conceptIndex].isExcluded = (csi.getIsExcluded()== 1);
      conceptIndex++;
    }

    return expression;
  }

  @POST
  @Path("{id}/items")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public boolean saveConceptSetItems(@PathParam("id") final int id, ConceptSetItem[] items) {
    getConceptSetItemRepository().deleteByConceptSetId(id);

    for (ConceptSetItem csi : items) {
      csi.setConceptSetId(id);
      getConceptSetItemRepository().save(csi);
    }

    return true;
  }

  @Path("/")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public ConceptSet saveConceptSet(ConceptSet conceptSet) {
    conceptSet = this.getConceptSetRepository().save(conceptSet);
    return conceptSet;
  }
}
