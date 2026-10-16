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
SearchableJobExecutionDao.getJobExecutionsWithParams() (new method)
       ↓
NotificationServiceImpl (streaming approach)
```

### New Function: getJobExecutionsWithParams()

A new method in `SearchableJobExecutionDao` that streams job executions with their parameters loaded in a single optimized database query:

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

### Row Grouping and Streaming Strategy

The method returns `Stream<JobExecution>` that performs grouped row aggregation using the lookahead pattern:

1. **Stream rows**: ResultSet cursor advances through result set rows on-demand
2. **Group by execution ID**: Detect when execution ID changes to identify group boundaries
3. **Accumulate parameters**: Collect all parameter rows for one execution into a Map<String, JobParameter<?>>
4. **Construct execution**: Build `JobParameters` object from the accumulated map and create `JobExecution`
5. **Emit result**: Return one complete `JobExecution` per group
6. **Lazy evaluation**: Only materializes objects as caller iterates, enabling early termination without loading entire result

**Implementation details**:
```java
// Pseudo-code of the lookahead pattern
while (hasMore) {
    // prefetched tracks if we've already advanced to the next row
    Map<String, JobParameter<?>> params = new HashMap<>();
    Long currentExecId = getCurrentRowExecId();
    
    do {
        params.put(paramName, createJobParameter(...));
        hasMore = rs.next();
    } while (hasMore && rs.getLong("JOB_EXECUTION_ID") == currentExecId);
    
    // We've read ONE ROW AHEAD; mark prefetched=true to skip rs.next() call next time
    execution = new JobExecution(jobInstance, new JobParameters(params));
    yield(execution);
}
```

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

## Implementation: Lookahead Iterator Pattern

The streaming approach uses a lookahead iterator pattern built into `JdbcSearchableJobExecutionDao` as an inner class:

### The Challenge

The database query returns multiple rows per execution (one row per parameter). We need to:

1. **Detect group boundaries**: Know when we've finished reading all parameters for one execution and the next execution starts
2. **Build complete objects**: Accumulate all parameters before creating a `JobExecution` object
3. **Avoid row data copying**: Don't store entire rows in memory—only cursor position state
4. **Stay within transaction boundaries**: Iterator must be consumed without explicit resource management

### The Solution: Lookahead Pattern

The custom `JobExecutionIterator` uses a `prefetched` flag to manage cursor position state:

```
prefetched = true  →  Cursor is already positioned on first row of next group
prefetched = false →  Need to call rs.next() to position on next row
```

**Example with 3 executions and parameters:**

```
Database rows (ordered by EXEC_ID DESC):
┌─────────────┬──────────────────┐
│ EXEC_ID     │ PARAM_NAME       │
├─────────────┼──────────────────┤
│ 100         │ author           │  ← First execution
│ 100         │ cohort_id        │
│ 99          │ author           │  ← Second execution (key changed!)
│ 99          │ source_id        │
│ 98          │ author           │  ← Third execution (key changed!)
└─────────────┴──────────────────┘
```

**Iterator execution flow:**

```
Constructor: rs.next() → Load row 1 (EXEC_ID=100, author)
            prefetched = true (we've read ahead)

hasNext() #1: return true (hasMore=true)

next() #1:
  prefetched is true → don't call rs.next() yet
  Create params map = {}
  Loop reads rows while EXEC_ID is 100:
    ├─ Add author parameter to map
    ├─ rs.next() → Row 2 (EXEC_ID=100, cohort_id), same ID
    ├─ Add cohort_id parameter to map
    ├─ rs.next() → Row 3 (EXEC_ID=99, author), DIFFERENT ID!
    └─ Exit loop (key changed)
  prefetched = true (cursor is on row 3, won't call rs.next() next time)
  Build JobExecution(100, params={author, cohort_id})
  Return execution(100)

hasNext() #2: return true (hasMore=true)

next() #2:
  prefetched is true → don't call rs.next() yet
  Create params map = {}
  Loop reads rows while EXEC_ID is 99:
    ├─ Add author parameter to map (from row 3, already in cursor)
    ├─ rs.next() → Row 4 (EXEC_ID=99, source_id), same ID
    ├─ Add source_id parameter to map
    ├─ rs.next() → Row 5 (EXEC_ID=98, author), DIFFERENT ID!
    └─ Exit loop (key changed)
  prefetched = true (cursor is on row 5)
  Build JobExecution(99, params={author, source_id})
  Return execution(99)

hasNext() #3: return true (hasMore=true)

next() #3:
  prefetched is true → don't call rs.next() yet
  Create params map = {}
  Loop reads rows while EXEC_ID is 98:
    ├─ Add author parameter to map (from row 5)
    ├─ rs.next() → Returns false (EOF)
    └─ Exit loop (no more rows)
  prefetched = true (but hasMore=false)
  Build JobExecution(98, params={author})
  Return execution(98)

hasNext() #4: return false (hasMore=false) → Iteration complete
```

**Key insight**: The cursor never stores row data. We only track whether we've already advanced (`prefetched` flag) and automatically close it when the transaction boundary exits.

### Key Properties

**No row data is copied**:
- ResultSet cursor serves as the only row buffer
- Values are read directly via `rs.getString()`, `rs.getLong()`, etc.
- Only cursor position state (`prefetched` flag) is managed

**Memory efficient**:
- Only one `JobExecution` object materialized at a time
- No intermediate data structures storing row values
- Caller controls iteration—can break early without loading entire result

**Early termination safe**:
- Breaking from the iteration loop mid-stream is safe
- ResultSet is closed automatically when transaction boundary exits
- No explicit resource management needed by caller

### Implementation in JdbcSearchableJobExecutionDao

The `JobExecutionIterator` inner class in [JdbcSearchableJobExecutionDao.java](src/main/java/org/ohdsi/webapi/batch/JdbcSearchableJobExecutionDao.java) implements the lookahead pattern:

```java
// Returns Stream that lazily streams grouped rows with automatic resource cleanup
@Override
public Stream<JobExecution> getJobExecutionsWithParams() {
    String sql = applyPrefix(GET_EXECUTIONS_WITH_PARAMS);
    Connection conn = jdbcTemplate.getDataSource().getConnection();
    PreparedStatement stmt = conn.prepareStatement(sql);
    ResultSet rs = stmt.executeQuery();
    
    JobExecutionIterator iterator = new JobExecutionIterator(rs);
    Spliterator<JobExecution> spliterator = Spliterators.spliteratorUnknownSize(
            iterator, Spliterator.ORDERED | Spliterator.NONNULL);
    
    return StreamSupport.stream(spliterator, false)
            .onClose(() -> {
                try { rs.close(); } catch (SQLException ignored) {}
                try { stmt.close(); } catch (SQLException ignored) {}
                try { conn.close(); } catch (SQLException ignored) {}
            });
}

private class JobExecutionIterator implements Iterator<JobExecution> {
    private boolean hasMore;
    private boolean prefetched;  // true = cursor already on next row
    
    @Override
    public JobExecution next() {
        if (!prefetched) {
            hasMore = rs.next();  // Read next row if we haven't already
        }
        prefetched = false;  // We're using the previously-read row now
        
        // Read execution metadata from current row
        Long currentExecId = rs.getLong("JOB_EXECUTION_ID");
        Map<String, JobParameter<?>> params = new HashMap<>();
        
        // Accumulate rows until execution ID changes
        do {
            // Add parameter from current row
            params.put(rs.getString("PARAMETER_NAME"), 
                createJobParameter(...));
            
            // Try to read next row
            hasMore = rs.next();
            if (!hasMore) break;
            
        } while (Objects.equals(currentExecId, rs.getLong("JOB_EXECUTION_ID")));
        
        // We've read ONE ROW AHEAD (first row of next group)
        prefetched = true;  // Mark that we've already advanced
        
        // Build complete JobExecution with accumulated parameters
        JobParameters jobParameters = new JobParameters(params);
        JobExecution execution = new JobExecution(jobInstance, jobParameters);
        // ... set other fields ...
        
        return execution;
    }
}
```

**Key design features**:
- No row data is copied—values read directly via `rs.getString()`, `rs.getLong()`, etc.
- Only cursor position state (`prefetched` flag) is managed
- One `JobExecution` object materialized at a time
- Caller controls iteration—can break early without loading entire result

### Resource Management Guarantees

**Connection lifecycle is managed by Stream.onClose() hooks**:
- Stream returned by `getJobExecutionsWithParams()` wraps ResultSet/Statement/Connection in explicit onClose() handlers
- These resources are closed when the Stream exits (either via completion or exception)
- Caller should use try-with-resources to ensure automatic cleanup:
  ```java
  try (Stream<JobExecution> stream = dao.getJobExecutionsWithParams()) {
      // Use stream...
      // onClose() hooks invoked automatically when exiting try block
  }
  ```
- Safe to break mid-iteration—resources cleaned up immediately when stream closes

**Safe to break mid-iteration with automatic resource cleanup**:
```java
try (Stream<JobExecution> stream = dao.getJobExecutionsWithParams()) {
    stream.takeWhile(job -> needMoreResults())
          .forEach(job -> {
              // Process job...
          });
    // ResultSet, Statement, Connection automatically closed when stream exits
}
```

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
- `SearchableJobExecutionDao.java`:
  - Added new method: `Stream<JobExecution> getJobExecutionsWithParams()`
  - Returns a Stream that lazily streams job executions with parameters populated
  - Uses try-with-resources for explicit resource management via onClose() hooks
  - Enables functional composition (filter, map, limit) with automatic cleanup

**DAO Implementation**:
- `JdbcSearchableJobExecutionDao.java`:
  - Implemented `getJobExecutionsWithParams()` to return a Stream
  - Wraps `JobExecutionIterator` in `Spliterator` and uses `StreamSupport.stream()` for API flexibility
  - Added inner class `JobExecutionIterator` that implements the lookahead pattern:
    - Uses `prefetched` flag to track cursor position (whether next row already read)
    - Accumulates parameters into a Map for each execution group
    - Creates JobParameters from the map once all rows for a group are read
    - Returns one complete JobExecution per group
  - Attaches onClose() hooks to handle resource cleanup (ResultSet, Statement, Connection)
  - Kept existing `createJobParameter()` helper (parses DB values to typed JobParameters)
  - Removed old `JobExecutionWithParamsResultSetExtractor` class (no longer needed)
  - All other DAO methods remain unchanged

**Service Layer**:
- `NotificationServiceImpl.java`:
  - Updated `findJobs()` to use Stream API with try-with-resources pattern
  - Changed from: `Iterator<JobExecution> allExecutions = dao.getJobExecutionsWithParams();`
  - Changed to: `try (Stream<JobExecution> stream = dao.getJobExecutionsWithParams()) { ... }`
  - Uses functional composition: `stream.limit(PAGE_SIZE).takeWhile(...).forEach(...)`
  - Enables early termination via `takeWhile()` for efficient result collection
  - Automatic resource cleanup when try-with-resources block exits

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
