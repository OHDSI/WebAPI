# WebAPI MCP Server Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an embedded Model Context Protocol (MCP) server to WebAPI that exposes ~40 analyst-copilot tools (the design's "~28" estimate, expanded once list/get/status/results were split into distinct tools; the Task 9 registration snapshot is the authoritative surface) (vocabulary, concept sets, cohorts, characterization, incidence-rate, pathway, feature analysis, sources/jobs/results), reusing existing `@PreAuthorize`-gated controller beans in-process, authenticated by the existing `X-API-KEY` filter.

**Architecture:** A new `org.ohdsi.webapi.mcp` package. Thin `@Tool`-annotated `@Component` classes (one per domain) inject the existing `@RestController`/service beans and call their permission-gated methods directly. Spring AI's MCP server starter (SYNC mode, WebMVC transport) publishes the tools over HTTP at `/WebAPI/mcp`. Because SYNC-mode tool execution stays on the servlet thread where `ApiKeyAuthFilter` set the `SecurityContext`, `@PreAuthorize` fires exactly as over HTTP. The whole feature is gated behind `mcp.server.enabled` (default false) so it ships dark.

**Tech Stack:** Java 21, Spring Boot 3.5.6, Spring AI MCP Server (WebMVC starter), Spring Security 6.x, JUnit 5, embedded PostgreSQL for integration tests.

## Global Constraints

- Java 21; Spring Boot 3.5.6 (property `spring.boot.version`, managed via `spring-boot-dependencies` BOM import in `pom.xml`).
- Spring AI version: pin `spring-ai-bom` to the latest 1.0.x release compatible with Spring Boot 3.5.x. Confirm the exact version at Task 1 (see verification step); do NOT hardcode a guess without confirming it resolves.
- Feature flag: everything MCP is `@ConditionalOnProperty(name = "mcp.server.enabled", havingValue = "true")`. Default OFF. The app MUST start and pass all existing tests with the flag absent/false.
- MCP server runs in **SYNC** mode (`spring.ai.mcp.server.type=SYNC`) so tool calls execute on the servlet thread and inherit the `SecurityContext`.
- Tools call the **`@PreAuthorize`-gated** bean methods only — never the "ungated internal" overloads. Authorization is a feature, not an obstacle.
- Tools never call servlet-coupled methods that return `ResponseEntity`/`StreamingResponseBody` and rely on the raw request. Where the only public method returns `ResponseEntity`, call it and use `.getBody()`.
- Every tool method returns an `McpResult` envelope (never a raw DTO, never a thrown exception across the tool boundary).
- New code lives under `src/main/java/org/ohdsi/webapi/mcp/`. Tests under `src/test/java/org/ohdsi/webapi/mcp/`.
- Commit after every task with a `feat:`/`test:`/`chore:` prefixed message.

---

## File Structure

- Create `src/main/java/org/ohdsi/webapi/mcp/McpServerConfig.java` — registers the `ToolCallbackProvider` from all `McpToolset` beans; feature-flagged.
- Create `src/main/java/org/ohdsi/webapi/mcp/McpToolset.java` — marker interface so tool classes auto-register.
- Create `src/main/java/org/ohdsi/webapi/mcp/support/McpResult.java` — uniform result/error envelope.
- Create `src/main/java/org/ohdsi/webapi/mcp/support/McpCall.java` — `guard(...)` helper that maps exceptions to `McpResult` error envelopes.
- Create `src/main/java/org/ohdsi/webapi/mcp/support/McpToolContext.java` — source-key validation + valid-key listing.
- Create `src/main/java/org/ohdsi/webapi/mcp/tools/VocabularyTools.java`
- Create `src/main/java/org/ohdsi/webapi/mcp/tools/SourceJobTools.java`
- Create `src/main/java/org/ohdsi/webapi/mcp/tools/ConceptSetTools.java`
- Create `src/main/java/org/ohdsi/webapi/mcp/tools/CohortTools.java`
- Create `src/main/java/org/ohdsi/webapi/mcp/tools/AnalysisTools.java`
- Modify `pom.xml` — add `spring-ai-bom` import + `spring-ai-starter-mcp-server-webmvc` dependency.
- Modify the Spring Security filter chain config (`src/main/java/org/ohdsi/webapi/security/spring/SpringSecurityConfig.java`) — ensure `/mcp/**` passes through `ApiKeyAuthFilter` and requires authentication.
- Modify `src/main/resources/application.properties` (or the active profile properties) — add MCP defaults.
- Modify `CLAUDE.md` — document enabling and connecting to the MCP server.
- Create test files mirroring each tool class under `src/test/java/org/ohdsi/webapi/mcp/`.

---

## Backing-bean reference (verified signatures)

These are the exact beans/methods tools call. Base path shown for orientation only; tools call the Java method, not HTTP.

| Domain | Bean (inject) | Base path | Key methods (return type) |
|---|---|---|---|
| Vocabulary | `VocabularyService` | `/vocabulary` | `executeSearch(ConceptSearch)`→`Collection<Concept>`; `executeSearch(String query)`→`Collection<Concept>`; `getConcept(long id)`→`Concept`; `getRelatedConcepts(Long id)`→`Collection<RelatedConcept>` |
| Sources | `SourceService` | `/source` | `getSourcesEndpoint()`→`ResponseEntity<Collection<SourceInfo>>` (use `.getBody()`); `findBySourceKey(String)`→`Source`; `getSources()`→`Collection<Source>` |
| Concept sets | `ConceptSetService` | `/conceptset` | `getConceptSets()`→`Collection<ConceptSetDTO>`; `getConceptSet(int id)`→`ConceptSetDTO`; `getConceptSetExpressionById(int id)`→`ConceptSetExpression`; `createConceptSet(ConceptSetDTO)`→`ConceptSetDTO`; `updateConceptSet(int id, ConceptSetDTO)`→`ConceptSetDTO`; `saveConceptSetItems(int id, ConceptSetItem[])`→`boolean` |
| CS resolve | `VocabularyService` | `/vocabulary` | `resolveConceptSetExpression(String sourceKey, ConceptSetExpression)` — POST `/{sourceKey}/resolveConceptSetExpression` (confirm exact method name at Task 6, line ~1038) |
| Cohorts | `CohortDefinitionService` | `/cohortdefinition` | `getCohortDefinitionList()`→`List<CohortMetadataDTO>`; `getCohortDefinition(int id)` GET `/{id}` (confirm name at Task 7); `generateCohort(int id, String sourceKey)`→`JobExecutionResource`; `getInfo(int id)`→`List<CohortGenerationInfoDTO>`; inclusion report GET `/{id}/report/{sourceKey}/inclusion` (confirm method name) |
| Characterization | `CcController` | `/cohort-characterization` | `list(Pageable)`→`Page<CcShortDTO>`; get GET `/{id}`; create POST `/{id}`; generate POST `/{id}/generation/{sourceKey}`; results GET `/generation/{generationId}/result` (confirm method names at Task 8) |
| Incidence rate | `IRAnalysisService` (impl of `IRAnalysisResource`) | `/ir` | `getIRAnalysisList()`→`List<IRAnalysisShortDTO>`; `getAnalysis(int id)`→`IRAnalysisDTO`; `createAnalysis(IRAnalysisDTO)`→`IRAnalysisDTO`; `performAnalysis(int analysisId, String sourceKey)`→`JobExecutionResource`; `getAnalysisReport(int id, String sourceKey, int targetId, int outcomeId)`→`AnalysisReport`; `getAnalysisInfo(int id)`→`List<AnalysisInfoDTO>` |
| Pathway | `PathwayController` | `/pathway-analysis` | `list(Pageable)`→`Page<PathwayAnalysisDTO>`; get GET `/{id}`; create POST `/{id}`; generate POST `/{id}/generation/{sourceKey}`; results GET `/generation/{generationId}/result` (confirm method names at Task 8) |
| Feature analysis | `FeAnalysisController` | `/feature-analysis` | `list(Pageable)`→`Page<FeAnalysisShortDTO>`; get GET `/{id}` (confirm method names at Task 8) |
| CDM results | `CDMResultsService` | `/cdmresults` | dashboard GET `/{sourceKey}/dashboard`; datadensity GET `/{sourceKey}/datadensity`; domain counts GET `/{sourceKey}/{domain}` (confirm method names at Task 5) |
| Jobs | `JobService` | `/job` | job by id GET `/{jobId}`; executions GET `/execution` (confirm method names at Task 5) |
| Notifications | `NotificationServiceImpl` | `/notifications` | viewed GET `/viewed` (confirm method name at Task 5) |

The "confirm at Task N" cells mean: the task's first step greps the file to copy the exact method name/signature before writing the wrapper. This is a real verification step, not a placeholder — the method exists; only its exact Java name needs copying.

---

