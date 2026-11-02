# WebAPI User Import System - Quick Reference

## Executive Summary

The WebAPI user import system is a **Spring Batch-based job scheduler** that imports users from LDAP/Active Directory into Atlas, with automatic role assignment based on group mappings.

### Key Statistics
- **43 Java classes** in provisioning package
- **9 REST endpoints** for user/import operations
- **2 LDAP providers** (AD and standard LDAP)
- **2-step batch job** (Find Users → Import Users)
- **3 main database tables** + history view

---

## Core Components Map

```
┌─────────────────────────────────────────────────────────────┐
│             REST API Layer (Endpoints)                      │
├─────────────────────────────────────────────────────────────┤
│  UserImportServiceImpl (@RestController)                    │
│  └─ GET /user/providers                                     │
│  └─ GET /user/import/{type}/test                            │
│  └─ GET /user/import/{type}/groups                          │
│  └─ POST /user/import/{type}                                │
│  └─ POST /user/import                                       │
│  └─ POST/GET /user/import/{type}/mapping                   │
│                                                              │
│  UserImportJobServiceImpl (@RestController)                 │
│  └─ POST /user/import/job                                   │
│  └─ PUT /user/import/job/{id}                               │
│  └─ GET /user/import/job(list)                              │
│  └─ GET /user/import/job/{id}                               │
│  └─ DELETE /user/import/job/{id}                            │
│  └─ GET /user/import/job/{id}/history                      │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│          Service Layer (Business Logic)                     │
├─────────────────────────────────────────────────────────────┤
│  UserImportService (interface)                              │
│  └─ findGroups()                                            │
│  └─ findUsers()                                             │
│  └─ importUsers()                                           │
│  └─ saveRoleGroupMapping()                                  │
│  └─ getRoleGroupMapping()                                   │
│  └─ testConnection()                                        │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│       LDAP Provider Layer (Directory Access)                │
├─────────────────────────────────────────────────────────────┤
│  LdapProvider (interface)                                   │
│  └─ AbstractLdapProvider                                    │
│     ├─ ActiveDirectoryProvider                             │
│     └─ DefaultLdapProvider                                  │
│                                                              │
│  Uses: LdapTemplate, SearchControls, AttributesMapper      │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│        Spring Batch Job Execution                           │
├─────────────────────────────────────────────────────────────┤
│  UserImportScheduledTask                                    │
│  └─ buildJobForUserImportTasklet()                         │
│     ├─ Step 1: FindUsersToImportTasklet                    │
│     └─ Step 2: UserImportTasklet                           │
│                                                              │
│  Job Parameters: USER_IMPORT_ID, JOB_NAME, JOB_AUTHOR      │
│  Execution Context: USER_ROLES (serialized)                │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│         Authorization Integration                           │
├─────────────────────────────────────────────────────────────┤
│  AuthorizationService                                       │
│  └─ registerUser()                                          │
│  └─ addUserToRole()                                         │
│  └─ removeUserFromRole()                                    │
│  └─ getUserRoles()                                          │
│                                                              │
│  UserRepository                                             │
│  └─ findByLogin()                                           │
│  └─ findByOrigin()                                          │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│          Database Layer                                     │
├─────────────────────────────────────────────────────────────┤
│  user_import_job                                            │
│  sec_group_role_import                                      │
│  user_import_job_weekdays                                   │
│  user_import_job_history (view)                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Main Entry Points

### REST Endpoints Quick Reference

| Endpoint | Method | Purpose | Auth |
|----------|--------|---------|------|
| `/user/providers` | GET | Get provider URLs | admin:security |
| `/user/import/{type}/test` | GET | Test connection | admin:security |
| `/user/import/{type}/groups` | GET | Find LDAP groups | admin:security |
| `/user/import/{type}` | POST | Find users | admin:security |
| `/user/import` | POST | Import users (create job) | admin:security |
| `/user/import/{type}/mapping` | POST | Save role mapping | admin:security |
| `/user/import/{type}/mapping` | GET | Get role mapping | admin:security |
| `/user/import/job` | POST | Create scheduled job | admin:security |
| `/user/import/job` | GET | List jobs | admin:security |
| `/user/import/job/{id}` | GET | Get job | admin:security |
| `/user/import/job/{id}` | PUT | Update job | admin:security |
| `/user/import/job/{id}` | DELETE | Delete job | admin:security |
| `/user/import/job/{id}/history` | GET | Get history | admin:security |

---

## Key Classes

### Services

| Class | Role | Scope |
|-------|------|-------|
| `UserImportService` | Interface defining user import operations | Service |
| `UserImportServiceImpl` | Implements service + REST controller for user ops | REST + Service |
| `UserImportJobService` | Interface for job management | Service |
| `UserImportJobServiceImpl` | Implements job service + REST controller | REST + Service |

### LDAP Providers

| Class | Type | Provider |
|-------|------|----------|
| `LdapProvider` | Interface | Directory access contract |
| `AbstractLdapProvider` | Abstract | Common LDAP operations |
| `ActiveDirectoryProvider` | Implementation | AD-specific logic |
| `DefaultLdapProvider` | Implementation | Standard LDAP logic |

### Batch Jobs

| Class | Purpose |
|-------|---------|
| `FindUsersToImportTasklet` | Step 1: Query LDAP for users |
| `UserImportTasklet` | Step 2: Create/update users in Atlas |
| `BaseUserImportTasklet` | Base class with common logic |
| `UserImportScheduledTask` | Wrapper for scheduled execution |

### Data Models

| Class | Type | Purpose |
|-------|------|---------|
| `UserImportJob` | Entity | Scheduled job configuration |
| `GroupRoleImportEntity` | Entity | Group-to-role mappings |
| `UserImportJobDTO` | DTO | Job request/response |
| `AtlasUserRoles` | Model | User with roles during import |
| `RoleGroupMapping` | Model | Role-group mapping definition |
| `LdapGroup` | Model | LDAP group from directory |
| `LdapUser` | Model | User from LDAP with groups |

### Repositories

| Class | Entity |
|-------|--------|
| `UserImportJobRepository` | UserImportJob |
| `GroupRoleImportRepository` | GroupRoleImportEntity |
| `UserImportJobHistoryItemRepository` | UserImportJobHistoryItem |

---

## Database Schema Quick View

### user_import_job
```
id (PK)               - Sequence: user_import_job_seq
provider_type        - LDAP or ACTIVE_DIRECTORY
is_enabled           - Job enabled/disabled
start_date           - When to start
frequency            - ONCE, DAILY, WEEKLY
cron                 - Cron expression
last_executed_at     - Last execution time
executed_times       - Count of executions
preserve_roles       - Keep existing roles?
user_roles           - Serialized user list (JSON)
```

### sec_group_role_import
```
id (PK)              - Sequence: sec_group_role_import_seq
provider             - LDAP or ACTIVE_DIRECTORY
group_dn             - LDAP group Distinguished Name
group_name           - Display name
role_id (FK)         - Reference to sec_role
job_id (FK)          - Optional: Reference to user_import_job
```

### user_import_job_weekdays
```
user_import_job_id (PK,FK)  - Reference to user_import_job
day_of_week (PK)            - MONDAY, TUESDAY, etc.
```

### user_import_job_history (View)
```
id, start_time, end_time, status, exit_code, exit_message
job_name, user_import_id, author (login)
```

---

## Configuration Properties

### Active Directory
```yaml
security.auth.ad:
  url: "ldap://server:389"
  enabled: false
  searchBase: "CN=Users,DC=example,DC=org"
  system.username: "service-account"
  system.password: "password"
  userImport:
    loginAttr: "sAMAccountName"
    usernameAttr: "cn"
  default.import.group: "public"  # Default role
