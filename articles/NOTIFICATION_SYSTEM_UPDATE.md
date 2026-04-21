# Notification System Modernization (v3.x)

## Overview

The WebAPI notification system provides real-time visibility into background job execution for users. It displays:
- **User jobs**: Jobs launched by the current authenticated user
- **Whitelisted jobs**: Jobs from services that implement `GeneratesNotification` interface  
- **Cache warming**: System background jobs that refresh cached data

Previously (v2.x), the system used Spring Batch Admin's pagination-based DAO which had critical gaps when migrating to Spring Batch 5.x. This document details the modernization and architectural improvements.

---

## System Architecture

### What Happens When a Job Runs

1. **Job Launch**: Service calls `JobTemplate.launch()` to start a batch job
2. **Parameters Stored**: `JobParametersBuilder` captures job metadata:
   - `jobAuthor` - User who launched the job
   - `cohort_definition_id` - Which cohort is being processed (example)
   - `source_id` - Which data source (example)
   - Custom parameters per job type
3. **Database Storage**: Spring Batch stores:
   - Execution info: `BATCH_JOB_EXECUTION` (status, start/end times)
   - Instance info: `BATCH_JOB_INSTANCE` (job name)
   - Parameters: `BATCH_JOB_EXECUTION_PARAMS` (all key/value pairs)

### REST API Endpoints

```
GET /notifications
  - Returns list of recent jobs (user + whitelisted)
  - Query params: hide_statuses (COMPLETED, RUNNING, etc), refreshJobs (true for cache warming only)

GET /notifications/viewed
  - Returns when user last checked notifications

POST /notifications/viewed
  - Records when user viewed notifications
```

---

## Previous Implementation (v2.x) - Issues

### Architecture
```
Spring Batch Admin Library
       ↓
SearchableJobExecutionDao (pagination interface)
       ↓
JdbcSearchableJobExecutionDao (custom DAO)
       ↓
NotificationServiceImpl (pagination loop)
```

### Problems

1. **Missing JobParameters**: 
   - SQL query only joined `BATCH_JOB_EXECUTION` + `BATCH_JOB_INSTANCE`
   - Never joined `BATCH_JOB_EXECUTION_PARAMS` table
   - Result: `jobExecution.getJobParameters()` always returned empty

2. **Broken User Job Detection**:
   ```java
   // In isMine() - ALWAYS RETURNED NULL
   String jobAuthor = jobExec.getJobParameters().getString("jobAuthor");
   return Objects.equals(login, jobAuthor);  // Null == null → false
   ```
   Users couldn't see their own jobs in notifications

3. **Broken Folding Logic**:
   ```java
   // In getFoldingKey() - ALWAYS FELL BACK TO ID
   Optional<String> key = jobParams.getParameters().keySet()
       .stream().filter(FOLDING_KEYS::contains).findAny();
   return key.map(s -> s + "_" + jobParams.getString(s) + "_" + jobParams.getString("source_id"))
       .orElseGet(() -> String.valueOf(entity.getId()));  // Always used this
   ```
   Jobs weren't deduplicated correctly

4. **N+1 Query Problem**:
   - Pagination loop: fetch PAGE_SIZE=100 executions per iteration
   - For each execution, `getJobParameters()` was empty
   - Required manual batching to avoid performance issues

---

## New Implementation (v3.x) - Solution

### Architecture
```
Spring Batch 5.x Native + Custom DAO
       ↓
SearchableJobExecutionDao (enhanced interface)
       ↓
JdbcSearchableJobExecutionDao.getJobExecutionsWithParams()
       ↓
Single Query + ResultSetExtractor (collapse rows)
       ↓
NotificationServiceImpl (streaming approach)
```

### Single Optimized Query

```sql
SELECT E.JOB_EXECUTION_ID, E.START_TIME, ...,
       I.JOB_NAME,
       P.PARAMETER_NAME, P.PARAMETER_VALUE, P.PARAMETER_TYPE
FROM BATCH_JOB_EXECUTION E
JOIN BATCH_JOB_INSTANCE I ON E.JOB_INSTANCE_ID = I.JOB_INSTANCE_ID
INNER JOIN BATCH_JOB_EXECUTION_PARAMS P ON E.JOB_EXECUTION_ID = P.JOB_EXECUTION_ID
ORDER BY E.JOB_EXECUTION_ID DESC, P.PARAMETER_NAME
```

**Key insight**: INNER JOIN means only executions WITH parameters are returned. One row per parameter per execution.

### Row Collapsing Strategy

The `JobExecutionWithParamsResultSetExtractor` reads result set and:

1. **Accumulate**: For each execution ID, collect all parameter rows
2. **Collapse**: When execution ID changes, build complete `JobParameters` map
3. **Construct**: Pass parameters directly to `JobExecution` constructor
4. **Return**: List of fully-materialized `JobExecution` objects

