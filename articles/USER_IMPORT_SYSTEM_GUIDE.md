# WebAPI User Import System - Comprehensive Developer Guide

**Document Date**: June 29, 2026  
**System**: OHDSI WebAPI v3.x  
**Focus**: User provisioning from LDAP/Active Directory

---

## Table of Contents
1. [System Overview](#system-overview)
2. [Main Entry Points & Services](#main-entry-points--services)
3. [Data Models & Entities](#data-models--entities)
4. [Task/Job Execution](#taskjob-execution)
5. [LDAP Provider Architecture](#ldap-provider-architecture)
6. [Configuration](#configuration)
7. [Database Schema](#database-schema)
8. [Integration Points](#integration-points)

---

## System Overview

The WebAPI user import system is a **Spring Batch-based scheduling system** that:
- Imports users from LDAP or Active Directory
- Maps LDAP groups to Atlas roles
- Manages scheduled import jobs with cron expressions
- Preserves or replaces user roles based on configuration
- Maintains job execution history

### Architecture Patterns Used
- **REST Controller + Service**: UserImportServiceImpl combines REST endpoints with business logic
- **Spring Batch**: Two-step jobs (Find → Import)
- **Scheduled Task Wrapper**: UserImportScheduledTask wraps batch jobs
- **Strategy Pattern**: Abstract LDAP provider with concrete implementations
- **Lazy Initialization**: Prevents circular dependencies in service injection
- **Entity Graph**: Optimizes queries with named graphs (jobWithMapping)

---

## Main Entry Points & Services

### 1. UserImportService (Interface)
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportService.java](src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportService.java)

**Key Methods**:
```java
// Find LDAP groups
List<LdapGroup> findGroups(LdapProviderType providerType, String searchStr);

// Find users with mapped roles
List<AtlasUserRoles> findUsers(LdapProviderType providerType, RoleGroupMapping mapping);

// Import users and create/update accounts
UserImportResult importUsers(List<AtlasUserRoles> users, 
                             LdapProviderType providerType, 
                             boolean preserveRoles);

// Role-group mapping management
void saveRoleGroupMapping(LdapProviderType providerType, 
                         List<GroupRoleImportEntity> mappingEntities);
List<GroupRoleImportEntity> getRoleGroupMapping(LdapProviderType providerType);

// Connection testing
void testConnection(LdapProviderType provider);

// Job retrieval
UserImportJob getImportUserJob(Long userImportId);
```

### 2. UserImportServiceImpl (Implementation)
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportServiceImpl.java](src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportServiceImpl.java)

**Scope**: @RestController, @RequestMapping("/user")

**Key Responsibilities**:
- Manages provider map (AD and LDAP)
- Implements all REST endpoints for user import
- Coordinates between LDAP providers and authorization service
- Handles user creation/update with role assignment
- Manages role-group mappings

**Key Fields**:
```java
private final Map<LdapProviderType, LdapProvider> providersMap;
private final UserRepository userRepository;
private final UserImportJobRepository userImportJobRepository;
private final AuthorizationService userManager;
private final GroupRoleImportRepository roleGroupMappingRepository;
private final UserImportJobService userImportJobService;
private final GenericConversionService conversionService;

@Value("${security.auth.ad.default.import.group}")
private List<String> defaultRoles;
```

**REST Endpoints**:
| Method | Path | Purpose |
|--------|------|---------|
| GET | /user/providers | Get enabled authentication providers (AD/LDAP URLs) |
| GET | /user/import/{type}/test | Test connection to provider |
| GET | /user/import/{type}/groups | Find groups in directory (search parameter optional) |
| POST | /user/import/{type} | Find users with role mapping |
| POST | /user/import | Import users (creates one-time job) |
| POST | /user/import/{type}/mapping | Save role-group mapping |
| GET | /user/import/{type}/mapping | Retrieve role-group mapping |

**Constructor Dependencies**:
```java
public UserImportServiceImpl(
    @Autowired(required = false) ActiveDirectoryProvider activeDirectoryProvider,
    @Autowired(required = false) DefaultLdapProvider ldapProvider,
    UserRepository userRepository,
    UserImportJobRepository userImportJobRepository,
    AuthorizationService userManager,
    GroupRoleImportRepository roleGroupMappingRepository,
    @Lazy @Autowired(required = false) UserImportJobService userImportJobService,
    GenericConversionService conversionService)
```

**Important**: Uses @Lazy to prevent circular dependency with UserImportJobService

---

### 3. UserImportJobService (Interface)
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportJobService.java](src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportJobService.java)

**Extends**: BaseJobService<UserImportJob>

**Key Methods**:
```java
List<UserImportJob> getJobs();
Optional<UserImportJob> getJob(Long id);
Stream<UserImportJobHistoryItem> getJobHistoryItems(Long id);
Optional<UserImportJobHistoryItem> getLatestHistoryItem(Long id);
```

### 4. UserImportJobServiceImpl (Implementation)
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportJobServiceImpl.java](src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportJobServiceImpl.java)

**Scope**: @RestController, @RequestMapping("/user/import/job"), @Transactional

**Key Responsibilities**:
- Creates/updates/deletes user import jobs
- Builds Spring Batch jobs with proper step configuration
- Manages scheduled task execution
- Handles role-group mapping persistence
- Maintains job execution history

**Key Fields**:
```java
private final UserImportService userImportService;
private final UserImportJobRepository jobRepository;
private final GroupRoleImportRepository roleGroupRepository;
private final UserImportJobHistoryItemRepository jobHistoryItemRepository;
private final TransactionTemplate transactionTemplate;
private final JobRepository jobRepositoryBatch;  // Spring Batch
private final PlatformTransactionManager transactionManager;
private final JobTemplate jobTemplate;
private final GenericConversionService conversionService;
```

**POST /user/import/job** - Create user import job
```java
@PostMapping
@PreAuthorize("isPermitted('admin:security')")
public UserImportJobDTO createJobEndpoint(@RequestBody UserImportJobDTO jobDTO)
```

**PUT /user/import/job/{id}** - Update user import job
```java
@PutMapping(value = "/{id}")
@PreAuthorize("isPermitted('admin:security')")
public UserImportJobDTO updateJobEndpoint(@PathVariable Long jobId, 
                                          @RequestBody UserImportJobDTO jobDTO)
```

**GET /user/import/job** - List all jobs with calculated next execution
```java
@GetMapping
@Transactional
@PreAuthorize("isPermitted('admin:security')")
public List<UserImportJobDTO> listJobsEndpoint()
```

**GET /user/import/job/{id}** - Get single job
```java
@GetMapping(value = "/{id}")
@PreAuthorize("isPermitted('admin:security')")
public UserImportJobDTO getJobEndpoint(@PathVariable Long id)
```

**DELETE /user/import/job/{id}** - Delete job
```java
@DeleteMapping(value = "/{id}")
@PreAuthorize("isPermitted('admin:security')")
public void deleteJobEndpoint(@PathVariable Long id)
```

**GET /user/import/job/{id}/history** - Get job execution history
```java
@GetMapping(value = "/{id}/history")
@PreAuthorize("isPermitted('admin:security')")
public List<JobHistoryItemDTO> getImportHistoryEndpoint(@PathVariable Long id)
```

---

## Data Models & Entities

### 1. UserImportJob (JPA Entity)
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/model/UserImportJob.java](src/main/java/org/ohdsi/webapi/security/provisioning/model/UserImportJob.java)

**Extends**: ArachneJob (base job entity with scheduling properties)

**Table**: `user_import_job`

**Fields**:
```java
@Id
private Long id;  // Generated from sequence: user_import_job_seq

// Inherited from ArachneJob
private boolean enabled;
private Date startDate;
private String frequency;  // JobExecutingType enum
private Integer recurringTimes;
private Date recurringUntilDate;
private String cron;
private Date lastExecutedAt;
private Integer executedTimes;
private Boolean isClosed;

// User import specific
@Enumerated(EnumType.STRING)
private LdapProviderType providerType;  // LDAP or ACTIVE_DIRECTORY

@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(name = "user_import_job_weekdays")
private List<DayOfWeek> weekDays;  // For scheduled recurrence

@OneToMany(mappedBy = "userImportJob")
private List<GroupRoleImportEntity> groupRoleImportMapping;

private Boolean preserveRoles;  // If true, don't override existing roles
private String userRoles;  // JSON serialized list of users (used in job context)
```

**Named Entity Graph**:
```
@NamedEntityGraph(name = "jobWithMapping",
    attributeNodes = @NamedAttributeNode("groupRoleImportMapping"))
```

### 2. GroupRoleImportEntity (JPA Entity)
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/model/GroupRoleImportEntity.java](src/main/java/org/ohdsi/webapi/security/provisioning/model/GroupRoleImportEntity.java)

**Table**: `sec_group_role_import`

**Purpose**: Maps LDAP groups to Atlas roles for user import

**Fields**:
```java
@Id
@GeneratedValue(generator = "sec_group_role_import_generator")
private int id;  // Sequence: sec_group_role_import_seq

@Enumerated(EnumType.STRING)
private LdapProviderType provider;  // LDAP or ACTIVE_DIRECTORY

private String groupDn;  // Distinguished Name of LDAP group
private String groupName;  // Display name of group

@ManyToOne
@JoinColumn(name = "role_id")
private RoleEntity role;  // Reference to Atlas role

@ManyToOne
@JoinColumn(name = "job_id")
private UserImportJob userImportJob;  // Back-reference to job
```

**Renamed in Migration V2.99.0006**: Previously named `sec_role_group`, renamed to `sec_group_role_import` for clarity

### 3. UserImportJobDTO (Data Transfer Object)
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/model/UserImportJobDTO.java](src/main/java/org/ohdsi/webapi/security/provisioning/model/UserImportJobDTO.java)

**Extends**: ArachneJobDTO

**Fields**:
```java
private LdapProviderType providerType;
private Boolean preserveRoles;
private String userRoles;  // JSON serialized
private Date lastExecuted;
private Date nextExecution;  // Calculated
private Date startDate;
private RoleGroupMapping groupRoleImportMapping;  // Contains role-group pairs
```

**Used for**: REST API request/response serialization

### 4. Supporting Model Classes

#### LdapGroup
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/model/LdapGroup.java](src/main/java/org/ohdsi/webapi/security/provisioning/model/LdapGroup.java)

