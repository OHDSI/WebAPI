package org.ohdsi.webapi.source;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.common.DBMSType;
import org.ohdsi.webapi.common.SourceMapKey;
import org.ohdsi.webapi.exception.SourceDuplicateKeyException;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.authz.RoleEntity;
import org.ohdsi.webapi.security.authz.UserEntity;
import org.ohdsi.webapi.security.authz.access.AccessType;
import org.ohdsi.webapi.security.authz.access.EntityType;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.VocabularyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.PersistenceException;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import javax.cache.CacheManager;
import org.springframework.beans.factory.ObjectProvider;
import javax.cache.configuration.MutableConfiguration;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.ohdsi.webapi.util.CacheHelper;

@RestController
@RequestMapping("/source")
@Transactional
public class SourceService extends AbstractDaoService {

    public static final String SECURE_MODE_ERROR = "This feature requires the administrator to enable security for the application";

    @Component
    public static class CachingSetup implements JCacheManagerCustomizer {

        public static final String SOURCE_LIST_CACHE = "sourceList";

        @Override
        public void customize(CacheManager cacheManager) {
            // Evict when a cohort definition is created or updated, or permissions, or tags
            if (!CacheHelper.getCacheNames(cacheManager).contains(SOURCE_LIST_CACHE)) {
                cacheManager.createCache(SOURCE_LIST_CACHE, new MutableConfiguration<Object, List<Source>>()
                    .setTypes(Object.class, (Class<List<Source>>) (Class<?>) List.class)
                    .setStoreByValue(false)
                    .setStatisticsEnabled(true));
            }
        }
    }

    @Value("${jasypt.encryptor.enabled}")
    private boolean encryptorEnabled;

    @Value("${datasource.ohdsi.schema}")
    private String schema;

    private Map<Source, Boolean> connectionAvailability = Collections.synchronizedMap(new PassiveExpiringMap<>(5000));

    private final SourceRepository sourceRepository;
    private final SourceDaimonRepository sourceDaimonRepository;
    private final AuthorizationService authorizationService;
    private final JdbcTemplate jdbcTemplate;
    private final PBEStringEncryptor defaultStringEncryptor;
    
    private final GenericConversionService conversionService;
    private final ObjectProvider<VocabularyService> vocabularyServiceProvider;

