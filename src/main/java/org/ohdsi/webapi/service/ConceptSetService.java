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

import org.apache.shiro.authz.UnauthorizedException;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.vocabulary.Concept;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.checker.conceptset.ConceptSetChecker;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationInfo;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationInfoRepository;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.conceptset.dto.ConceptSetVersionFullDTO;
import org.ohdsi.webapi.exception.ConceptNotExistException;
import org.ohdsi.webapi.security.PermissionService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.shiro.management.datasource.SourceAccessor;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.tag.domain.HasTags;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.ohdsi.webapi.util.ExportUtil;
import org.ohdsi.webapi.util.NameUtils;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.versioning.domain.ConceptSetVersion;
import org.ohdsi.webapi.versioning.domain.Version;
import org.ohdsi.webapi.versioning.domain.VersionBase;
import org.ohdsi.webapi.versioning.domain.VersionType;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.ohdsi.webapi.versioning.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

 /**
  * Provides REST services for working with
  * concept sets.
  * 
  * @summary Concept Set
  */
@Component
@Transactional
@Path("/conceptset/")
public class ConceptSetService extends AbstractDaoService implements HasTags<Integer> {

    @Autowired
    private ConceptSetGenerationInfoRepository conceptSetGenerationInfoRepository;

    @Autowired
    private VocabularyService vocabService;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private SourceAccessor sourceAccessor;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GenericConversionService conversionService;

    @Autowired
    private Security security;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ConceptSetChecker checker;

    @Autowired
    private VersionService<ConceptSetVersion> versionService;

    @Value("${security.defaultGlobalReadPermissions}")
    private boolean defaultGlobalReadPermissions;
    
    public static final String COPY_NAME = "copyName";

    /**
     * Get the concept set based in the identifier
     * 
     * @summary Get concept set by ID
     * @param id The concept set ID
     * @return The concept set definition
     */
    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ConceptSetDTO getConceptSet(@PathParam("id") final int id) {
        ConceptSet conceptSet = getConceptSetRepository().findById(id);
        ExceptionUtils.throwNotFoundExceptionIfNull(conceptSet, String.format("There is no concept set with id = %d.", id));
        return conversionService.convert(conceptSet, ConceptSetDTO.class);
    }

    /**
     * Get the full list of concept sets in the WebAPI database
     * 
     * @summary Get all concept sets
     * @return A list of all concept sets in the WebAPI database
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ConceptSetDTO> getConceptSets() {
        return getTransactionTemplate().execute(
                transactionStatus -> StreamSupport.stream(getConceptSetRepository().findAll().spliterator(), false)
                        .filter(!defaultGlobalReadPermissions ? entity -> permissionService.hasReadAccess(entity) : entity -> true)
                        .map(conceptSet -> {
                            ConceptSetDTO dto = conversionService.convert(conceptSet, ConceptSetDTO.class);
                            permissionService.fillWriteAccess(conceptSet, dto);
                            permissionService.fillReadAccess(conceptSet, dto);
                            return dto;
                        })
                        .collect(Collectors.toList()));

    }

    /**
     * Get the concept set items for a selected concept set ID.
     * 
     * @summary Get the concept set items
     * @param id The concept set identifier
     * @return A list of concept set items
     */
    @GET
    @Path("{id}/items")
    @Produces(MediaType.APPLICATION_JSON)
    public Iterable<ConceptSetItem> getConceptSetItems(@PathParam("id") final int id) {
        return getConceptSetItemRepository().findAllByConceptSetId(id);
    }

    /**
     * Get the concept set expression for a selected version of the expression
     * 
     * @summary Get concept set expression by version
     * @param id The concept set ID
     * @param version The version identifier
     * @return The concept set expression
     */
    @GET
    @Path("{id}/version/{version}/expression")
    @Produces(MediaType.APPLICATION_JSON)
    public ConceptSetExpression getConceptSetExpression(@PathParam("id") final int id,
                                                        @PathParam("version") final int version) {
        SourceInfo sourceInfo = sourceService.getPriorityVocabularySourceInfo();
        if (sourceInfo == null) {
            throw new UnauthorizedException();
        }
        return getConceptSetExpression(id, version, sourceInfo);
    }