**Extends**: LdapObject

**Fields**:
- displayName: String
- distinguishedName: String (DN)

#### LdapUser
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/model/LdapUser.java](src/main/java/org/ohdsi/webapi/security/provisioning/model/LdapUser.java)

**Extends**: LdapObject

**Fields**:
```java
private List<LdapGroup> groups;  // Groups user is member of
private String login;  // User login (from loginAttributeName)
private String displayName;
private String distinguishedName;
```

#### AtlasUserRoles
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/model/AtlasUserRoles.java](src/main/java/org/ohdsi/webapi/security/provisioning/model/AtlasUserRoles.java)

**Purpose**: Intermediate model for import process

**Fields**:
```java
@JsonProperty("login")
private String login;  // LDAP login
@JsonProperty("displayName")
private String displayName;
@JsonProperty("roles")
private List<Role> roles;  // Mapped roles (from group mapping)
@JsonProperty("status")
private LdapUserImportStatus status;  // NEW, MODIFIED, EXISTS, or DISABLED
```

#### RoleGroupMapping
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/model/RoleGroupMapping.java](src/main/java/org/ohdsi/webapi/security/provisioning/model/RoleGroupMapping.java)

**Purpose**: Maps roles to LDAP groups for import

**Fields**:
```java
@JsonProperty("provider")
private String provider;  // "ad" or "ldap"
@JsonProperty("roleGroups")
private List<RoleGroupsMap> roleGroups;  // List of role-group pairs
```

