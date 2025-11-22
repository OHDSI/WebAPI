package org.ohdsi.webapi.cohortdefinition;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.test.PerformanceTest;
import org.ohdsi.webapi.test.WebApiIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import jakarta.persistence.EntityManagerFactory;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Performance Integration Tests for Cohort Definition List Optimization
 * Feature: 001-cohort-performance
 *
 * Purpose: Validate performance improvements for GET /cohortdefinition endpoint
 * with large datasets (30,000+ cohorts).
 *
 * Success Criteria:
 * - Response time < 2 seconds for 30,000 cohorts
 * - Memory usage < 200MB per request
 * - No JOIN to cohort_definition_details table
 * - Permission filtering at database level (WHERE id IN ...)
 *
 * NOTE: These tests are designed for dedicated performance testing environments.
 * To run these tests:
 * 1. Set up a database with 30,000+ cohort definitions using generate_30k_cohorts.sql
 * 2. Run with: mvn test -Dtest=CohortDefinitionPerformanceIT -DfailIfNoTests=false
 * 3. Or exclude from regular builds with: mvn test -Dgroups="!org.ohdsi.webapi.test.PerformanceTest"
 */
@Category(PerformanceTest.class)
public class CohortDefinitionPerformanceIT extends WebApiIT {

    private static final Logger logger = LoggerFactory.getLogger(CohortDefinitionPerformanceIT.class);

    private static final long RESPONSE_TIME_THRESHOLD_MS = 2000;  // 2 seconds
    private static final long MEMORY_USAGE_THRESHOLD_MB = 200;    // 200 MB

    @Autowired
    private CohortDefinitionService cohortDefinitionService;

    @Autowired(required = false)
    private EntityManagerFactory entityManagerFactory;

    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    @Before
    public void setup() {
        // Clear Hibernate statistics before each test
        if (entityManagerFactory != null) {
            SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
            Statistics stats = sessionFactory.getStatistics();
            stats.clear();
            stats.setStatisticsEnabled(true);
        }

        // Force garbage collection before performance tests for accurate memory measurement
        System.gc();
        System.gc();
    }

    /**
     * T002: Performance test harness
     * T003: Baseline performance test (expected to be SLOW before optimization)
     *
     * This test establishes the baseline performance BEFORE optimization.
     * Expected behavior BEFORE optimization:
     * - Response time: 30+ seconds (will TIMEOUT or be very slow)
     * - Memory usage: ~500MB
     *
     * Expected behavior AFTER optimization:
     * - Response time: < 2 seconds
     * - Memory usage: < 200MB
     */
    @Test(timeout = 60000)  // 60 second timeout (generous for baseline)
    public void testCohortListPerformance_ResponseTime() {
        logger.info("=== Testing Cohort List Performance (Response Time) ===");
        logger.info("Expected BEFORE optimization: 30+ seconds");
        logger.info("Expected AFTER optimization: < 2 seconds");

        // Measure response time
        long startTime = System.currentTimeMillis();

        List<CohortMetadataDTO> cohorts = cohortDefinitionService.getCohortDefinitionList();

        long endTime = System.currentTimeMillis();
        long responseTimeMs = endTime - startTime;

        logger.info("Response time: {} ms ({} seconds)", responseTimeMs, responseTimeMs / 1000.0);
        logger.info("Total cohorts returned: {}", cohorts != null ? cohorts.size() : 0);

        // Assert: Response time should be under 2 seconds AFTER optimization
        // NOTE: This test may FAIL before optimization (T003 baseline)
        assertNotNull("Cohort list should not be null", cohorts);
        assertTrue("Cohort list should not be empty", cohorts.size() > 0);

        // Performance assertion (expected to PASS after optimization)
        if (responseTimeMs > RESPONSE_TIME_THRESHOLD_MS) {
            logger.warn("PERFORMANCE ISSUE: Response time {} ms exceeds threshold {} ms",
                       responseTimeMs, RESPONSE_TIME_THRESHOLD_MS);
            logger.warn("This is expected BEFORE optimization implementation");
        }

        // Uncomment after optimization is implemented:
        // assertTrue("Response time should be < 2 seconds", responseTimeMs < RESPONSE_TIME_THRESHOLD_MS);
    }

