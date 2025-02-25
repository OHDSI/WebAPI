package org.ohdsi.webapi.service;

import java.util.Arrays;
import java.util.List;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.management.CacheStatisticsMXBean;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.springframework.beans.factory.annotation.Autowired;

import org.ohdsi.webapi.util.CacheHelper;

public class CohortDefinitionServiceTest extends AbstractDatabaseTest {

	@Autowired
	private CohortDefinitionService cdService;
	@Autowired
	protected CohortDefinitionRepository cdRepository;
	@Autowired(required = false)
	private CacheManager cacheManager	;
  @Autowired
  private DefaultWebSecurityManager securityManager;

	// in JUnit 4 it's impossible to mark methods inside interface with annotations, it was implemented in JUnit 5. After upgrade it's needed
	// to mark interface methods with @Test, @Before, @After and to remove them from this class
	@After
	public void tearDownDB() {
		cdRepository.deleteAll();
	}

	@Before
	public void setup() {
		// Set the SecurityManager for the current thread
		SimplePrincipalCollection principalCollection = new SimplePrincipalCollection();
		principalCollection.addAll(Arrays.asList("permsTest"), "testRealm");
		Subject subject = new Subject.Builder(securityManager)
			.authenticated(true)
			.principals(principalCollection)
			.buildSubject();
		ThreadContext.bind(subject);
	}

	private CohortDTO createEntity(String name) {
		CohortDTO dto = new CohortDTO();
		dto.setName(name);
		return cdService.createCohortDefinition(dto);
	}
	
	@Test
	public void cohortDefinitionListCacheTest() throws Exception {
		
		if (cacheManager == null) return; // cache is disabled, so nothing to test

		CacheStatisticsMXBean cacheStatistics = CacheHelper.getCacheStats(cacheManager , CohortDefinitionService.CachingSetup.COHORT_DEFINITION_LIST_CACHE);
		Cache cohortListCache = cacheManager.getCache(CohortDefinitionService.CachingSetup.COHORT_DEFINITION_LIST_CACHE);

		// reset the cache and statistics for this test
		cacheStatistics.clear();
		cohortListCache.clear();
		int cacheHits = 0;
		int cacheMisses = 0;
		List<CohortMetadataDTO> cohortDefList;
		cohortDefList = cdService.getCohortDefinitionList();
		cacheMisses++;
		assertThat(cacheStatistics.getCacheMisses()).isEqualTo(cacheMisses);
		assertThat(cacheStatistics.getCacheHits()).isEqualTo(cacheHits);
		
		cohortDefList = cdService.getCohortDefinitionList();
		cacheHits++;
		assertThat(cacheStatistics.getCacheMisses()).isEqualTo(cacheMisses);
		assertThat(cacheStatistics.getCacheHits()).isEqualTo(cacheHits);

	}
	
	@Test
	public void cohortDefinitionListEvictTest() throws Exception {
		
		if (cacheManager == null) return; // cache is disabled, so nothing to test

		CacheStatisticsMXBean cacheStatistics = CacheHelper.getCacheStats(cacheManager , CohortDefinitionService.CachingSetup.COHORT_DEFINITION_LIST_CACHE);
		Cache cohortListCache = cacheManager.getCache(CohortDefinitionService.CachingSetup.COHORT_DEFINITION_LIST_CACHE);

		// reset the cache and statistics for this test
		cacheStatistics.clear();
		cohortListCache.clear();
		int cacheHits = 0;
		int cacheMisses = 0;
		List<CohortMetadataDTO> cohortDefList;
		cohortDefList = cdService.getCohortDefinitionList();
		cacheMisses++;
		assertThat(cacheStatistics.getCacheMisses()).isEqualTo(cacheMisses);
		assertThat(cacheStatistics.getCacheHits()).isEqualTo(cacheHits);
		CohortDTO c = createEntity("Cohort 1");
		cohortDefList = cdService.getCohortDefinitionList();
		cacheMisses++;
		assertThat(cacheStatistics.getCacheMisses()).isEqualTo(cacheMisses);
		assertThat(cacheStatistics.getCacheHits()).isEqualTo(cacheHits);
		c = cdService.saveCohortDefinition(c.getId(), c);
		cohortDefList = cdService.getCohortDefinitionList();
		cacheMisses++;
		assertThat(cacheStatistics.getCacheMisses()).isEqualTo(cacheMisses);
		assertThat(cacheStatistics.getCacheHits()).isEqualTo(cacheHits);
	}
}