#### RoleGroupsMap
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/model/RoleGroupsMap.java](src/main/java/org/ohdsi/webapi/security/provisioning/model/RoleGroupsMap.java)

**Fields**:
```java
@JsonProperty("role")
private Role role;  // Atlas role
@JsonProperty("groups")
private List<LdapGroup> groups;  // LDAP groups that map to this role
```

#### UserImportResult
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/model/UserImportResult.java](src/main/java/org/ohdsi/webapi/security/provisioning/model/UserImportResult.java)

**Purpose**: Import job result summary

**Fields**:
```java
private int created;  // New users created
private int updated;  // Existing users updated

// Getters/setters and increment methods
```

#### UserImportJobHistoryItem
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/model/UserImportJobHistoryItem.java](src/main/java/org/ohdsi/webapi/security/provisioning/model/UserImportJobHistoryItem.java)

**Table**: `user_import_job_history` (view)

**Purpose**: Audit trail of job executions

**Fields**:
```java
@Id
private Long id;
@Temporal(TemporalType.TIMESTAMP)
private Date startTime;
@Temporal(TemporalType.TIMESTAMP)
private Date endTime;
private String status;  // STARTED, COMPLETED, FAILED, etc.
private String exitCode;
private String exitMessage;
private String author;  // User who triggered job
@OneToOne(fetch = FetchType.LAZY)
private UserImportJob userImport;
private String jobName;
```

#### LdapProviderType (Enum)
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/model/LdapProviderType.java](src/main/java/org/ohdsi/webapi/security/provisioning/model/LdapProviderType.java)

**Values**:
```java
ACTIVE_DIRECTORY("ad")
LDAP("ldap")

// Factory method
public static LdapProviderType fromValue(String value)
```

#### LdapUserImportStatus (Enum)
**Purpose**: User import status classification

**Values**:
- NEW: User doesn't exist in Atlas
- MODIFIED: User exists but roles have changed
- EXISTS: User exists with same roles
- DISABLED: User has been disabled

---

## Task/Job Execution

### 1. FindUsersToImportTasklet
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/service/FindUsersToImportTasklet.java](src/main/java/org/ohdsi/webapi/security/provisioning/service/FindUsersToImportTasklet.java)

**Implements**: StepExecutionListener

**Purpose**: First step in user import job - finds users matching group mappings

**Key Method**:
```java
@Override
protected List<AtlasUserRoles> doUserImportTask(ChunkContext chunkContext, 
                                                UserImportJob userImportJob) {
    RoleGroupMapping roleGroupMapping = RoleGroupMappingConverter
        .convertRoleGroupMapping(
            userImportJob.getProviderType().toString(), 
            userImportJob.getRoleGroupMapping());
    userRoles = userImportService.findUsers(
        userImportJob.getProviderType(), 
        roleGroupMapping);
    return userRoles;
}
```

**Step Execution Listener**:
```java
@Override
public ExitStatus afterStep(StepExecution stepExecution) {
    // Serialize found users to job execution context for next step
    stepExecution.getJobExecution().getExecutionContext()
        .putString(Constants.Params.USER_ROLES, Utils.serialize(userRoles));
    return null;
}
```

### 2. UserImportTasklet
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportTasklet.java](src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportTasklet.java)

**Implements**: StepExecutionListener

**Purpose**: Second step - imports users and assigns roles

**Key Method**:
```java
@Override
protected UserImportResult doUserImportTask(ChunkContext chunkContext, 
                                            UserImportJob userImportJob) {
    // Get users from previous step context
    if (Objects.isNull(users)) {
        if (Objects.isNull(userImportJob.getUserRoles())) {
            throw new IllegalArgumentException("userRoles required");
        }
        users = Utils.deserialize(userImportJob.getUserRoles(), 
            factory -> factory.constructCollectionType(
                List.class, AtlasUserRoles.class));
    }
    return result = userImportService.importUsers(
        users, 
        userImportJob.getProviderType(), 
        userImportJob.getPreserveRoles());
}
```

**Step Listener** - Retrieves users from previous step:
```java
@Override
public void beforeStep(StepExecution stepExecution) {
    String userRolesJson = stepExecution.getJobExecution()
        .getExecutionContext()
        .getString(Constants.Params.USER_ROLES, null);
    if (Objects.nonNull(userRolesJson)) {
        users = Utils.deserialize(userRolesJson, ...);
    }
}
```

### 3. BaseUserImportTasklet (Abstract Base)
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/service/BaseUserImportTasklet.java](src/main/java/org/ohdsi/webapi/security/provisioning/service/BaseUserImportTasklet.java)

**Extends**: TransactionalTasklet<T>

**Purpose**: Common base for both tasklets

**Key Method**:
```java
@Override
protected T doTask(ChunkContext chunkContext) {
    // Extract job parameters
    Map<String, Object> jobParameters = 
        chunkContext.getStepContext().getJobParameters();
    Long userImportId = Long.valueOf(
        jobParameters.get(Constants.Params.USER_IMPORT_ID).toString());
    
    // Load UserImportJob from repository
    UserImportJob userImportJob = 
        userImportService.getImportUserJob(userImportId);
    
    // Delegate to subclass implementation
    return doUserImportTask(chunkContext, userImportJob);
}

protected abstract T doUserImportTask(ChunkContext chunkContext, 
                                      UserImportJob userImportJob);
```

### 4. Spring Batch Job Configuration

**Location**: [src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportJobServiceImpl.java](src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportJobServiceImpl.java) (lines ~280-300)