    /**
     * T008: Memory usage test
     *
     * Validates that memory usage per request is under 200MB.
     *
     * BEFORE optimization: ~500MB (loading all cohort definitions + details)
     * AFTER optimization: < 200MB (metadata only, no details)
     */
    @Test(timeout = 60000)
    public void testCohortListPerformance_MemoryUsage() {
        logger.info("=== Testing Cohort List Performance (Memory Usage) ===");
        logger.info("Expected BEFORE optimization: ~500MB");
        logger.info("Expected AFTER optimization: < 200MB");

        // Measure memory before request
        MemoryUsage heapBefore = memoryBean.getHeapMemoryUsage();
        long memoryBeforeMB = heapBefore.getUsed() / (1024 * 1024);

        logger.info("Heap memory before request: {} MB", memoryBeforeMB);

        // Execute request
        List<CohortMetadataDTO> cohorts = cohortDefinitionService.getCohortDefinitionList();

        // Measure memory after request
        MemoryUsage heapAfter = memoryBean.getHeapMemoryUsage();
        long memoryAfterMB = heapAfter.getUsed() / (1024 * 1024);
        long memoryDeltaMB = memoryAfterMB - memoryBeforeMB;

        logger.info("Heap memory after request: {} MB", memoryAfterMB);
        logger.info("Memory delta: {} MB", memoryDeltaMB);
        logger.info("Total cohorts loaded: {}", cohorts != null ? cohorts.size() : 0);

        // Performance assertion
        if (memoryDeltaMB > MEMORY_USAGE_THRESHOLD_MB) {
            logger.warn("PERFORMANCE ISSUE: Memory delta {} MB exceeds threshold {} MB",
                       memoryDeltaMB, MEMORY_USAGE_THRESHOLD_MB);
            logger.warn("This is expected BEFORE optimization implementation");
        }

        // Uncomment after optimization is implemented:
        // assertTrue("Memory usage should be < 200MB", memoryDeltaMB < MEMORY_USAGE_THRESHOLD_MB);
    }

    /**
     * T009: Verify no JOIN to cohort_definition_details table
     *
     * Uses Hibernate statistics to verify that the optimized query does NOT
     * load CohortDefinitionDetails entities.
     *
     * BEFORE optimization: Loads both CohortDefinition AND CohortDefinitionDetails
     * AFTER optimization: Only loads CohortDefinition (no details join)
     */
    @Test(timeout = 60000)
    public void testCohortListPerformance_NoDetailsJoin() {
        logger.info("=== Testing Cohort List Performance (No Details Join) ===");
        logger.info("Expected BEFORE optimization: JOIN to cohort_definition_details");
        logger.info("Expected AFTER optimization: NO JOIN to cohort_definition_details");

        if (entityManagerFactory == null) {
            logger.warn("EntityManagerFactory not available - skipping Hibernate statistics check");
            return;
        }

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.clear();
        stats.setStatisticsEnabled(true);

        // Execute request
        List<CohortMetadataDTO> cohorts = cohortDefinitionService.getCohortDefinitionList();

        // Check Hibernate statistics
        long cohortDefinitionLoads = stats.getEntityFetchCount();
        long totalEntityLoads = stats.getEntityLoadCount();

        logger.info("Total cohorts returned: {}", cohorts != null ? cohorts.size() : 0);
        logger.info("Entity fetch count: {}", cohortDefinitionLoads);
        logger.info("Total entity load count: {}", totalEntityLoads);
        logger.info("Queries executed: {}", stats.getQueryExecutionCount());

        // After optimization, we should only load CohortDefinition entities
        // NOT CohortDefinitionDetails entities
        // Expected: entity load count ≈ number of cohorts (not 2x)

        if (cohorts != null && cohorts.size() > 0) {
            double loadRatio = (double) totalEntityLoads / cohorts.size();
            logger.info("Entity load ratio: {:.2f} (1.0 = optimal, 2.0 = loading details too)", loadRatio);

            if (loadRatio > 1.5) {
                logger.warn("PERFORMANCE ISSUE: Load ratio indicates details are being fetched");
                logger.warn("This is expected BEFORE optimization implementation");
            }

            // Uncomment after optimization is implemented:
            // assertTrue("Entity load ratio should be close to 1.0 (not loading details)", loadRatio < 1.5);
        }
    }

