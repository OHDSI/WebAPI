# Transaction Boundary Isolation for Concurrent Batch Jobs

## Summary

Fixed transaction manager conflicts that caused "transaction already open" errors during cohort characterization generation. All batch job tasklets now use `batchTransactionManager` consistently, enabling proper transaction isolation and concurrent job execution.

## Changes Made

### 1. Added Batch Transaction Template Beans ([JobConfig.java](src/main/java/org/ohdsi/webapi/JobConfig.java))

Created two new transaction template beans specifically for batch operations:
- **`batchTransactionTemplate`**: Uses `batchTransactionManager` with default propagation
- **`batchTransactionTemplateRequiresNew`**: Uses `batchTransactionManager` with `PROPAGATION_REQUIRES_NEW` for immediate commits

These ensure all batch-related transactions use the same transaction manager as Spring Batch steps.

### 2. Updated AbstractDaoService ([AbstractDaoService.java](src/main/java/org/ohdsi/webapi/service/AbstractDaoService.java))

Added autowired fields and getter methods for batch transaction templates:
- `getBatchTransactionTemplate()`
- `getBatchTransactionTemplateRequiresNew()`

This allows all services extending AbstractDaoService to access batch transaction templates.

### 3. Updated GenerationUtils ([GenerationUtils.java](src/main/java/org/ohdsi/webapi/common/generation/GenerationUtils.java))

- Injected `batchTransactionTemplate` in constructor
- Updated `buildJobForCohortBasedAnalysisTasklet()` to pass `batchTransactionTemplate` to all tasklets:
  - CreateCohortTableTasklet
  - GenerateLocalCohortTasklet
  - TempTableCleanupManager
  - DropCohortTableListener

### 4. Updated CohortGenerationService ([CohortGenerationService.java](src/main/java/org/ohdsi/webapi/cohortdefinition/CohortGenerationService.java))

Changed `buildGenerateCohortJob()` to use batch transaction templates:
- GenerateCohortTasklet uses `getBatchTransactionTemplate()`
- TempTableCleanupManager uses `getBatchTransactionTemplate()`
- **GenerationJobExecutionListener uses `getTransactionTemplateRequiresNew()`** (jpaTransactionManager)
  - Listeners run outside step execution context, need JPA for entity operations
  - No transaction conflicts because they run before/after steps, not during

### 5. Updated CohortSamplingService ([CohortSamplingService.java](src/main/java/org/ohdsi/webapi/cohortsample/CohortSamplingService.java))

Changed `createDeleteSamplesTasklet()` to use `getBatchTransactionTemplate()` for CleanupCohortSamplesTasklet.

### 6. Fixed CcServiceImpl Job Launching ([CcServiceImpl.java](src/main/java/org/ohdsi/webapi/cohortcharacterization/CcServiceImpl.java))

- Added `@Transactional(propagation = Propagation.NOT_SUPPORTED)` to `generateCc()` method
  - **Critical:** Suspends class-level transaction before launching Spring Batch job
  - Without this, JobRepository detects active transaction and throws error
- Changed GenerateCohortCharacterizationTasklet to use `getBatchTransactionTemplate()` instead of `getTransactionTemplate()`

### 7. Enhanced AnalysisTasklet ([AnalysisTasklet.java](src/main/java/org/ohdsi/webapi/common/generation/AnalysisTasklet.java))

Added null check when rolling back transaction in `saveInfoWithinTheSeparateTransaction()` to prevent NPE if transaction creation fails.

## How It Works

### Transaction Architecture

**Two Transaction Managers:**
1. **`jpaTransactionManager`** (JpaTransactionManager) - For service layer WebAPI DB operations
2. **`batchTransactionManager`** (DataSourceTransactionManager) - For Spring Batch and all batch job operations

**Both target the same WebAPI DataSource**, but using the same transaction manager throughout a batch job enables proper transaction suspension/resumption.

### Transaction Scope Per Job

**Before Job Starts:**
- Service layer can use `jpaTransactionManager` for pre-job setup
- Short-lived transactions commit before job launches

**During Job Execution:**
- Each **step** has its own transaction (managed by `batchTransactionManager`)
- Step transaction commits when step completes
- JobRepository operations happen in separate transactions (automatic)

**Job Listeners (beforeJob/afterJob):**
- Run **outside** step execution context
- Can use `jpaTransactionManager` for JPA entity operations
- No conflicts with step transactions because they run before/after, not during

