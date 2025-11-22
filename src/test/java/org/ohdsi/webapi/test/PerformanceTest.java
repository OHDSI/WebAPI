package org.ohdsi.webapi.test;

/**
 * Marker interface for performance tests.
 *
 * Tests annotated with @Category(PerformanceTest.class) are performance-sensitive
 * and may require special test data or longer timeouts.
 *
 * Usage:
 * <pre>
 * @Category(PerformanceTest.class)
 * public class MyPerformanceIT extends WebApiIT {
 *     // Performance tests here
 * }
 * </pre>
 *
 * Run only performance tests:
 * <code>mvn test -Dgroups=org.ohdsi.webapi.test.PerformanceTest</code>
 *
 * Exclude performance tests from regular builds:
 * <code>mvn test -DexcludedGroups=org.ohdsi.webapi.test.PerformanceTest</code>
 */
public interface PerformanceTest {
    // Marker interface - no methods
}