    /**
     * Get the concept set expression by version for the selected
     * source key. NOTE: This method requires the specification
     * of a source key but it does not appear to be used by the underlying
     * code.
     * 
     * @summary Get concept set expression by version and source.
     * @param id The concept set identifier
     * @param version The version of the concept set
     * @param sourceKey The source key
     * @return The concept set expression for the selected version
     */
    @GET
    @Path("{id}/version/{version}/expression/{sourceKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public ConceptSetExpression getConceptSetExpression(@PathParam("id") final int id,
                                                        @PathParam("version") final int version,
                                                        @PathParam("sourceKey") final String sourceKey) {
        SourceInfo sourceInfo = sourceService.getPriorityVocabularySourceInfo();
        if (sourceInfo == null) {
            throw new UnauthorizedException();
        }
        return getConceptSetExpression(id, version, sourceInfo);
    }

    /**
     * Get the concept set expression by identifier
     * 
     * @summary Get concept set by ID
     * @param id The concept set identifier
     * @return The concept set expression
     */
    @GET
    @Path("{id}/expression")
    @Produces(MediaType.APPLICATION_JSON)
    public ConceptSetExpression getConceptSetExpression(@PathParam("id") final int id) {
        SourceInfo sourceInfo = sourceService.getPriorityVocabularySourceInfo();
        if (sourceInfo == null) {
            throw new UnauthorizedException();
        }
        return getConceptSetExpression(id, null, sourceInfo);
    }

    /**
     * Get the concept set expression by identifier and source key
     * 
     * @summary Get concept set by ID and source
     * @param id The concept set ID
     * @param sourceKey The source key
     * @return The concept set expression
     */
    @GET
    @Path("{id}/expression/{sourceKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public ConceptSetExpression getConceptSetExpression(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey) {

        Source source = sourceService.findBySourceKey(sourceKey);
        sourceAccessor.checkAccess(source);
        return getConceptSetExpression(id, null, source.getSourceInfo());
    }

    private ConceptSetExpression getConceptSetExpression(int id, Integer version, SourceInfo sourceInfo) {
        HashMap<Long, Concept> map = new HashMap<>();

        // create our expression to return
        ConceptSetExpression expression = new ConceptSetExpression();
        ArrayList<ConceptSetExpression.ConceptSetItem> expressionItems = new ArrayList<>();

        List<ConceptSetItem> repositoryItems = new ArrayList<>();
        if (Objects.isNull(version)) {
            getConceptSetItems(id).forEach(repositoryItems::add);
        } else {
            ConceptSetVersionFullDTO dto = getVersion(id, version);
            repositoryItems.addAll(dto.getItems());
        }

        // collect the unique concept IDs so we can load the concept object later.
        for (ConceptSetItem csi : repositoryItems) {
            map.put(csi.getConceptId(), null);
        }

        // lookup the concepts we need information for
        long[] identifiers = new long[map.size()];
        int identifierIndex = 0;
        for (Long identifier : map.keySet()) {
            identifiers[identifierIndex] = identifier;
            identifierIndex++;
        }

        String sourceKey;
        if (Objects.isNull(sourceInfo)) {
            sourceKey = sourceService.getPriorityVocabularySource().getSourceKey();
        } else {
            sourceKey = sourceInfo.sourceKey;
        }

        Collection<Concept> concepts = vocabService.executeIdentifierLookup(sourceKey, identifiers);
        if (concepts.size() != identifiers.length) {
            String ids = Arrays.stream(identifiers).boxed()
                    .filter(identifier -> concepts.stream().noneMatch(c -> c.conceptId.equals(identifier)))
                    .map(String::valueOf)
                    .collect(Collectors.joining(",", "(", ")"));
            throw new ConceptNotExistException("Current data source does not contain required concepts " + ids);
        }
        for(Concept concept : concepts) {
          map.put(concept.conceptId, concept); // associate the concept object to the conceptID in the map
        }

        // put the concept information into the expression along with the concept set item information 
        for (ConceptSetItem repositoryItem : repositoryItems) {
          ConceptSetExpression.ConceptSetItem currentItem  = new ConceptSetExpression.ConceptSetItem();
          currentItem.concept = map.get(repositoryItem.getConceptId());
          currentItem.includeDescendants = (repositoryItem.getIncludeDescendants() == 1);
          currentItem.includeMapped = (repositoryItem.getIncludeMapped() == 1);
          currentItem.isExcluded = (repositoryItem.getIsExcluded() == 1);
          expressionItems.add(currentItem); 
        }
        expression.items = expressionItems.toArray(new ConceptSetExpression.ConceptSetItem[0]); // this will return a new array
        
        return expression;
    }