**Within Tasklets:**
- Tasklets receive `batchTransactionTemplate` using `batchTransactionManager`
- Can create nested `PROPAGATION_REQUIRES_NEW` transactions for immediate commits (e.g., cache updates)
- Proper suspend/resume because same transaction manager is used

## Benefits

### 1. Eliminates Transaction Conflicts
No more "transaction already open" errors when tasklets try to create nested transactions during step execution.

### 2. Enables Concurrent Job Execution
Multiple jobs can run simultaneously without blocking each other:
- Each job updates its own `job_execution_id` rows (no row-level conflicts)
- Cache updates commit immediately and are visible to other jobs
- Connection pool properly handles concurrent transactions

### 3. Short-Lived Transactions Per Step
- Each step's transaction commits when step completes (not held for entire job)
- Database locks released quickly
- No long-held transactions blocking WebAPI tables

### 4. Proper Transaction Isolation
- Cache updates with `PROPAGATION_REQUIRES_NEW` commit immediately
- Analysis metadata saves don't roll back with step failure
- CDM queries (separate database) don't interfere with WebAPI transactions

## Verification Steps

To verify the changes work correctly:

1. **Start Multiple Concurrent Jobs:**
   ```
   POST /cohortcharacterization/{id1}/generation/{source1}
   POST /cohortcharacterization/{id2}/generation/{source2}
   ```

2. **Check Transaction Isolation:**
   - Monitor `BATCH_STEP_EXECUTION` table - each step should commit independently
   - Check cache updates are visible to concurrent jobs during execution
   - Verify no long-held locks on WebAPI tables

3. **Test Failure Scenarios:**
   - If a step fails, its transaction should roll back
   - But cache updates from earlier `REQUIRES_NEW` transactions should remain committed
   - JobRepository metadata should persist correctly

4. **Monitor Database Locks:**
   - PostgreSQL: `SELECT * FROM pg_locks WHERE granted = true AND locktype = 'relation';`
   - SQL Server: `SELECT * FROM sys.dm_tran_locks;`
   - Should see short-lived locks, not locks held for entire job duration

## Technical Details

### When to Use Which Transaction Manager

**Use `jpaTransactionManager` (JPA operations):**
- Service layer methods annotated with `@Transactional`
- JobExecutionListeners (beforeJob/afterJob callbacks)
- StepExecutionListeners (beforeStep/afterStep callbacks)
- Any code that needs to save/update JPA entities outside of batch steps
- Pre-job and post-job setup/cleanup

**CRITICAL: Always use `@Transactional(propagation = Propagation.NOT_SUPPORTED)` when launching Spring Batch jobs**
- Service methods that call `jobService.runJob()` or `jobTemplate.launch()` MUST suspend active transactions
- Spring Batch JobRepository throws `IllegalStateException` if transaction is active during job launch
- Example: `CcServiceImpl.generateCc()`, `CohortGenerationService.generateCohortViaJob()`

**Use `batchTransactionManager` (Batch operations):**
- Inside tasklet `execute()` or `doTask()` methods
- Nested transactions within steps (`PROPAGATION_REQUIRES_NEW`)
- Cache updates during step execution
- Any database operations within Spring Batch step context

**Key Rule:** If code runs **inside a Spring Batch step**, use `batchTransactionManager`. If it runs **outside steps** (service layer, listeners), use `jpaTransactionManager`. When **launching a job**, ensure no transaction is active.

### Why Same Transaction Manager Matters

When a Spring Batch step creates a transaction with `batchTransactionManager` and a tasklet tries to create a `PROPAGATION_REQUIRES_NEW` transaction:

**Before (Mixed Managers):**
```
Step Transaction (batchTransactionManager) [ACTIVE]
  └─> Tasklet tries REQUIRES_NEW with jpaTransactionManager
      └─> CONFLICT: Both manage same DataSource
          └─> Error: "transaction already open"
```

**After (Consistent Manager):**
```
Step Transaction (batchTransactionManager) [ACTIVE]
  └─> Tasklet creates REQUIRES_NEW with batchTransactionManager
      └─> Step transaction SUSPENDED
      └─> New transaction COMMITS
      └─> Step transaction RESUMES
```

### Transaction Propagation Behaviors Used

- **PROPAGATION_REQUIRED** (default): Join existing transaction or create new one
- **PROPAGATION_REQUIRES_NEW**: Suspend current transaction and create new independent one
- **PROPAGATION_NOT_SUPPORTED**: Execute non-transactionally, suspend any existing transaction

## Related Files