### Task 1: Add Spring AI MCP dependency and feature-flagged empty server

**Files:**
- Modify: `pom.xml` (dependencyManagement ~line 314-338; dependencies section)
- Modify: `src/main/resources/application.properties`
- Create: `src/main/java/org/ohdsi/webapi/mcp/McpServerConfig.java`
- Create: `src/main/java/org/ohdsi/webapi/mcp/McpToolset.java`
- Test: `src/test/java/org/ohdsi/webapi/mcp/McpServerConfigTest.java`

**Interfaces:**
- Produces: marker interface `org.ohdsi.webapi.mcp.McpToolset` (empty); bean `ToolCallbackProvider webApiMcpTools(List<McpToolset>)`; property namespace `mcp.server.enabled` and `spring.ai.mcp.server.*`.

- [ ] **Step 1: Confirm the Spring AI version that resolves against Spring Boot 3.5.6**

Run:
```bash
cd /home/ph/code/WebAPI
# Inspect what Spring AI versions are available and compatible with Spring Boot 3.5.x
mvn -q dependency:get -Dartifact=org.springframework.ai:spring-ai-bom:1.0.1:pom 2>&1 | tail -20 || \
mvn -q dependency:get -Dartifact=org.springframework.ai:spring-ai-bom:1.0.0:pom 2>&1 | tail -20
```
Expected: one of the coordinates downloads successfully. Record the resolved version (call it `<AI_VERSION>`); use it in Step 2. If neither resolves, search Maven Central for the latest `spring-ai-bom` 1.0.x and use that.

- [ ] **Step 2: Add the BOM import and starter dependency to `pom.xml`**

In `<dependencyManagement><dependencies>` (after the `spring-boot-dependencies` import, ~line 322), add:
```xml
      <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-bom</artifactId>
        <version>1.0.1</version> <!-- replace with confirmed <AI_VERSION> from Step 1 -->
        <type>pom</type>
        <scope>import</scope>
      </dependency>
```
In the main `<dependencies>` block add:
```xml
    <!-- MCP server (analyst-copilot tools). Feature-flagged via mcp.server.enabled. -->
    <dependency>
      <groupId>org.springframework.ai</groupId>
      <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
    </dependency>
```

- [ ] **Step 3: Add MCP default properties**

Append to `src/main/resources/application.properties`:
```properties
# --- MCP server (analyst copilot). Disabled by default; enable per-deployment. ---
mcp.server.enabled=${MCP_SERVER_ENABLED:false}
spring.ai.mcp.server.enabled=${MCP_SERVER_ENABLED:false}
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.name=ohdsi-webapi
spring.ai.mcp.server.version=1.0.0
spring.ai.mcp.server.sse-message-endpoint=/mcp/message
```

- [ ] **Step 4: Create the marker interface**

`src/main/java/org/ohdsi/webapi/mcp/McpToolset.java`:
```java
package org.ohdsi.webapi.mcp;

/**
 * Marker for MCP tool classes. All beans implementing this are aggregated into
 * the {@code ToolCallbackProvider} registered by {@link McpServerConfig}.
 */
public interface McpToolset {
}
```

- [ ] **Step 5: Create the feature-flagged server config (no tools yet)**

`src/main/java/org/ohdsi/webapi/mcp/McpServerConfig.java`:
```java
package org.ohdsi.webapi.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Registers every {@link McpToolset} bean's {@code @Tool} methods with the MCP server.
 * Active only when {@code mcp.server.enabled=true}.
 */
@Configuration
@ConditionalOnProperty(name = "mcp.server.enabled", havingValue = "true")
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider webApiMcpTools(List<McpToolset> toolsets) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(toolsets.toArray())
                .build();
    }
}
```
Note: if `MethodToolCallbackProvider`/`ToolCallbackProvider` import paths differ in the confirmed Spring AI version, adjust to the actual package (grep the resolved jar: `find ~/.m2 -name 'spring-ai-*tool*.jar'`).

- [ ] **Step 6: Write the failing config test**

`src/test/java/org/ohdsi/webapi/mcp/McpServerConfigTest.java`:
```java
package org.ohdsi.webapi.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class McpServerConfigTest {

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withUserConfiguration(McpServerConfig.class);

    @Test
    void toolProviderIsAbsentWhenDisabled() {
        runner.run(ctx -> assertThat(ctx).doesNotHaveBean(ToolCallbackProvider.class));
    }

    @Test
    void toolProviderIsPresentWhenEnabled() {
        runner.withPropertyValues("mcp.server.enabled=true")
              .run(ctx -> assertThat(ctx).hasSingleBean(ToolCallbackProvider.class));
    }
}
```

- [ ] **Step 7: Run the test (expect failure until deps resolve/compile)**

Run: `mvn -q -Dtest=McpServerConfigTest test -Dpackaging.type=jar`
Expected: FAIL first (compilation or bean assertions), then after deps download and code compiles, PASS. Iterate on import paths until green.

- [ ] **Step 8: Verify the whole app still starts with the flag OFF**

Run: `mvn -q clean package -DskipTests -Dpackaging.type=jar -P trexsql`
Expected: BUILD SUCCESS. (Do not run the server here; just confirm it packages.)

- [ ] **Step 9: Commit**

```bash
git add pom.xml src/main/resources/application.properties \
  src/main/java/org/ohdsi/webapi/mcp/McpToolset.java \
  src/main/java/org/ohdsi/webapi/mcp/McpServerConfig.java \
  src/test/java/org/ohdsi/webapi/mcp/McpServerConfigTest.java
git commit -m "feat(mcp): add feature-flagged Spring AI MCP server scaffolding"
```

---

### Task 2: Result envelope and exception-mapping helper

**Files:**
- Create: `src/main/java/org/ohdsi/webapi/mcp/support/McpResult.java`
- Create: `src/main/java/org/ohdsi/webapi/mcp/support/McpCall.java`
- Test: `src/test/java/org/ohdsi/webapi/mcp/support/McpCallTest.java`

**Interfaces:**
- Produces: `McpResult` (record: `boolean ok`, `String status`, `Object data`, `String message`) with static `ok(Object)`, `error(String,String)`. `McpCall.guard(Supplier<?>)` → `McpResult`, mapping `AccessDeniedException`→`permission_denied`, `IllegalArgumentException`→`invalid_input`, anything else→`upstream_error` (logs full detail, returns a short message).

- [ ] **Step 1: Write the failing test**

`src/test/java/org/ohdsi/webapi/mcp/support/McpCallTest.java`:
```java
package org.ohdsi.webapi.mcp.support;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class McpCallTest {

    @Test
    void wrapsSuccessfulValue() {
        McpResult r = McpCall.guard(() -> "hello");
        assertThat(r.ok()).isTrue();
        assertThat(r.status()).isEqualTo("ok");
        assertThat(r.data()).isEqualTo("hello");
    }

    @Test
    void mapsAccessDeniedToPermissionDenied() {
        McpResult r = McpCall.guard(() -> { throw new AccessDeniedException("no read:cohort"); });
        assertThat(r.ok()).isFalse();
        assertThat(r.status()).isEqualTo("permission_denied");
        assertThat(r.message()).contains("no read:cohort");
    }

    @Test
    void mapsIllegalArgumentToInvalidInput() {
        McpResult r = McpCall.guard(() -> { throw new IllegalArgumentException("bad sourceKey"); });
        assertThat(r.status()).isEqualTo("invalid_input");
        assertThat(r.message()).contains("bad sourceKey");
    }

    @Test
    void mapsUnknownToUpstreamErrorWithoutLeakingDetail() {
        McpResult r = McpCall.guard(() -> { throw new RuntimeException("SQLState 42P01 relation missing"); });
        assertThat(r.status()).isEqualTo("upstream_error");
        assertThat(r.message()).doesNotContain("42P01");
    }
}
```

- [ ] **Step 2: Run it, expect failure**

Run: `mvn -q -Dtest=McpCallTest test -Dpackaging.type=jar`
Expected: FAIL — `McpResult`/`McpCall` do not exist.

- [ ] **Step 3: Implement `McpResult`**

`src/main/java/org/ohdsi/webapi/mcp/support/McpResult.java`:
```java
package org.ohdsi.webapi.mcp.support;

/** Uniform envelope returned by every MCP tool. Serialized to JSON for the model. */
public record McpResult(boolean ok, String status, Object data, String message) {

    public static McpResult ok(Object data) {
        return new McpResult(true, "ok", data, null);
    }

    public static McpResult error(String status, String message) {
        return new McpResult(false, status, null, message);
    }
}
```

- [ ] **Step 4: Implement `McpCall`**

