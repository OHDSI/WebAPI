package org.ohdsi.webapi.mvc.controller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.arachne.logging.event.AddDataSourceEvent;
import org.ohdsi.webapi.arachne.logging.event.ChangeDataSourceEvent;
import org.ohdsi.webapi.arachne.logging.event.DeleteDataSourceEvent;
import org.ohdsi.webapi.common.DBMSType;
import org.ohdsi.webapi.exception.SourceDuplicateKeyException;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.source.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.PersistenceException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring MVC version of SourceController
 *
 * Migration Status: Replaces /source/SourceController.java (Jersey)
 * Endpoints: 10 endpoints (3 GET, 2 POST, 1 PUT, 1 DELETE)
 * Special: Multipart file upload endpoints for keyfile handling
 */
@RestController
@RequestMapping("/source")
@Transactional
public class SourceMvcController extends AbstractMvcController {

    public static final String SECURE_MODE_ERROR = "This feature requires the administrator to enable security for the application";

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private SourceDaimonRepository sourceDaimonRepository;

    @Autowired
    private GenericConversionService conversionService;

    @Autowired
    private UserRepository userRepository;

    @Value("#{!'${security.provider}'.equals('DisabledSecurity')}")
    private boolean securityEnabled;

    protected UserEntity getCurrentUserEntity() {
        return userRepository.findByLogin(security.getSubject());
    }

    /**
     * Gets the list of all Sources in WebAPI database. Sources with a non-null
     * deleted_date are not returned (ie: these are soft deleted)
     *
     * @summary Get Sources
     * @return A list of all CDM sources with the ID, name, SQL dialect, and key
     * for each source. The {sourceKey} is used in other WebAPI endpoints to
     * identify CDMs.
     */
    @GetMapping(value = "/sources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<SourceInfo>> getSources() {
        return ok(sourceService.getSources().stream().map(SourceInfo::new).collect(Collectors.toList()));
    }

    /**
     * Refresh cached CDM database metadata
     *
     * @summary Refresh Sources
     * @return A list of all CDM sources with the ID, name, SQL dialect, and key
     * for each source (same as the 'sources' endpoint) after refreshing the cached sourced data.
     */
    @GetMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<SourceInfo>> refreshSources() {
        sourceService.invalidateCache();
        vocabularyService.clearVocabularyInfoCache();
        sourceService.ensureSourceEncrypted();
        return getSources();
    }

    /**
     * Get the priority vocabulary source.
     *
     * WebAPI designates one CDM vocabulary as the priority vocabulary to be used for vocabulary searches in Atlas.
     *
     * @summary Get Priority Vocabulary Source
     * @return The CDM metadata for the priority vocabulary.
     */
    @GetMapping(value = "/priorityVocabulary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SourceInfo> getPriorityVocabularySourceInfo() {
        return ok(sourceService.getPriorityVocabularySourceInfo());
    }

    /**
     * Get source by key
     * @summary Get Source By Key
     * @param sourceKey
     * @return  Metadata for a single Source that matches the <code>sourceKey</code>.
     */
    @GetMapping(value = "/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SourceInfo> getSource(@PathVariable("key") final String sourceKey) {
        return ok(sourceRepository.findBySourceKey(sourceKey).getSourceInfo());
    }

