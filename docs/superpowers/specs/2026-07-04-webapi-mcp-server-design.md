# WebAPI MCP Server — Design

**Date:** 2026-07-04
**Status:** Approved design, pre-implementation
**Author:** Peter Hoffmann (with Claude)

## 1. Purpose & Goal

Expose OHDSI WebAPI's analyst-facing capabilities to LLM clients (Claude Desktop,
Claude Code, etc.) through a **Model Context Protocol (MCP)** server, so an LLM can
act as an **analyst copilot**: search vocabulary/concepts, build and generate
cohorts, run characterizations and incidence-rate analyses, and read results —
under the same permissions as a real WebAPI user.

The MCP server is **embedded in the existing Spring Boot WebAPI application** (not a
separate service), reusing WebAPI's business logic and Spring Security authorization
directly in-process.

### Scope (v1)

Curated, task-oriented tools (~28) across four domains:

- **Vocabulary & concepts** (read-only)
- **Concept sets** (read + create/edit + resolve)
- **Cohorts** (read + create/edit + generate)
- **Analyses** — characterization, incidence rate, pathway, feature analysis
  (read + create + run + results)
- **Sources, jobs & results** (read-only supporting tools)

### Out of scope (v1)

- Admin/security management (users, roles, permissions, API-key CRUD, source CRUD,
  user import, tags). API keys are *consumed* for auth but not managed via MCP.
- A 1:1 mapping of all ~386 endpoints. Tools are curated for analyst workflows.

## 2. Architecture

### Runtime

Embedded in the WebAPI Spring Boot app. Built on **Spring AI's MCP server starter**
(`spring-ai-starter-mcp-server-webmvc`), which fits the existing servlet / Spring MVC
stack and exposes MCP over **streamable HTTP + SSE**.

- Add `spring-ai-bom` to `dependencyManagement` in `pom.xml`, pinned to the version
  compatible with Spring Boot 3.5.6 / Java 21. Verify compatibility at implementation
  time.
- Activated by a feature flag (`mcp.server.enabled`, default `false`) so it ships dark
  and is enabled per-deployment.

### Package layout

```
src/main/java/org/ohdsi/webapi/mcp/
  McpServerConfig.java        // registers MCP server + ToolCallbackProvider, @ConditionalOnProperty
  McpSecurityConfig.java      // adds /mcp/** to the Spring Security filter chain
  tools/
    VocabularyTools.java      // @Tool methods → call VocabularyService bean
    ConceptSetTools.java
    CohortTools.java
    AnalysisTools.java        // characterization, IR, pathway, feature analysis
    SourceJobTools.java       // sources, CDM results, job status
  support/
    McpToolContext.java       // resolves sourceKey, current user, pagination helpers
    McpResult.java            // uniform result/error envelope returned to the model
    SecurityContextToolDecorator.java  // propagates SecurityContext onto tool threads
```

- Each `tools/*.java` class is a `@Component` holding a handful of `@Tool`-annotated
  methods. **Tool classes are thin:** validate/coerce inputs → call the injected
  `@RestController` bean method → map the DTO into a compact `McpResult`. No business
  logic lives in the tool layer.
- A single `ToolCallbackProvider` bean aggregates all tool classes — the one
  registration point.
- `support/` holds cross-cutting concerns (source resolution, result shaping, security
  propagation) so tool methods stay readable and each unit has one clear purpose.

### Invocation strategy (chosen: Approach A)

Tools call the existing `@PreAuthorize`-gated `@RestController` / service beans
directly. Because those beans are Spring AOP proxies, **method security fires exactly
as it does over HTTP** — authorization is reused for free, along with all business
logic and DTOs. No network hop, no logic duplication.

Rejected alternatives:

- **B. Internal HTTP self-call** — full fidelity but adds a network hop and JSON
  re-serialization per call for no in-process benefit.
- **C. Extract a new shared service layer** — cleanest long-term but a large refactor
  across 34 controllers; disproportionate for v1.

## 3. Transport, Authentication & Security-Context Propagation

### Transport

MCP over streamable HTTP + SSE, mounted at `/WebAPI/mcp` (configurable via
`spring.ai.mcp.server.*`). MCP clients connect to that URL.

### Authentication

No new auth code. `/mcp/**` is added to the existing Spring Security filter chain, so
the already-present `ApiKeyAuthFilter` runs first:

- The MCP client sends the `X-API-KEY` header (see
  `security/apikey/ApiKeyAuthFilter.java`, header constant `API_KEY_HEADER`).
- The filter validates the key and populates the `SecurityContext` with a
  `WebApiAuthenticationToken`.
- Invalid/absent key → 401 before any tool runs.
- Each MCP user therefore acts as a real WebAPI user with their own permissions.

### Security-context propagation (highest-risk mechanism)

MCP tool invocations may run on a different thread than the HTTP request thread
(SSE / async dispatch). `@PreAuthorize` reads the `SecurityContext` from a
`ThreadLocal`; if the context does not follow the tool thread, gated calls fail or —
worse — run without the intended principal.

Mitigation:

- Implement `SecurityContextToolDecorator` that snapshots the `SecurityContext` at
  request time and explicitly re-establishes it on the executing thread per tool call,
  clearing it in a `finally` block. Preferred over relying solely on
  `MODE_INHERITABLETHREADLOCAL`.