`src/main/java/org/ohdsi/webapi/mcp/support/McpCall.java`:
```java
package org.ohdsi.webapi.mcp.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import java.util.function.Supplier;

/** Runs a tool body and converts any exception into an {@link McpResult} envelope. */
public final class McpCall {

    private static final Logger log = LoggerFactory.getLogger(McpCall.class);

    private McpCall() {
    }

    public static McpResult guard(Supplier<?> body) {
        try {
            return McpResult.ok(body.get());
        } catch (AccessDeniedException e) {
            return McpResult.error("permission_denied", e.getMessage());
        } catch (IllegalArgumentException e) {
            return McpResult.error("invalid_input", e.getMessage());
        } catch (Exception e) {
            log.error("MCP tool call failed", e);
            return McpResult.error("upstream_error", "The WebAPI call failed. See server logs.");
        }
    }
}
```

- [ ] **Step 5: Run tests, expect pass**

Run: `mvn -q -Dtest=McpCallTest test -Dpackaging.type=jar`
Expected: PASS (4 tests).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/ohdsi/webapi/mcp/support/McpResult.java \
  src/main/java/org/ohdsi/webapi/mcp/support/McpCall.java \
  src/test/java/org/ohdsi/webapi/mcp/support/McpCallTest.java
git commit -m "feat(mcp): add McpResult envelope and McpCall exception mapper"
```

---

### Task 3: Source-key context helper and security wiring

**Files:**
- Create: `src/main/java/org/ohdsi/webapi/mcp/support/McpToolContext.java`
- Modify: `src/main/java/org/ohdsi/webapi/security/spring/SpringSecurityConfig.java`
- Test: `src/test/java/org/ohdsi/webapi/mcp/support/McpToolContextTest.java`

**Interfaces:**
- Consumes: `SourceService.findBySourceKey(String)`, `SourceService.getSources()`.
- Produces: `McpToolContext` bean with `String requireSource(String sourceKey)` — returns the key if valid, else throws `IllegalArgumentException` listing valid keys; `List<String> validSourceKeys()`.

- [ ] **Step 1: Confirm `SourceService.getSources()`/`findBySourceKey` and `Source.getSourceKey()`**

Run:
```bash
cd /home/ph/code/WebAPI
grep -nE "public .*getSourceKey|public .*findBySourceKey|public Collection<Source> getSources" \
  src/main/java/org/ohdsi/webapi/source/SourceService.java src/main/java/org/ohdsi/webapi/source/Source.java
```
Expected: confirms `Source getSourceKey()` accessor and the two `SourceService` methods. Use the exact accessor name in Step 3.

- [ ] **Step 2: Write the failing test**

`src/test/java/org/ohdsi/webapi/mcp/support/McpToolContextTest.java`:
```java
package org.ohdsi.webapi.mcp.support;