**Job Building**:
```java
Job buildJobForUserImportTasklet(UserImportJob job) {
    // Create first step: find users
    FindUsersToImportTasklet findUsersTasklet = 
        new FindUsersToImportTasklet(transactionTemplate, userImportService);
    Step findUsersStep = new StepBuilder("findUsersForImport", jobRepositoryBatch)
        .tasklet(findUsersTasklet, transactionManager)
        .build();
    
    // Decide flow based on whether users are pre-loaded
    if (job.getUserRoles() != null) {
        // Skip finding users, go directly to import
        return new JobBuilder(Constants.USERS_IMPORT, jobRepositoryBatch)
            .start(userImportStep())
            .build();
    } else {
        // Run both steps: find users, then import
        return new JobBuilder(Constants.USERS_IMPORT, jobRepositoryBatch)
            .start(findUsersStep)
            .next(userImportStep())
            .build();
    }
}

Step userImportStep() {
    UserImportTasklet userImportTasklet = 
        new UserImportTasklet(transactionTemplate, userImportService);
    return new StepBuilder("importUsers", jobRepositoryBatch)
        .tasklet(userImportTasklet, transactionManager)
        .build();
}
```

**Job Parameters** (passed via JobParametersBuilder):
```
JOB_NAME: "Users import for {provider}"
JOB_AUTHOR: "SYSTEM"
USER_IMPORT_ID: {job.id}
```

### 5. UserImportScheduledTask (Inner Class)
**Location**: [src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportJobServiceImpl.java](src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportJobServiceImpl.java) (lines ~298-320)

**Extends**: ScheduledTask<UserImportJob>

**Purpose**: Wraps batch job for scheduled execution

```java
private class UserImportScheduledTask extends ScheduledTask<UserImportJob> {
    UserImportScheduledTask(UserImportJob job) {
        super(job);
    }
    
    @Override
    public void run() {
        // Build job parameters
        JobParameters jobParameters = new JobParametersBuilder()
            .addString(Constants.Params.JOB_NAME, 
                String.format("Users import for %s", 
                    getProviderName(job.getProviderType())))
            .addString(Constants.Params.JOB_AUTHOR, SYSTEM_USER)
            .addString(Constants.Params.USER_IMPORT_ID, 
                String.valueOf(job.getId()))
            .toJobParameters();
        
        // Launch Spring Batch job
        Job batchJob = buildJobForUserImportTasklet(job);
        jobTemplate.launch(batchJob, jobParameters);
    }
}
```

**Job Scheduling**:
- Jobs are scheduled using Spring's TaskScheduler
- Cron expressions or daily/weekly schedules
- Scheduled tasks are managed by BaseJobServiceImpl

---

## LDAP Provider Architecture

### 1. LdapProvider (Interface)
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/providers/LdapProvider.java](src/main/java/org/ohdsi/webapi/security/provisioning/providers/LdapProvider.java)

**Key Methods**:
```java
// Core operations
LdapTemplate getLdapTemplate();
List<LdapGroup> findGroups(String searchStr);
List<LdapUser> findUsers();

// Configuration getters
List<LdapGroup> getLdapGroups(Attributes attributes) throws NamingException;
SearchControls getUserSearchControls();
Set<String> getGroupClasses();
Set<String> getUserClass();
String getSearchUserFilter();
String getLoginAttributeName();
String getDistinguishedAttributeName();
String getDisplayNameAttributeName();

// Search handlers
CollectingNameClassPairCallbackHandler<LdapUser> getUserSearchCallbackHandler(
    AttributesMapper<LdapUser> attributesMapper);

// Credentials
String getPrincipal();
String getPassword();
```

### 2. AbstractLdapProvider (Base Implementation)
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/providers/AbstractLdapProvider.java](src/main/java/org/ohdsi/webapi/security/provisioning/providers/AbstractLdapProvider.java)

**Purpose**: Common LDAP operations for both AD and standard LDAP

**Key Methods**:
```java
// Find groups by CN with wildcard search
@Override
public List<LdapGroup> findGroups(String searchStr) {
    LdapTemplate ldapTemplate = getLdapTemplate();
    return ldapTemplate.search(
        LdapUtils.emptyLdapName(), 
        getFilterString(searchStr),  // (cn=*searchStr*)
        getAttributesMapper(LdapGroup::new));
}

// Find all users and their groups
@Override
public List<LdapUser> findUsers() {
    CollectingNameClassPairCallbackHandler<LdapUser> handler = 
        getUserSearchCallbackHandler(getUserAttributesMapper());
    return search(getUserFilter(), handler);
}

// Map LDAP attributes to LdapUser/LdapGroup
private <T extends LdapObject> AttributesMapper<T> getAttributesMapper(
    Supplier<T> supplier) {
    return attributes -> {
        String name = valueAsString(
            attributes.get(getDisplayNameAttributeName()));
        String dn = valueAsString(
            attributes.get(getDistinguishedAttributeName()));
        T object = supplier.get();
        object.setDisplayName(name);
        object.setDistinguishedName(dn);
        return object;
    };
}
```

### 3. ActiveDirectoryProvider
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/providers/ActiveDirectoryProvider.java](src/main/java/org/ohdsi/webapi/security/provisioning/providers/ActiveDirectoryProvider.java)

**Annotation**: `@Component @ConditionalOnProperty("security.auth.ad.url")`

**Configuration Properties** (from application.yaml):
```yaml
security.auth.ad:
  url: ""                                    # ldap://server:389 or ldaps://
  enabled: false
  searchBase: "CN=Users,DC=example,DC=org"
  principalSuffix: "@example.org"            # For login mapping
  system.username: ""                        # Service account
  system.password: ""                        # Service account password
  searchFilter: "(&(objectClass=person)(cn=%s))"
  searchString: "(&(objectClass=person)(userPrincipalName=%s))"
  userImport:
    loginAttr: "sAMAccountName"              # Attribute for login
    usernameAttr: "cn"                       # Attribute for display name
  userMapping:
    displaynameAttr: "displayname"
    firstnameAttr: "givenname"
    lastnameAttr: "sn"
    middlenameAttr: "initials"
  result:
    count.limit: 30000
  ignore:
    partial.result.exception: true
  referral: null
```

