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
package org.ohdsi.webapi.mvc.controller;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.ohdsi.webapi.conceptset.ConceptSetItemRepository;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.conceptset.dto.ConceptSetVersionFullDTO;
import org.ohdsi.webapi.conceptset.annotation.ConceptSetAnnotation;
import org.ohdsi.webapi.conceptset.annotation.ConceptSetAnnotationRepository;
import org.ohdsi.webapi.exception.ConceptNotExistException;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.ohdsi.webapi.security.PermissionService;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.service.annotations.SearchDataTransformer;
import org.ohdsi.webapi.service.dto.AnnotationDetailsDTO;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.ohdsi.webapi.service.dto.SaveConceptSetAnnotationsRequest;
import org.ohdsi.webapi.service.dto.AnnotationDTO;
import org.ohdsi.webapi.service.dto.CopyAnnotationsRequest;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.datasource.SourceAccessor;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.tag.TagService;
import org.ohdsi.webapi.tag.domain.Tag;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import static org.ohdsi.webapi.service.ConceptSetService.CachingSetup.CONCEPT_SET_LIST_CACHE;

/**
 * Provides REST services for working with concept sets using Spring MVC.
 * This is a Spring MVC migration of the original Jersey-based ConceptSetService.
 *
 * @summary Concept Set (Spring MVC)
 */
@RestController
@RequestMapping("/conceptset")
@Transactional
public class ConceptSetMvcController extends AbstractMvcController {

    private static final Logger log = LoggerFactory.getLogger(ConceptSetMvcController.class);

    @Autowired
    private ConceptSetRepository conceptSetRepository;

    @Autowired
    private ConceptSetItemRepository conceptSetItemRepository;

    @Autowired
    private ConceptSetAnnotationRepository conceptSetAnnotationRepository;

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
    private PermissionService permissionService;

    @Autowired
    private ConceptSetChecker checker;

    @Autowired
    private VersionService<ConceptSetVersion> versionService;

    @Autowired
    private SearchDataTransformer searchDataTransformer;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private TagService tagService;

    @Value("${security.defaultGlobalReadPermissions}")
    private boolean defaultGlobalReadPermissions;

    public static final String COPY_NAME = "copyName";

    protected ConceptSetRepository getConceptSetRepository() {
        return conceptSetRepository;
    }

    protected ConceptSetItemRepository getConceptSetItemRepository() {
        return conceptSetItemRepository;
    }

    protected ConceptSetAnnotationRepository getConceptSetAnnotationRepository() {
        return conceptSetAnnotationRepository;
    }

    protected TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    protected UserEntity getCurrentUserEntity() {
        return userRepository.findByLogin(security.getSubject());
    }

    protected void checkOwnerOrAdminOrGranted(ConceptSet entity) {
        // Check permission - if user doesn't have write access, this will throw an exception
        if (!permissionService.hasWriteAccess(entity)) {
            throw new org.apache.shiro.authz.UnauthorizedException("No write access to this concept set");
        }
    }

    protected void assignTag(ConceptSet entity, int tagId) {
        if (Objects.nonNull(entity)) {
            Tag tag = tagService.getById(tagId);
            if (Objects.nonNull(tag)) {
                entity.getTags().add(tag);
                getConceptSetRepository().save(entity);
            }
        }
    }

    protected void unassignTag(ConceptSet entity, int tagId) {
        if (Objects.nonNull(entity)) {
            Set<Tag> tags = entity.getTags().stream()
                    .filter(t -> t.getId() != tagId)
                    .collect(Collectors.toSet());
            entity.setTags(tags);
            getConceptSetRepository().save(entity);
        }
    }

    protected <T extends org.ohdsi.webapi.service.dto.CommonEntityDTO> List<T> listByTags(
            List<ConceptSet> entities,
            List<String> names,
            Class<T> clazz) {
        return entities.stream()
                .filter(e -> e.getTags().stream()
                        .map(tag -> tag.getName().toLowerCase(Locale.ROOT))
                        .anyMatch(names::contains))
                .map(e -> conversionService.convert(e, clazz))
                .collect(Collectors.toList());
    }