import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class McpToolContextTest {

    private SourceService sourceService = mock(SourceService.class);
    private McpToolContext context = new McpToolContext(sourceService);

    @Test
    void requireSourceReturnsKeyWhenValid() {
        Source s = new Source();
        s.setSourceKey("DEMO_CDM");
        when(sourceService.findBySourceKey("DEMO_CDM")).thenReturn(s);
        assertThat(context.requireSource("DEMO_CDM")).isEqualTo("DEMO_CDM");
    }

    @Test
    void requireSourceThrowsListingValidKeysWhenUnknown() {
        Source s = new Source();
        s.setSourceKey("DEMO_CDM");
        when(sourceService.findBySourceKey("NOPE")).thenReturn(null);
        when(sourceService.getSources()).thenReturn(List.of(s));
        assertThatThrownBy(() -> context.requireSource("NOPE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DEMO_CDM");
    }
}
```
(If `Source` lacks a public no-arg constructor / `setSourceKey`, adapt the test to build a `Source` the way other tests in `src/test/.../source` do — grep for existing `new Source(` usage.)

- [ ] **Step 3: Run it, expect failure**

Run: `mvn -q -Dtest=McpToolContextTest test -Dpackaging.type=jar`
Expected: FAIL — `McpToolContext` does not exist.

- [ ] **Step 4: Implement `McpToolContext`**

`src/main/java/org/ohdsi/webapi/mcp/support/McpToolContext.java`:
```java
package org.ohdsi.webapi.mcp.support;

import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/** Resolves and validates human-friendly source keys used across MCP tools. */
@Component
public class McpToolContext {

    private final SourceService sourceService;

    public McpToolContext(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    /** @return the key if a source exists; otherwise throws with the list of valid keys. */
    public String requireSource(String sourceKey) {
        if (sourceKey != null && sourceService.findBySourceKey(sourceKey) != null) {
            return sourceKey;
        }
        throw new IllegalArgumentException(
                "Unknown sourceKey '" + sourceKey + "'. Valid keys: " + validSourceKeys());
    }

    public List<String> validSourceKeys() {
        return sourceService.getSources().stream()
                .map(Source::getSourceKey)
                .collect(Collectors.toList());
    }
}
```

- [ ] **Step 5: Run test, expect pass**

Run: `mvn -q -Dtest=McpToolContextTest test -Dpackaging.type=jar`
Expected: PASS.

- [ ] **Step 6: Wire `/mcp/**` into the security filter chain**

First inspect the current chain:
```bash
grep -nE "SecurityFilterChain|authorizeHttpRequests|requestMatchers|permitAll|addFilterBefore|ApiKeyAuthFilter" \
  src/main/java/org/ohdsi/webapi/security/spring/SpringSecurityConfig.java
```
Then, following the existing pattern in that file, ensure:
- `ApiKeyAuthFilter` is registered in the chain that serves `/mcp/**` (it is a `@Component`; confirm it is added via `addFilterBefore(apiKeyAuthFilter, BearerTokenAuthenticationFilter.class)` or equivalent — it likely already applies globally).
- `/mcp/**` requires authentication: add `.requestMatchers("/mcp/**").authenticated()` (or leave to the default-authenticated rule if the chain already denies anonymous). Do NOT `permitAll()` it.
- The MCP SSE endpoints must not be blocked by CSRF: if CSRF is enabled for the chain, add `/mcp/**` to CSRF ignore (match how other stateless/token endpoints are handled in this file).

Make the minimal edit consistent with the file's existing style. If the chain already authenticates all non-allowlisted paths and applies `ApiKeyAuthFilter` globally, only the CSRF-ignore edit may be needed.

- [ ] **Step 7: Verify app still packages**

Run: `mvn -q clean package -DskipTests -Dpackaging.type=jar -P trexsql`
Expected: BUILD SUCCESS.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/org/ohdsi/webapi/mcp/support/McpToolContext.java \
  src/test/java/org/ohdsi/webapi/mcp/support/McpToolContextTest.java \
  src/main/java/org/ohdsi/webapi/security/spring/SpringSecurityConfig.java
git commit -m "feat(mcp): add source-key context helper and wire /mcp security"
```

---

### Task 4: Vocabulary tools + end-to-end security-propagation integration test

This is the first real tool class and the task that proves the core mechanism (SecurityContext reaches `@PreAuthorize` through the MCP path). It is the highest-value gate in the plan.

**Files:**
- Create: `src/main/java/org/ohdsi/webapi/mcp/tools/VocabularyTools.java`
- Test: `src/test/java/org/ohdsi/webapi/mcp/tools/VocabularyToolsTest.java`
- Test: `src/test/java/org/ohdsi/webapi/mcp/McpSecurityPropagationIT.java`

**Interfaces:**
- Consumes: `VocabularyService.executeSearch(ConceptSearch)`, `.executeSearch(String)`, `.getConcept(long)`, `.getRelatedConcepts(Long)`; `McpToolContext`; `McpCall.guard`.
- Produces: bean `VocabularyTools implements McpToolset` with `@Tool` methods `vocabSearchConcepts`, `vocabGetConcept`, `vocabRelatedConcepts`, `vocabConceptAncestors`, `vocabConceptDescendants`, `vocabDomains`, `vocabVocabularies`.

- [ ] **Step 1: Confirm ancestor/descendant/domain/vocabulary method names**

Run:
```bash
cd /home/ph/code/WebAPI
grep -nE "public .*(Ancestor|Descendant|getDomains|getVocabularies|domains|vocabularies)\b" \
  src/main/java/org/ohdsi/webapi/service/VocabularyService.java | head
```
Expected: identifies `getDescendantConcepts(...)`, `getConceptAncestorAndDescendant(...)`, and the domains/vocabularies list methods. Copy exact names/signatures. For ancestors/descendants that require a `sourceKey`, the tool accepts a `sourceKey` param and calls the `@PreAuthorize`-gated `/{sourceKey}/...` overload via `context.requireSource`. If a no-sourceKey variant exists, prefer it and drop the param.

- [ ] **Step 2: Write the failing unit test (mocked service)**

`src/test/java/org/ohdsi/webapi/mcp/tools/VocabularyToolsTest.java`:
```java
package org.ohdsi.webapi.mcp.tools;

import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.vocabulary.Concept;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VocabularyToolsTest {

    private final VocabularyService vocab = mock(VocabularyService.class);
    private final McpToolContext context = mock(McpToolContext.class);
    private final VocabularyTools tools = new VocabularyTools(vocab, context);

    @Test
    void searchReturnsOkEnvelopeWithConcepts() {
        Concept c = new Concept();
        c.conceptId = 1L;
        c.conceptName = "Diabetes";
        when(vocab.executeSearch(any(org.ohdsi.webapi.vocabulary.ConceptSearch.class)))
                .thenReturn(List.of(c));

        McpResult r = tools.vocabSearchConcepts("diabetes", null, null, null);

        assertThat(r.ok()).isTrue();
        assertThat(r.data().toString()).contains("Diabetes");
    }
}
```
(Confirm `Concept`'s public field/accessor names via `grep -nE "conceptId|conceptName" src/main/java/org/ohdsi/webapi/vocabulary/Concept.java` and adjust.)

- [ ] **Step 3: Run it, expect failure**

Run: `mvn -q -Dtest=VocabularyToolsTest test -Dpackaging.type=jar`
Expected: FAIL — `VocabularyTools` does not exist.

- [ ] **Step 4: Implement `VocabularyTools`**

`src/main/java/org/ohdsi/webapi/mcp/tools/VocabularyTools.java`:
```java
package org.ohdsi.webapi.mcp.tools;

import org.ohdsi.webapi.mcp.McpToolset;
import org.ohdsi.webapi.mcp.support.McpCall;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.vocabulary.ConceptSearch;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** MCP tools for browsing the OMOP vocabulary. Read-only. */
@Component
public class VocabularyTools implements McpToolset {

    private final VocabularyService vocab;
    private final McpToolContext context;

    public VocabularyTools(VocabularyService vocab, McpToolContext context) {
        this.vocab = vocab;
        this.context = context;
    }

    @Tool(description = "Search the OMOP vocabulary for concepts by term. Optional filters: "
            + "domainId (e.g. Condition, Drug), vocabularyId (e.g. SNOMED, RxNorm), "
            + "standardConcept ('S' for standard only). Returns matching concepts.")
    public McpResult vocabSearchConcepts(
            @ToolParam(description = "Free-text search term") String query,
            @ToolParam(required = false, description = "Domain id filter, e.g. Condition") String domainId,
            @ToolParam(required = false, description = "Vocabulary id filter, e.g. SNOMED") String vocabularyId,
            @ToolParam(required = false, description = "'S' to restrict to standard concepts") String standardConcept) {
        return McpCall.guard(() -> {
            ConceptSearch search = new ConceptSearch();
            search.query = query;
            if (domainId != null) search.domainId = new String[]{domainId};
            if (vocabularyId != null) search.vocabularyId = new String[]{vocabularyId};
            search.standardConcept = standardConcept;
            return vocab.executeSearch(search);
        });
    }

    @Tool(description = "Get full details for a single concept by its conceptId.")
    public McpResult vocabGetConcept(
            @ToolParam(description = "OMOP conceptId") long conceptId) {
        return McpCall.guard(() -> vocab.getConcept(conceptId));
    }

    @Tool(description = "List concepts directly related to the given conceptId (relationships).")
    public McpResult vocabRelatedConcepts(
            @ToolParam(description = "OMOP conceptId") long conceptId) {
        return McpCall.guard(() -> vocab.getRelatedConcepts(conceptId));
    }

    @Tool(description = "List ancestor and descendant concepts in the hierarchy for a conceptId, "
            + "resolved against a data source. Use source_list to find valid sourceKey values.")
    public McpResult vocabConceptAncestors(
            @ToolParam(description = "Source key (see source_list)") String sourceKey,
            @ToolParam(description = "OMOP conceptId") long conceptId) {
        return McpCall.guard(() ->
                vocab.getConceptAncestorAndDescendant(context.requireSource(sourceKey), conceptId));
    }

    @Tool(description = "List descendant concepts in the hierarchy for a conceptId, "
            + "resolved against a data source. Use source_list to find valid sourceKey values.")
    public McpResult vocabConceptDescendants(
            @ToolParam(description = "Source key (see source_list)") String sourceKey,
            @ToolParam(description = "OMOP conceptId") long conceptId) {
        return McpCall.guard(() ->
                vocab.getDescendantConcepts(context.requireSource(sourceKey), conceptId));
    }

    @Tool(description = "List the OMOP domains available for filtering concept searches.")
    public McpResult vocabDomains(
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> vocab.getDomains(context.requireSource(sourceKey)));
    }

    @Tool(description = "List the OMOP vocabularies available for filtering concept searches.")
    public McpResult vocabVocabularies(
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> vocab.getVocabularies(context.requireSource(sourceKey)));
    }
}
```
Adjust `getConceptAncestorAndDescendant`, `getDescendantConcepts`, `getDomains`, `getVocabularies` to the exact signatures confirmed in Step 1 (parameter order/types). If any requires a `Source` object rather than a `String sourceKey`, resolve via `sourceService.findBySourceKey(...)` inside the guard.

- [ ] **Step 5: Run the unit test, expect pass**

Run: `mvn -q -Dtest=VocabularyToolsTest test -Dpackaging.type=jar`
Expected: PASS.

- [ ] **Step 6: Write the security-propagation + tool-registration integration test**

Find how existing integration tests boot the context and seed users/permissions:
```bash
grep -rlnE "@SpringBootTest|AbstractDatabaseTest|EmbeddedPostgres|Testcontainers" src/test/java | head
```
Model `McpSecurityPropagationIT` on the existing base test class. It must:
1. Boot the app with `mcp.server.enabled=true`.
2. Obtain the registered `ToolCallbackProvider` bean and assert `VocabularyTools`' tools are present (e.g. a callback named `vocabSearchConcepts`).
3. With a `SecurityContext` for a user **lacking** the required source/vocabulary permission set on the current thread, invoke a gated tool (e.g. `vocabConceptDescendants`) and assert the returned `McpResult.status()` equals `permission_denied` (proving `@PreAuthorize` fired via the tool path).
4. With a user **granted** the permission, assert `status()=="ok"`.

`src/test/java/org/ohdsi/webapi/mcp/McpSecurityPropagationIT.java` (skeleton to complete against the repo's test base):
```java
package org.ohdsi.webapi.mcp;

import org.junit.jupiter.api.Test;
// import the project's integration test base + security test helpers
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "mcp.server.enabled=true")
class McpSecurityPropagationIT /* extends <ProjectIntegrationTestBase> */ {

    @Autowired
    ToolCallbackProvider toolCallbackProvider;