    public SourceService(SourceRepository sourceRepository,
                         SourceDaimonRepository sourceDaimonRepository,
                         AuthorizationService authorizationService,
                         JdbcTemplate jdbcTemplate,
                         PBEStringEncryptor defaultStringEncryptor,
                         GenericConversionService conversionService,
                         ObjectProvider<VocabularyService> vocabularyServiceProvider) {
        this.sourceRepository = sourceRepository;
        this.sourceDaimonRepository = sourceDaimonRepository;
        this.authorizationService = authorizationService;
        this.jdbcTemplate = jdbcTemplate;
        this.defaultStringEncryptor = defaultStringEncryptor;
        this.conversionService = conversionService;
        this.vocabularyServiceProvider = vocabularyServiceProvider;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void postConstruct() {
        ensureSourceEncrypted();
    }

    public void ensureSourceEncrypted() {
        if (encryptorEnabled) {
            String query = "SELECT source_id, username, password FROM ${schema}.source".replaceAll("\\$\\{schema\\}", schema);
            String update = "UPDATE ${schema}.source SET username = ?, password = ? WHERE source_id = ?".replaceAll("\\$\\{schema\\}", schema);
            getTransactionTemplateRequiresNew().execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    jdbcTemplate.query(query, rs -> {
                        int id = rs.getInt("source_id");
                        String username = rs.getString("username");
                        String password = rs.getString("password");
                        if (username != null && !PropertyValueEncryptionUtils.isEncryptedValue(username)) {
                            username = "ENC(" + defaultStringEncryptor.encrypt(username) + ")";
                        }
                        if (password != null && !PropertyValueEncryptionUtils.isEncryptedValue(password)) {
                            password = "ENC(" + defaultStringEncryptor.encrypt(password) + ")";
                        }
                        jdbcTemplate.update(update, username, password, id);
                    });
                }
            });
        }
    }

    // ==================== REST Endpoints ====================

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
    @PreAuthorize("isAnyPermitted(anyOf('read:source','write:source'))")
    public ResponseEntity<Collection<SourceInfo>> getSourcesEndpoint() {
        return ResponseEntity.ok(getSources().stream().map(SourceInfo::new).collect(Collectors.toList()));
    }


    /**
     * Refresh cached CDM database metadata
     *
     * @summary Refresh Sources
     * @return A list of all CDM sources with the ID, name, SQL dialect, and key
     * for each source (same as the 'sources' endpoint) after refreshing the cached sourced data.
     */
    @GetMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isPermitted('admin:source')")
    public ResponseEntity<Collection<SourceInfo>> refreshSources() {
        invalidateCache();
        vocabularyServiceProvider.getObject().clearVocabularyInfoCache();
        ensureSourceEncrypted();
        return getSourcesEndpoint();
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
    @PreAuthorize("isAnyPermitted(anyOf('read:source','write:source'))")
    public ResponseEntity<SourceInfo> getPriorityVocabularySourceInfoEndpoint() {
        return ResponseEntity.ok(getPriorityVocabularySourceInfo());
    }

    /**
     * Get source by key
     * @summary Get Source By Key
     * @param sourceKey
     * @return  Metadata for a single Source that matches the <code>sourceKey</code>.
     */
    @GetMapping(value = "/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAnyPermitted(anyOf('read:source','write:source'))")
    public ResponseEntity<SourceInfo> getSource(@PathVariable("key") final String sourceKey) {
        return ResponseEntity.ok(sourceRepository.findBySourceKey(sourceKey).getSourceInfo());
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
    @PreAuthorize("isPermitted('admin:source')")
    public ResponseEntity<SourceDetails> getSourceDetails(@PathVariable("sourceId") Integer sourceId) {
        Source source = sourceRepository.findBySourceId(sourceId);
        return ResponseEntity.ok(new SourceDetails(source));
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
    @CacheEvict(cacheNames = CachingSetup.SOURCE_LIST_CACHE, allEntries = true)
    @PreAuthorize("isPermitted('admin:source')")
    public ResponseEntity<SourceInfo> createSource(
            @RequestPart(value = "keyfile", required = false) MultipartFile keyfile,
            @RequestPart("source") SourceRequest source) throws Exception {
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
            // Sources have a role to grant write access to the source
            AuthorizationService authSvc = this.getAuthorizationService();
            RoleEntity sourceRole = authSvc.addRole(getSourceRoleName(saved.getSourceKey()), true);
            // we are going to default giving this role 'WRITE', but could possibly define a read-only and a writeable roles in the future.
            authSvc.grantEntityAccess(EntityType.SOURCE, Long.valueOf(saved.getId().longValue()),sourceRole.getId(), AccessType.WRITE);
            invalidateCache();
            SourceInfo sourceInfo = new SourceInfo(saved);
            return ResponseEntity.ok(sourceInfo);
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
    @CacheEvict(cacheNames = CachingSetup.SOURCE_LIST_CACHE, allEntries = true)
    @PreAuthorize("isPermitted('admin:source')")
    public ResponseEntity<SourceInfo> updateSource(
            @PathVariable("sourceId") Integer sourceId,
            @RequestPart(value = "keyfile", required = false) MultipartFile keyfile,
            @RequestPart("source") SourceRequest source) throws IOException {
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
            invalidateCache();
            return ResponseEntity.ok(new SourceInfo(result));
        } else {
            return ResponseEntity.notFound().build();
        }
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
    @CacheEvict(cacheNames = CachingSetup.SOURCE_LIST_CACHE, allEntries = true)
    @PreAuthorize("isPermitted('admin:source')")
    public ResponseEntity<Void> delete(@PathVariable("sourceId") Integer sourceId) throws Exception {

        Source source = sourceRepository.findBySourceId(sourceId);
        if (source != null) {
            sourceRepository.delete(source);
            // TODO: Deletes are 'soft-delete' so need to determine how to clean up the source's role.
            invalidateCache();
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
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
    @PreAuthorize("isPermitted('admin:source') or hasSourceAccess(#sourceKey, READ)")
    public ResponseEntity<SourceInfo> checkConnectionEndpoint(@PathVariable("key") final String sourceKey) {
        final Source source = findBySourceKey(sourceKey);
        checkConnection(source);
        return ResponseEntity.ok(source.getSourceInfo());
    }

    /**
     * Get the first daimon (and associated source) that has priority. In the event
     * of a tie, the first source searched wins.
     *
     * @summary Get Priority Daimons
     * @return
     */
    @GetMapping(value = "/daimon/priority", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAnyPermitted(anyOf('read:source','write:source'))")
    public ResponseEntity<Map<SourceDaimon.DaimonType, SourceInfo>> getPriorityDaimonsEndpoint() {
        return ResponseEntity.ok(getPriorityDaimons()
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
    @CacheEvict(cacheNames = CachingSetup.SOURCE_LIST_CACHE, allEntries = true)
    @PreAuthorize("isPermitted('admin:source')")
    public ResponseEntity<Void> updateSourcePriority(
            @PathVariable("sourceKey") final String sourceKey,
            @PathVariable("daimonType") final String daimonTypeName) {

        SourceDaimon.DaimonType daimonType = SourceDaimon.DaimonType.valueOf(daimonTypeName);
        List<SourceDaimon> daimonList = sourceDaimonRepository.findByDaimonType(daimonType);
        daimonList.forEach(daimon -> {
            Integer newPriority = daimon.getSource().getSourceKey().equals(sourceKey) ? 1 : 0;
            daimon.setPriority(newPriority);
            sourceDaimonRepository.save(daimon);
        });
        invalidateCache();
        return ResponseEntity.ok().build();
    }

    // ==================== Service Methods ====================

    @Cacheable(cacheNames = CachingSetup.SOURCE_LIST_CACHE)
    public Collection<Source> getSources() {
        List<Source> sources = sourceRepository.findAll();
        Collections.sort(sources, new SortByKey());
        return sources;
    }

    public Source findBySourceKey(final String sourceKey) {
        return sourceRepository.findBySourceKey(sourceKey);
    }

    public Source findBySourceId(final Integer sourceId) {
        return sourceRepository.findBySourceId(sourceId);
    }

    public <T> Map<T, Source> getSourcesMap(SourceMapKey<T> mapKey) {
        return getSources().stream().collect(Collectors.toMap(mapKey.getKeyFunc(), s -> s));
    }

    public void checkConnection(Source source) {
        if (source.isCheckConnection()) {
            forceCheckConnection(source);
        }
    }

    /**
     * Force a connection check regardless of the source's checkConnection flag.
     * Used when explicitly testing connection via API endpoint.
     */
    public void forceCheckConnection(Source source) {
        final JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
        jdbcTemplate.execute(SqlTranslate.translateSql("select 1;", source.getSourceDialect()).replaceAll(";$", ""));
    }

    public Source getPrioritySourceForDaimon(SourceDaimon.DaimonType daimonType) {
        List<Source> sourcesByDaimonPriority = sourceRepository.findAllSortedByDiamonPrioirty(daimonType);

        for (Source source : sourcesByDaimonPriority) {
            if (!(true && connectionAvailability.computeIfAbsent(source, this::checkConnectionSafe))) {
                continue;
            }
            return source;
        }

        return null;
    }

    public Map<SourceDaimon.DaimonType, Source> getPriorityDaimons() {
        class SourceValidator {
            private Map<Integer, Boolean> checkedSources = new HashMap<>();

            private boolean isSourceAvaialble(Source source) {
                return checkedSources.computeIfAbsent(source.getSourceId(),
                        v -> true && connectionAvailability.computeIfAbsent(source, SourceService.this::checkConnectionSafe));
            }
        }

        SourceValidator sourceValidator = new SourceValidator();
        Map<SourceDaimon.DaimonType, Source> priorityDaimons = new HashMap<>();
        Arrays.asList(SourceDaimon.DaimonType.values()).forEach(d -> {
            List<Source> sources = sourceRepository.findAllSortedByDiamonPrioirty(d);
            Optional<Source> source = sources.stream().filter(sourceValidator::isSourceAvaialble)
                    .findFirst();
            source.ifPresent(s -> priorityDaimons.put(d, s));
        });
        return priorityDaimons;
    }

    public Source getPriorityVocabularySource() {
        return getPrioritySourceForDaimon(SourceDaimon.DaimonType.Vocabulary);
    }

    public SourceInfo getPriorityVocabularySourceInfo() {
        Source source = getPrioritySourceForDaimon(SourceDaimon.DaimonType.Vocabulary);
        if (source == null) {
            return null;
        }
        return new SourceInfo(source);
    }

    @CacheEvict(cacheNames = CachingSetup.SOURCE_LIST_CACHE, allEntries = true)
    public void invalidateCache() {
    }

    // ==================== Private Helper Methods ====================

    protected UserEntity getCurrentUserEntity() {
        return userRepository.findById(authorizationService.getAuthenticatedPrincipal().getUserId()).orElseThrow();
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

    private boolean checkConnectionSafe(Source source) {
        try {
            checkConnection(source);
            return true;
        } catch (CannotGetJdbcConnectionException ex) {
            return false;
        }
    }

    private String getSourceRoleName(String sourceKey) {
        return String.format("Source user (%s)", sourceKey);
    }

    private class SortByKey implements Comparator<Source> {
        private boolean isAscending;

        public SortByKey(boolean ascending) {
            isAscending = ascending;
        }

        public SortByKey() {
            this(true);
        }

        public int compare(Source s1, Source s2) {
            return s1.getSourceKey().compareTo(s2.getSourceKey()) * (isAscending ? 1 : -1);
        }
    }
}
