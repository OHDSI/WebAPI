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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.transaction.Transactional;
import javax.ws.rs.*;
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
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.util.NameUtils;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

/**
 *
 * @author fdefalco
 */
@Component
@Transactional
@Path("/conceptset/")
public class ConceptSetService extends AbstractDaoService {

    @Autowired
    private ConceptSetGenerationInfoRepository conceptSetGenerationInfoRepository;

    @Autowired
    private VocabularyService vocabService;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GenericConversionService conversionService;

    @Autowired
    private Security security;
    
    public static final String COPY_NAME = "copyName";

    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ConceptSetDTO getConceptSet(@PathParam("id") final int id) {
        ConceptSet conceptSet = getConceptSetRepository().findById(id);
        ExceptionUtils.throwNotFoundExceptionIfNull(conceptSet, String.format("There is no concept set with id = %d.", id));
        return conversionService.convert(conceptSet, ConceptSetDTO.class);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ConceptSetDTO> getConceptSets() {
        return getTransactionTemplate().execute(transactionStatus ->
                StreamSupport.stream(getConceptSetRepository().findAll().spliterator(), false)
                        .map(conceptSet -> conversionService.convert(conceptSet, ConceptSetDTO.class))
                        .collect(Collectors.toList())
        );
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
        Source vocabSourceInfo = sourceService.getPriorityVocabularySource();
        Collection<Concept> concepts = vocabService.executeIdentifierLookup(vocabSourceInfo.getSourceKey(), identifiers);

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

    @Deprecated
    @GET
    @Path("{id}/{name}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConceptSetExistsDeprecated(@PathParam("id") final int id, @PathParam("name") String name) {
        String warningMessage = "This method will be deprecated in the next release. Instead, please use the new REST endpoint: conceptset/{id}/exists?name={name}";
        Collection<ConceptSet> cs = getConceptSetRepository().conceptSetExists(id, name);
        return Response.ok(cs).header("Warning: 299", warningMessage).build();
    }
		
    @GET
    @Path("/{id}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    public int getCountCSetWithSameName(@PathParam("id") @DefaultValue("0") final int id, @QueryParam("name") String name) {
        return getConceptSetRepository().getCountCSetWithSameName(id, name);
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
        Source source = sourceService.getPriorityVocabularySource();
        ArrayList<ConceptSetExport> cs = new ArrayList<>();
        Response response = null;
        try {
            // Load all of the concept sets requested
            for (int i = 0; i < conceptSetIds.size(); i++) {
                // Get the concept set information
                cs.add(getConceptSetForExport(conceptSetIds.get(i), new SourceInfo(source)));
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
    public ConceptSetDTO createConceptSet(ConceptSetDTO conceptSetDTO) {

        UserEntity user = userRepository.findByLogin(security.getSubject());
        ConceptSet conceptSet = conversionService.convert(conceptSetDTO, ConceptSet.class);
        ConceptSet updated = new ConceptSet();
        updated.setCreatedBy(user);
        updated.setCreatedDate(new Date());
        updateConceptSet(updated, conceptSet);
        return conversionService.convert(updated, ConceptSetDTO.class);
    }

    @GET
    @Path("/{id}/copy-name")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getNameForCopy (@PathParam("id") final int id){
        ConceptSetDTO source = getConceptSet(id);
        String name = NameUtils.getNameForCopy(source.getName(), this::getNamesLike, getConceptSetRepository().findByName(source.getName()));
        return Collections.singletonMap(COPY_NAME, name);
    }

    public List<String> getNamesLike(String copyName) {

        return getConceptSetRepository().findAllByNameStartsWith(copyName).stream().map(ConceptSet::getName).collect(Collectors.toList());
    }
    
    @Path("/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ConceptSetDTO updateConceptSet(@PathParam("id") final int id, ConceptSetDTO conceptSetDTO) throws Exception {

        ConceptSet updated = getConceptSetRepository().findById(id);
        if (updated == null) {
          throw new Exception("Concept Set does not exist.");
        }

        ConceptSet conceptSet = conversionService.convert(conceptSetDTO, ConceptSet.class);
        return conversionService.convert(updateConceptSet(updated, conceptSet), ConceptSetDTO.class);
    }

    private ConceptSet updateConceptSet(ConceptSet dst, ConceptSet src) {

        UserEntity user = userRepository.findByLogin(security.getSubject());
        dst.setName(src.getName());
        dst.setModifiedDate(new Date());
        dst.setModifiedBy(user);
        
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
  public void deleteConceptSet(@PathParam("id") final int id) {
      // Remove any generation info
      try {
        this.conceptSetGenerationInfoRepository.deleteByConceptSetId(id);
      } catch (EmptyResultDataAccessException e) {
          // Ignore - there may be no data
          log.warn("Failed to delete Generation Info by ConceptSet with ID = {}, {}", id, e);
      }
      catch (Exception e) {
          throw e;
      }
      
      // Remove the concept set items
      try {
        getConceptSetItemRepository().deleteByConceptSetId(id);
      } catch (EmptyResultDataAccessException e) {
          // Ignore - there may be no data
          log.warn("Failed to delete ConceptSet items with ID = {}, {}", id, e);
      }
      catch (Exception e) {
          throw e;
      }

      // Remove the concept set
      try {
        getConceptSetRepository().delete(id);
      } catch (EmptyResultDataAccessException e) {
          // Ignore - there may be no data
          log.warn("Failed to delete ConceptSet with ID = {}, {}", id, e);
      }
      catch (Exception e) {
          throw e;
      }
  }
}