    private ToolCallback tool(String name) {
        return Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .filter(t -> t.getToolDefinition().name().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    void vocabularyToolsAreRegistered() {
        assertThat(Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(t -> t.getToolDefinition().name()))
                .contains("vocabSearchConcepts", "vocabGetConcept");
    }

    @Test
    void gatedToolDeniesWithoutPermission() {
        // set an authenticated principal WITHOUT source/vocabulary read permission (use project test helper)
        // SecurityContextHolder.getContext().setAuthentication(unprivilegedAuth());
        String json = tool("vocabConceptDescendants")
                .call("{\"sourceKey\":\"DEMO_CDM\",\"conceptId\":201826}");
        assertThat(json).contains("permission_denied");
        SecurityContextHolder.clearContext();
    }
}
```
Complete the `unprivilegedAuth()`/privileged setup using the same helpers existing `@PreAuthorize` integration tests use (grep for tests that assert `AccessDeniedException` or set `WebApiAuthenticationToken`). If the demo source/permission seeding is heavy, assert propagation against the simplest gated tool available and document the chosen tool in a comment.

- [ ] **Step 7: Run the integration test**

Run: `mvn -q -Dtest=McpSecurityPropagationIT test -Dpackaging.type=jar`
Expected: PASS. If `permission_denied` is NOT returned (tool ran on a thread without the context), the SYNC-mode assumption is broken → add a `SecurityContext`-propagating `ToolCallback` wrapper in `McpServerConfig` (wrap each callback: capture `SecurityContextHolder.getContext()` at call time is too late, so instead capture at the servlet boundary via a `DelegatingSecurityContextExecutor` on the MCP async executor). Re-run until green. This is the single riskiest step; do not proceed until it passes.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/org/ohdsi/webapi/mcp/tools/VocabularyTools.java \
  src/test/java/org/ohdsi/webapi/mcp/tools/VocabularyToolsTest.java \
  src/test/java/org/ohdsi/webapi/mcp/McpSecurityPropagationIT.java
git commit -m "feat(mcp): add vocabulary tools with security-propagation integration test"
```

---

### Task 5: Sources, jobs and results tools

**Files:**
- Create: `src/main/java/org/ohdsi/webapi/mcp/tools/SourceJobTools.java`
- Test: `src/test/java/org/ohdsi/webapi/mcp/tools/SourceJobToolsTest.java`

**Interfaces:**
- Consumes: `SourceService.getSourcesEndpoint()` (use `.getBody()`), `CDMResultsService` dashboard/datadensity/domain-count methods, `JobService` job/execution methods, `NotificationServiceImpl` viewed method, `McpToolContext`.
- Produces: `SourceJobTools implements McpToolset` with `@Tool` methods `sourceList`, `cdmResultsDashboard`, `cdmResultsDomainCounts`, `jobStatus`, `jobListRecent`.

- [ ] **Step 1: Confirm exact method names/signatures**

Run:
```bash
cd /home/ph/code/WebAPI
grep -nE "public .*getSourcesEndpoint|public .*getSources\b" src/main/java/org/ohdsi/webapi/source/SourceService.java
sed -n '221,240p;321,410p' src/main/java/org/ohdsi/webapi/service/CDMResultsService.java | grep -nE "public .*\("
grep -nE "public .* getJob|public .*getExecution|public .*findViewed|public .* execution" \
  src/main/java/org/ohdsi/webapi/service/JobService.java src/main/java/org/ohdsi/webapi/job/NotificationServiceImpl.java
```
Expected: exact names for dashboard (`getDashboard`?), datadensity, domain count, `getJob`, `getExecution(s)`, notifications viewed. Copy them.

- [ ] **Step 2: Write the failing unit test**

`src/test/java/org/ohdsi/webapi/mcp/tools/SourceJobToolsTest.java`:
```java
package org.ohdsi.webapi.mcp.tools;

import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.source.SourceService;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SourceJobToolsTest {

    private final SourceService sourceService = mock(SourceService.class);
    private final org.ohdsi.webapi.service.CDMResultsService cdm = mock(org.ohdsi.webapi.service.CDMResultsService.class);
    private final org.ohdsi.webapi.service.JobService jobs = mock(org.ohdsi.webapi.service.JobService.class);
    private final org.ohdsi.webapi.job.NotificationServiceImpl notifications = mock(org.ohdsi.webapi.job.NotificationServiceImpl.class);
    private final McpToolContext context = mock(McpToolContext.class);
    private final SourceJobTools tools = new SourceJobTools(sourceService, cdm, jobs, notifications, context);

    @Test
    void sourceListReturnsSources() {
        SourceInfo info = new SourceInfo();
        when(sourceService.getSourcesEndpoint())
                .thenReturn(ResponseEntity.ok(List.of(info)));
        McpResult r = tools.sourceList();
        assertThat(r.ok()).isTrue();
    }
}
```
(Adjust `SourceInfo` construction to how it is built elsewhere; grep `new SourceInfo(`.)

- [ ] **Step 3: Run it, expect failure**

Run: `mvn -q -Dtest=SourceJobToolsTest test -Dpackaging.type=jar`
Expected: FAIL — class missing.

- [ ] **Step 4: Implement `SourceJobTools`**

`src/main/java/org/ohdsi/webapi/mcp/tools/SourceJobTools.java`:
```java
package org.ohdsi.webapi.mcp.tools;

import org.ohdsi.webapi.job.NotificationServiceImpl;
import org.ohdsi.webapi.mcp.McpToolset;
import org.ohdsi.webapi.mcp.support.McpCall;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.service.CDMResultsService;
import org.ohdsi.webapi.service.JobService;
import org.ohdsi.webapi.source.SourceService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** MCP tools for data sources, CDM result summaries, and job status. Read-only. */
@Component
public class SourceJobTools implements McpToolset {

    private final SourceService sourceService;
    private final CDMResultsService cdmResults;
    private final JobService jobService;
    private final NotificationServiceImpl notifications;
    private final McpToolContext context;

    public SourceJobTools(SourceService sourceService, CDMResultsService cdmResults,
                          JobService jobService, NotificationServiceImpl notifications,
                          McpToolContext context) {
        this.sourceService = sourceService;
        this.cdmResults = cdmResults;
        this.jobService = jobService;
        this.notifications = notifications;
        this.context = context;
    }

    @Tool(description = "List all configured CDM data sources with their keys and daimons. "
            + "Call this first to discover valid sourceKey values for other tools.")
    public McpResult sourceList() {
        return McpCall.guard(() -> sourceService.getSourcesEndpoint().getBody());
    }

    @Tool(description = "Get the record-count / data-density dashboard for a source.")
    public McpResult cdmResultsDashboard(
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> cdmResults.getDashboard(context.requireSource(sourceKey)));
    }

    @Tool(description = "Get per-domain record counts for a source (e.g. Condition, Drug, Procedure).")
    public McpResult cdmResultsDomainCounts(
            @ToolParam(description = "Source key (see source_list)") String sourceKey,
            @ToolParam(description = "OMOP domain, e.g. condition, drug") String domain) {
        return McpCall.guard(() -> cdmResults.getConceptRecordCount(context.requireSource(sourceKey), domain));
    }

    @Tool(description = "Get the status of a job execution by jobId. Use to poll generation/analysis jobs.")
    public McpResult jobStatus(
            @ToolParam(description = "Job id returned by a *_generate/*_execute tool") long jobId) {
        return McpCall.guard(() -> jobService.getJob(jobId));
    }

    @Tool(description = "List recent job executions.")
    public McpResult jobListRecent() {
        return McpCall.guard(() -> jobService.getExecutions());
    }
}
```
Replace `getDashboard`, `getConceptRecordCount`, `getJob`, `getExecutions` with the exact names/params from Step 1 (e.g. the dashboard method may be named `getDashboard(String sourceKey)`; the job status method may take `int jobId` and require an execution id). If `cdmResultsDomainCounts`' backing method takes different params, adjust the tool params to match.

- [ ] **Step 5: Run unit test, expect pass**

Run: `mvn -q -Dtest=SourceJobToolsTest test -Dpackaging.type=jar`
Expected: PASS.

- [ ] **Step 6: Extend the registration IT to assert these tools appear**

Add to `McpSecurityPropagationIT`:
```java
    @Test
    void sourceAndJobToolsAreRegistered() {
        assertThat(java.util.Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(t -> t.getToolDefinition().name()))
                .contains("sourceList", "cdmResultsDashboard", "jobStatus");
    }
```

- [ ] **Step 7: Run it, expect pass**

Run: `mvn -q -Dtest=McpSecurityPropagationIT test -Dpackaging.type=jar`
Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/org/ohdsi/webapi/mcp/tools/SourceJobTools.java \
  src/test/java/org/ohdsi/webapi/mcp/tools/SourceJobToolsTest.java \
  src/test/java/org/ohdsi/webapi/mcp/McpSecurityPropagationIT.java
git commit -m "feat(mcp): add source, CDM-results and job tools"
```

---

### Task 6: Concept-set tools

**Files:**
- Create: `src/main/java/org/ohdsi/webapi/mcp/tools/ConceptSetTools.java`
- Test: `src/test/java/org/ohdsi/webapi/mcp/tools/ConceptSetToolsTest.java`

**Interfaces:**
- Consumes: `ConceptSetService.getConceptSets()`, `.getConceptSet(int)`, `.getConceptSetExpressionById(int)`, `.createConceptSet(ConceptSetDTO)`, `.updateConceptSet(int, ConceptSetDTO)`; `VocabularyService.resolveConceptSetExpression(String, ConceptSetExpression)`; `McpToolContext`.
- Produces: `ConceptSetTools implements McpToolset` with `@Tool` methods `conceptsetList`, `conceptsetGet`, `conceptsetExpression`, `conceptsetCreate`, `conceptsetUpdate`, `conceptsetResolve` (the resolve tool surfaces the included standard concept ids).

- [ ] **Step 1: Confirm resolve + update signatures**

Run:
```bash
cd /home/ph/code/WebAPI
sed -n '1038,1130p' src/main/java/org/ohdsi/webapi/service/VocabularyService.java | grep -nE "public .*\("
sed -n '520,640p' src/main/java/org/ohdsi/webapi/conceptset/ConceptSetService.java | grep -nE "public .*\("
```
Expected: exact `resolveConceptSetExpression(...)`, `getConceptSetExpressionById(...)`, `createConceptSet(...)`, `updateConceptSet(...)` signatures. Also confirm whether create/update take a `ConceptSetDTO` (which embeds the expression) — copy the DTO's expression field name via `grep -nE "expression|ConceptSetExpression" src/main/java/org/ohdsi/webapi/conceptset/dto/ConceptSetDTO.java` (path may differ; locate with `grep -rl "class ConceptSetDTO"`).

- [ ] **Step 2: Write the failing unit test**

`src/test/java/org/ohdsi/webapi/mcp/tools/ConceptSetToolsTest.java`:
```java
package org.ohdsi.webapi.mcp.tools;

import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.conceptset.ConceptSetService;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.service.VocabularyService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConceptSetToolsTest {

    private final ConceptSetService conceptSets = mock(ConceptSetService.class);
    private final VocabularyService vocab = mock(VocabularyService.class);
    private final McpToolContext context = mock(McpToolContext.class);
    private final ConceptSetTools tools = new ConceptSetTools(conceptSets, vocab, context);

    @Test
    void listReturnsConceptSets() {
        when(conceptSets.getConceptSets()).thenReturn(List.of());
        McpResult r = tools.conceptsetList();
        assertThat(r.ok()).isTrue();
    }
}
```

- [ ] **Step 3: Run it, expect failure**

Run: `mvn -q -Dtest=ConceptSetToolsTest test -Dpackaging.type=jar`
Expected: FAIL.

- [ ] **Step 4: Implement `ConceptSetTools`**

`src/main/java/org/ohdsi/webapi/mcp/tools/ConceptSetTools.java`:
```java
package org.ohdsi.webapi.mcp.tools;

import org.ohdsi.webapi.conceptset.ConceptSetService;
import org.ohdsi.webapi.mcp.McpToolset;
import org.ohdsi.webapi.mcp.support.McpCall;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.service.VocabularyService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** MCP tools for browsing, creating, and resolving concept sets. */
@Component
public class ConceptSetTools implements McpToolset {

    private final ConceptSetService conceptSets;
    private final VocabularyService vocab;
    private final McpToolContext context;

    public ConceptSetTools(ConceptSetService conceptSets, VocabularyService vocab, McpToolContext context) {
        this.conceptSets = conceptSets;
        this.vocab = vocab;
        this.context = context;
    }

    @Tool(description = "List all concept sets (id and name).")
    public McpResult conceptsetList() {
        return McpCall.guard(conceptSets::getConceptSets);
    }

    @Tool(description = "Get a concept set's metadata by id.")
    public McpResult conceptsetGet(
            @ToolParam(description = "Concept set id") int id) {
        return McpCall.guard(() -> conceptSets.getConceptSet(id));
    }

    @Tool(description = "Get the full expression (concept items with include-descendants / mapped flags) of a concept set.")
    public McpResult conceptsetExpression(
            @ToolParam(description = "Concept set id") int id) {
        return McpCall.guard(() -> conceptSets.getConceptSetExpressionById(id));
    }

    @Tool(description = "Create a new concept set from a ConceptSetDTO (name + expression items). "
            + "Returns the created concept set with its new id.")
    public McpResult conceptsetCreate(
            @ToolParam(description = "ConceptSetDTO JSON: name and expression.items[]") org.ohdsi.webapi.conceptset.ConceptSetDTO dto) {
        return McpCall.guard(() -> conceptSets.createConceptSet(dto));
    }

    @Tool(description = "Update an existing concept set (name and/or expression) by id.")
    public McpResult conceptsetUpdate(
            @ToolParam(description = "Concept set id") int id,
            @ToolParam(description = "ConceptSetDTO JSON with updated fields") org.ohdsi.webapi.conceptset.ConceptSetDTO dto) {
        return McpCall.guard(() -> conceptSets.updateConceptSet(id, dto));
    }

    @Tool(description = "Resolve a concept set's expression to the list of included standard concept ids "
            + "against a data source. Use source_list to find valid sourceKey values.")
    public McpResult conceptsetResolve(
            @ToolParam(description = "Source key (see source_list)") String sourceKey,
            @ToolParam(description = "Concept set id") int id) {
        return McpCall.guard(() -> {
            String key = context.requireSource(sourceKey);
            return vocab.resolveConceptSetExpression(key, conceptSets.getConceptSetExpressionById(id));
        });
    }
}
```
Fix the import path of `ConceptSetDTO` and the `resolveConceptSetExpression` signature to what Step 1 confirmed (the resolve method may accept the expression object or an id; adapt accordingly). Rename `conceptsetExpression`→`conceptsetIncludedConcepts` only if you additionally surface resolved concept detail; otherwise keep both a raw-expression tool and the resolve tool as written.

- [ ] **Step 5: Run unit test, expect pass**

Run: `mvn -q -Dtest=ConceptSetToolsTest test -Dpackaging.type=jar`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/ohdsi/webapi/mcp/tools/ConceptSetTools.java \
  src/test/java/org/ohdsi/webapi/mcp/tools/ConceptSetToolsTest.java
git commit -m "feat(mcp): add concept-set tools"
```

---

### Task 7: Cohort tools

**Files:**
- Create: `src/main/java/org/ohdsi/webapi/mcp/tools/CohortTools.java`
- Test: `src/test/java/org/ohdsi/webapi/mcp/tools/CohortToolsTest.java`

**Interfaces:**
- Consumes: `CohortDefinitionService.getCohortDefinitionList()`, get-by-id, create/update, `generateCohort(int, String)`, `getInfo(int)`, inclusion-report method; `McpToolContext`.
- Produces: `CohortTools implements McpToolset` with `@Tool` methods `cohortList`, `cohortGet`, `cohortCreate`, `cohortUpdate`, `cohortGenerate`, `cohortGenerationStatus`, `cohortInclusionReport`.

- [ ] **Step 1: Confirm exact method names/signatures**

Run:
```bash
cd /home/ph/code/WebAPI
sed -n '719,1200p' src/main/java/org/ohdsi/webapi/cohortdefinition/CohortDefinitionService.java \
 | grep -nE "public .*(getCohortDefinition|createCohortDefinition|saveCohortDefinition|generateCohort|getInfo|getInclusion|InclusionRules|CohortDTO)\b"
grep -rl "class CohortDTO" src/main/java
```
Expected: exact names — likely `getCohortDefinition(int id)`→`CohortDTO`, `createCohortDefinition(CohortDTO)`, `saveCohortDefinition(int, CohortDTO)`, `generateCohort(int, String)`→`JobExecutionResource`, `getInfo(int)`, and an inclusion-report method around line 1153. Copy them and `CohortDTO`'s package.

- [ ] **Step 2: Write the failing unit test**

`src/test/java/org/ohdsi/webapi/mcp/tools/CohortToolsTest.java`:
```java
package org.ohdsi.webapi.mcp.tools;

import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionService;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CohortToolsTest {

    private final CohortDefinitionService cohorts = mock(CohortDefinitionService.class);
    private final McpToolContext context = mock(McpToolContext.class);
    private final CohortTools tools = new CohortTools(cohorts, context);

    @Test
    void listReturnsCohorts() {
        when(cohorts.getCohortDefinitionList()).thenReturn(List.of());
        McpResult r = tools.cohortList();
        assertThat(r.ok()).isTrue();
    }

    @Test
    void generateValidatesSourceKey() {
        when(context.requireSource("BAD"))
                .thenThrow(new IllegalArgumentException("Unknown sourceKey 'BAD'. Valid keys: [DEMO_CDM]"));
        McpResult r = tools.cohortGenerate(1, "BAD");
        assertThat(r.status()).isEqualTo("invalid_input");
        assertThat(r.message()).contains("DEMO_CDM");
    }
}
```

- [ ] **Step 3: Run it, expect failure**

Run: `mvn -q -Dtest=CohortToolsTest test -Dpackaging.type=jar`
Expected: FAIL.

- [ ] **Step 4: Implement `CohortTools`**

`src/main/java/org/ohdsi/webapi/mcp/tools/CohortTools.java`:
```java
package org.ohdsi.webapi.mcp.tools;

import org.ohdsi.webapi.cohortdefinition.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionService;
import org.ohdsi.webapi.mcp.McpToolset;
import org.ohdsi.webapi.mcp.support.McpCall;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** MCP tools for cohort definitions: browse, edit, generate, read results. */
@Component
public class CohortTools implements McpToolset {

    private final CohortDefinitionService cohorts;
    private final McpToolContext context;

    public CohortTools(CohortDefinitionService cohorts, McpToolContext context) {
        this.cohorts = cohorts;
        this.context = context;
    }

    @Tool(description = "List all cohort definitions (id, name, metadata).")
    public McpResult cohortList() {
        return McpCall.guard(cohorts::getCohortDefinitionList);
    }

    @Tool(description = "Get a cohort definition (including its Circe expression) by id.")
    public McpResult cohortGet(
            @ToolParam(description = "Cohort definition id") int id) {
        return McpCall.guard(() -> cohorts.getCohortDefinition(id));
    }

    @Tool(description = "Create a new cohort definition from a CohortDTO (name, description, expressionType, expression).")
    public McpResult cohortCreate(
            @ToolParam(description = "CohortDTO JSON") CohortDTO dto) {
        return McpCall.guard(() -> cohorts.createCohortDefinition(dto));
    }

    @Tool(description = "Update an existing cohort definition by id.")
    public McpResult cohortUpdate(
            @ToolParam(description = "Cohort definition id") int id,
            @ToolParam(description = "CohortDTO JSON with updated fields") CohortDTO dto) {
        return McpCall.guard(() -> cohorts.saveCohortDefinition(id, dto));
    }

    @Tool(description = "Trigger generation of a cohort against a data source. Returns a job handle; "
            + "poll job_status with the returned execution id. Generation runs long SQL and is expensive.")
    public McpResult cohortGenerate(
            @ToolParam(description = "Cohort definition id") int id,
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> cohorts.generateCohort(id, context.requireSource(sourceKey)));
    }

    @Tool(description = "Get generation status/info for a cohort across sources (counts, dates, state).")
    public McpResult cohortGenerationStatus(
            @ToolParam(description = "Cohort definition id") int id) {
        return McpCall.guard(() -> cohorts.getInfo(id));
    }

    @Tool(description = "Get the inclusion-rule / attrition report for a generated cohort on a source.")
    public McpResult cohortInclusionReport(
            @ToolParam(description = "Cohort definition id") int id,
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> cohorts.getInclusionRules(id, context.requireSource(sourceKey)));
    }
}
```
Replace `getCohortDefinition`, `createCohortDefinition`, `saveCohortDefinition`, `getInclusionRules`, and the `CohortDTO` import with the exact names confirmed in Step 1. (`saveCohortDefinition(int, CohortDTO)` was verified at line 867; the create method and inclusion-report name must be copied from the grep. If create is actually a POST without id, find its method — often `createCohortDefinition(CohortDTO)`.)

- [ ] **Step 5: Run unit tests, expect pass**

Run: `mvn -q -Dtest=CohortToolsTest test -Dpackaging.type=jar`
Expected: PASS (2 tests).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/ohdsi/webapi/mcp/tools/CohortTools.java \
  src/test/java/org/ohdsi/webapi/mcp/tools/CohortToolsTest.java
git commit -m "feat(mcp): add cohort definition tools (browse/edit/generate/results)"
```

---

### Task 8: Analysis tools (characterization, incidence rate, pathway, feature analysis)

**Files:**
- Create: `src/main/java/org/ohdsi/webapi/mcp/tools/AnalysisTools.java`
- Test: `src/test/java/org/ohdsi/webapi/mcp/tools/AnalysisToolsTest.java`

**Interfaces:**
- Consumes: `CcController` (list/get/create/generate/results), `IRAnalysisService` (`getIRAnalysisList`, `getAnalysis`, `createAnalysis`, `performAnalysis`, `getAnalysisReport`, `getAnalysisInfo`), `PathwayController` (list/get/create/generate/results), `FeAnalysisController` (list/get); `McpToolContext`.
- Produces: `AnalysisTools implements McpToolset` with `@Tool` methods `characList`, `characGet`, `characCreate`, `characGenerate`, `characResults`, `irList`, `irGet`, `irCreate`, `irExecute`, `irResults`, `irStatus`, `pathwayList`, `pathwayGet`, `pathwayCreate`, `pathwayGenerate`, `pathwayResults`, `feanalysisList`, `feanalysisGet`.

- [ ] **Step 1: Confirm CC / pathway / FE method names and DTO types**

Run:
```bash
cd /home/ph/code/WebAPI
grep -nE "public .*\(" src/main/java/org/ohdsi/webapi/cohortcharacterization/CcController.java | grep -iE "list|get|create|import|generat|result|design" | head -20
grep -nE "public .*\(" src/main/java/org/ohdsi/webapi/pathway/PathwayController.java | grep -iE "list|get|create|generat|result" | head -20
grep -nE "public .*\(" src/main/java/org/ohdsi/webapi/feanalysis/FeAnalysisController.java | grep -iE "list|get\b" | head
```
Expected: exact method names and their DTO parameter/return types for CC (`list`, get, create, `generateCc?`, `getGenerationsResults`), pathway (`list`, get, `create`, generate, results), FE (`list`, get). Copy them. Note the `list` methods take `Pageable`; the tools expose `page`/`size` ints and build a `PageRequest`.

- [ ] **Step 2: Write the failing unit test**

`src/test/java/org/ohdsi/webapi/mcp/tools/AnalysisToolsTest.java`:
```java
package org.ohdsi.webapi.mcp.tools;

import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.cohortcharacterization.CcController;
import org.ohdsi.webapi.feanalysis.FeAnalysisController;
import org.ohdsi.webapi.ircalc.IRAnalysisService;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.pathway.PathwayController;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalysisToolsTest {

    private final CcController cc = mock(CcController.class);
    private final IRAnalysisService ir = mock(IRAnalysisService.class);
    private final PathwayController pathway = mock(PathwayController.class);
    private final FeAnalysisController fe = mock(FeAnalysisController.class);
    private final McpToolContext context = mock(McpToolContext.class);
    private final AnalysisTools tools = new AnalysisTools(cc, ir, pathway, fe, context);

    @Test
    void irListReturnsAnalyses() {
        when(ir.getIRAnalysisList()).thenReturn(List.of());
        McpResult r = tools.irList();
        assertThat(r.ok()).isTrue();
    }

    @Test
    void irExecuteValidatesSourceKey() {
        when(context.requireSource("BAD"))
                .thenThrow(new IllegalArgumentException("Unknown sourceKey 'BAD'. Valid keys: [DEMO_CDM]"));
        McpResult r = tools.irExecute(5, "BAD");
        assertThat(r.status()).isEqualTo("invalid_input");
    }
}
```

- [ ] **Step 3: Run it, expect failure**

Run: `mvn -q -Dtest=AnalysisToolsTest test -Dpackaging.type=jar`
Expected: FAIL.

- [ ] **Step 4: Implement `AnalysisTools`**

`src/main/java/org/ohdsi/webapi/mcp/tools/AnalysisTools.java`:
```java
package org.ohdsi.webapi.mcp.tools;

import org.ohdsi.webapi.cohortcharacterization.CcController;
import org.ohdsi.webapi.feanalysis.FeAnalysisController;
import org.ohdsi.webapi.ircalc.IRAnalysisService;
import org.ohdsi.webapi.mcp.McpToolset;
import org.ohdsi.webapi.mcp.support.McpCall;
import org.ohdsi.webapi.mcp.support.McpResult;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.pathway.PathwayController;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/** MCP tools for the four analysis types: characterization, incidence rate, pathway, feature. */
@Component
public class AnalysisTools implements McpToolset {

    private final CcController cc;
    private final IRAnalysisService ir;
    private final PathwayController pathway;
    private final FeAnalysisController fe;
    private final McpToolContext context;

    public AnalysisTools(CcController cc, IRAnalysisService ir, PathwayController pathway,
                         FeAnalysisController fe, McpToolContext context) {
        this.cc = cc;
        this.ir = ir;
        this.pathway = pathway;
        this.fe = fe;
        this.context = context;
    }

    // ---- Cohort characterization ----
    @Tool(description = "List cohort characterizations (paged).")
    public McpResult characList(
            @ToolParam(required = false, description = "0-based page") Integer page,
            @ToolParam(required = false, description = "page size") Integer size) {
        return McpCall.guard(() -> cc.list(PageRequest.of(page == null ? 0 : page, size == null ? 20 : size)));
    }

    @Tool(description = "Get a cohort characterization by id.")
    public McpResult characGet(@ToolParam(description = "Characterization id") long id) {
        return McpCall.guard(() -> cc.get(id));
    }

    @Tool(description = "Trigger generation of a cohort characterization against a source. Returns a job handle.")
    public McpResult characGenerate(
            @ToolParam(description = "Characterization id") Long id,
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> cc.generate(id, context.requireSource(sourceKey)));
    }

    @Tool(description = "Get the results of a characterization generation by generationId.")
    public McpResult characResults(@ToolParam(description = "Generation id") Long generationId) {
        return McpCall.guard(() -> cc.getGenerationsResults(generationId, null));
    }

    // ---- Incidence rate ----
    @Tool(description = "List incidence-rate analyses.")
    public McpResult irList() {
        return McpCall.guard(ir::getIRAnalysisList);
    }

    @Tool(description = "Get an incidence-rate analysis by id.")
    public McpResult irGet(@ToolParam(description = "IR analysis id") int id) {
        return McpCall.guard(() -> ir.getAnalysis(id));
    }

    @Tool(description = "Trigger execution of an incidence-rate analysis against a source. Returns a job handle.")
    public McpResult irExecute(
            @ToolParam(description = "IR analysis id") int id,
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> ir.performAnalysis(id, context.requireSource(sourceKey)));
    }

