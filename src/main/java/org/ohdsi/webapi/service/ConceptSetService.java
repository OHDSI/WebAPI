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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationInfo;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationInfoRepository;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.conceptset.ExportUtil;
import org.ohdsi.webapi.evidence.NegativeControlRepository;
import org.ohdsi.webapi.source.SourceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

/**
 *
 * @author fdefalco
 */
@Path("/conceptset/")
@Component
public class ConceptSetService extends AbstractDaoService {

    @Autowired
    private ConceptSetGenerationInfoRepository conceptSetGenerationInfoRepository;

    @Autowired
    private NegativeControlRepository negativeControlRepository;

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
//        expression.items = new ConceptSetExpression.ConceptSetItem[map.size()];
        ArrayList<ConceptSetExpression.ConceptSetItem> expressionItems = new ArrayList<>();
        
        // lookup the concepts we need information for
        long[] identifiers = new long[map.size()];
        int identifierIndex = 0;
        for (Long identifier : map.keySet()) {
            identifiers[identifierIndex] = identifier;
            identifierIndex++;
        }

        // assume we want to resolve using the priority vocabulary provider
        SourceInfo vocabSourceInfo = sourceService.getPriorityVocabularySourceInfo();
        Collection<Concept> concepts = vocabService.executeIdentifierLookup(vocabSourceInfo.sourceKey, identifiers);

        // put the concept information into the expression along with the concept set item information 
        for (Concept concept : concepts) {
          ConceptSetExpression.ConceptSetItem currentItem  = new ConceptSetExpression.ConceptSetItem();
          currentItem.concept = concept;
          ConceptSetItem csi = map.get(concept.conceptId);
          currentItem.includeDescendants = (csi.getIncludeDescendants() == 1);
          currentItem.includeMapped = (csi.getIncludeMapped() == 1);
          currentItem.isExcluded = (csi.getIsExcluded() == 1);
          expressionItems.add(currentItem); 
        }
        expression.items = expressionItems.toArray(new ConceptSetExpression.ConceptSetItem[0]);
        
        return expression;
    }

    @GET
    @Path("{id}/{name}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ConceptSet> getConceptSetExists(@PathParam("id") final int id, @PathParam("name") String name) {
        return getConceptSetRepository().conceptSetExists(id, name);
    }

    @PUT
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

    @GET
    @Path("/exportlist")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response exportConceptSetList(@QueryParam("conceptsets") final String conceptSetList) throws Exception {
        ArrayList<Integer> conceptSetIds = new ArrayList<>();
        try {
            String[] conceptSetItems = conceptSetList.split("\\+");
            for(String csi : conceptSetItems) {
                conceptSetIds.add(Integer.valueOf(csi));
            }
            if (conceptSetIds.size() <= 0) {
                throw new IllegalArgumentException("You must supply a querystring value for conceptsets that is of the form: ?conceptset=<concept_set_id_1>+<concept_set_id_2>+<concept_set_id_n>");
            }
        } catch (Exception e) {
            throw e;
        }

        ByteArrayOutputStream baos;
        SourceInfo sourceInfo = sourceService.getPriorityVocabularySourceInfo();
        ArrayList<ConceptSetExport> cs = new ArrayList<>();
        Response response = null;
        try {
            // Load all of the concept sets requested
            for (int i = 0; i < conceptSetIds.size(); i++) {
                // Get the concept set information
                cs.add(getConceptSetForExport(conceptSetIds.get(i), sourceInfo));
            }
           // Write Concept Set Expression to a CSV
            baos = ExportUtil.writeConceptSetExportToCSVAndZip(cs);

            response = Response
                    .ok(baos)
                    .type(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"conceptSetExport.zip\"")
                    .build();

        } catch (Exception ex) {
            throw ex;
        }
        return response;
    }

    @GET
    @Path("{id}/export")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response exportConceptSetToCSV(@PathParam("id") final String id) throws Exception {
        return this.exportConceptSetList(id);
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ConceptSet createConceptSet(ConceptSet conceptSet) {
        ConceptSet updated = new ConceptSet();
        return updateConceptSet(updated, conceptSet);
    }

    @Path("/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ConceptSet updateConceptSet(@PathParam("id") final int id, ConceptSet conceptSet) throws Exception {
        ConceptSet updated = this.getConceptSet(id);
        if (updated == null) {
          throw new Exception("Concept Set does not exist.");
        }

        return updateConceptSet(updated, conceptSet);
    }

    private ConceptSet updateConceptSet(ConceptSet dst, ConceptSet src) {
        dst.setName(src.getName());
        
        dst = this.getConceptSetRepository().save(dst);
        return dst;
    }
    
    private ConceptSetExport getConceptSetForExport(int conceptSetId, SourceInfo vocabSource) {
        ConceptSetExport cs = new ConceptSetExport();

        // Set the concept set id
        cs.ConceptSetId = conceptSetId;
        // Get the concept set information
        cs.ConceptSetName = this.getConceptSet(conceptSetId).getName();
        // Get the concept set expression
        cs.csExpression = this.getConceptSetExpression(conceptSetId);

        // Lookup the identifiers
        cs.identifierConcepts = vocabService.executeIncludedConceptLookup(vocabSource.sourceKey, cs.csExpression); //vocabService.executeIdentifierLookup(vocabSource.sourceKey, conceptIds);
        // Lookup the mapped items
        cs.mappedConcepts = vocabService.executeMappedLookup(vocabSource.sourceKey, cs.csExpression);

        return cs;
    }

  
  @GET
  @Path("{id}/generationinfo")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ConceptSetGenerationInfo> getConceptSetGenerationInfo(@PathParam("id") final int id) {
      return this.conceptSetGenerationInfoRepository.findAllByConceptSetId(id);
  }
  
  @DELETE
  @Transactional(rollbackOn = Exception.class, dontRollbackOn = EmptyResultDataAccessException.class)
  @Path("{id}")
  public void deleteConceptSet(@PathParam("id") final int id) throws Exception {
      // Remove any evidence
      try {
        this.negativeControlRepository.deleteAllByConceptSetId(id);
      } catch (EmptyResultDataAccessException e) {
          // Ignore - there may be no data
          log.debug(e.getMessage());
      }
      catch (Exception e) {
          throw e;
      }
      
      // Remove any generation info
      try {
        this.conceptSetGenerationInfoRepository.deleteByConceptSetId(id);
      } catch (EmptyResultDataAccessException e) {
          // Ignore - there may be no data
          log.debug(e.getMessage());
      }
      catch (Exception e) {
          throw e;
      }
      
      // Remove the concept set items
      try {
        getConceptSetItemRepository().deleteByConceptSetId(id);
      } catch (EmptyResultDataAccessException e) {
          // Ignore - there may be no data
          log.debug(e.getMessage());
      }
      catch (Exception e) {
          throw e;
      }

      // Remove the concept set
      try {
        getConceptSetRepository().delete(id);
      } catch (EmptyResultDataAccessException e) {
          // Ignore - there may be no data
          log.debug(e.getMessage());
      }
      catch (Exception e) {
          throw e;
      }
  }
}