    /**
     * Get Source Details
     *
     * Source Details contains connection-specific information like JDBC url and authentication information.
     *
     * @summary Get Source Details
     * @param sourceId
     * @return
     */
    @GetMapping(value = "/details/{sourceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SourceDetails> getSourceDetails(@PathVariable("sourceId") Integer sourceId) {
        if (!securityEnabled) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, SECURE_MODE_ERROR);
        }
        Source source = sourceRepository.findBySourceId(sourceId);
        return ok(new SourceDetails(source));
    }

    /**
     * Create a Source
     *
     * @summary Create Source
     * @param keyfile the keyfile
     * @param source contains the source information (name, key, etc)
     * @return a new SourceInfo for the created source
     * @throws Exception
     */
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheEvict(cacheNames = SourceService.CachingSetup.SOURCE_LIST_CACHE, allEntries = true)
    public ResponseEntity<SourceInfo> createSource(
            @RequestPart(value = "keyfile", required = false) MultipartFile keyfile,
            @RequestPart("source") SourceRequest source) throws Exception {
        if (!securityEnabled) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, SECURE_MODE_ERROR);
        }
        Source sourceByKey = sourceRepository.findBySourceKey(source.getKey());
        if (Objects.nonNull(sourceByKey)) {
            throw new SourceDuplicateKeyException("The source key has been already used.");
        }
        Source sourceEntity = conversionService.convert(source, Source.class);
        if (sourceEntity.getDaimons() != null) {
            // First source should get priority = 1
            Iterable<Source> sources = sourceRepository.findAll();
            sourceEntity.getDaimons()
                    .stream()
                    .filter(sd -> sd.getPriority() <= 0)
                    .filter(sd -> {
                        boolean accept = true;
                        // Check if source daimon of given type with priority > 0 already exists in other sources
                        for (Source innerSource : sources) {
                            accept = !innerSource.getDaimons()
                                    .stream()
                                    .anyMatch(innerDaimon -> innerDaimon.getPriority() > 0
                                            && innerDaimon.getDaimonType().equals(sd.getDaimonType()));
                            if (!accept) {
                                break;
                            }
                        }
                        return accept;
                    })
                    .forEach(sd -> sd.setPriority(1));
        }
        Source original = new Source();
        original.setSourceDialect(sourceEntity.getSourceDialect());
        setKeyfileData(sourceEntity, original, keyfile);
        sourceEntity.setCreatedBy(getCurrentUserEntity());
        sourceEntity.setCreatedDate(new Date());
        try {
            Source saved = sourceRepository.saveAndFlush(sourceEntity);
            sourceService.invalidateCache();
            SourceInfo sourceInfo = new SourceInfo(saved);
            publisher.publishEvent(new AddDataSourceEvent(this, sourceEntity.getSourceId(), sourceEntity.getSourceName()));
            return ok(sourceInfo);
        } catch (PersistenceException ex) {
            throw new SourceDuplicateKeyException("You cannot use this Source Key, please use different one");
        }
    }

    /**
     * Updates a Source with the provided details from multiple files
     *
     * @summary Update Source
     * @param sourceId
     * @param keyfile the keyfile
     * @param source contains the source information (name, key, etc)
     * @return the updated SourceInfo for the source
     * @throws Exception
     */
    @PutMapping(value = "/{sourceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @CacheEvict(cacheNames = SourceService.CachingSetup.SOURCE_LIST_CACHE, allEntries = true)
    public ResponseEntity<SourceInfo> updateSource(
            @PathVariable("sourceId") Integer sourceId,
            @RequestPart(value = "keyfile", required = false) MultipartFile keyfile,
            @RequestPart("source") SourceRequest source) throws IOException {
        if (!securityEnabled) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, SECURE_MODE_ERROR);
        }
        Source updated = conversionService.convert(source, Source.class);
        Source existingSource = sourceRepository.findBySourceId(sourceId);
        if (existingSource != null) {
            updated.setSourceId(sourceId);
            updated.setSourceKey(existingSource.getSourceKey());
            if (StringUtils.isBlank(updated.getUsername()) ||
                    Objects.equals(updated.getUsername().trim(), Source.MASQUERADED_USERNAME)) {
                updated.setUsername(existingSource.getUsername());
            }
            if (StringUtils.isBlank(updated.getPassword()) ||
                    Objects.equals(updated.getPassword().trim(), Source.MASQUERADED_PASSWORD)) {
                updated.setPassword(existingSource.getPassword());
            }
            setKeyfileData(updated, existingSource, keyfile);
            transformIfRequired(updated);
            if (source.isCheckConnection() == null) {
                updated.setCheckConnection(existingSource.isCheckConnection());
            }
            updated.setModifiedBy(getCurrentUserEntity());
            updated.setModifiedDate(new Date());

            reuseDeletedDaimons(updated, existingSource);

            List<SourceDaimon> removed = existingSource.getDaimons().stream()
                    .filter(d -> !updated.getDaimons().contains(d))
                    .collect(Collectors.toList());
            // Delete MUST be called after fetching user or source data to prevent autoflush (see DefaultPersistEventListener.onPersist)
            sourceDaimonRepository.deleteAll(removed);
            Source result = sourceRepository.save(updated);
            publisher.publishEvent(new ChangeDataSourceEvent(this, updated.getSourceId(), updated.getSourceName()));
            sourceService.invalidateCache();
            return ok(new SourceInfo(result));
        } else {
            return notFound();
        }
    }

    private void reuseDeletedDaimons(Source updated, Source source) {
        List<SourceDaimon> daimons = updated.getDaimons().stream().filter(d -> source.getDaimons().contains(d))
                .collect(Collectors.toList());
        List<SourceDaimon> newDaimons = updated.getDaimons().stream().filter(d -> !source.getDaimons().contains(d))
                .collect(Collectors.toList());

        List<SourceDaimon> allDaimons = sourceDaimonRepository.findBySource(source);

        for (SourceDaimon newSourceDaimon : newDaimons) {
            Optional<SourceDaimon> reusedDaimonOpt = allDaimons.stream()
                    .filter(d -> d.equals(newSourceDaimon))
                    .findFirst();
            if (reusedDaimonOpt.isPresent()) {
                SourceDaimon reusedDaimon = reusedDaimonOpt.get();
                reusedDaimon.setPriority(newSourceDaimon.getPriority());
                reusedDaimon.setTableQualifier(newSourceDaimon.getTableQualifier());
                daimons.add(reusedDaimon);
            } else {
                daimons.add(newSourceDaimon);
            }
        }
        updated.setDaimons(daimons);
    }

    private void transformIfRequired(Source source) {
        if (DBMSType.BIGQUERY.getOhdsiDB().equals(source.getSourceDialect()) && ArrayUtils.isNotEmpty(source.getKeyfile())) {
            String connStr = source.getSourceConnection().replaceAll("OAuthPvtKeyPath=.+?(;|\\z)", "");
            source.setSourceConnection(connStr);
        }
    }

    private void setKeyfileData(Source updated, Source source, MultipartFile file) throws IOException {
        if (source.supportsKeyfile()) {
            if (updated.getKeyfileName() != null) {
                if (!Objects.equals(updated.getKeyfileName(), source.getKeyfileName())) {
                    byte[] fileBytes = file != null ? file.getBytes() : new byte[0];
                    updated.setKeyfile(fileBytes);
                } else {
                    updated.setKeyfile(source.getKeyfile());
                }
                return;
            }
        }
        updated.setKeyfile(null);
        updated.setKeyfileName(null);
    }

    /**
     * Delete a source.
     *
     * @summary Delete Source
     * @param sourceId
     * @return
     * @throws Exception
     */
    @DeleteMapping("/{sourceId}")
    @Transactional
    @CacheEvict(cacheNames = SourceService.CachingSetup.SOURCE_LIST_CACHE, allEntries = true)
    public ResponseEntity<Void> delete(@PathVariable("sourceId") Integer sourceId) throws Exception {
        if (!securityEnabled) {
            return unauthorized();
        }
        Source source = sourceRepository.findBySourceId(sourceId);
        if (source != null) {
            sourceRepository.delete(source);
            publisher.publishEvent(new DeleteDataSourceEvent(this, sourceId, source.getSourceName()));
            sourceService.invalidateCache();
            return ok();
        } else {
            return notFound();
        }
    }

    /**
     * Check source connection.
     *
     * This method attempts to connect to the source by calling 'select 1' on the source connection.
     * @summary Check connection
     * @param sourceKey
     * @return
     */
    @GetMapping(value = "/connection/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(noRollbackFor = CannotGetJdbcConnectionException.class)
    public ResponseEntity<SourceInfo> checkConnection(@PathVariable("key") final String sourceKey) {
        final Source source = sourceService.findBySourceKey(sourceKey);
        sourceService.checkConnection(source);
        return ok(source.getSourceInfo());
    }

    /**
     * Get the first daimon (ad associated source) that has priority. In the event
     * of a tie, the first source searched wins.
     *
     * @summary Get Priority Daimons
     * @return
     */
    @GetMapping(value = "/daimon/priority", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<SourceDaimon.DaimonType, SourceInfo>> getPriorityDaimons() {
        return ok(sourceService.getPriorityDaimons()
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new SourceInfo(e.getValue())
                )));
    }

    /**
     * Set priority of daimon
     *
     * Set the priority of the specified daimon of the specified source, and set the other daimons to 0.
     * @summary Set Priority
     * @param sourceKey
     * @param daimonTypeName
     * @return
     */
    @PostMapping(value = "/{sourceKey}/daimons/{daimonType}/set-priority", produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheEvict(cacheNames = SourceService.CachingSetup.SOURCE_LIST_CACHE, allEntries = true)
    public ResponseEntity<Void> updateSourcePriority(
            @PathVariable("sourceKey") final String sourceKey,
            @PathVariable("daimonType") final String daimonTypeName) {
        if (!securityEnabled) {
            return unauthorized();
        }
        SourceDaimon.DaimonType daimonType = SourceDaimon.DaimonType.valueOf(daimonTypeName);
        List<SourceDaimon> daimonList = sourceDaimonRepository.findByDaimonType(daimonType);
        daimonList.forEach(daimon -> {
            Integer newPriority = daimon.getSource().getSourceKey().equals(sourceKey) ? 1 : 0;
            daimon.setPriority(newPriority);
            sourceDaimonRepository.save(daimon);
        });
        sourceService.invalidateCache();
        return ok();
    }
}