    @Tool(description = "Get the incidence-rate report for an analysis on a source and a target/outcome pair.")
    public McpResult irResults(
            @ToolParam(description = "IR analysis id") int id,
            @ToolParam(description = "Source key (see source_list)") String sourceKey,
            @ToolParam(description = "Target cohort id") int targetId,
            @ToolParam(description = "Outcome cohort id") int outcomeId) {
        return McpCall.guard(() -> ir.getAnalysisReport(id, context.requireSource(sourceKey), targetId, outcomeId));
    }

    @Tool(description = "Get execution status/info for an incidence-rate analysis across sources.")
    public McpResult irStatus(@ToolParam(description = "IR analysis id") int id) {
        return McpCall.guard(() -> ir.getAnalysisInfo(id));
    }

    // ---- Pathway ----
    @Tool(description = "List pathway analyses (paged).")
    public McpResult pathwayList(
            @ToolParam(required = false, description = "0-based page") Integer page,
            @ToolParam(required = false, description = "page size") Integer size) {
        return McpCall.guard(() -> pathway.list(PageRequest.of(page == null ? 0 : page, size == null ? 20 : size)));
    }

    @Tool(description = "Get a pathway analysis by id.")
    public McpResult pathwayGet(@ToolParam(description = "Pathway analysis id") int id) {
        return McpCall.guard(() -> pathway.get(id));
    }