    /**
     * Get the concept set based in the identifier
     *
     * @summary Get concept set by ID
     * @param id The concept set ID
     * @return The concept set definition
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConceptSetDTO> getConceptSet(@PathVariable("id") final int id) {
        ConceptSet conceptSet = getConceptSetRepository().findById(id).orElse(null);
        ExceptionUtils.throwNotFoundExceptionIfNull(conceptSet, String.format("There is no concept set with id = %d.", id));
        return ok(conversionService.convert(conceptSet, ConceptSetDTO.class));
    }

    /**
     * Get the full list of concept sets in the WebAPI database
     *
     * @summary Get all concept sets
     * @return A list of all concept sets in the WebAPI database
     */
    @GetMapping("/")
    @Cacheable(cacheNames = CONCEPT_SET_LIST_CACHE, key = "@permissionService.getSubjectCacheKey()")
    public ResponseEntity<Collection<ConceptSetDTO>> getConceptSets() {
        Collection<ConceptSetDTO> result = getTransactionTemplate().execute(
                transactionStatus -> StreamSupport.stream(getConceptSetRepository().findAll().spliterator(), false)
                        .filter(!defaultGlobalReadPermissions ? entity -> permissionService.hasReadAccess(entity) : entity -> true)
                        .map(conceptSet -> {
                            ConceptSetDTO dto = conversionService.convert(conceptSet, ConceptSetDTO.class);
                            permissionService.fillWriteAccess(conceptSet, dto);
                            permissionService.fillReadAccess(conceptSet, dto);
                            return dto;
                        })
                        .collect(Collectors.toList()));
        return ok(result);
    }

    /**
     * Get the concept set items for a selected concept set ID.
     *
     * @summary Get the concept set items
     * @param id The concept set identifier
     * @return A list of concept set items
     */
    @GetMapping("/{id}/items")
    public ResponseEntity<Iterable<ConceptSetItem>> getConceptSetItems(@PathVariable("id") final int id) {
        return ok(getConceptSetItemRepository().findAllByConceptSetId(id));
    }