**AD-Specific Implementation**:
```java
@Override
public LdapTemplate getLdapTemplate() {
    LdapContextSource contextSource = new LdapContextSource();
    contextSource.setUrl(dequote(adUrl));
    contextSource.setBase(dequote(adSearchBase));
    contextSource.setUserDn(dequote(adSystemUsername));
    contextSource.setPassword(dequote(adSystemPassword));
    contextSource.setReferral(dequote(referral));
    contextSource.setAuthenticationStrategy(new SimpleDirContextAuthenticationStrategy());
    contextSource.setAuthenticationSource(new AuthenticationSource() {
        @Override
        public String getPrincipal() { return ActiveDirectoryProvider.this.getPrincipal(); }
        @Override
        public String getCredentials() { return ActiveDirectoryProvider.this.getPassword(); }
    });
    LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
    ldapTemplate.setIgnorePartialResultException(adIgnorePartialResultException);
    return ldapTemplate;
}

@Override
public List<LdapGroup> getLdapGroups(Attributes attributes) throws NamingException {
    return valueAsList(attributes.get("memberOf"))
        .stream()
        .map(v -> new LdapGroup("", v))
        .collect(Collectors.toList());
}

private static final Set<String> GROUP_CLASSES = ImmutableSet.of("group");
private static final Set<String> USER_CLASSES = ImmutableSet.of("user");
private static final int PAGE_SIZE = 500;  // For paged results

@Override
public SearchControls getUserSearchControls() {
    SearchControls controls = new SearchControls();
    controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    controls.setCountLimit(countLimit);
    controls.setReturningAttributes(userAttributes);
    return controls;
}
```

### 4. DefaultLdapProvider
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/providers/DefaultLdapProvider.java](src/main/java/org/ohdsi/webapi/security/provisioning/providers/DefaultLdapProvider.java)

**Annotation**: `@Component @ConditionalOnProperty(name = "security.auth.ldap.enabled", havingValue = "true")`

**Configuration Properties**:
```yaml
security.auth.ldap:
  enabled: false
  url: "ldap://localhost:389"
  baseDn: ""
  system.username: ""                        # Service account DN
  system.password: ""
  userImport:
    loginAttr: "uid"                         # Attribute for login
    usernameAttr: "cn"                       # Attribute for display name
  userMapping:
    displaynameAttr: "displayName"
    firstnameAttr: "givenName"
    lastnameAttr: "sn"
```

**LDAP-Specific Implementation**:
```java
private static final Set<String> GROUP_CLASSES = ImmutableSet.of(
    "groupOfUniqueNames", "groupOfNames", "posixGroup");
private static final Set<String> USER_CLASSES = ImmutableSet.of("account", "person");

@PostConstruct
private void init() {
    // Build user attributes array from config + standard attrs
    List<String> attrs = Arrays.stream(USER_ATTRIBUTES)
        .collect(Collectors.toList());
    attrs.add(usernameAttr);
    attrs.add(loginAttr);
    userAttributes = attrs.stream()
        .distinct()
        .toArray(String[]::new);
}

@Override
public LdapTemplate getLdapTemplate() {
    LdapContextSource contextSource = new LdapContextSource();
    contextSource.setUrl(ldapUrl);
    contextSource.setBase(baseDn);
    contextSource.setUserDn(systemUsername);
    contextSource.setPassword(systemPassword);
    contextSource.setReferral(referral);
    contextSource.setCacheEnvironmentProperties(false);
    // ... AuthenticationSource setup
    LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
    ldapTemplate.setIgnorePartialResultException(ldapIgnorePartialResultException);
    return ldapTemplate;
}

@Override
public List<LdapGroup> getLdapGroups(Attributes attributes) throws NamingException {
    // For standard LDAP, extract from group membership attributes
    return valueAsList(attributes.get("memberOf"))
        .stream()
        .map(v -> new LdapGroup("", v))
        .collect(Collectors.toList());
}
```

### 5. OhdsiLdapUtils (Utility Class)
**File**: [src/main/java/org/ohdsi/webapi/security/provisioning/providers/OhdsiLdapUtils.java](src/main/java/org/ohdsi/webapi/security/provisioning/providers/OhdsiLdapUtils.java)

**Utility Methods**:
```java
// Convert LDAP Attribute to string
public static String valueAsString(Attribute attribute) throws NamingException {
    return Objects.nonNull(attribute) ? attribute.get().toString() : "";
}

// Convert LDAP Attribute to list of strings
public static List<String> valueAsList(Attribute attribute) throws NamingException {
    List<String> result = new ArrayList<>();
    if (Objects.nonNull(attribute)) {
        for (int i = 0; i < attribute.size(); i++) {
            result.add(attribute.get(i).toString());
        }
    }
    return result;
}

// Build OR filter from multiple attribute values
public static OrFilter getCriteria(String attribute, Set<String> values) {
    OrFilter filter = new OrFilter();
    values.forEach(v -> filter.or(new EqualsFilter(attribute, v)));
    return filter;
}
```

---

## Configuration

### 1. Application YAML Properties

**File**: `src/main/resources/application.yaml`

