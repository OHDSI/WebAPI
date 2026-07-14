# User Import System Developer Guide

## Table of Contents

1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Configuration Variables](#configuration-variables)
4. [Core Classes](#core-classes)
5. [Import Workflow](#import-workflow)
6. [Scheduled Tasks & Job Execution](#scheduled-tasks--job-execution)
7. [REST API Reference](#rest-api-reference)
8. [Database Schema](#database-schema)
9. [Integration Points](#integration-points)
10. [Common Development Tasks](#common-development-tasks)

---

## Overview

The WebAPI user import system enables automated provisioning of users from LDAP/Active Directory into WebAPI with automatic role assignment based on group-to-role mappings. The system supports:

- **Multiple authentication sources**: LDAP, Active Directory (with separate configurations)
- **Flexible scheduling**: One-time jobs, daily recurring, or weekly on specific days
- **Group-to-role mapping**: Maps LDAP/AD groups to WebAPI roles
- **Role preservation**: Optional preservation of manually assigned roles during import
- **Audit trail**: Complete history of import jobs and executions

**Technology Stack**:
- Spring Batch for job orchestration
- Arachne scheduler for job scheduling
- Spring LDAP for directory operations
- Flyway for database migrations
- Spring Data JPA for persistence

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        REST API Layer                            │
│  UserImportServiceImpl (REST Controller + Service Logic)         │
│  UserImportJobServiceImpl (REST Controller + Job Management)     │
└──────────────────┬──────────────────────────────────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
        ▼                     ▼
┌───────────────────┐  ┌──────────────────┐
│ Service Layer     │  │ Job Orchestration│
│                   │  │                  │
│ UserImportService │  │Spring Batch Job: │
│ (Interface)       │  │- Find Users Step │
│                   │  │- Import Users    │
│ LDAP Providers:   │  │  Step            │
│- ActiveDirectory  │  │                  │
│- DefaultLdap      │  │Scheduled via     │
│- AbstractLdap     │  │Arachne Scheduler │
└────────┬──────────┘  └──────────┬───────┘
         │                        │
    ┌────▼────────────────────────▼────┐
    │   Persistence Layer              │
    │                                   │
    │ JPA Entities:                    │
    │ - UserImportJob                 │
    │ - GroupRoleImportEntity         │
    │ - UserEntity                    │
    │ - RoleEntity                    │
    │                                   │
    │ Repositories:                    │
    │ - UserImportJobRepository       │
    │ - GroupRoleImportRepository     │
    │ - UserRepository                │
    └────────┬─────────────────────────┘
             │
    ┌────────▼──────────┐
    │   Database        │
    │                   │
    │ user_import_job   │
    │ sec_group_role_   │
    │   import          │
    │ sec_user          │
    │ sec_role          │
    └───────────────────┘
```

### Key Design Patterns

**Strategy Pattern**: LDAP provider implementations
- `AbstractLdapProvider` - Base functionality
- `ActiveDirectoryProvider` - AD-specific overrides
- `DefaultLdapProvider` - Standard LDAP implementation

**Job Pattern**: Spring Batch with Arachne wrapper
- Jobs are built dynamically at runtime
- Steps are tasklets for fine-grained control
- Scheduled through Arachne's TaskScheduler

**DTO Pattern**: Separation of entities and REST models
- `UserImportJob` (entity) ↔ `UserImportJobDTO` (REST)
- Converters handle transformation (e.g., `BaseUserImportJobDTOToUserImportJobConverter`)

---

## Configuration Variables

### LDAP Configuration

#### Active Directory

```yaml
security:
  auth:
    ad:
      enabled: false  # Set to true to enable AD authentication
      url: ldap://your-ad-server:389  # AD server URL
      system:
        username: AD_USERNAME  # Service account login
        password: AD_PASSWORD  # Service account password
      default:
        import:
          group: []  # Optional: default roles assigned to imported users
      attributes:
        login: sAMAccountName  # Attribute for username (AD-specific)
        displayName: displayName  # Attribute for display name
```

#### Standard LDAP

```yaml
security:
  auth:
    ldap:
      enabled: false  # Set to true to enable LDAP authentication
      url: ldap://your-ldap-server:389  # LDAP server URL
      base: dc=example,dc=com  # LDAP base DN
      user:
        dn: cn={0},ou=users,dc=example,dc=com  # User DN pattern ({0} = username)
      system:
        username: cn=admin,dc=example,dc=com  # Service account DN
        password: SERVICE_PASSWORD  # Service account password
      default:
        import:
          group: []  # Optional: default roles
      attributes:
        login: uid  # Attribute for username (LDAP-specific)
        displayName: cn  # Attribute for display name
      groups:
        base: ou=groups,dc=example,dc=com  # Groups base DN
```

### Import-Related Configuration

```yaml
security:
  auth:
    # Applied to both LDAP and AD imports
    ldap:
      # ... configuration above
      default:
        import:
          group: [ADMIN, ANALYST]  # Comma-separated list of default roles
```

### Database Migration

Flyway migrations control the database schema:
- `V2.99.0006__role_group_import.sql` - Renames `sec_role_group` → `sec_group_role_import` (latest)
- `V2.6.0.20181015182101__role-group-mapping.sql` - Creates import mapping infrastructure
- `V2.5.0.20180725172844__add-ad-import-permissions.sql` - AD-specific permissions

---

## Core Classes

### Service Layer

#### `UserImportService` (Interface)

**Location**: `org.ohdsi.webapi.security.provisioning.service`

Defines core operations for user import:

```java
// Core operations
List<LdapGroup> findGroups(LdapProviderType providerType, String searchStr);
List<AtlasUserRoles> findUsers(LdapProviderType providerType, RoleGroupMapping mapping);
UserImportResult importUsers(List<AtlasUserRoles> users, LdapProviderType providerType, 
                              boolean preserveRoles);

// Group-to-role mapping
void saveRoleGroupMapping(LdapProviderType providerType, 
                          List<GroupRoleImportEntity> mappingEntities);
List<GroupRoleImportEntity> getRoleGroupMapping(LdapProviderType providerType);

// Testing
void testConnection(LdapProviderType provider);

// Job queries
UserImportJob getImportUserJob(Long userImportId);
```

#### `UserImportServiceImpl` (Implementation)

**Location**: `org.ohdsi.webapi.security.provisioning.service`

Implements user import logic and REST endpoints:

```java
@RestController
@RequestMapping("/user")
public class UserImportServiceImpl implements UserImportService {
  private Map<LdapProviderType, LdapProvider> providersMap;  // AD and LDAP providers
  private RoleGroupMappingRepository roleGroupMappingRepository;  // Persistence
  private AuthorizationService userManager;  // Role assignment
}
```

**Key Methods**:
- `findUsers()` - Queries directory and applies group-to-role mapping
- `importUsers()` - Creates/updates users and assigns roles
- `saveRoleGroupMapping()` / `getRoleGroupMapping()` - Manages LDAP group mappings

#### `UserImportJobService` (Interface)

**Location**: `org.ohdsi.webapi.security.provisioning.service`

Manages import job lifecycle:

```java
Optional<UserImportJob> getJob(Long id);
List<UserImportJob> getJobs();
UserImportJob createJob(UserImportJob job);
UserImportJob updateJob(UserImportJob job);
void delete(UserImportJob job);

// History tracking
Stream<UserImportJobHistoryItem> getJobHistoryItems(Long id);
Optional<UserImportJobHistoryItem> getLatestHistoryItem(Long id);
```

#### `UserImportJobServiceImpl` (Implementation)

**Location**: `org.ohdsi.webapi.security.provisioning.service`

Extends `BaseJobServiceImpl` to manage job scheduling and Spring Batch integration:

```java
@RestController
@RequestMapping("/user/import/job")
public class UserImportJobServiceImpl extends BaseJobServiceImpl<UserImportJob> 
    implements UserImportJobService {
  
  private JobRepository jobRepositoryBatch;  // Spring Batch
  private PlatformTransactionManager transactionManager;
  private JobTemplate jobTemplate;  // Job building
  
  // Dynamically builds Spring Batch jobs
  Job buildJobForUserImportTasklet(UserImportJob job);
}
```

### Data Model Classes

#### `UserImportJob` (JPA Entity)

**Location**: `org.ohdsi.webapi.security.provisioning.model`

Represents a scheduled import job:

```java
@Entity
@Table(name = "user_import_job")
public class UserImportJob extends ArachneJob {
  private List<DayOfWeek> weekDays;  // For weekly scheduling
  private LdapProviderType providerType;  // LDAP or ACTIVE_DIRECTORY
  private List<GroupRoleImportEntity> groupRoleImportMapping;  // Group→role mappings
  private Boolean preserveRoles;  // Keep manually assigned roles?
  private String userRoles;  // Serialized list of users to import (one-time jobs)
}
```

**Database Mapping**: `user_import_job` table
- `id` - Primary key
- `provider_type` - LDAP provider type
- `preserve_roles` - Boolean flag
- `user_roles` - JSON-serialized user list

#### `GroupRoleImportEntity` (JPA Entity)

**Location**: `org.ohdsi.webapi.security.provisioning.model`

Represents an LDAP group → WebAPI role mapping (import-specific):

```java
@Entity
@Table(name = "sec_group_role_import")
public class GroupRoleImportEntity {
  private int id;
  private LdapProviderType provider;  // LDAP provider
  private String groupDn;  // LDAP group DN
  private String groupName;  // LDAP group name
  private RoleEntity role;  // Target WebAPI role
  private UserImportJob userImportJob;  // Job this mapping belongs to
}
```

**Database Mapping**: `sec_group_role_import` table (renamed from `sec_role_group` in v2.99)
- One row per LDAP group → role assignment
- Either associated with a job or shared (job_id IS NULL)

#### `UserImportJobDTO` (REST Model)

**Location**: `org.ohdsi.webapi.security.provisioning.model`

REST representation of import job:

```java
public class UserImportJobDTO extends ArachneJobDTO {
  private LdapProviderType providerType;
  private Boolean preserveRoles;
  private String userRoles;
  private RoleGroupMapping groupRoleImportMapping;  // Renamed from roleGroupMapping
  private Date lastExecuted;
  private Date nextExecution;
}
```

#### `RoleGroupMapping` (Model)

**Location**: `org.ohdsi.webapi.security.provisioning.model`

Structured representation of group-to-role mappings:

```java
public class RoleGroupMapping {
  private String provider;  // Provider name
  private List<RoleGroupsMap> roleGroups;  // Groups per role
  
  public static class RoleGroupsMap {
    private Role role;  // WebAPI role
    private List<LdapGroup> groups;  // LDAP groups mapped to this role
  }
}
```

### LDAP Provider Classes

#### `AbstractLdapProvider` (Base Class)

**Location**: `org.ohdsi.webapi.security.provisioning.providers`

Base implementation for LDAP operations:

```java
public abstract class AbstractLdapProvider implements LdapProvider {
  protected LdapTemplate ldapTemplate;  // Spring LDAP template
  protected LdapContextSource contextSource;  // LDAP connection
  
  // Template methods overridden by subclasses
  public abstract List<LdapUserDetail> findUsers();
  public abstract List<LdapGroup> findGroups(String searchStr);
  
  // Shared functionality
  public final void testConnection();
  protected LdapUserDetail[] findUsersByName(String username);
  protected List<LdapUserDetail> convertEntriesToUsers(List<SearchResult> results);
}
```

#### `ActiveDirectoryProvider`

**Location**: `org.ohdsi.webapi.security.provisioning.providers`

Active Directory-specific implementation:

```java
@Component
public class ActiveDirectoryProvider extends AbstractLdapProvider {
  @Override
  public List<LdapUserDetail> findUsers() {
    // AD-specific LDAP query with AD attributes
  }
  
  @Override
  public List<LdapGroup> findGroups(String searchStr) {
    // AD group search (objectClass=group)
  }
}
```

**AD-Specific Behavior**:
- Uses `sAMAccountName` for login (configurable)
- Queries with `objectClass=group`
- Supports nested group membership

#### `DefaultLdapProvider`

**Location**: `org.ohdsi.webapi.security.provisioning.providers`

Standard LDAP implementation:

```java
@Component
public class DefaultLdapProvider extends AbstractLdapProvider {
  @Override
  public List<LdapUserDetail> findUsers() {
    // Standard LDAP query with generic attributes
  }
  
  @Override
  public List<LdapGroup> findGroups(String searchStr) {
    // LDAP group search (groupOfNames/groupOfUniqueNames)
  }
}
```

**Standard LDAP Behavior**:
- Uses `uid` for login (configurable)
- Queries with `objectClass=groupOfNames` or `objectClass=groupOfUniqueNames`
- Configurable DN patterns

### Repository Classes

#### `UserImportJobRepository`

```java
public interface UserImportJobRepository 
    extends JpaRepository<UserImportJob, Long> {
  Stream<UserImportJob> findUserImportJobsBy();
  List<UserImportJob> findAllByEnabledTrueAndIsClosedFalse();
}
```

#### `GroupRoleImportRepository`

```java
public interface GroupRoleImportRepository 
    extends JpaRepository<GroupRoleImportEntity, Integer> {
  List<GroupRoleImportEntity> findByProviderAndUserImportJobNull(
      LdapProviderType provider);
  void deleteByRoleId(Long roleId);
}
```

### Utility Classes

#### `GroupRoleImportUtils`

**Location**: `org.ohdsi.webapi.security.provisioning`

Utilities for comparing and diffing group-to-role mappings:

```java
public class GroupRoleImportUtils {
  // Deep equality check for import entities
  static boolean equalsRoleGroupMapping(GroupRoleImportEntity a, 
                                        GroupRoleImportEntity b);
  
  // List operations for detecting changes
  static List<GroupRoleImportEntity> findCreated(List<GroupRoleImportEntity> source,
                                                  List<GroupRoleImportEntity> target);
  static List<GroupRoleImportEntity> findDeleted(List<GroupRoleImportEntity> source,
                                                  List<GroupRoleImportEntity> target);
  static List<GroupRoleImportEntity> subtract(List<GroupRoleImportEntity> source,
                                               List<GroupRoleImportEntity> target);
}
```

#### `RoleGroupMappingConverter`

**Location**: `org.ohdsi.webapi.security.provisioning.converter`

Converts between entity and DTO representations:

```java
public class RoleGroupMappingConverter {
  // Entity → REST DTO
  static RoleGroupMapping convertRoleGroupMapping(
      String provider, 
      List<GroupRoleImportEntity> mappingEntities);
  
  // REST DTO → Entity
  static List<GroupRoleImportEntity> convertRoleGroupMapping(
      RoleGroupMapping mapping);
}
```

---

## Import Workflow

### High-Level Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│ 1. Admin creates job via REST API (POST /user/import/job)          │
│    - Specifies LDAP provider, schedule, role mappings             │
│    - Job stored in user_import_job table                           │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────────┐
│ 2. Arachne Scheduler triggers job at scheduled time                │
│    - Converts UserImportJob to Spring Batch Job                    │
│    - Submits to Spring Batch JobLauncher                           │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
            ┌──────────────────┴──────────────────┐
            │                                     │
            ▼                                     ▼
┌──────────────────────────┐    ┌────────────────────────────┐
│ Step 1: Find Users       │    │ Step 2: Import Users       │
│ (FindUsersToImportTasklet)    │ (UserImportTasklet)        │
│                          │    │                            │
│ 1. Query LDAP/AD         │    │ 1. For each AtlasUserRoles:│
│    based on job config   │    │    a. Find/create user     │
│ 2. Apply role mappings   │    │    b. Update display name  │
│    from group-to-role    │    │    c. Set origin (LDAP/AD) │
│ 3. Result: List of       │    │    d. Assign/remove roles  │
│    AtlasUserRoles with   │    │       (respecting preserve) │
│    groups resolved to    │    │ 2. Delete users not in     │
│    WebAPI roles          │    │    import list (optional)   │
└──────────────────────────┘    │                            │
            │                    │ 3. Record changes in       │
            │                    │    UserImportResult        │
            └────────────────────┬────────────────────────────┘
                                 │
┌────────────────────────────────▼────────────────────────────┐
│ 3. Spring Batch records execution in BATCH_STEP_EXECUTION  │
│    - Execution time, status, exit code, etc.               │
│    - Spring creates entry via JobRepository                │
└────────────────────────────────┬────────────────────────────┘
                                 │
┌────────────────────────────────▼────────────────────────────┐
│ 4. Application queries history via                          │
│    - UserImportJobHistoryItemRepository                     │
│    - View joining Spring Batch tables with job metadata     │
└───────────────────────────────────────────────────────────────┘
```

### Step 1: FindUsersToImportTasklet

**Purpose**: Query directory and prepare user list with resolved roles

```java
public class FindUsersToImportTasklet extends BaseUserImportTasklet {
  protected List<AtlasUserRoles> doUserImportTask(ChunkContext chunkContext, 
                                                   UserImportJob userImportJob) {
    // 1. Get provider based on job config
    LdapProvider provider = getProvider(userImportJob.getProviderType());
    
    // 2. Query directory for all users
    List<LdapUserDetail> dirUsers = provider.findUsers();
    
    // 3. Get group-to-role mappings
    RoleGroupMapping roleMapping = RoleGroupMappingConverter
        .convertRoleGroupMapping(userImportJob.getProviderType().getValue(),
                                  userImportJob.getRoleGroupMapping());
    
    // 4. Apply mappings: LDAP groups → WebAPI roles
    return findUsers(userImportJob.getProviderType(), roleMapping);
    
    // 5. Store result in job execution context
    stepExecution.getJobExecution().getExecutionContext()
        .putString(Constants.Params.USER_ROLES, Utils.serialize(userRoles));
  }
}
```

**Mapping Resolution**:
```
LDAP User: jsmith
├─ Groups: ["cn=engineers,ou=groups,dc=example,dc=com",
│            "cn=admin,ou=groups,dc=example,dc=com"]
│
├─ Against mappings:
│  ├─ cn=engineers → Role: DEVELOPER
│  ├─ cn=admin → Role: ADMIN
│
└─ Result: AtlasUserRoles(login=jsmith, roles=[DEVELOPER, ADMIN])
```

### Step 2: UserImportTasklet

**Purpose**: Create/update users and assign roles

```java
public class UserImportTasklet extends BaseUserImportTasklet {
  protected UserImportResult doUserImportTask(ChunkContext chunkContext,
                                               UserImportJob userImportJob) {
    // 1. Retrieve prepared users from previous step
    List<AtlasUserRoles> users = // from execution context
    
    // 2. For each user:
    for (AtlasUserRoles user : users) {
      UserEntity existingUser = userRepository.findByLogin(user.getLogin());
      
      if (existingUser == null) {
        // New user: create and assign roles
        userManager.registerUser(user.getLogin(), user.getDisplayName(),
                                  userOrigin, user.getRoles());
      } else {
        // Existing user: update and apply role changes
        existingUser.setName(user.getDisplayName());
        existingUser.setOrigin(userOrigin);
        
        if (preserveRoles) {
          // Only add new roles, keep existing
          currentRoles = getCurrentRoles(existingUser);
          newRoles = user.getRoles().stream()
              .filter(r → !currentRoles.contains(r))
              .collect(toSet());
        } else {
          // Replace all roles
          removeCurrentRoles(existingUser);
          newRoles = user.getRoles();
        }
        
        // Assign roles
        for (String role : newRoles) {
          userManager.addUserToRole(role, user.getLogin(), userOrigin);
        }
      }
    }
    
    return result;  // UserImportResult with counters
  }
}
```

---

## Scheduled Tasks & Job Execution

### Job Scheduling via Arachne

The Arachne scheduler manages job execution:

```java
@Component
public class UserImportScheduledTask extends ScheduledTask<UserImportJob> {
  
  @Override
  public void execute() {
    // 1. Build Spring Batch job from UserImportJob config
    Job batchJob = buildJobForUserImportTasklet(userImportJob);
    
    // 2. Create job parameters (unique run ID)
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("run.id", System.currentTimeMillis())
        .toJobParameters();
    
    // 3. Launch job
    JobExecution execution = jobLauncher.run(batchJob, jobParameters);
    
    // 4. Spring Batch stores execution details in:
    //    - BATCH_JOB_EXECUTION
    //    - BATCH_STEP_EXECUTION
    //    - BATCH_JOB_EXECUTION_CONTEXT
    //    - BATCH_STEP_EXECUTION_CONTEXT
  }
}
```

### Job Building

```java
Job buildJobForUserImportTasklet(UserImportJob job) {
  // Create Spring Batch steps
  Step findUsersStep = new StepBuilder("findUsersForImport", jobRepositoryBatch)
      .tasklet(new FindUsersToImportTasklet(...), transactionManager)
      .build();
  
  Step importUsersStep = new StepBuilder("importUsers", jobRepositoryBatch)
      .tasklet(new UserImportTasklet(...), transactionManager)
      .build();
  
  // Build job: Step 1 → Step 2
  if (job.getUserRoles() != null) {
    // One-time import: skip finding step
    return new JobBuilder(Constants.USERS_IMPORT, jobRepositoryBatch)
        .start(importUsersStep)
        .build();
  } else {
    // Recurring import: both steps
    return new JobBuilder(Constants.USERS_IMPORT, jobRepositoryBatch)
        .start(findUsersStep)
        .next(importUsersStep)
        .build();
  }
}
```

### Schedule Types

Inherited from `ArachneJob` base class:

```java
// Daily at specific time
job.setFrequency(JobExecutingType.DAILY);
job.setStartDate(new Date()); // First run

// Weekly on specific days
job.setFrequency(JobExecutingType.WEEKLY);
job.setWeekDays(List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));

// One-time execution
job.setFrequency(JobExecutingType.ONCE);

// Cron expression (if supported)
job.setCronExpression("0 2 * * *");  // 2 AM daily
```

---

## REST API Reference

### Job Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| **POST** | `/user/import/job` | Create new import job |
| **PUT** | `/user/import/job/{id}` | Update job configuration |
| **GET** | `/user/import/job` | List all jobs |
| **GET** | `/user/import/job/{id}` | Get job details |
| **DELETE** | `/user/import/job/{id}` | Delete job |
| **GET** | `/user/import/job/{id}/history` | Get execution history |

### Group-to-Role Mapping

| Method | Endpoint | Description |
|--------|----------|-------------|
| **GET** | `/user/import/{type}/mapping` | Get current mappings for provider |
| **POST** | `/user/import/{type}/mapping` | Save group mappings |

### Directory Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| **GET** | `/user/import/{type}/test` | Test provider connection |
| **GET** | `/user/import/{type}/groups` | Search LDAP groups |
| **POST** | `/user/import/{type}` | Find users matching mapping |

### Authentication Providers

| Method | Endpoint | Description |
|--------|----------|-------------|
| **GET** | `/user/providers` | Get enabled auth providers |

### Example: Create Job

```bash
POST /user/import/job
Content-Type: application/json

{
  "name": "Weekly AD Sync",
  "providerType": "ACTIVE_DIRECTORY",
  "preserveRoles": true,
  "frequency": "WEEKLY",
  "weekDays": ["MONDAY", "FRIDAY"],
  "startDate": "2024-06-29T02:00:00Z",
  "enabled": true,
  "groupRoleImportMapping": {
    "provider": "ACTIVE_DIRECTORY",
    "roleGroups": [
      {
        "role": { "id": 1, "name": "admin" },
        "groups": [
          { "displayName": "Admins", 
            "distinguishedName": "cn=admins,ou=groups,dc=example,dc=com" }
        ]
      }
    ]
  }
}
```

---

## Database Schema

### user_import_job

Stores import job configurations and scheduling:

```sql
CREATE TABLE ${ohdsiSchema}.user_import_job (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255),
  enabled BOOLEAN,
  provider_type VARCHAR(32),  -- LDAP, ACTIVE_DIRECTORY
  preserve_roles BOOLEAN,     -- Keep manually assigned roles?
  user_roles TEXT,            -- JSON: users for one-time import
  start_date TIMESTAMP,       -- First execution
  frequency VARCHAR(32),      -- ONCE, DAILY, WEEKLY, CRON
  cron_expression VARCHAR(255),
  next_fire_time TIMESTAMP,
  is_closed BOOLEAN,
  -- Foreign key to schedule provider (Arachne)
  CONSTRAINT fk_user_import_job_schedule 
    FOREIGN KEY (schedule_id) REFERENCES user_import_schedule(id)
);

CREATE TABLE ${ohdsiSchema}.user_import_job_weekdays (
  user_import_job_id BIGINT,
  day_of_week VARCHAR(32),  -- MONDAY, TUESDAY, etc.
  CONSTRAINT fk_user_import_job_weekdays 
    FOREIGN KEY (user_import_job_id) REFERENCES user_import_job(id)
);
```

### sec_group_role_import

Stores LDAP/AD group-to-role mappings (renamed from sec_role_group in v2.99):

```sql
CREATE TABLE ${ohdsiSchema}.sec_group_role_import (
  id INTEGER PRIMARY KEY,
  provider VARCHAR(32) NOT NULL,      -- LDAP or ACTIVE_DIRECTORY
  group_dn VARCHAR(255) NOT NULL,     -- Distinguished name in LDAP
  group_name VARCHAR(255),            -- Display name
  role_id INTEGER NOT NULL,
  job_id BIGINT,                      -- NULL if shared, else per-job mapping
  
  CONSTRAINT sec_group_role_import_pkey PRIMARY KEY (id),
  CONSTRAINT fk_group_role_import_job 
    FOREIGN KEY (job_id) REFERENCES user_import_job(id) ON DELETE CASCADE,
  CONSTRAINT fk_group_role_import_role 
    FOREIGN KEY (role_id) REFERENCES sec_role(id),
  
  -- Unique per provider/group/role/job
  CONSTRAINT uc_provider_group_role 
    UNIQUE (provider, group_dn, role_id, job_id)
);

CREATE SEQUENCE ${ohdsiSchema}.sec_group_role_import_seq
  START WITH 1 INCREMENT BY 1;
```

### Spring Batch Tables

Spring Batch records execution details:

```sql
-- Job execution header
BATCH_JOB_EXECUTION (
  JOB_EXECUTION_ID PRIMARY KEY,
  JOB_INSTANCE_ID,
  JOB_NAME (e.g., 'USERS_IMPORT'),
  START_TIME, END_TIME,
  STATUS (STARTING, STARTED, COMPLETED, FAILED, STOPPED),
  EXIT_CODE (COMPLETED, FAILED, NOOP)
);

-- Per-step execution
BATCH_STEP_EXECUTION (
  STEP_EXECUTION_ID PRIMARY KEY,
  JOB_EXECUTION_ID,
  STEP_NAME (e.g., 'findUsersForImport', 'importUsers'),
  START_TIME, END_TIME,
  STATUS, EXIT_CODE,
  READ_COUNT, WRITE_COUNT, COMMIT_COUNT, ROLLBACK_COUNT,
  READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT
);

-- Execution context (state)
BATCH_JOB_EXECUTION_CONTEXT (
  JOB_EXECUTION_ID PRIMARY KEY,
  SHORT_CONTEXT (serialized state)
);

BATCH_STEP_EXECUTION_CONTEXT (
  STEP_EXECUTION_ID PRIMARY KEY,
  SHORT_CONTEXT (serialized state - e.g., user list)
);
```

Query job history:

```sql
-- Latest 10 jobs
SELECT j.id, j.name, je.job_name, je.start_time, je.end_time, je.status, je.exit_code
FROM user_import_job j
JOIN batch_job_instance ji ON j.arachne_job_id = ji.job_instance_id
JOIN batch_job_execution je ON ji.job_instance_id = je.job_instance_id
ORDER BY je.start_time DESC
LIMIT 10;

-- Step execution details for a job
SELECT se.step_name, se.start_time, se.end_time, se.status,
       se.read_count, se.write_count
FROM batch_job_execution je
JOIN batch_step_execution se ON je.job_execution_id = se.job_execution_id
WHERE je.job_execution_id = ?;
```

---

## Integration Points

### UserOrigin Enum

Controls which authentication source a user/role came from:

```java
public enum UserOrigin {
  SYSTEM,      // Manual creation
  AD,          // Active Directory import
  LDAP,        // LDAP import
  WINDOWS,     // Kerberos/Windows auth
  KERBEROS,    // Kerberos auth
  GOOGLE,      // Google OAuth
  FACEBOOK,    // Facebook OAuth
  DATABASE,    // Database auth
  OIDC         // Generic OIDC
}

// Used during import
UserOrigin userOrigin = UserOrigin.getFrom(providerType);
userEntity.setOrigin(userOrigin);
```

### AuthorizationService Integration

Manages user roles:

```java
// In UserImportTasklet
AuthorizationService userManager;

// Create user with roles
userManager.registerUser(login, displayName, userOrigin, roles);

// Update roles
userManager.addUserToRole(roleNam, login, userOrigin);
userManager.removeUserFromRole(roleName, login, userOrigin);

// Query roles
List<Role> userRoles = userManager.getUserRoles(userId);
```

### UserRepository Integration

Accesses user entities:

```java
Optional<UserEntity> user = userRepository.findByLogin(login);
List<UserEntity> users = userRepository.findByOrigin(userOrigin);
```

---

## Common Development Tasks

### Adding a New LDAP Provider Type

1. **Create provider class** in `org.ohdsi.webapi.security.provisioning.providers`:

```java
@Component
public class CustomLdapProvider extends AbstractLdapProvider {
  @Override
  public List<LdapUserDetail> findUsers() { ... }
  
  @Override
  public List<LdapGroup> findGroups(String searchStr) { ... }
}
```

2. **Register in map** (UserImportServiceImpl constructor):

```java
Optional.ofNullable(customLdapProvider)
  .ifPresent(provider → providersMap.put(LdapProviderType.CUSTOM, provider));
```

3. **Add configuration** to `application.yaml`:

```yaml
security:
  auth:
    custom:
      enabled: false
      url: ldap://...
      system:
        username: ...
        password: ...
```

### Modifying Import Logic

**FindUsersToImportTasklet**: Customize user query/filtering
**UserImportTasklet**: Customize role assignment logic

Changes cascade through Spring Batch job execution automatically.

### Querying Import History

```java
Stream<UserImportJobHistoryItem> history = 
    userImportJobService.getJobHistoryItems(jobId);

Optional<UserImportJobHistoryItem> latest =
    userImportJobService.getLatestHistoryItem(jobId);
```

### Testing Group Mappings

```bash
# Get current mappings
GET /user/import/LDAP/mapping

# Get users matching mappings (test)
POST /user/import/LDAP
Content-Type: application/json
{
  "provider": "LDAP",
  "roleGroups": [ ... ]
}

# Response: List<AtlasUserRoles> with roles resolved
```

### Debugging Job Failures

1. **Check Spring Batch status**:

```sql
SELECT * FROM batch_job_execution 
WHERE job_name = 'USERS_IMPORT' 
ORDER BY start_time DESC 
LIMIT 1;
```

2. **View step details**:

```sql
SELECT * FROM batch_step_execution 
WHERE job_execution_id = ? 
ORDER BY start_time ASC;
```

3. **Check execution context**:

```sql
SELECT short_context FROM batch_job_execution_context 
WHERE job_execution_id = ?;
```

4. **Application logs** for step-specific errors

---

## References

- **LoginPipeline.md** - User authentication flow
- **NOTIFICATION_SYSTEM_UPDATE.md** - Event system integration
- **Flyway Migrations** - Database schema versioning
- **Spring Batch Documentation** - Job orchestration framework
- **Spring LDAP Documentation** - Directory operations