- Dedicated integration test asserts propagation both ways (see §5).

### Servlet-coupled methods

A few controller methods return `ResponseEntity` / `StreamingResponseBody` or read
`HttpServletRequest`. Tools never call those variants. Where a domain only offers a
servlet-coupled method, the tool calls the underlying service method — deliberately
choosing the `@PreAuthorize`-gated path (never the "ungated internal" overloads) to
preserve authorization. Any method that cannot be cleanly called in-process is listed
as out-of-scope for its tool.

## 4. Tool Inventory (~28)

Naming convention: `domain_action`. Each tool notes its backing bean and whether it
mutates.

### Vocabulary & concepts — `VocabularyService` (read-only)

- `vocab_search_concepts` — search by term with domain/vocabulary/standard filters
- `vocab_get_concept` — full detail for a conceptId
- `vocab_related_concepts` — relationships for a conceptId
- `vocab_concept_ancestors` / `vocab_concept_descendants` — hierarchy navigation
- `vocab_domains` / `vocab_vocabularies` — list domains/vocabularies (for filters)

### Concept sets — `ConceptSetService` (read + write)

- `conceptset_list` / `conceptset_get` — browse & read expression
- `conceptset_create` / `conceptset_update` — create/edit expression
- `conceptset_resolve` — resolve expression to included concept IDs against a source
- `conceptset_included_concepts` — resolved concepts with detail

### Cohorts — `CohortDefinitionService` (read + write + generate)

- `cohort_list` / `cohort_get` — browse & read definition
- `cohort_create` / `cohort_update` — create/edit (Circe expression JSON)
- `cohort_generate` — trigger generation against a `sourceKey` (returns job handle)
- `cohort_generation_status` — generation info per source
- `cohort_count` — resolved subject/entry counts
- `cohort_inclusion_report` — inclusion-rule results / attrition

### Analyses — `CcController`, `IRAnalysisService`, `PathwayController`, `FeAnalysisController` (read + write + generate)

- `charac_list` / `charac_get` / `charac_create` / `charac_generate` / `charac_results`
- `ir_list` / `ir_get` / `ir_create` / `ir_execute` / `ir_results`
- `pathway_list` / `pathway_get` / `pathway_create` / `pathway_generate` / `pathway_results`
- `feanalysis_list` / `feanalysis_get`

### Sources, jobs & results — `SourceService`, `CDMResultsService`, `JobService`, `NotificationService` (read-only)

- `source_list` — sources + daimons/capabilities; the discovery entry point for
  `sourceKey` values used across generate/resolve tools
- `cdm_results_dashboard` / `cdm_results_domain_counts` — record counts / data density
- `job_status` — execution status by job id (poll generation/analysis jobs)
- `job_list_recent` — recent executions & notifications

### Cross-cutting conventions

- **Source resolution:** any tool needing a source takes a human-friendly `sourceKey`;
  `source_list` is how the model discovers valid keys. `McpToolContext` validates it and
  returns a clear error listing valid keys on mismatch.
- **Async awareness:** `*_generate` / `*_execute` return immediately with a job handle
  plus a note to poll `job_status`. They never block.
- **Output shaping:** tools return compact JSON (ids, names, key metrics) rather than
  raw WebAPI DTOs, to keep model context small. An optional `verbose`/`fields` argument
  pulls full detail when needed.

## 5. Error Handling & Testing

### Error handling

Every tool returns a uniform `McpResult` envelope so the model gets structured,
actionable text rather than a stack trace:

- `AccessDeniedException` (from `@PreAuthorize`) → `permission_denied`, naming the
  missing permission.
- Invalid input (bad `sourceKey`, unknown id, malformed expression) → `invalid_input`
  describing what was wrong; for source keys, lists valid options.
- Downstream/DB failure → `upstream_error` with a short message; full detail logged
  server-side, not returned to the model.
- One try/catch mapper in the tool decorator centralizes this so tools stay clean.

### Testing

- **Unit:** each tool class with its backing bean mocked — verifies input coercion and
  result shaping.
- **Integration** (Spring Boot test + embedded PostgreSQL, matching the repo's existing
  test pattern), MCP flag on:
  1. **Security-context propagation** — a gated tool with a low-privilege key throws
     `permission_denied`; with a permitted key it succeeds. (Highest-risk mechanism →
     dedicated test.)
  2. **End-to-end tool call** — e.g. `vocab_search_concepts` and `source_list` return
     well-formed results against the demo CDM.
  3. **Async handshake** — `cohort_generate` returns a job handle and `job_status`
     reflects it.
- **Tool-listing snapshot** — asserts registered tool names/count, so accidental
  additions/removals surface in review.

## 6. Rollout & Configuration

- Feature flag `mcp.server.enabled=false` by default (`@ConditionalOnProperty`) —
  ships dark, enabled per-deployment.
- Documentation: a short section in `CLAUDE.md` / README on enabling the server and
  pointing an MCP client at `/WebAPI/mcp` with an `X-API-KEY` header.
- No DB migrations — reuses existing API-key and permission tables.

## 7. Open Items for Implementation

- Confirm the exact Spring AI version compatible with Spring Boot 3.5.6 and pin it.
- Confirm each backing bean method signature and identify any servlet-coupled methods
  needing a non-HTTP call path.
- Decide the compact result field set per tool (which DTO fields to surface by default).