- Configuration: [JobConfig.java](src/main/java/org/ohdsi/webapi/JobConfig.java), [DataAccessConfig.java](src/main/java/org/ohdsi/webapi/DataAccessConfig.java)
- Base Classes: [AbstractDaoService.java](src/main/java/org/ohdsi/webapi/service/AbstractDaoService.java)
- Generation: [GenerationUtils.java](src/main/java/org/ohdsi/webapi/common/generation/GenerationUtils.java)
- Services: [CohortGenerationService.java](src/main/java/org/ohdsi/webapi/cohortdefinition/CohortGenerationService.java), [CcServiceImpl.java](src/main/java/org/ohdsi/webapi/cohortcharacterization/CcServiceImpl.java)
- Tasklets: [AnalysisTasklet.java](src/main/java/org/ohdsi/webapi/common/generation/AnalysisTasklet.java), [GenerateCohortCharacterizationTasklet.java](src/main/java/org/ohdsi/webapi/cohortcharacterization/GenerateCohortCharacterizationTasklet.java)

## Spring Batch 5 Migration Issues and Fixes

### Issue 1: JobParameter.toString() Breaking Change

**Problem:**
Spring Batch 5.x changed the behavior of `JobParameter<?>` objects. Calling `.toString()` directly on a `JobParameter` now returns its internal representation instead of just the value:

```java
// Spring Batch 4.x behavior:
jobParameters.get(SOURCE_ID).toString() → "5"

// Spring Batch 5.x behavior:
jobParameters.get(SOURCE_ID).toString() → "{value=5, type=class java.lang.String, identifying=true}"
```

This caused `NumberFormatException` when trying to parse integer parameters:
```
java.lang.NumberFormatException: For input string: "{value=5, type=class java.lang.String, identifying=true}"
```

**Critical Distinction:**
- **`JobParameters.getParameters()`** → Returns `Map<String, JobParameter<?>>` (wrapper objects)
- **`ChunkContext.getStepContext().getJobParameters()`** → Returns `Map<String, Object>` (already extracted values)

**Solution:**

Only use `.getValue()` when dealing with `JobParameter<?>` objects from `JobParameters.getParameters()`:

```java
// In JobExecutionListener (receives JobParameters):
private Object doTask(JobParameters parameters) {
    final Map<String, JobParameter<?>> jobParameters = parameters.getParameters();
    final Integer sourceId = Integer.valueOf(jobParameters.get(SOURCE_ID).getValue().toString());
    // ^^^^^^^^^^^^ CORRECT - need .getValue()
}

// In Tasklets (receives ChunkContext):
private Integer doTask(ChunkContext chunkContext) {
    Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
    int cohortDefinitionId = Integer.parseInt(jobParams.get(COHORT_DEFINITION_ID).toString());
    // ^^^^^^^^^^^^ CORRECT - values already extracted, no .getValue() needed
}
```

**Fixed File:**
- [DropCohortTableListener.java](src/main/java/org/ohdsi/webapi/cohortcharacterization/DropCohortTableListener.java) - Line 43

**Pattern Recognition:**
- If you see `Map<String, JobParameter<?>>` → Use `.getValue().toString()`
- If you see `Map<String, Object>` → Use `.toString()` only

### Issue 2: Mixed Transaction Managers in JobExecutionListener

**Problem:**
After introducing separate transaction managers for Batch and JPA, we encountered this error in `DropCohortTableListener.afterJob()`:

```
java.lang.IllegalStateException: Already value [ConnectionHolder] for key [HikariDataSource] bound to thread
    at org.springframework.orm.jpa.JpaTransactionManager.doBegin(JpaTransactionManager.java:442)
```

**Root Cause:**
1. Spring Batch binds a JDBC connection to the thread during job execution
2. The `afterJob()` listener ran on the same thread with that connection still bound
3. Inside `afterJob()`, the code used `batchTransactionTemplate` (DataSourceTransactionManager)
4. Within that transaction, it called `sourceService.findBySourceId()` which is `@Transactional` (JpaTransactionManager)
5. JPA tried to bind its own connection to the same thread/DataSource → **Conflict**

**Incorrect Pattern (Before Fix):**
```java
@Override
public void afterJob(JobExecution jobExecution) {
    // BAD: Using batch transaction manager
    batchTransactionTemplate.execute(transactionStatus -> {
        // This calls @Transactional service with JPA transaction manager
        sourceService.findBySourceId(sourceId); // CONFLICT!
        return doTask(jobExecution.getJobParameters());
    });
}
```