    /**
     * Check if the concept set name exists (DEPRECATED)
     * 
     * @summary DO NOT USE
     * @deprecated
     * @param id The concept set ID
     * @param sourceKey The source key
     * @return The concept set expression
     */
    @Deprecated
    @GET
    @Path("{id}/{name}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConceptSetExistsDeprecated(@PathParam("id") final int id, @PathParam("name") String name) {
        String warningMessage = "This method will be deprecated in the next release. Instead, please use the new REST endpoint: conceptset/{id}/exists?name={name}";
        Collection<ConceptSet> cs = getConceptSetRepository().conceptSetExists(id, name);
        return Response.ok(cs).header("Warning: 299", warningMessage).build();
    }

    /**
     * Check if a concept set with the same name exists in the WebAPI
     * database. The name is checked against the selected concept set ID
     * to ensure that only the selected concept set ID has the name specified.
     * 
     * @summary Concept set with same name exists
     * @param id The concept set ID
     * @param name The name of the concept set
     * @return The count of concept sets with the name, excluding the 
     * specified concept set ID.
     */
    @GET
    @Path("/{id}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    public int getCountCSetWithSameName(@PathParam("id") @DefaultValue("0") final int id, @QueryParam("name") String name) {
        return getConceptSetRepository().getCountCSetWithSameName(id, name);
    }

    /**
     * Update the concept set items for the selected concept set ID in the
     * WebAPI database.
     * 
     * The concept set has two parts: 1) the elements of the ConceptSetDTO that
     * consist of the identifier, name, etc. 2) the concept set items which 
     * contain the concepts and their mapping (i.e. include descendants).
     * 
     * @summary Update concept set items
     * @param id The concept set ID
     * @param items An array of ConceptSetItems
     * @return Boolean: true if the save is successful
     */
    @PUT
    @Path("{id}/items")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public boolean saveConceptSetItems(@PathParam("id") final int id, ConceptSetItem[] items) {
        getConceptSetItemRepository().deleteByConceptSetId(id);

        for (ConceptSetItem csi : items) {
            // ID must be set to null in case of copying from version, so the new item will be created
            csi.setId(0);
            csi.setConceptSetId(id);
            getConceptSetItemRepository().save(csi);
        }

        return true;
    }

    /**
     * Exports a list of concept sets, based on the conceptSetList argument,
     * to one or more comma separated value (CSV) file(s), compresses the files
     * into a ZIP file and sends the ZIP file to the client.
     * 
     * @summary Export concept set list to CSV files
     * @param conceptSetList A list of concept set identifiers in the format
     * conceptset=<concept_set_id_1>+<concept_set_id_2>+<concept_set_id_n>
     * @return
     * @throws Exception 
     */
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

    /**
     * Exports a single concept set to a comma separated value (CSV) 
     * file, compresses to a ZIP file and sends to the client.

     * @param id The concept set ID
     * @return A zip file containing the exported concept set
     * @throws Exception 
     */
    @GET
    @Path("{id}/export")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response exportConceptSetToCSV(@PathParam("id") final String id) throws Exception {
        return this.exportConceptSetList(id);
    }