```

### LDAP
```yaml
security.auth.ldap:
  url: "ldap://server:389"
  enabled: false
  baseDn: "dc=example,dc=org"
  system.username: "cn=admin,dc=example,dc=org"
  system.password: "password"
  userImport:
    loginAttr: "uid"
    usernameAttr: "cn"
```

---

## Typical Workflows

### 1. Setup Role-Group Mapping
```
Admin → POST /user/import/{type}/mapping
        ├─ Specify provider (ad/ldap)
        └─ Map role names to group DNs
        
Result: Stored in sec_group_role_import table
```

### 2. Find Users for Preview
```
Admin → POST /user/import/{type}
        ├─ Provider type
        └─ Role mapping
        
Service: Queries LDAP, applies mappings, returns users
Result: List of AtlasUserRoles with status
```

### 3. Create Scheduled Import Job
```
Admin → POST /user/import/job
        ├─ Provider type
        ├─ Frequency (ONCE, DAILY, WEEKLY)
        ├─ Cron expression (optional)
        ├─ preserveRoles flag
        └─ startDate
        
Service: Creates job, schedules via TaskScheduler
Result: UserImportJobDTO with nextExecution calculated
```

### 4. Execute Job (Manual or Scheduled)
```
TaskScheduler → triggers UserImportScheduledTask.run()
        ├─ Builds JobParameters
        └─ Launches Spring Batch Job
        