**Correct Pattern (After Fix):**
```java
@Override
public void afterJob(JobExecution jobExecution) {
    // GOOD: No transaction wrapper - let JPA services manage their own transactions
    doTask(jobExecution.getJobParameters());
}

private Object doTask(JobParameters parameters) {
    // sourceService.findBySourceId() has @Transactional - creates its own JPA transaction
    final Source source = sourceService.findBySourceId(sourceId);
    // DROP TABLE is DDL that auto-commits anyway
    jdbcTemplate.execute(dropTableSql);
}
```

**Solution:**
Removed the transaction wrapper entirely from `DropCohortTableListener` because:
1. `sourceService.findBySourceId()` already has `@Transactional` and manages its own JPA transaction
2. The `DROP TABLE` statement is DDL and auto-commits anyway
3. No transactional rollback protection is needed for this cleanup operation

**Fixed Files:**
- [DropCohortTableListener.java](src/main/java/org/ohdsi/webapi/cohortcharacterization/DropCohortTableListener.java)
  - Removed `TransactionTemplate` field and constructor parameter
  - Changed `afterJob()` to call `doTask()` directly without any transaction wrapper
- [GenerationUtils.java](src/main/java/org/ohdsi/webapi/common/generation/GenerationUtils.java)
  - Updated listener instantiation to not pass `batchTransactionTemplate`

### Best Practices for JobExecutionListener

**Key Rules for JobExecutionListener.afterJob() and beforeJob():**

1. **Never nest Batch and JPA transactions on the same thread**
   - Don't wrap JPA service calls in `batchTransactionTemplate`
   - Let `@Transactional` services manage their own transactions

2. **Use only one transaction manager per execution path**
   - If calling JPA services, let them use `jpaTransactionManager`
   - If doing JDBC operations, use appropriate transaction manager or none for DDL

3. **Prefer PROPAGATION_REQUIRES_NEW for JPA operations in listeners**
   - Creates clean, isolated transaction independent of batch context
   - Example: `GenerationJobExecutionListener` uses `transactionTemplateRequiresNew`

4. **Understand when transactions are needed**
   - DDL operations (CREATE, DROP, ALTER) auto-commit - no transaction needed
   - JPA entity operations need transactions - let `@Transactional` handle it
   - Batch operations within steps need `batchTransactionManager`

**Mental Model:**

```
BAD - Mixed Transaction Managers:
afterJob()
  └─> Batch TransactionTemplate
      └─> calls @Transactional JPA service
          └─> Same DataSource
              └─> CONFLICT: "Already value bound to thread"

GOOD - Single Transaction Manager:
afterJob()
  └─> calls @Transactional JPA service (with REQUIRES_NEW)
      └─> Clean, independent JPA transaction
          └─> No conflict
```

**Comparison with GenerationJobExecutionListener:**

`GenerationJobExecutionListener` (correct pattern) uses JPA transaction template directly:
```java
public GenerationJobExecutionListener(
    SourceService sourceService,
    CohortDefinitionRepository cohortDefinitionRepository,
    TransactionTemplate transactionTemplate,  // JPA transaction template (transactionTemplateRequiresNew)
    JdbcTemplate sourceTemplate
) { ... }

@Override
public void afterJob(JobExecution je) {
    // Uses PROPAGATION_REQUIRES_NEW with jpaTransactionManager
    transactionTemplate.getTransactionManager().getTransaction(...);
    sourceService.findBySourceId(sourceId);  // @Transactional - works correctly
}
```

This works because it consistently uses `jpaTransactionManager` throughout the listener, never mixing it with `batchTransactionManager`.

### Summary of Transaction Manager Rules

| Context | Transaction Manager | When to Use |
|---------|-------------------|-------------|
| **Service Layer** | `jpaTransactionManager` | Annotated with `@Transactional`, pre/post job operations |
| **Batch Steps** | `batchTransactionManager` | Inside tasklet `execute()`, nested transactions within steps |
| **Job Listeners** | `jpaTransactionManager` | If calling JPA services; let `@Transactional` manage it |
| **Job Launch** | None (`PROPAGATION_NOT_SUPPORTED`) | Must suspend active transactions before launching jobs |
| **DDL Operations** | None | DDL auto-commits, no transaction wrapper needed |

**Critical Reminder:** When sharing a DataSource between multiple transaction managers, never nest transactions from different managers on the same thread. This is especially important in Spring Batch 5 / Spring Boot 3.x which has stricter transaction and resource binding behavior.

## Date

April 1, 2026