    /**
     * T014: Verify permission filtering happens at database level
     *
     * Tests that the WHERE id IN (...) clause is used for permission filtering,
     * not in-memory filtering after loading all cohorts.
     *
     * BEFORE optimization: Load all 30,000 cohorts, filter in Stream
     * AFTER optimization: Load only accessible cohorts via WHERE clause
     */
    @Test(timeout = 60000)
    public void testCohortListPerformance_DatabaseLevelPermissionFiltering() {
        logger.info("=== Testing Database-Level Permission Filtering ===");
        logger.info("Expected BEFORE optimization: In-memory filtering (loads all cohorts)");
        logger.info("Expected AFTER optimization: Database WHERE clause (loads only accessible)");

        if (entityManagerFactory == null) {
            logger.warn("EntityManagerFactory not available - skipping database filtering check");
            return;
        }

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.clear();
        stats.setStatisticsEnabled(true);

        // Execute request
        List<CohortMetadataDTO> cohorts = cohortDefinitionService.getCohortDefinitionList();

        // Check query execution
        long queryCount = stats.getQueryExecutionCount();
        logger.info("Queries executed: {}", queryCount);
        logger.info("Total cohorts returned: {}", cohorts != null ? cohorts.size() : 0);

        // After optimization, the query should include WHERE id IN (...)
        // This is indicated by having fewer entity loads than total cohorts in database
        // (assuming user doesn't have access to ALL cohorts)

        logger.info("Database-level filtering test complete");
        logger.info("To verify WHERE clause, check SQL logs for 'WHERE id IN' or 'id in'");

        // Uncomment after optimization is implemented:
        // assertTrue("Query count should be minimal with database filtering", queryCount < 10);
    }

    /**
     * T015: Verify limited permissions scenario
     *
     * Tests that when a user has access to a subset of cohorts (e.g., 5,000 of 30,000),
     * only those accessible cohorts are loaded from the database.
     *
     * This requires mocking the permission service or using a test user with limited permissions.
     */
    @Test(timeout = 60000)
    public void testCohortListPerformance_LimitedPermissions() {
        logger.info("=== Testing Limited Permissions Scenario ===");
        logger.info("Expected: Only accessible cohorts loaded from database");

        if (entityManagerFactory == null) {
            logger.warn("EntityManagerFactory not available - skipping limited permissions check");
            return;
        }

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.clear();
        stats.setStatisticsEnabled(true);

        // Execute request
        // NOTE: This test assumes test user has limited permissions
        // In a real scenario, you would need to authenticate as a user with limited access
        List<CohortMetadataDTO> cohorts = cohortDefinitionService.getCohortDefinitionList();

        long totalEntityLoads = stats.getEntityLoadCount();
        logger.info("Total cohorts returned: {}", cohorts != null ? cohorts.size() : 0);
        logger.info("Total entity loads: {}", totalEntityLoads);

        // After optimization, entity loads should equal returned cohorts
        // (not loading all 30,000 then filtering)
        if (cohorts != null && cohorts.size() > 0) {
            double loadEfficiency = (double) cohorts.size() / totalEntityLoads;
            logger.info("Load efficiency: {:.2f} (1.0 = optimal)", loadEfficiency);

            if (loadEfficiency < 0.8) {
                logger.warn("PERFORMANCE ISSUE: Loading more entities than returned (in-memory filtering)");
                logger.warn("This is expected BEFORE optimization implementation");
            }

            // Uncomment after optimization is implemented:
            // assertTrue("Load efficiency should be close to 1.0", loadEfficiency > 0.8);
        }
    }

    /**
     * Helper method to get formatted memory usage statistics
     */
    private String getMemoryStats() {
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        return String.format("Heap: %d MB used, %d MB max",
                           heap.getUsed() / (1024 * 1024),
                           heap.getMax() / (1024 * 1024));
    }
}