    @Tool(description = "Trigger generation of a pathway analysis against a source. Returns a job handle.")
    public McpResult pathwayGenerate(
            @ToolParam(description = "Pathway analysis id") int id,
            @ToolParam(description = "Source key (see source_list)") String sourceKey) {
        return McpCall.guard(() -> pathway.generate(id, context.requireSource(sourceKey)));
    }

    @Tool(description = "Get pathway-analysis results by generationId.")
    public McpResult pathwayResults(@ToolParam(description = "Generation id") Long generationId) {
        return McpCall.guard(() -> pathway.getResults(generationId));
    }

    // ---- Feature analysis ----
    @Tool(description = "List feature analyses (paged).")
    public McpResult feanalysisList(
            @ToolParam(required = false, description = "0-based page") Integer page,
            @ToolParam(required = false, description = "page size") Integer size) {
        return McpCall.guard(() -> fe.list(PageRequest.of(page == null ? 0 : page, size == null ? 20 : size)));
    }

    @Tool(description = "Get a feature analysis by id.")
    public McpResult feanalysisGet(@ToolParam(description = "Feature analysis id") int id) {
        return McpCall.guard(() -> fe.get(id));
    }
}
```
Fix every method name that Step 1 shows differs (`cc.get`, `cc.generate`, `cc.getGenerationsResults`, `pathway.get`, `pathway.generate`, `pathway.getResults`, `fe.get`). Match parameter types exactly (e.g. `Long` vs `int`, and whether `getGenerationsResults` takes a second arg — the verified signature is `getGenerationsResults(Long generationId, ...)`; pass the confirmed args). For create tools (`characCreate`, `irCreate`, `pathwayCreate`), add them once their exact DTO types are confirmed, following the same `guard(() -> bean.create(dto))` shape; if a create signature is awkward (e.g. POST `/{id}` upsert), model the tool param on the actual method.

- [ ] **Step 5: Run unit tests, expect pass**

Run: `mvn -q -Dtest=AnalysisToolsTest test -Dpackaging.type=jar`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/ohdsi/webapi/mcp/tools/AnalysisTools.java \
  src/test/java/org/ohdsi/webapi/mcp/tools/AnalysisToolsTest.java
git commit -m "feat(mcp): add characterization, IR, pathway and feature-analysis tools"
```