    /**
     * Save a new concept set to the WebAPI database
     * 
     * @summary Create a new concept set
     * @param conceptSetDTO The concept set to save
     * @return The concept set saved with the concept set identifier
     */
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
        updated.setTags(null);
        updateConceptSet(updated, conceptSet);
        return conversionService.convert(updated, ConceptSetDTO.class);
    }

    /**
     * Creates a concept set name, based on the selected concept set ID,
     * that is used when generating a copy of an existing concept set. This
     * function is generally used in conjunction with the copy endpoint to
     * create a unique name and then save a copy of an existing concept set.
     * 
     * @sumamry Get concept set name suggestion for copying
     * @param id The concept set ID
     * @return A map of the new concept set name and the existing concept set
     * name
     */
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

    /**
     * Updates the concept set for the selected concept set.
     * 
     * The concept set has two parts: 1) the elements of the ConceptSetDTO that
     * consist of the identifier, name, etc. 2) the concept set items which 
     * contain the concepts and their mapping (i.e. include descendants).
     * 
     * @summary Update concept set
     * @param id The concept set identifier
     * @param conceptSetDTO The concept set header
     * @return The
     * @throws Exception 
     */    
    @Path("/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public ConceptSetDTO updateConceptSet(@PathParam("id") final int id, ConceptSetDTO conceptSetDTO) throws Exception {

        ConceptSet updated = getConceptSetRepository().findById(id);
        if (updated == null) {
          throw new Exception("Concept Set does not exist.");
        }

        saveVersion(id);

        ConceptSet conceptSet = conversionService.convert(conceptSetDTO, ConceptSet.class);
        return conversionService.convert(updateConceptSet(updated, conceptSet), ConceptSetDTO.class);
    }

    private ConceptSet updateConceptSet(ConceptSet dst, ConceptSet src) {

        UserEntity user = userRepository.findByLogin(security.getSubject());
        dst.setName(src.getName());
        dst.setDescription(src.getDescription());
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


    /**
     * Get the concept set generation information for the selected concept
     * set ID. This function only works with the configuration of the CEM
     * data source.
     * 
     * @link https://github.com/OHDSI/CommonEvidenceModel/wiki
     * 
     * @summary Get concept set generation info
     * @param id The concept set identifier.
     * @return A collection of concept set generation info objects
     */
  @GET
  @Path("{id}/generationinfo")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ConceptSetGenerationInfo> getConceptSetGenerationInfo(@PathParam("id") final int id) {
      return this.conceptSetGenerationInfoRepository.findAllByConceptSetId(id);
  }
  
  /**
   * Delete the selected concept set by concept set identifier
   * 
   * @summary Delete concept set
   * @param id The concept set ID
   */
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

    /**
     * Assign tag to Concept Set
     *
     * @summary Assign concept set tag
     * @since v2.10.0
     * @param id The concept set ID
     * @param tagId The tag ID
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/tag/")
    @Transactional
    public void assignTag(@PathParam("id") final Integer id, final int tagId) {
        ConceptSet entity = getConceptSetRepository().findById(id);
        checkOwnerOrAdminOrGranted(entity);
        assignTag(entity, tagId);
    }

    /**
     * Unassign tag from Concept Set
     *
     * @summary Remove tag from concept set
     * @since v2.10.0
     * @param id The concept set ID
     * @param tagId The tag ID
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/tag/{tagId}")
    @Transactional
    public void unassignTag(@PathParam("id") final Integer id, @PathParam("tagId") final int tagId) {
        ConceptSet entity = getConceptSetRepository().findById(id);
        checkOwnerOrAdminOrGranted(entity);
        unassignTag(entity, tagId);
    }

    /**
     * Assign protected tag to Concept Set
     *
     * @summary Assign protected concept set tag
     * @since v2.10.0
     * @param id The concept set ID
     * @param tagId The tag ID
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/protectedtag/")
    @Transactional
    public void assignPermissionProtectedTag(@PathParam("id") final int id, final int tagId) {
        assignTag(id, tagId);
    }

    /**
     * Unassign protected tag from Concept Set
     *
     * @summary Remove protected concept set tag
     * @since v2.10.0
     * @param id The concept set ID
     * @param tagId The tag ID
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/protectedtag/{tagId}")
    @Transactional
    public void unassignPermissionProtectedTag(@PathParam("id") final int id, @PathParam("tagId") final int tagId) {
        unassignTag(id, tagId);
    }

    /**
     * Checks a concept set for diagnostic problems. At this time, 
     * this appears to be an endpoint used to check to see which tags
     * are applied to a concept set.
     * 
     * @summary Concept set tag check
     * @since v2.10.0
     * @param conceptSetDTO The concept set
     * @return A check result
     */
    @POST
    @Path("/check")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public CheckResult runDiagnostics(ConceptSetDTO conceptSetDTO) {
        return new CheckResult(checker.check(conceptSetDTO));
    }

    /**
     * Get a list of versions of the selected concept set
     *
     * @summary Get concept set version list
     * @since v2.10.0
     * @param id The concept set ID
     * @return A list of version information
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/")
    @Transactional
    public List<VersionDTO> getVersions(@PathParam("id") final int id) {
        List<VersionBase> versions = versionService.getVersions(VersionType.CONCEPT_SET, id);
        return versions.stream()
                .map(v -> conversionService.convert(v, VersionDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Get a specific version of a concept set
     *
     * @summary Get concept set by version
     * @since v2.10.0
     * @param id The concept set ID
     * @param version The version ID
     * @return The concept set for the selected version
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/{version}")
    @Transactional
    public ConceptSetVersionFullDTO getVersion(@PathParam("id") final int id, @PathParam("version") final int version) {
        checkVersion(id, version, false);
        ConceptSetVersion conceptSetVersion = versionService.getById(VersionType.CONCEPT_SET, id, version);

        return conversionService.convert(conceptSetVersion, ConceptSetVersionFullDTO.class);
    }

    /**
     * Update a specific version of a selected concept set
     *
     * @summary Update a concept set version
     * @since v2.10.0
     * @param id The concept set ID
     * @param version The version ID
     * @param updateDTO The version update
     * @return The version information
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/{version}")
    @Transactional
    public VersionDTO updateVersion(@PathParam("id") final int id, @PathParam("version") final int version,
                                    VersionUpdateDTO updateDTO) {
        checkVersion(id, version);
        updateDTO.setAssetId(id);
        updateDTO.setVersion(version);
        ConceptSetVersion updated = versionService.update(VersionType.CONCEPT_SET, updateDTO);

        return conversionService.convert(updated, VersionDTO.class);
    }

    /**
     * Delete a version of a concept set
     *
     * @summary Delete a concept set version
     * @since v2.10.0
     * @param id The concept ID
     * @param version THe version ID
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/{version}")
    @Transactional
    public void deleteVersion(@PathParam("id") final int id, @PathParam("version") final int version) {
        checkVersion(id, version);
        versionService.delete(VersionType.CONCEPT_SET, id, version);
    }

    /**
     * Create a new asset from a specific version of the selected
     * concept set
     *
     * @summary Create a concept set copy from a specific concept set version
     * @since v2.10.0
     * @param id The concept set ID
     * @param version The version ID
     * @return The concept set copy
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/{version}/createAsset")
    @Transactional
    public ConceptSetDTO copyAssetFromVersion(@PathParam("id") final int id, @PathParam("version") final int version) {
        checkVersion(id, version, false);
        ConceptSetVersion conceptSetVersion = versionService.getById(VersionType.CONCEPT_SET, id, version);

        ConceptSetVersionFullDTO fullDTO = conversionService.convert(conceptSetVersion, ConceptSetVersionFullDTO.class);
        ConceptSetDTO conceptSetDTO = fullDTO.getEntityDTO();
        // Reset id so it won't be used during saving
        conceptSetDTO.setId(0);
        conceptSetDTO.setTags(null);
        conceptSetDTO.setName(NameUtils.getNameForCopy(conceptSetDTO.getName(), this::getNamesLike, getConceptSetRepository().findByName(conceptSetDTO.getName())));
        ConceptSetDTO createdDTO = createConceptSet(conceptSetDTO);
        saveConceptSetItems(createdDTO.getId(), fullDTO.getItems().toArray(new ConceptSetItem[0]));

        return createdDTO;
    }

    /**
     * Get list of concept sets with their assigned tags
     *
     * @summary Get concept sets and tag information
     * @param requestDTO The tagNameListRequest
     * @return A list of concept sets with their assigned tags
     */
    @POST
    @Path("/byTags")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<ConceptSetDTO> listByTags(TagNameListRequestDTO requestDTO) {
        if (requestDTO == null || requestDTO.getNames() == null || requestDTO.getNames().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> names = requestDTO.getNames().stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        List<ConceptSet> entities = getConceptSetRepository().findByTags(names);
        return listByTags(entities, names, ConceptSetDTO.class);
    }

    private void checkVersion(int id, int version) {
        checkVersion(id, version, true);
    }

    private void checkVersion(int id, int version, boolean checkOwnerShip) {
        Version conceptSetVersion = versionService.getById(VersionType.CONCEPT_SET, id, version);
        ExceptionUtils.throwNotFoundExceptionIfNull(conceptSetVersion, String.format("There is no concept set version with id = %d.", version));

        ConceptSet entity = getConceptSetRepository().findOne(id);
        if (checkOwnerShip) {
            checkOwnerOrAdminOrGranted(entity);
        }
    }

    private ConceptSetVersion saveVersion(int id) {
        ConceptSet def = getConceptSetRepository().findById(id);
        ConceptSetVersion version = conversionService.convert(def, ConceptSetVersion.class);

        UserEntity user = Objects.nonNull(def.getModifiedBy()) ? def.getModifiedBy() : def.getCreatedBy();
        Date versionDate = Objects.nonNull(def.getModifiedDate()) ? def.getModifiedDate() : def.getCreatedDate();
        version.setCreatedBy(user);
        version.setCreatedDate(versionDate);
        return versionService.create(VersionType.CONCEPT_SET, version);
    }
}