**LDAP Configuration Block**:
```yaml
security:
  auth:
    # Active Directory Settings
    ad:
      enabled: false
      url: ""
      searchBase: "CN=Users,DC=example,DC=org"
      principalSuffix: "@example.org"
      system:
        username: ""
        password: ""
      searchFilter: "(&(objectClass=person)(cn=%s))"
      searchString: "(&(objectClass=person)(userPrincipalName=%s))"
      userImport:
        loginAttr: "sAMAccountName"
        usernameAttr: "cn"
      default:
        import:
          group: "public"  # Default role for imported users
      result:
        count:
          limit: 30000
      ignore:
        partial:
          result:
            exception: true
      referral: null
    
    # LDAP Settings
    ldap:
      enabled: false
      url: "ldap://localhost:389"
      baseDn: ""
      dn: "cn={0},dc=example,dc=org"
      searchBase: "CN=Users,DC=example,DC=org"
      searchString: "(&(objectClass=person)(CN={0}))"
      system:
        username: ""
        password: ""
      userImport:
        loginAttr: "uid"
        usernameAttr: "cn"
      userMapping:
        displaynameAttr: "displayName"
        firstnameAttr: "givenName"
        lastnameAttr: "sn"
```

### 2. Provider Dependency Injection

**Configuration Pattern** (in UserImportServiceImpl):
```java
@Component  // ActiveDirectoryProvider
@ConditionalOnProperty("security.auth.ad.url")
public class ActiveDirectoryProvider extends AbstractLdapProvider { }

@Component  // DefaultLdapProvider
@ConditionalOnProperty(name = "security.auth.ldap.enabled", 
                       havingValue = "true", 
                       matchIfMissing = false)
public class DefaultLdapProvider extends AbstractLdapProvider { }
```

Both are injected as `@Autowired(required = false)` to prevent failures when not configured.

### 3. Spring Batch Configuration

**Dependencies Injected into UserImportJobServiceImpl**:
```java
private final JobRepository jobRepositoryBatch;  // Spring Batch repository
private final PlatformTransactionManager transactionManager;
private final JobTemplate jobTemplate;  // Custom template for launching jobs
private final TransactionTemplate transactionTemplate;
```

**Job Execution Context Constants** (from Constants class):
```java
Constants.Params.USER_IMPORT_ID     // Long
Constants.Params.USER_ROLES         // Serialized list
Constants.Params.JOB_NAME           // String
Constants.Params.JOB_AUTHOR         // String (SYSTEM_USER)
Constants.USERS_IMPORT              // Job name: "userImport"
```

---

## Database Schema

### 1. user_import_job Table
**File**: `src/main/resources/db/migration/postgresql/B3.0.0__webapi_baseline.sql`

```sql
CREATE SEQUENCE ${ohdsiSchema}.user_import_job_seq START WITH 1;

CREATE TABLE ${ohdsiSchema}.user_import_job (
    id bigint DEFAULT nextval('${ohdsiSchema}.user_import_job_seq'::regclass) NOT NULL,
    is_enabled boolean DEFAULT false NOT NULL,
    start_date timestamp with time zone,
    frequency character varying NOT NULL,  -- JobExecutingType: ONCE, DAILY, WEEKLY
    recurring_times integer NOT NULL,
    recurring_until_date timestamp with time zone,
    cron character varying NOT NULL,       -- Cron expression for scheduling
    last_executed_at timestamp with time zone,
    executed_times integer DEFAULT 0 NOT NULL,
    is_closed boolean DEFAULT false NOT NULL,
    provider_type character varying NOT NULL,  -- LDAP or ACTIVE_DIRECTORY
    preserve_roles boolean DEFAULT true NOT NULL,  -- Whether to preserve existing roles
    user_roles character varying,           -- JSON serialized user list
    
    CONSTRAINT pk_user_import_job PRIMARY KEY (id)
);
```

### 2. user_import_job_weekdays Table
**Purpose**: Many-to-many for weekly job scheduling

```sql
CREATE TABLE ${ohdsiSchema}.user_import_job_weekdays (
    user_import_job_id bigint NOT NULL,
    day_of_week character varying NOT NULL,  -- e.g., MONDAY, TUESDAY, etc.
    
    CONSTRAINT pk_user_import_job_weekdays PRIMARY KEY (user_import_job_id, day_of_week),
    CONSTRAINT fk_user_import_job_weekdays FOREIGN KEY (user_import_job_id)
        REFERENCES ${ohdsiSchema}.user_import_job(id) ON DELETE CASCADE
);
```

### 3. sec_group_role_import Table
**File**: `src/main/resources/db/migration/postgresql/B3.0.0__webapi_baseline.sql`

**Migration History**: 
- **Original (v2.6)**: Created as `sec_role_group`
- **v2.99.0006**: Renamed to `sec_group_role_import` for clarity

```sql
CREATE SEQUENCE ${ohdsiSchema}.sec_group_role_import_seq START WITH 1;

CREATE TABLE ${ohdsiSchema}.sec_group_role_import (
    id integer DEFAULT nextval('${ohdsiSchema}.sec_group_role_import_seq'::regclass) NOT NULL,
    provider character varying NOT NULL,  -- LDAP or ACTIVE_DIRECTORY
    group_dn character varying NOT NULL,  -- Distinguished Name of LDAP group
    group_name character varying,         -- Display name
    role_id integer NOT NULL,             -- Reference to sec_role.id
    job_id bigint,                        -- Optional: Reference to user_import_job
    
    CONSTRAINT sec_group_role_import_pkey PRIMARY KEY (id),
    CONSTRAINT fk_group_role_import_role FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_group_role_import_job FOREIGN KEY (job_id)
        REFERENCES ${ohdsiSchema}.user_import_job(id) ON DELETE CASCADE ON UPDATE NO ACTION
);
```

### 4. user_import_job_history View
**File**: `src/main/resources/db/migration/postgresql/V2.6.0.20181010185037__schema-user-import-scheduler-history.sql`

```sql
CREATE OR REPLACE VIEW ${ohdsiSchema}.user_import_job_history AS
    SELECT 
        je.id,
        je.start_time,
        je.end_time,
        je.status,
        je.exit_code,
        je.exit_message,
        je.job_instance_id,
        ji.job_name,
        jpm.string_val AS user_import_id,
        je.version,
        (SELECT login FROM sec_user WHERE id = je.create_by) AS author
    FROM batch_job_execution je
    INNER JOIN batch_job_instance ji ON je.job_instance_id = ji.id
    INNER JOIN batch_job_execution_params jpm ON je.id = jpm.job_execution_id
    WHERE jpm.key_name = 'USER_IMPORT_ID'
    ORDER BY je.id DESC;
```