Step 1: FindUsersToImportTasklet
        ├─ Queries LDAP using provider
        ├─ Applies group-to-role mapping
        └─ Serializes users to execution context
        
Step 2: UserImportTasklet
        ├─ Deserializes users
        ├─ Creates/updates UserEntity in DB
        ├─ Assigns roles based on mapping
        └─ Returns import result
        
Result: Written to user_import_job_history view
```

---

## Error Handling

| Error | Cause | Solution |
|-------|-------|----------|
| `IllegalArgumentException` | Provider not configured | Enable AD/LDAP in application.yaml |
| `JobAlreadyExistException` | Job for provider already active | Delete or disable existing job |
| Connection timeout | LDAP server unreachable | Test connection, check credentials |
| Role mapping empty | No groups mapped | Save role-group mapping first |
| User role assignment fails | Authorization error | Check role permissions |

---

## Performance Tips

1. **LDAP Queries**: Use specific searchBase, not entire directory
2. **Group Mapping**: Keep mappings clean, avoid orphaned groups
3. **Job Frequency**: Run during off-peak hours for large directories
4. **Preserve Roles**: Set to `true` to reduce role churn
5. **Batch Size**: Configure Spring Batch chunk size for user processing

---

## Dependencies & Integration Points

### Spring Batch
- JobRepository, JobBuilder, StepBuilder
- TaskScheduler for scheduling
- JobTemplate for launching jobs

### Spring LDAP
- LdapTemplate for directory queries
- SearchControls, AttributesMapper for query config
- LdapContextSource for connection management

### WebAPI Security
- AuthorizationService for user provisioning
- UserRepository for user access
- RoleEntity, UserEntity for domain models

### Serialization
- Jackson for JSON (AtlasUserRoles, RoleGroupMapping)
- Custom serialization for job execution context (Utils.serialize)

---

## File Structure

```
src/main/java/org/ohdsi/webapi/security/provisioning/
├── service/
│   ├── UserImportService.java          (interface)
│   ├── UserImportServiceImpl.java       (REST + service)
│   ├── UserImportJobService.java       (interface)
│   ├── UserImportJobServiceImpl.java    (REST + service + job builder)
│   ├── FindUsersToImportTasklet.java   (batch step 1)
│   ├── UserImportTasklet.java          (batch step 2)
│   └── BaseUserImportTasklet.java      (base tasklet)
├── providers/
│   ├── LdapProvider.java               (interface)
│   ├── AbstractLdapProvider.java       (abstract impl)
│   ├── ActiveDirectoryProvider.java    (AD provider)
│   ├── DefaultLdapProvider.java        (LDAP provider)
│   └── OhdsiLdapUtils.java             (utilities)
├── model/
│   ├── UserImportJob.java              (entity)
│   ├── GroupRoleImportEntity.java      (entity)
│   ├── UserImportJobDTO.java           (DTO)
│   ├── AtlasUserRoles.java             (model)
│   ├── RoleGroupMapping.java           (model)
│   ├── LdapGroup.java, LdapUser.java   (models)
│   ├── LdapProviderType.java           (enum)
│   ├── UserImportResult.java           (result)
│   └── *Repository.java                (5 repos)
├── converter/
│   ├── UserImportJobToUserImportJobDTOConverter.java
│   ├── UserImportJobDTOToUserImportJobConverter.java
│   └── RoleGroupMappingConverter.java
├── GroupRoleImportUtils.java
├── RoleGroupUtils.java
└── JobAlreadyExistException.java
```

---

## Migration Timeline

- **v2.6.0**: Initial user import schema
- **v2.6.0**: Job history view
- **v2.6.0**: Role-group mapping table
- **v2.8.0**: Added user_roles column
- **v2.99.0006**: Renamed sec_role_group → sec_group_role_import
- **v3.0.0**: Consolidated into baseline

---

## Related HOWTO Documents

- **Group_Role_Import_HOWTO.md** - (Currently empty - needs documentation)
- **LoginPipeline.md** - Overall authentication flow
- **EntityAccess_HOWTO.md** - Authorization details

---

## Quick Setup Checklist

- [ ] Configure AD or LDAP URL in application.yaml
- [ ] Set system credentials for directory access
- [ ] Enable provider (enabled: true)
- [ ] Test connection via `/user/import/{type}/test`
- [ ] Find groups via `/user/import/{type}/groups`
- [ ] Create role-group mapping via `/user/import/{type}/mapping`
- [ ] Create scheduled import job via `/user/import/job`
- [ ] Monitor job execution history via `/user/import/job/{id}/history`

---

*Quick Reference v1.0 - June 29, 2026*