---

### Task 9: Tool-registration snapshot test and documentation

**Files:**
- Test: `src/test/java/org/ohdsi/webapi/mcp/McpToolRegistrationIT.java`
- Modify: `CLAUDE.md`

**Interfaces:**
- Consumes: the `ToolCallbackProvider` bean with all five tool classes registered.

- [ ] **Step 1: Write the snapshot test asserting the full tool set**

`src/test/java/org/ohdsi/webapi/mcp/McpToolRegistrationIT.java`:
```java
package org.ohdsi.webapi.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "mcp.server.enabled=true")
class McpToolRegistrationIT /* extends <ProjectIntegrationTestBase> */ {

    @Autowired
    ToolCallbackProvider toolCallbackProvider;

    @Test
    void allExpectedToolsAreRegistered() {
        List<String> names = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(t -> t.getToolDefinition().name())
                .toList();

        assertThat(names).containsExactlyInAnyOrder(
                // vocabulary
                "vocabSearchConcepts", "vocabGetConcept", "vocabRelatedConcepts",
                "vocabConceptAncestors", "vocabConceptDescendants", "vocabDomains", "vocabVocabularies",
                // source/jobs/results
                "sourceList", "cdmResultsDashboard", "cdmResultsDomainCounts", "jobStatus", "jobListRecent",
                // concept sets
                "conceptsetList", "conceptsetGet", "conceptsetExpression",
                "conceptsetCreate", "conceptsetUpdate", "conceptsetResolve",
                // cohorts
                "cohortList", "cohortGet", "cohortCreate", "cohortUpdate",
                "cohortGenerate", "cohortGenerationStatus", "cohortInclusionReport",
                // analyses
                "characList", "characGet", "characGenerate", "characResults",
                "irList", "irGet", "irExecute", "irResults", "irStatus",
                "pathwayList", "pathwayGet", "pathwayGenerate", "pathwayResults",
                "feanalysisList", "feanalysisGet");
    }
}
```
If create-tools were added in Task 8 (`characCreate`, `irCreate`, `pathwayCreate`), add their names here. Update this list to be the single source of truth for the registered tool surface.

- [ ] **Step 2: Run it, expect pass (fix mismatches)**

Run: `mvn -q -Dtest=McpToolRegistrationIT test -Dpackaging.type=jar`
Expected: PASS. If it fails, the message lists missing/extra tools — reconcile names with the tool classes until exact.

- [ ] **Step 3: Document enabling and connecting in `CLAUDE.md`**

Add a section to `CLAUDE.md` after "## Testing":
```markdown
## MCP Server (analyst copilot)

WebAPI can expose an embedded Model Context Protocol server with ~40 analyst tools
(vocabulary, concept sets, cohorts, characterization, incidence rate, pathway,
feature analysis, sources/jobs/results). It is **disabled by default**.

Enable it:
```bash
DATASOURCE_PASSWORD=mypass MCP_SERVER_ENABLED=true java -jar target/WebAPI.jar
```

Connect an MCP client (e.g. Claude Desktop/Code) to:
```
URL:    http://<host>:8080/WebAPI/mcp
Header: X-API-KEY: <a personal API key>
```
Create an API key via `POST /WebAPI/user/apikeys` (the raw key is shown once).
Every tool runs under that user's WebAPI permissions — tools that hit a data
source require the corresponding `read:source`/source-access grants.
```

- [ ] **Step 4: Full build + full MCP test suite**

Run: `mvn -q -Dtest='org.ohdsi.webapi.mcp.**' test -Dpackaging.type=jar`
Expected: all MCP unit + integration tests PASS.

- [ ] **Step 5: Confirm the app still builds packaged with flag off**

Run: `mvn -q clean package -DskipTests -Dpackaging.type=jar -P trexsql`
Expected: BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add src/test/java/org/ohdsi/webapi/mcp/McpToolRegistrationIT.java CLAUDE.md
git commit -m "feat(mcp): add tool-registration snapshot test and docs"
```

---

## Self-Review Notes

**Spec coverage:**
- Embedded Spring AI MCP server, feature-flagged → Task 1.
- `McpResult` envelope + error taxonomy (permission_denied/invalid_input/upstream_error) → Task 2.
- Source-key resolution + `/mcp` security wiring → Task 3.
- SecurityContext → `@PreAuthorize` propagation (highest risk) → Task 4 (dedicated IT, Step 7).
- Approach A (call gated beans directly) → every tool task.
- All four domains + supporting tools → Tasks 4–8 (~28 tools; see registration snapshot in Task 9).
- Async job handle + poll pattern → `*_generate`/`*_execute` return `JobExecutionResource`; `jobStatus` polls (Tasks 5,7,8).
- Compact output shaping → tools return DTOs wrapped in `McpResult`; a `fields`/`verbose` option is deferred as YAGNI unless model context proves too large (noted, not built).
- Tests: unit per class, propagation IT, e2e IT, registration snapshot → Tasks 2–9.
- Docs + rollout flag → Task 9 + Task 1 properties.

**Deferred (documented, not built):** `verbose`/`fields` per-tool projection; pathway/FE create+generate beyond list/get were included as tools where a clean gated method exists, otherwise limited to read (per Task 8 Step 4 guidance).

**Verification steps vs placeholders:** Several tasks begin with a `grep` "confirm signature" step. These are not placeholders for missing logic — the target methods are verified to exist; only their exact Java identifiers must be copied to avoid compile drift. Each such step names the file, line range, and what to copy.