**Related Tables**:
- `batch_job_instance`: Spring Batch metadata
- `batch_job_execution`: Execution details
- `batch_job_execution_params`: Job parameters
- `sec_user`: User who triggered job

---

## Integration Points

### 1. Authorization Service Integration

**File**: `org.ohdsi.webapi.security.authz.AuthorizationService`

**Methods Used**:
```java
// Used in UserImportServiceImpl.importUsers()
List<Role> getUserRoles(Long userId);           // Get current roles
void addUserToRole(String roleName, 
                   String userLogin, 
                   UserOrigin userOrigin);      // Assign role
void removeUserFromRole(String roleName, 
                        String userLogin, 
                        String origin);          // Remove role
void registerUser(String login, 
                  String displayName, 
                  UserOrigin userOrigin, 
                  Set<String> roles);            // Create new user
```

### 2. User Entity & Repository

**File**: `org.ohdsi.webapi.security.authz.UserEntity` and `UserRepository`

**Repository Methods**:
```java
Optional<UserEntity> findByLogin(String login);
List<UserEntity> findByOrigin(UserOrigin origin);  // Find users from specific provider
```

**Fields**:
```java
private String login;
private String name;               // Display name
private UserOrigin origin;         // LDAP, AD, etc.
private List<Role> userRoles;
```

### 3. Role & RoleEntity

**File**: `org.ohdsi.webapi.security.authz.Role` (DTO) and `RoleEntity` (JPA)

**Role DTO** (used in AtlasUserRoles):
```java
private Long id;
private String name;
private Boolean systemRole;

public static Role fromEntity(RoleEntity entity) { }
```

### 4. UserOrigin Enum

**Purpose**: Identifies user source for provisioning tracking

**Values**:
- LDAP
- ACTIVE_DIRECTORY
- LOCAL
- WINDOWS

**Factory Method**:
```java
public static UserOrigin getFrom(LdapProviderType providerType) {
    if (LdapProviderType.ACTIVE_DIRECTORY == providerType) {
        return ACTIVE_DIRECTORY;
    }
    return LDAP;
}
```

### 5. Converters

#### UserImportJobToUserImportJobDTOConverter
**File**: `org.ohdsi.webapi.security.provisioning.converter.UserImportJobToUserImportJobDTOConverter`

**Conversion**: `UserImportJob` → `UserImportJobDTO`

#### UserImportJobDTOToUserImportJobConverter
**File**: `org.ohdsi.webapi.security.provisioning.converter.UserImportJobDTOToUserImportJobConverter`

**Conversion**: `UserImportJobDTO` → `UserImportJob`

#### RoleGroupMappingConverter
**File**: `org.ohdsi.webapi.security.provisioning.converter.RoleGroupMappingConverter`

**Key Methods**:
```java
// GroupRoleImportEntity list → RoleGroupMapping DTO
public static RoleGroupMapping convertRoleGroupMapping(
    String provider, 
    List<GroupRoleImportEntity> mappingEntities) { }

// RoleGroupMapping DTO → GroupRoleImportEntity list
public static List<GroupRoleImportEntity> convertRoleGroupMapping(
    RoleGroupMapping mapping) { }
```

### 6. Job Template Integration

**File**: `org.ohdsi.webapi.job.JobTemplate`

**Usage**:
```java
// In UserImportScheduledTask.run()
jobTemplate.launch(batchJob, jobParameters);
```

**Responsibilities**:
- Launches Spring Batch jobs
- Manages job execution context
- Persists execution results

---

## Utility Classes

### GroupRoleImportUtils
**File**: `org.ohdsi.webapi.security.provisioning.GroupRoleImportUtils`

**Methods**:
```java
// Compare two mappings for equality
static boolean equalsRoleGroupMapping(GroupRoleImportEntity a, 
                                      GroupRoleImportEntity b)

// Find new mappings added in update
static List<GroupRoleImportEntity> findCreated(
    List<GroupRoleImportEntity> source, 
    List<GroupRoleImportEntity> target)

// Find mappings removed in update
static List<GroupRoleImportEntity> findDeleted(
    List<GroupRoleImportEntity> source, 
    List<GroupRoleImportEntity> target)
```

**Usage**: In `UserImportJobServiceImpl.updateAdditionalFields()` to track changes

---

## Common Workflows

### Workflow 1: Setup User Import Job

1. **Admin calls** `POST /user/import/job` with UserImportJobDTO:
   - providerType: ACTIVE_DIRECTORY or LDAP
   - frequency: ONCE, DAILY, or WEEKLY
   - cron: Optional cron expression
   - enabled: true/false
   - startDate: When to start job

2. **UserImportJobServiceImpl.createJobEndpoint()**:
   - Converts DTO to entity
   - Persists role-group mappings
   - Schedules task via TaskScheduler
   - Returns updated job with calculated nextExecution

3. **Job scheduled** via `ScheduledTask<UserImportJob>`

### Workflow 2: Save Role-Group Mapping

1. **Admin calls** `POST /user/import/{type}/mapping` with RoleGroupMapping:
   ```json
   {
     "provider": "ad",
     "roleGroups": [
       {
         "role": {"id": 1, "name": "admin"},
         "groups": [
           {"displayName": "AD Admins", "distinguishedName": "CN=Admins,DC=example,DC=org"}
         ]
       }
     ]
   }
   ```

2. **UserImportServiceImpl.saveMappingEndpoint()**:
   - Converts RoleGroupMapping to GroupRoleImportEntity list
   - Persists to `sec_group_role_import` table

### Workflow 3: Find Users to Import

1. **Admin calls** `POST /user/import/{type}` with RoleGroupMapping