    /**
     * Get the concept set expression for a selected version of the expression
     *
     * @summary Get concept set expression by version
     * @param id The concept set ID
     * @param version The version identifier
     * @return The concept set expression
     */
    @GetMapping("/{id}/version/{version}/expression")
    public ResponseEntity<ConceptSetExpression> getConceptSetExpressionByVersion(
            @PathVariable("id") final int id,
            @PathVariable("version") final int version) {
        SourceInfo sourceInfo = sourceService.getPriorityVocabularySourceInfo();
        if (sourceInfo == null) {
            throw new UnauthorizedException();
        }
        return ok(getConceptSetExpression(id, version, sourceInfo));
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
    @GetMapping("/{id}/version/{version}/expression/{sourceKey}")
    public ResponseEntity<ConceptSetExpression> getConceptSetExpressionByVersionAndSource(
            @PathVariable("id") final int id,
            @PathVariable("version") final int version,
            @PathVariable("sourceKey") final String sourceKey) {
        SourceInfo sourceInfo = sourceService.getPriorityVocabularySourceInfo();
        if (sourceInfo == null) {
            throw new UnauthorizedException();
        }
        return ok(getConceptSetExpression(id, version, sourceInfo));
    }

    /**
     * Get the concept set expression by identifier
     *
     * @summary Get concept set by ID
     * @param id The concept set identifier
     * @return The concept set expression
     */
    @GetMapping("/{id}/expression")
    public ResponseEntity<ConceptSetExpression> getConceptSetExpressionById(@PathVariable("id") final int id) {
        SourceInfo sourceInfo = sourceService.getPriorityVocabularySourceInfo();
        if (sourceInfo == null) {
            throw new UnauthorizedException();
        }
        return ok(getConceptSetExpression(id, null, sourceInfo));
    }

    /**
     * Get the concept set expression by identifier and source key
     *
     * @summary Get concept set by ID and source
     * @param id The concept set ID
     * @param sourceKey The source key
     * @return The concept set expression
     */
    @GetMapping("/{id}/expression/{sourceKey}")
    public ResponseEntity<ConceptSetExpression> getConceptSetExpressionByIdAndSource(
            @PathVariable("id") final int id,
            @PathVariable("sourceKey") final String sourceKey) {
        Source source = sourceService.findBySourceKey(sourceKey);
        sourceAccessor.checkAccess(source);
        return ok(getConceptSetExpression(id, null, source.getSourceInfo()));
    }

    private ConceptSetExpression getConceptSetExpression(int id, Integer version, SourceInfo sourceInfo) {
        HashMap<Long, Concept> map = new HashMap<>();

        // create our expression to return
        ConceptSetExpression expression = new ConceptSetExpression();
        ArrayList<ConceptSetExpression.ConceptSetItem> expressionItems = new ArrayList<>();

        List<ConceptSetItem> repositoryItems = new ArrayList<>();
        if (Objects.isNull(version)) {
            getConceptSetItemRepository().findAllByConceptSetId(id).forEach(repositoryItems::add);
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
     * @param name The concept set name
     * @return The concept set expression
     */
    @Deprecated
    @GetMapping("/{id}/{name}/exists")
    public ResponseEntity<Collection<ConceptSet>> getConceptSetExistsDeprecated(
            @PathVariable("id") final int id,
            @PathVariable("name") String name) {
        String warningMessage = "This method will be deprecated in the next release. Instead, please use the new REST endpoint: conceptset/{id}/exists?name={name}";
        Collection<ConceptSet> cs = getConceptSetRepository().conceptSetExists(id, name);
        return ResponseEntity.ok()
                .header("Warning", "299 - " + warningMessage)
                .body(cs);
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
    @GetMapping("/{id}/exists")
    public ResponseEntity<Integer> getCountCSetWithSameName(
            @PathVariable("id") final int id,
            @RequestParam(value = "name", required = false) String name) {
        return ok(getConceptSetRepository().getCountCSetWithSameName(id, name));
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
    @PutMapping("/{id}/items")
    public ResponseEntity<Boolean> saveConceptSetItems(
            @PathVariable("id") final int id,
            @RequestBody ConceptSetItem[] items) {
        getConceptSetItemRepository().deleteByConceptSetId(id);

        for (ConceptSetItem csi : items) {
            // ID must be set to null in case of copying from version, so the new item will be created
            csi.setId(0);
            csi.setConceptSetId(id);
            getConceptSetItemRepository().save(csi);
        }

        return ok(true);
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
    @GetMapping("/exportlist")
    public ResponseEntity<byte[]> exportConceptSetList(
            @RequestParam("conceptsets") final String conceptSetList) throws Exception {
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
        try {
            // Load all of the concept sets requested
            for (int i = 0; i < conceptSetIds.size(); i++) {
                // Get the concept set information
                cs.add(getConceptSetForExport(conceptSetIds.get(i), new SourceInfo(source)));
            }
            // Write Concept Set Expression to a CSV
            baos = ExportUtil.writeConceptSetExportToCSVAndZip(cs);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "conceptSetExport.zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(baos.toByteArray());

        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Exports a single concept set to a comma separated value (CSV)
     * file, compresses to a ZIP file and sends to the client.
     *
     * @param id The concept set ID
     * @return A zip file containing the exported concept set
     * @throws Exception
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportConceptSetToCSV(@PathVariable("id") final String id) throws Exception {
        return this.exportConceptSetList(id);
    }

    /**
     * Save a new concept set to the WebAPI database
     *
     * @summary Create a new concept set
     * @param conceptSetDTO The concept set to save
     * @return The concept set saved with the concept set identifier
     */
    @PostMapping("/")
    @CacheEvict(cacheNames = CONCEPT_SET_LIST_CACHE, allEntries = true)
    public ResponseEntity<ConceptSetDTO> createConceptSet(@RequestBody ConceptSetDTO conceptSetDTO) {
        UserEntity user = getCurrentUserEntity();
        ConceptSet conceptSet = conversionService.convert(conceptSetDTO, ConceptSet.class);
        ConceptSet updated = new ConceptSet();
        updated.setCreatedBy(user);
        updated.setCreatedDate(new Date());
        updated.setTags(null);
        updateConceptSet(updated, conceptSet);
        return ok(conversionService.convert(updated, ConceptSetDTO.class));
    }

    /**
     * Creates a concept set name, based on the selected concept set ID,
     * that is used when generating a copy of an existing concept set. This
     * function is generally used in conjunction with the copy endpoint to
     * create a unique name and then save a copy of an existing concept set.
     *
     * @summary Get concept set name suggestion for copying
     * @param id The concept set ID
     * @return A map of the new concept set name and the existing concept set
     * name
     */
    @GetMapping("/{id}/copy-name")
    public ResponseEntity<Map<String, String>> getNameForCopy(@PathVariable("id") final int id) {
        ConceptSetDTO source = getConceptSet(id).getBody();
        String name = NameUtils.getNameForCopy(source.getName(), this::getNamesLike, getConceptSetRepository().findByName(source.getName()));
        return ok(Collections.singletonMap(COPY_NAME, name));
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
    @PutMapping("/{id}")
    @CacheEvict(cacheNames = CONCEPT_SET_LIST_CACHE, allEntries = true)
    public ResponseEntity<ConceptSetDTO> updateConceptSet(
            @PathVariable("id") final int id,
            @RequestBody ConceptSetDTO conceptSetDTO) throws Exception {
        ConceptSet updated = getConceptSetRepository().findById(id).orElse(null);
        if (updated == null) {
            throw new Exception("Concept Set does not exist.");
        }

        saveVersion(id);

        ConceptSet conceptSet = conversionService.convert(conceptSetDTO, ConceptSet.class);
        return ok(conversionService.convert(updateConceptSet(updated, conceptSet), ConceptSetDTO.class));
    }

    private ConceptSet updateConceptSet(ConceptSet dst, ConceptSet src) {
        UserEntity user = getCurrentUserEntity();
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
        cs.ConceptSetName = this.getConceptSet(conceptSetId).getBody().getName();
        // Get the concept set expression
        cs.csExpression = this.getConceptSetExpressionById(conceptSetId).getBody();

        // Lookup the identifiers
        cs.identifierConcepts = vocabService.executeIncludedConceptLookup(vocabSource.sourceKey, cs.csExpression);
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
    @GetMapping("/{id}/generationinfo")
    public ResponseEntity<Collection<ConceptSetGenerationInfo>> getConceptSetGenerationInfo(@PathVariable("id") final int id) {
        return ok(this.conceptSetGenerationInfoRepository.findAllByConceptSetId(id));
    }

    /**
     * Delete the selected concept set by concept set identifier
     *
     * @summary Delete concept set
     * @param id The concept set ID
     */
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class, noRollbackFor = EmptyResultDataAccessException.class)
    @CacheEvict(cacheNames = CONCEPT_SET_LIST_CACHE, allEntries = true)
    public ResponseEntity<Void> deleteConceptSet(@PathVariable("id") final int id) {
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
            getConceptSetRepository().deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            // Ignore - there may be no data
            log.warn("Failed to delete ConceptSet with ID = {}, {}", id, e);
        }
        catch (Exception e) {
            throw e;
        }

        return ok();
    }

    /**
     * Assign tag to Concept Set
     *
     * @summary Assign concept set tag
     * @since v2.10.0
     * @param id The concept set ID
     * @param tagId The tag ID
     */
    @PostMapping("/{id}/tag/")
    @CacheEvict(cacheNames = CONCEPT_SET_LIST_CACHE, allEntries = true)
    public ResponseEntity<Void> assignTag(
            @PathVariable("id") final Integer id,
            @RequestBody int tagId) {
        ConceptSet entity = getConceptSetRepository().findById(id).orElse(null);
        assignTag(entity, tagId);
        return ok();
    }

    /**
     * Unassign tag from Concept Set
     *
     * @summary Remove tag from concept set
     * @since v2.10.0
     * @param id The concept set ID
     * @param tagId The tag ID
     */
    @DeleteMapping("/{id}/tag/{tagId}")
    @CacheEvict(cacheNames = CONCEPT_SET_LIST_CACHE, allEntries = true)
    public ResponseEntity<Void> unassignTag(
            @PathVariable("id") final Integer id,
            @PathVariable("tagId") final int tagId) {
        ConceptSet entity = getConceptSetRepository().findById(id).orElse(null);
        unassignTag(entity, tagId);
        return ok();
    }

    /**
     * Assign protected tag to Concept Set
     *
     * @summary Assign protected concept set tag
     * @since v2.10.0
     * @param id The concept set ID
     * @param tagId The tag ID
     */
    @PostMapping("/{id}/protectedtag/")
    public ResponseEntity<Void> assignPermissionProtectedTag(
            @PathVariable("id") final int id,
            @RequestBody final int tagId) {
        assignTag(id, tagId);
        return ok();
    }

    /**
     * Unassign protected tag from Concept Set
     *
     * @summary Remove protected concept set tag
     * @since v2.10.0
     * @param id The concept set ID
     * @param tagId The tag ID
     */
    @DeleteMapping("/{id}/protectedtag/{tagId}")
    @CacheEvict(cacheNames = CONCEPT_SET_LIST_CACHE, allEntries = true)
    public ResponseEntity<Void> unassignPermissionProtectedTag(
            @PathVariable("id") final int id,
            @PathVariable("tagId") final int tagId) {
        unassignTag(id, tagId);
        return ok();
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
    @PostMapping("/check")
    public ResponseEntity<CheckResult> runDiagnostics(@RequestBody ConceptSetDTO conceptSetDTO) {
        return ok(new CheckResult(checker.check(conceptSetDTO)));
    }

    /**
     * Get a list of versions of the selected concept set
     *
     * @summary Get concept set version list
     * @since v2.10.0
     * @param id The concept set ID
     * @return A list of version information
     */
    @GetMapping("/{id}/version/")
    public ResponseEntity<List<VersionDTO>> getVersions(@PathVariable("id") final int id) {
        List<VersionBase> versions = versionService.getVersions(VersionType.CONCEPT_SET, id);
        List<VersionDTO> result = versions.stream()
                .map(v -> conversionService.convert(v, VersionDTO.class))
                .collect(Collectors.toList());
        return ok(result);
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
    @GetMapping("/{id}/version/{version}")
    public ResponseEntity<ConceptSetVersionFullDTO> getVersionResponse(
            @PathVariable("id") final int id,
            @PathVariable("version") final int version) {
        return ok(getVersion(id, version));
    }

    private ConceptSetVersionFullDTO getVersion(int id, int version) {
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
    @PutMapping("/{id}/version/{version}")
    public ResponseEntity<VersionDTO> updateVersion(
            @PathVariable("id") final int id,
            @PathVariable("version") final int version,
            @RequestBody VersionUpdateDTO updateDTO) {
        checkVersion(id, version);
        updateDTO.setAssetId(id);
        updateDTO.setVersion(version);
        ConceptSetVersion updated = versionService.update(VersionType.CONCEPT_SET, updateDTO);
        return ok(conversionService.convert(updated, VersionDTO.class));
    }

    /**
     * Delete a version of a concept set
     *
     * @summary Delete a concept set version
     * @since v2.10.0
     * @param id The concept ID
     * @param version The version ID
     */
    @DeleteMapping("/{id}/version/{version}")
    public ResponseEntity<Void> deleteVersion(
            @PathVariable("id") final int id,
            @PathVariable("version") final int version) {
        checkVersion(id, version);
        versionService.delete(VersionType.CONCEPT_SET, id, version);
        return ok();
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
    @PutMapping("/{id}/version/{version}/createAsset")
    @CacheEvict(cacheNames = CONCEPT_SET_LIST_CACHE, allEntries = true)
    public ResponseEntity<ConceptSetDTO> copyAssetFromVersion(
            @PathVariable("id") final int id,
            @PathVariable("version") final int version) {
        checkVersion(id, version, false);
        ConceptSetVersion conceptSetVersion = versionService.getById(VersionType.CONCEPT_SET, id, version);

        ConceptSetVersionFullDTO fullDTO = conversionService.convert(conceptSetVersion, ConceptSetVersionFullDTO.class);
        ConceptSetDTO conceptSetDTO = fullDTO.getEntityDTO();
        // Reset id so it won't be used during saving
        conceptSetDTO.setId(0);
        conceptSetDTO.setTags(null);
        conceptSetDTO.setName(NameUtils.getNameForCopy(conceptSetDTO.getName(), this::getNamesLike, getConceptSetRepository().findByName(conceptSetDTO.getName())));
        ConceptSetDTO createdDTO = createConceptSet(conceptSetDTO).getBody();
        saveConceptSetItems(createdDTO.getId(), fullDTO.getItems().toArray(new ConceptSetItem[0]));

        return ok(createdDTO);
    }

    /**
     * Get list of concept sets with their assigned tags
     *
     * @summary Get concept sets and tag information
     * @param requestDTO The tagNameListRequest
     * @return A list of concept sets with their assigned tags
     */
    @PostMapping("/byTags")
    public ResponseEntity<List<ConceptSetDTO>> listByTags(@RequestBody TagNameListRequestDTO requestDTO) {
        if (requestDTO == null || requestDTO.getNames() == null || requestDTO.getNames().isEmpty()) {
            return ok(Collections.emptyList());
        }
        List<String> names = requestDTO.getNames().stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        List<ConceptSet> entities = getConceptSetRepository().findByTags(names);
        return ok(listByTags(entities, names, ConceptSetDTO.class));
    }

    private void checkVersion(int id, int version) {
        checkVersion(id, version, true);
    }

    private void checkVersion(int id, int version, boolean checkOwnerShip) {
        Version conceptSetVersion = versionService.getById(VersionType.CONCEPT_SET, id, version);
        ExceptionUtils.throwNotFoundExceptionIfNull(conceptSetVersion, String.format("There is no concept set version with id = %d.", version));

        ConceptSet entity = getConceptSetRepository().findById(id).orElse(null);
        if (checkOwnerShip) {
            checkOwnerOrAdminOrGranted(entity);
        }
    }

    private ConceptSetVersion saveVersion(int id) {
        ConceptSet def = getConceptSetRepository().findById(id).orElse(null);
        ConceptSetVersion version = conversionService.convert(def, ConceptSetVersion.class);

        UserEntity user = Objects.nonNull(def.getModifiedBy()) ? def.getModifiedBy() : def.getCreatedBy();
        Date versionDate = Objects.nonNull(def.getModifiedDate()) ? def.getModifiedDate() : def.getCreatedDate();
        version.setCreatedBy(user);
        version.setCreatedDate(versionDate);
        return versionService.create(VersionType.CONCEPT_SET, version);
    }

    /**
     * Update the concept set annotation for each concept in concept set ID in the
     * WebAPI database.
     * <p>
     * The body has two parts: 1) the elements new concept which added to the
     * concept set. 2) the elements concept which remove from concept set.
     *
     * @param conceptSetId  The concept set ID
     * @param request An object of 2 Array new annotation and remove annotation
     * @return Boolean: true if the save is successful
     * @summary Create new or delete concept set annotation items
     */
    @PutMapping("/{id}/annotation")
    public ResponseEntity<Boolean> saveConceptSetAnnotation(
            @PathVariable("id") final int conceptSetId,
            @RequestBody SaveConceptSetAnnotationsRequest request) {
        removeAnnotations(conceptSetId, request);
        if (request.getNewAnnotation() != null && !request.getNewAnnotation().isEmpty()) {
            List<ConceptSetAnnotation> annotationList = request.getNewAnnotation()
                    .stream()
                    .map(newAnnotationData -> {
                        ConceptSetAnnotation conceptSetAnnotation = new ConceptSetAnnotation();
                        conceptSetAnnotation.setConceptSetId(conceptSetId);
                        try {
                            AnnotationDetailsDTO annotationDetailsDTO = new AnnotationDetailsDTO();
                            annotationDetailsDTO.setId(newAnnotationData.getId());
                            annotationDetailsDTO.setConceptId(newAnnotationData.getConceptId());
                            annotationDetailsDTO.setSearchData(newAnnotationData.getSearchData());
                            conceptSetAnnotation.setAnnotationDetails(mapper.writeValueAsString(annotationDetailsDTO));
                        } catch (JsonProcessingException e) {
                            log.error("Could not serialize Concept Set AnnotationDetailsDTO", e);
                            throw new RuntimeException(e);
                        }
                        conceptSetAnnotation.setVocabularyVersion(newAnnotationData.getVocabularyVersion());
                        conceptSetAnnotation.setConceptSetVersion(newAnnotationData.getConceptSetVersion());
                        conceptSetAnnotation.setConceptId(newAnnotationData.getConceptId());
                        conceptSetAnnotation.setCreatedBy(getCurrentUserEntity());
                        conceptSetAnnotation.setCreatedDate(new Date());
                        return conceptSetAnnotation;
                    }).collect(Collectors.toList());

            this.getConceptSetAnnotationRepository().saveAll(annotationList);
        }

        return ok(true);
    }

    private void removeAnnotations(int id, SaveConceptSetAnnotationsRequest request) {
        if (request.getRemoveAnnotation() != null && !request.getRemoveAnnotation().isEmpty()) {
            for (AnnotationDTO annotationDTO : request.getRemoveAnnotation()) {
                this.getConceptSetAnnotationRepository().deleteAnnotationByConceptSetIdAndConceptId(id, annotationDTO.getConceptId());
            }
        }
    }

    @PostMapping("/copy-annotations")
    public ResponseEntity<Void> copyAnnotations(@RequestBody CopyAnnotationsRequest copyAnnotationsRequest) {
        List<ConceptSetAnnotation> sourceAnnotations = getConceptSetAnnotationRepository().findByConceptSetId(copyAnnotationsRequest.getSourceConceptSetId());
        List<ConceptSetAnnotation> copiedAnnotations= sourceAnnotations.stream()
                .map(sourceAnnotation -> copyAnnotation(sourceAnnotation, copyAnnotationsRequest.getSourceConceptSetId(), copyAnnotationsRequest.getTargetConceptSetId()))
                .collect(Collectors.toList());
        getConceptSetAnnotationRepository().saveAll(copiedAnnotations);
        return ok();
    }

    private ConceptSetAnnotation copyAnnotation(ConceptSetAnnotation sourceConceptSetAnnotation, int sourceConceptSetId, int targetConceptSetId) {
        ConceptSetAnnotation targetConceptSetAnnotation = new ConceptSetAnnotation();
        targetConceptSetAnnotation.setConceptSetId(targetConceptSetId);
        targetConceptSetAnnotation.setConceptSetVersion(sourceConceptSetAnnotation.getConceptSetVersion());
        targetConceptSetAnnotation.setAnnotationDetails(sourceConceptSetAnnotation.getAnnotationDetails());
        targetConceptSetAnnotation.setConceptId(sourceConceptSetAnnotation.getConceptId());
        targetConceptSetAnnotation.setVocabularyVersion(sourceConceptSetAnnotation.getVocabularyVersion());
        targetConceptSetAnnotation.setCreatedBy(sourceConceptSetAnnotation.getCreatedBy());
        targetConceptSetAnnotation.setCreatedDate(sourceConceptSetAnnotation.getCreatedDate());
        targetConceptSetAnnotation.setModifiedBy(sourceConceptSetAnnotation.getModifiedBy());
        targetConceptSetAnnotation.setModifiedDate(sourceConceptSetAnnotation.getModifiedDate());
        targetConceptSetAnnotation.setCopiedFromConceptSetIds(appendCopiedFromConceptSetId(sourceConceptSetAnnotation.getCopiedFromConceptSetIds(), sourceConceptSetId));
        return targetConceptSetAnnotation;
    }

    private String appendCopiedFromConceptSetId(String copiedFromConceptSetIds, int sourceConceptSetId) {
        if(copiedFromConceptSetIds == null || copiedFromConceptSetIds.isEmpty()){
            return Integer.toString(sourceConceptSetId);
        }
        return copiedFromConceptSetIds.concat(",").concat(Integer.toString(sourceConceptSetId));
    }

    @GetMapping("/{id}/annotation")
    public ResponseEntity<List<AnnotationDTO>> getConceptSetAnnotation(@PathVariable("id") final int id) {
        List<ConceptSetAnnotation> annotationList = getConceptSetAnnotationRepository().findByConceptSetId(id);
        List<AnnotationDTO> result = annotationList.stream()
                .map(this::convertAnnotationEntityToDTO)
                .collect(Collectors.toList());
        return ok(result);
    }

    private AnnotationDTO convertAnnotationEntityToDTO(ConceptSetAnnotation conceptSetAnnotation) {
        AnnotationDetailsDTO annotationDetails;
        try {
            annotationDetails = mapper.readValue(conceptSetAnnotation.getAnnotationDetails(), AnnotationDetailsDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Could not deserialize Concept Set AnnotationDetailsDTO", e);
            throw new RuntimeException(e);
        }

        AnnotationDTO annotationDTO = new AnnotationDTO();

        annotationDTO.setId(conceptSetAnnotation.getId());
        annotationDTO.setConceptId(conceptSetAnnotation.getConceptId());

        String searchDataJSON = annotationDetails.getSearchData();
        String humanReadableData = searchDataTransformer.convertJsonToReadableFormat(searchDataJSON);
        annotationDTO.setSearchData(humanReadableData);

        annotationDTO.setVocabularyVersion(conceptSetAnnotation.getVocabularyVersion());
        annotationDTO.setConceptSetVersion(conceptSetAnnotation.getConceptSetVersion());
        annotationDTO.setCopiedFromConceptSetIds(conceptSetAnnotation.getCopiedFromConceptSetIds());
        annotationDTO.setCreatedBy(conceptSetAnnotation.getCreatedBy() != null ? conceptSetAnnotation.getCreatedBy().getName() : null);
        annotationDTO.setCreatedDate(conceptSetAnnotation.getCreatedDate() != null ? conceptSetAnnotation.getCreatedDate().toString() : null);
        return annotationDTO;
    }

    @DeleteMapping("/{conceptSetId}/annotation/{annotationId}")
    public ResponseEntity<Void> deleteConceptSetAnnotation(
            @PathVariable("conceptSetId") final int conceptSetId,
            @PathVariable("annotationId") final int annotationId) {
        ConceptSetAnnotation conceptSetAnnotation = getConceptSetAnnotationRepository().findById(annotationId);
        if (conceptSetAnnotation != null) {
            getConceptSetAnnotationRepository().deleteById(annotationId);
            return ok();
        } else {
            return notFound();
        }
    }
}
