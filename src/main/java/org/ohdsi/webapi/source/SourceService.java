package org.ohdsi.webapi.source;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.common.SourceMapKey;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.shiro.management.datasource.SourceAccessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import org.ohdsi.webapi.util.CacheHelper;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Service
public class SourceService extends AbstractDaoService {

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
    private final JdbcTemplate jdbcTemplate;
    private PBEStringEncryptor defaultStringEncryptor;
    private SourceAccessor sourceAccessor;

    public SourceService(SourceRepository sourceRepository, JdbcTemplate jdbcTemplate, PBEStringEncryptor defaultStringEncryptor, SourceAccessor sourceAccessor) {

        this.sourceRepository = sourceRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.defaultStringEncryptor = defaultStringEncryptor;
        this.sourceAccessor = sourceAccessor;
    }

    @PostConstruct
    private void postConstruct() {

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
            final JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
            jdbcTemplate.execute(SqlTranslate.translateSql("select 1;", source.getSourceDialect()).replaceAll(";$", ""));
        }
    }

    public Source getPrioritySourceForDaimon(SourceDaimon.DaimonType daimonType) {

        List<Source> sourcesByDaimonPriority = sourceRepository.findAllSortedByDiamonPrioirty(daimonType);

        for (Source source : sourcesByDaimonPriority) {
            if (!(sourceAccessor.hasAccess(source) && connectionAvailability.computeIfAbsent(source, this::checkConnectionSafe))) {
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
                        v -> sourceAccessor.hasAccess(source) && connectionAvailability.computeIfAbsent(source, SourceService.this::checkConnectionSafe));
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

    private boolean checkConnectionSafe(Source source) {

        try {
            checkConnection(source);
            return true;
        } catch (CannotGetJdbcConnectionException ex) {
            return false;
        }
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