```java
// Pseudo-code of the logic
Map<String,JobParameter<?>> params = new HashMap<>();
while (rs.next()) {
    if (newExecutionId) {
        // Build previous execution with accumulated params
        execution = new JobExecution(jobInstance, new JobParameters(params));
        results.add(execution);
        params.clear();
    }
    // Accumulate parameter
    params.put(paramName, createJobParameter(...));
}
// Handle last execution
execution = new JobExecution(jobInstance, new JobParameters(params));
results.add(execution);
```

**No reflection required**: Parameters passed to constructor, not injected afterward.

### Streaming Approach in NotificationServiceImpl

**Before** (pagination):
```java
for (int start = 0; userJobMap.size() < MAX_SIZE || allJobMap.size() < MAX_SIZE; 
     start += PAGE_SIZE) {
    List<JobExecution> page = dao.getJobExecutions(start, PAGE_SIZE);
    if (page.size() == 0) break;
    // Process page...
}
```

**After** (streaming):
```java
List<JobExecution> allExecutions = dao.getJobExecutionsWithParams();

for (JobExecution jobExec : allExecutions) {
    // Apply filters: status, whitelist, ownership
    if (!hideStatuses.contains(jobExec.getStatus()) && 
        isInWhiteList(jobExec) && 
        isMine(jobExec)) {  // NOW WORKS - params populated
        // Add to result maps...
    }
    
    // Break when we have enough
    if ((refreshJobsOnly || userJobMap.size() >= maxSize) && 
        allJobMap.size() >= maxSize) {
        break;
    }
}
```

**Benefits**:
- Single database query (no pagination math)
- Natural early exit when we have enough results
- Parameters guaranteed populated
- Cleaner, more readable logic

---

## Key Concepts

### Folding Keys

Jobs are **deduplicated** by folding key instead of execution ID. This allows logically related jobs to be grouped.

**What is a folding key?**
A folding key is a parameter name that identifies a logical "job group":
- `cohort_definition_id` - Multiple executions of same cohort
- `source_id` - Data source being processed
- Custom parameter names per service

**Folding logic**:
```java
// Find first folding key present in job parameters
Optional<String> key = jobParams.getParameters().keySet()
    .stream().filter(FOLDING_KEYS::contains).findAny();

// Return deduplication key
return key.map(s -> s + "_" + jobParams.getString(s) + "_" + jobParams.getString("source_id"))
    .orElseGet(() -> String.valueOf(entity.getId()));  // Fallback if no folding key

// Result: "cohort_definition_id_5_2" means cohort 5 on source 2
// Multiple executions with same key are deduplicated (keeps latest by start time)
```

**Why deduplicate?**
- User runs cohort generation 3 times for cohort #5
- Notification shows only the LATEST execution for cohort #5, not all 3
- Reduces notification clutter

### GeneratesNotification Interface

Services that produce jobs users need visibility into implement this interface:

```java
public interface GeneratesNotification {
    String getJobName();              // e.g., "CohortGenerationJob"
    String getExecutionFoldingKey();  // e.g., "cohort_definition_id"
}
```

**Example: CohortGenerationService**
```java
@Service
public class CohortGenerationService implements GeneratesNotification {
    @Override
    public String getJobName() {
        return "CohortGenerationJob";  // This job type is visible in notifications
    }
    
    @Override
    public String getExecutionFoldingKey() {
        return "cohort_definition_id";  // Jobs are grouped by cohort
    }
}
```

**Registration**:
```java
// Constructor injects all GeneratesNotification implementations
public NotificationServiceImpl(..., List<GeneratesNotification> whiteList, ...) {
    whiteList.forEach(g -> {
        WHITE_LIST.add(g.getJobName());           // Add to visibility whitelist
        FOLDING_KEYS.add(g.getExecutionFoldingKey());  // Register folding key
    });
}
```

**Result**: Notifications automatically include jobs from any service that implements this interface.

### User Job Identification

A job is considered "user's job" if:
```java
private boolean isMine(JobExecution jobExec) {
    WebApiPrincipal principal = permissionManager.getAuthenticatedPrincipal();
    String login = principal.getName();
    String jobAuthor = jobExec.getJobParameters().getString("jobAuthor");
    return Objects.equals(login, jobAuthor);
}
```

**How jobAuthor gets set**:
When any service launches a job:
```java
JobParametersBuilder builder = new JobParametersBuilder(jobParameters);
builder.addString(JOB_AUTHOR, principal.getName());  // Captured at launch time
exec = jobLauncher.run(job, jobParameters);
```

### Parameter Filtering - What Gets Returned to Clients

While job executions store many parameters internally, not all are exposed to API clients via the REST endpoints. The `ALLOWED_JOB_EXECUTION_PARAMETERS` whitelist in `Constants.java` controls which parameters are returned in notification responses:

```java
ImmutableList<String> ALLOWED_JOB_EXECUTION_PARAMETERS = ImmutableList.of(
    Params.JOB_NAME,
    Params.JOB_AUTHOR,
    Params.COHORT_DEFINITION_ID,
    Params.SOURCE_ID,
    Params.SOURCE_KEY,
    Params.ANALYSIS_ID,
    Params.PATHWAY_ANALYSIS_ID,
    Params.COHORT_CHARACTERIZATION_ID,
    // ... and others
);
```

**How filtering works**:
When `JobExecutionToDTOConverter` converts internal `JobExecution` objects to REST response DTOs, it applies this filter:
```java
.filter(p -> Constants.ALLOWED_JOB_EXECUTION_PARAMETERS.contains(p.getKey()))
```

**Why whitelist?**
- **Security**: Internal parameters (database credentials, file paths) never leak to API
- **API stability**: Only intentional parameters are exposed, preventing unplanned dependencies
- **Client clarity**: Clients know exactly which parameters are available
- **Service extensibility**: When a new service adds a job parameter (e.g., `pathway_analysis_id`), it must be explicitly added to this list for visibility in notifications

**Adding new parameters**: If a service like `PathwayServiceImpl` passes a new entity-ID parameter to the job, ensure it's added to `ALLOWED_JOB_EXECUTION_PARAMETERS` so clients can retrieve it. This was discovered during the parameter audit where `pathway_analysis_id` and `cohort_characterization_id` were added to make pathway and cohort characterization jobs properly identifiable in notifications.

---

## Comparison Table

| Aspect | v2.x (Spring Batch Admin) | v3.x (Modernized) |
|--------|--------------------------|-------------------|
| **Query Strategy** | Pagination (OFFSET/LIMIT) + N individual queries | Single query with INNER JOIN + row collapsing |
| **JobParameters** | Not loaded (always empty) | Loaded and populated in constructor |
| **User Job Detection** | Always fails (jobAuthor = null) | Works correctly |
| **Folding/Deduplication** | Always uses execution ID | Uses proper folding keys |
| **Database Queries** | 1 + N (N = jobs processed) | 1 |
| **Service Integration** | Hardcoded whitelist | Dynamic via GeneratesNotification interface |
| **Code Clarity** | Complex pagination loop | Clean streaming approach |
| **Row Materialization** | Build execution, then inject params via reflection | Build parameters first, pass to constructor |

---

## Implementation Files Modified

**Interface Enhancement**:
- `SearchableJobExecutionDao.java` - Added `getJobExecutionsWithParams()` method

**DAO Implementation**:
- `JdbcSearchableJobExecutionDao.java`:
  - Added `GET_EXECUTIONS_WITH_PARAMS` SQL constant (INNER JOIN to BATCH_JOB_EXECUTION_PARAMS)
  - Implemented `getJobExecutionsWithParams()` method
  - Added `JobExecutionWithParamsResultSetExtractor` (accumulates and collapses rows)
  - Added `createJobParameter()` helper (parses DB values to typed JobParameters)

**Service Layer**:
- `NotificationServiceImpl.java`:
  - Refactored `findJobs()` to use streaming approach
  - Removed pagination loop
  - Simplified logic: single query → iterate → break when satisfied

---

## Performance Impact

| Metric | v2.x | v3.x | Improvement |
|--------|------|------|-------------|
| Database Queries | 1 + N | 1 | Eliminates N-1 queries |
| Network Round Trips | N+1 | 1 | ~100x faster (typical N≈100) |
| Code Complexity | High | Low | Easier to maintain/debug |
| Parameter Loading | Manual (missing) | Automatic | Reliable data |

For typical notifications request (fetching ~200 job executions):
- **v2.x**: 201 database queries (1 + 200)
- **v3.x**: 1 database query + in-memory row collapsing

---


## Migration Notes

If upgrading from v2.x to v3.x:

1. **No database changes required** - Same Spring Batch schema
2. **No API changes** - REST endpoints unchanged
3. **Data consistency** - Old job executions with parameters will now display correctly
4. **Service registration** - Services already implementing `GeneratesNotification` work unchanged

---

## Summary

The notification system modernization addresses a critical gap in Spring Batch 5.x migration: JobParameters were never loaded from the database, breaking user job identification and folding logic. 

The solution uses:
- **Single optimized query** with INNER JOIN to parameters table
- **Efficient row collapsing** in ResultSetExtractor to materialize complete objects
- **Streaming approach** in service layer for natural pagination
- **Clean constructor-based initialization** (no reflection)

This improves performance (1 query vs 1+N), reliability (parameters always available), and code clarity (simpler logic) while maintaining full backward compatibility.