2. **UserImportServiceImpl.findDirectoryUsers()**:
   - Gets provider by type
   - Calls `provider.findUsers()` to query LDAP
   - Maps found users to roles via RoleGroupMapping
   - Filters out existing users (status != EXISTS)
   - Returns list of AtlasUserRoles with status

### Workflow 4: Execute Import Job

1. **Job execution triggered** (scheduled or manual via `/user/import`)

2. **UserImportScheduledTask.run()**:
   - Builds JobParameters with USER_IMPORT_ID
   - Calls `jobTemplate.launch(batchJob, jobParameters)`

3. **Step 1 - FindUsersToImportTasklet**:
   - Loads UserImportJob from DB
   - Queries LDAP for users
   - Serializes users to job execution context

4. **Step 2 - UserImportTasklet**:
   - Deserializes users from execution context
   - For each user:
     - Create new UserEntity if not exists
     - Update existing user with new display name
     - Assign/remove roles based on preserveRoles flag
   - Returns UserImportResult (created count, updated count)

5. **Job completion**:
   - Results written to UserImportJobHistoryItem
   - lastExecutedAt updated in UserImportJob
   - NextExecution recalculated

---

## Error Handling & Edge Cases

### 1. Provider Not Available
- Throws `IllegalArgumentException` if provider not configured
- Check: Provider bean must be enabled in configuration

### 2. Role Mapping Missing
- Import proceeds but users get no roles except default
- Users still created/updated

### 3. Preserve Roles Flag
- **true**: Only adds new roles, doesn't remove old ones
- **false**: Replaces all roles with mapped roles

### 4. User Already Exists
- Status = EXISTS, filtered out of import list
- Not returned in findUsers API

### 5. Job Duplication
- Throws `JobAlreadyExistException` if job for same provider already active
- Check: Only one job per provider can be active

---

## Performance Considerations

1. **LDAP Search Optimization**:
   - Use `SearchControls` with appropriate scope
   - AD uses paged results (PAGE_SIZE = 500)
   - Result count limited by `result.count.limit`

2. **Entity Graph**:
   - `user_import_job` uses `@NamedEntityGraph("jobWithMapping")`
   - Eager loads role-group mappings in one query

3. **Transaction Management**:
   - Each tasklet wrapped in transaction via `TransactionalTasklet`
   - TransactionTemplate used for synchronization

4. **Serialization**:
   - User list serialized to job execution context (not in DB)
   - Reduces memory footprint between steps

---

## Migration History

| Version | File | Change |
|---------|------|--------|
| v2.6.0 | schema-user-import-scheduler.sql | Initial create of user_import_job table |
| v2.6.0 | schema-user-import-scheduler-history.sql | Create user_import_job_history view |
| v2.6.0 | role-group-mapping.sql | Create sec_role_group table |
| v2.8.0 | alter_job-execution-params_string-val.sql | Add user_roles VARCHAR column |
| v2.99.0006 | role_group_import.sql | Rename sec_role_group → sec_group_role_import |
| v3.0.0 | webapi_baseline.sql | Consolidated all tables into baseline |

---

## Related Documentation

- **Authentication Pipeline**: See [articles/LoginPipeline.md](articles/LoginPipeline.md)
- **Entity Access Control**: See [articles/EntityAccess_HOWTO.md](articles/EntityAccess_HOWTO.md)
- **Database Performance**: See [articles/DBPerf_Diagnosis_HOWTO.md](articles/DBPerf_Diagnosis_HOWTO.md)
- **Role/Group Import Details**: See [articles/Group_Role_Import_HOWTO.md](articles/Group_Role_Import_HOWTO.md) (currently empty - needs documentation)

---

## Quick Reference - File Locations

| Component | File Path |
|-----------|-----------|
| Main Service | `src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportService.java` |
| Service Implementation | `src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportServiceImpl.java` |
| Job Service | `src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportJobService.java` |
| Job Service Implementation | `src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportJobServiceImpl.java` |
| Find Users Tasklet | `src/main/java/org/ohdsi/webapi/security/provisioning/service/FindUsersToImportTasklet.java` |
| Import Users Tasklet | `src/main/java/org/ohdsi/webapi/security/provisioning/service/UserImportTasklet.java` |
| Base Tasklet | `src/main/java/org/ohdsi/webapi/security/provisioning/service/BaseUserImportTasklet.java` |
| LDAP Provider Interface | `src/main/java/org/ohdsi/webapi/security/provisioning/providers/LdapProvider.java` |
| Abstract LDAP Provider | `src/main/java/org/ohdsi/webapi/security/provisioning/providers/AbstractLdapProvider.java` |
| Active Directory Provider | `src/main/java/org/ohdsi/webapi/security/provisioning/providers/ActiveDirectoryProvider.java` |
| Default LDAP Provider | `src/main/java/org/ohdsi/webapi/security/provisioning/providers/DefaultLdapProvider.java` |
| UserImportJob Entity | `src/main/java/org/ohdsi/webapi/security/provisioning/model/UserImportJob.java` |
| GroupRoleImportEntity | `src/main/java/org/ohdsi/webapi/security/provisioning/model/GroupRoleImportEntity.java` |
| UserImportJobDTO | `src/main/java/org/ohdsi/webapi/security/provisioning/model/UserImportJobDTO.java` |
| Utility Classes | `src/main/java/org/ohdsi/webapi/security/provisioning/` |
| Database Migrations | `src/main/resources/db/migration/postgresql/` |
| Application Config | `src/main/resources/application.yaml` |

---

## Version Info
- **WebAPI Version**: 3.x
- **Java**: 11+
- **Spring Boot**: 2.x / 3.x
- **Spring Batch**: 4.x / 5.x
- **Database**: PostgreSQL (primary), others supported
- **LDAP Libraries**: spring-ldap, spring-security-ldap

---

*End of Document*
