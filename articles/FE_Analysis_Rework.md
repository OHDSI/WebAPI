# Feature Analysis Entity Refactoring Analysis

## Executive Summary

The `FeAnalysisEntity` hierarchy in WebAPI uses multiple competing abstraction mechanisms (inheritance, generics, interfaces, discriminators) to handle different types of feature analyses. This creates confusion and violates the type contract defined in StandardizedAnalysisAPI. This document analyzes the current implementation and proposes refactoring strategies to simplify the design.

---

## Current Implementation Analysis

### Entity Hierarchy Overview

```
FeAnalysisEntity<T> (abstract)
│   Table: fe_analysis
│   Strategy: SINGLE_TABLE inheritance
│   Generic: <T> (unconstrained - PROBLEM!)
│   Implements: FeatureAnalysis<T, Integer>
│
├── FeAnalysisWithStringEntity
│   │   Discriminator: "not null" (catch-all)
│   │   Generic: extends FeAnalysisEntity<String>
│   │   Handles: PRESET, CUSTOM_FE
│   │   Storage: inline 'design' column (String)
│   │
│   └── Design Type: String (violates API contract!)
│
└── FeAnalysisWithCriteriaEntity<T extends FeAnalysisCriteriaEntity> (abstract)
    │   Generic: <T extends FeAnalysisCriteriaEntity>
    │   Storage: @OneToMany relationships
    │
    ├── FeAnalysisWithPrevalenceCriteriaEntity
    │   │   Discriminator: "CRITERIA_SET_PREVALENCE"
    │   │   Generic: extends FeAnalysisWithCriteriaEntity<FeAnalysisCriteriaGroupEntity>
    │   │   Storage: → fe_analysis_criteria table
    │   │
    │   └── Design Type: List<FeAnalysisCriteriaGroupEntity>
    │
    └── FeAnalysisWithDistributionCriteriaEntity
        │   Discriminator: "CRITERIA_SET_DISTRIBUTION"
        │   Generic: extends FeAnalysisWithCriteriaEntity<FeAnalysisDistributionCriteriaEntity>
        │   Storage: → fe_analysis_dist_criteria table
        │
        └── Design Type: List<FeAnalysisDistributionCriteriaEntity>
```

### Discriminator Formula Logic

```sql
CASE WHEN type = 'CRITERIA_SET' 
     THEN CONCAT(CONCAT(type,'_'),stat_type) 
     ELSE type 
END
```

**Produces:**
- `'PRESET'` → FeAnalysisWithStringEntity
- `'CUSTOM_FE'` → FeAnalysisWithStringEntity
- `'CRITERIA_SET_PREVALENCE'` → FeAnalysisWithPrevalenceCriteriaEntity
- `'CRITERIA_SET_DISTRIBUTION'` → FeAnalysisWithDistributionCriteriaEntity

---

## Problems Identified

### 1. Type Contract Violation

**StandardizedAnalysisAPI defines:**
```java
public interface FeatureAnalysis<T extends FeatureAnalysisDesign, ID> {
    T getDesign();
}
```

**WebAPI incorrectly implements:**
```java
public abstract class FeAnalysisEntity<T> // T is unconstrained!
    implements FeatureAnalysis<T, Integer>

public class FeAnalysisWithStringEntity extends FeAnalysisEntity<String>
    // String does NOT extend FeatureAnalysisDesign - VIOLATION!
```

### 2. Competing Abstraction Mechanisms

Four different polymorphism strategies working at cross-purposes:

1. **Single Table Inheritance** - Database-level polymorphism via discriminator
2. **Generic Type Parameters** - Compile-time type abstraction
3. **Interface Implementation** - Contract-based polymorphism
4. **Multiple Design Storage Strategies** - Inline String vs relational tables

### 3. Inconsistent Field Usage

```java
// Base class: FeAnalysisEntity
@Column(name = "design", insertable = false, updatable = false)
private String rawDesign;  // READ-ONLY, shadowed by subclasses

// FeAnalysisWithStringEntity
@Lob
private String design;  // Maps to SAME column, writable

// FeAnalysisWithCriteriaEntity
@OneToMany
private List<T> design;  // DIFFERENT storage - foreign key relationship
```

The `rawDesign` field is a phantom - declared but never properly used.

### 4. Vague Discriminator Value

```java
@DiscriminatorValue("not null")  // Means: "everything else"
```

This is a workaround for JPA's limitation of one discriminator value per class. It obscures that this entity handles both `PRESET` and `CUSTOM_FE` types.

### 5. Three Different Design Storage Patterns

| Entity Type | Storage Strategy | Table(s) |
|------------|------------------|----------|
| String-based (PRESET/CUSTOM_FE) | Inline JSON column | `fe_analysis.design` |
| Prevalence Criteria | @OneToMany relationship | `fe_analysis` + `fe_analysis_criteria` |
| Distribution Criteria | @OneToMany relationship | `fe_analysis` + `fe_analysis_dist_criteria` |

---

## StandardizedAnalysisAPI Contract

### Required Design Types

```java
// Base marker interface
public interface FeatureAnalysisDesign {}

// Concrete implementations
public class PresetFeatureAnalysisDesign implements FeatureAnalysisDesign {}

public class CriteriaFeatureDesign implements FeatureAnalysisDesign {
    private List<? extends FeAnalysisCriteria> criteriaList;
}
```

### Interface Contract

```java
public interface FeatureAnalysis<T extends FeatureAnalysisDesign, ID> 
    extends CommonEntity<ID> {
    
    StandardFeatureAnalysisType getType();
    String getName();
    T getDesign();
    StandardFeatureAnalysisDomain getDomain();
    String getDescr();
}
```

**Key requirement:** `T` must extend `FeatureAnalysisDesign`, not be a raw `String` or `List`.

---

## Refactoring Options

### Option 1: Remove Generics, Push Interface to Subclasses (Recommended)

**Rationale:**
- Eliminates type contract violation
- Maintains single table inheritance (minimal database changes)
- Each subclass implements interface with correct design type
- Clear separation: base entity handles persistence, subclasses handle domain contracts

**Implementation:**

```java
// Base entity - NO generics, NO interface
@Entity
@Table(name = "fe_analysis")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
    "CASE WHEN type = 'CRITERIA_SET' THEN CONCAT(CONCAT(type,'_'),stat_type) " +
    "ELSE type END"
)
public abstract class FeAnalysisEntity extends CommonEntity<Integer> 
    implements Comparable<FeAnalysisEntity> {
    
    @Id
    @GeneratedValue(generator = "fe_analysis_generator")
    private Integer id;
    
    @Column
    @Enumerated(EnumType.STRING)
    private StandardFeatureAnalysisType type;
    
    @Column
    private String name;
    
    @Column
    @Enumerated(EnumType.STRING)
    private StandardFeatureAnalysisDomain domain;
    
    @Column
    private String descr;
    
    @Column(name = "is_locked")
    private Boolean isLocked;
    
    @Column(name = "stat_type")
    @Enumerated(value = EnumType.STRING)
    private CcResultType statType;
    
    // Protected raw design storage for subclasses
    @Lob
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "design")
    protected String rawDesign;
    
    // Getters for common fields
    public Integer getId() { return id; }
    public StandardFeatureAnalysisType getType() { return type; }
    public String getName() { return name; }
    public StandardFeatureAnalysisDomain getDomain() { return domain; }
    public String getDescr() { return descr; }
    
    protected String getRawDesign() { return rawDesign; }
    protected void setRawDesign(String rawDesign) { this.rawDesign = rawDesign; }
}
```

```java
// String-based designs (PRESET, CUSTOM_FE)
@Entity
@DiscriminatorValue("not null")
public class FeAnalysisWithStringEntity extends FeAnalysisEntity 
    implements FeatureAnalysis<PresetFeatureAnalysisDesign, Integer> {
    
    private transient PresetFeatureAnalysisDesign design;
    
    @Override
    public PresetFeatureAnalysisDesign getDesign() {
        if (design == null && getRawDesign() != null) {
            // Deserialize JSON string to PresetFeatureAnalysisDesign
            design = JsonUtil.fromJson(getRawDesign(), PresetFeatureAnalysisDesign.class);
        }
        return design;
    }
    
    public void setDesign(PresetFeatureAnalysisDesign design) {
        this.design = design;
        setRawDesign(JsonUtil.toJson(design));
    }
    
    // Delegate interface methods to base entity
    // (Already inherited: getId, getType, getName, getDomain, getDescr)
}
```

```java
// Criteria-based designs - abstract middle layer
@Entity
public abstract class FeAnalysisWithCriteriaEntity extends FeAnalysisEntity 
    implements FeatureAnalysis<CriteriaFeatureDesign, Integer> {
    
    private transient CriteriaFeatureDesign design;
    
    // Subclasses provide concrete criteria lists
    protected abstract List<? extends FeAnalysisCriteria> getCriteriaList();
    
    @Override
    public CriteriaFeatureDesign getDesign() {
        if (design == null) {
            design = new CriteriaFeatureDesign();
            design.setCriteriaList(getCriteriaList());
        }
        return design;
    }
}
```

```java
// Prevalence criteria
@Entity
@DiscriminatorValue("CRITERIA_SET_PREVALENCE")
public class FeAnalysisWithPrevalenceCriteriaEntity 
    extends FeAnalysisWithCriteriaEntity {
    
    @OneToMany(mappedBy = "featureAnalysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeAnalysisCriteriaGroupEntity> criteriaList = new ArrayList<>();
    
    @Override
    protected List<? extends FeAnalysisCriteria> getCriteriaList() {
        return criteriaList;
    }
    
    public List<FeAnalysisCriteriaGroupEntity> getCriteria() {
        return criteriaList;
    }
    
    public void setCriteria(List<FeAnalysisCriteriaGroupEntity> criteria) {
        this.criteriaList = criteria;
    }
}
```

```java
// Distribution criteria
@Entity
@DiscriminatorValue("CRITERIA_SET_DISTRIBUTION")
public class FeAnalysisWithDistributionCriteriaEntity 
    extends FeAnalysisWithCriteriaEntity {
    
    @OneToMany(mappedBy = "featureAnalysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeAnalysisDistributionCriteriaEntity> criteriaList = new ArrayList<>();
    
    @Override
    protected List<? extends FeAnalysisCriteria> getCriteriaList() {
        return criteriaList;
    }
    
    public List<FeAnalysisDistributionCriteriaEntity> getCriteria() {
        return criteriaList;
    }
    
    public void setCriteria(List<FeAnalysisDistributionCriteriaEntity> criteria) {
        this.criteriaList = criteria;
    }
}
```

**Benefits:**
- ✅ Complies with StandardizedAnalysisAPI contract
- ✅ Eliminates raw type warnings
- ✅ No database schema changes required
- ✅ Each subclass has clear, typed API
- ✅ Separates persistence concerns from domain contracts

**Drawbacks:**
- ⚠️ Interface implementation duplicated across subclasses (mitigated by inheritance)
- ⚠️ Still uses "not null" discriminator (JPA limitation)

---

### Option 2: Unified Design Storage with Composition

**Rationale:**
- Eliminate inheritance complexity
- Single consistent storage strategy
- Use composition over inheritance

**Implementation:**

```java
@Entity
@Table(name = "fe_analysis")
public class FeAnalysisEntity extends CommonEntity<Integer> {
    
    @Id
    @GeneratedValue(generator = "fe_analysis_generator")
    private Integer id;
    
    @Column
    @Enumerated(EnumType.STRING)
    private StandardFeatureAnalysisType type;
    
    @Column(name = "stat_type")
    @Enumerated(value = EnumType.STRING)
    private CcResultType statType;
    
    // String-based design storage
    @Lob
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "design")
    private String designJson;
    
    // Criteria-based design storage
    @OneToMany(mappedBy = "featureAnalysis", cascade = CascadeType.ALL)
    private List<FeAnalysisCriteriaEntity> criteriaList = new ArrayList<>();
    
    // Factory method returns appropriate FeatureAnalysis implementation
    public FeatureAnalysis<?, Integer> toFeatureAnalysis() {
        switch (type) {
            case PRESET:
            case CUSTOM_FE:
                return new StringBasedFeatureAnalysis(this);
            case CRITERIA_SET:
                if (statType == CcResultType.PREVALENCE) {
                    return new PrevalenceFeatureAnalysis(this);
                } else {
                    return new DistributionFeatureAnalysis(this);
                }
            default:
                throw new IllegalStateException("Unknown type: " + type);
        }
    }
}

// Domain model implementations (not entities, just wrappers)
public class StringBasedFeatureAnalysis 
    implements FeatureAnalysis<PresetFeatureAnalysisDesign, Integer> {
    
    private final FeAnalysisEntity entity;
    private PresetFeatureAnalysisDesign design;
    
    public StringBasedFeatureAnalysis(FeAnalysisEntity entity) {
        this.entity = entity;
    }
    
    @Override
    public PresetFeatureAnalysisDesign getDesign() {
        if (design == null) {
            design = JsonUtil.fromJson(entity.getDesignJson(), 
                                     PresetFeatureAnalysisDesign.class);
        }
        return design;
    }
    
    @Override
    public Integer getId() { return entity.getId(); }
    // ... delegate other methods
}
```

**Benefits:**
- ✅ Single entity class - massive simplification
- ✅ No inheritance complexity
- ✅ Clear separation: entity = persistence, wrappers = domain
- ✅ No discriminator issues

**Drawbacks:**
- ⚠️ Requires significant refactoring
- ⚠️ Database changes needed (remove discriminator column)
- ⚠️ All code using entity hierarchy must be updated
- ⚠️ Loses JPA polymorphic query capabilities

---

### Option 3: Joined Table Inheritance with Explicit Subclasses

**Rationale:**
- Clearer table structure
- Each analysis type gets its own table
- More explicit discriminators

**Implementation:**

```java
@Entity
@Table(name = "fe_analysis")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "analysis_category", discriminatorType = DiscriminatorType.STRING)
public abstract class FeAnalysisEntity extends CommonEntity<Integer> {
    // Common fields only
}

@Entity
@Table(name = "fe_analysis_preset")
@DiscriminatorValue("PRESET")
public class PresetFeAnalysisEntity extends FeAnalysisEntity 
    implements FeatureAnalysis<PresetFeatureAnalysisDesign, Integer> {
    
    @Lob
    @Column(name = "design")
    private String designJson;
    
    // Typed API
}

@Entity
@Table(name = "fe_analysis_custom")
@DiscriminatorValue("CUSTOM_FE") 
public class CustomFeAnalysisEntity extends FeAnalysisEntity 
    implements FeatureAnalysis<PresetFeatureAnalysisDesign, Integer> {
    
    @Lob
    @Column(name = "design")
    private String designJson;
    
    // Typed API
}

@Entity
@Table(name = "fe_analysis_prevalence")
@DiscriminatorValue("PREVALENCE")
public class PrevalenceFeAnalysisEntity extends FeAnalysisEntity 
    implements FeatureAnalysis<CriteriaFeatureDesign, Integer> {
    
    @OneToMany(mappedBy = "featureAnalysis")
    private List<FeAnalysisCriteriaGroupEntity> criteriaList;
    
    // Typed API
}

@Entity
@Table(name = "fe_analysis_distribution")
@DiscriminatorValue("DISTRIBUTION")
public class DistributionFeAnalysisEntity extends FeAnalysisEntity 
    implements FeatureAnalysis<CriteriaFeatureDesign, Integer> {
    
    @OneToMany(mappedBy = "featureAnalysis")
    private List<FeAnalysisDistributionCriteriaEntity> criteriaList;
    
    // Typed API
}
```

**Benefits:**
- ✅ Explicit discriminator values (no "not null")
- ✅ Clearer table structure
- ✅ Each type fully independent
- ✅ Complies with API contract

**Drawbacks:**
- ⚠️ Requires database migration (schema changes)
- ⚠️ More tables to manage
- ⚠️ Potential performance impact (joins)
- ⚠️ PRESET and CUSTOM_FE are structurally identical (duplication)

---

## Recommendation

**Implement Option 1: Remove Generics, Push Interface to Subclasses**

### Why This Option?

1. **Minimal Breaking Changes**
   - No database schema changes required
   - Maintains single table structure
   - Preserves discriminator logic

2. **Fixes Type Safety Issues**
   - Complies with StandardizedAnalysisAPI contract
   - Eliminates raw type warnings
   - Each subclass has proper typed API

3. **Improves Clarity**
   - Removes competing generic abstraction
   - Clear separation: base = persistence, subclasses = domain contracts
   - Easier to reason about

4. **Practical Implementation Path**
   - Can be done incrementally
   - Update entity classes first
   - Update services/converters second
   - Update tests last

### Migration Steps

#### Phase 1: Update Entity Classes

1. **Remove generic from FeAnalysisEntity**
   - Remove `<T>` parameter
   - Remove `FeatureAnalysis` interface implementation
   - Keep `rawDesign` field as protected
   - Remove abstract `getDesign()`/`setDesign()` methods

2. **Update FeAnalysisWithStringEntity**
   - Remove `<String>` parameter
   - Add `implements FeatureAnalysis<PresetFeatureAnalysisDesign, Integer>`
   - Implement `getDesign()` returning `PresetFeatureAnalysisDesign`
   - Add deserialization logic from `rawDesign`

3. **Update FeAnalysisWithCriteriaEntity**
   - Remove `<T extends FeAnalysisCriteriaEntity>` parameter
   - Add `implements FeatureAnalysis<CriteriaFeatureDesign, Integer>`
   - Change abstract method to return `CriteriaFeatureDesign`

4. **Update concrete criteria entities**
   - Remove generic parameters
   - Keep specific typed methods for criteria lists

#### Phase 2: Update Service Layer

1. **Update method signatures**
   ```java
   // BEFORE
   Optional<FeAnalysisEntity> findById(Integer id)
   
   // AFTER
   Optional<FeAnalysisEntity> findById(Integer id)  // Same, but no warning!
   ```

2. **Update casting logic**
   ```java
   // BEFORE
   if (entity instanceof FeAnalysisWithCriteriaEntity) {
       FeAnalysisWithCriteriaEntity<?> criteria = 
           (FeAnalysisWithCriteriaEntity<?>) entity;
   }
   
   // AFTER  
   if (entity instanceof FeAnalysisWithCriteriaEntity) {
       FeAnalysisWithCriteriaEntity criteria = 
           (FeAnalysisWithCriteriaEntity) entity;
   }
   ```

#### Phase 3: Update Converters

1. **Update converter interfaces**
   ```java
   // BEFORE
   public class FeAnalysisDTOToFeAnalysisConverter 
       extends BaseConverter<FeAnalysisDTO, FeAnalysisEntity>
   
   // AFTER
   public class FeAnalysisDTOToFeAnalysisConverter 
       extends BaseConverter<FeAnalysisDTO, FeAnalysisEntity>  // No change!
   ```

2. **Update design conversion logic**
   - Handle `PresetFeatureAnalysisDesign` serialization/deserialization
   - Handle `CriteriaFeatureDesign` conversion

#### Phase 4: Testing

1. **Unit tests** - verify type safety
2. **Integration tests** - verify persistence works
3. **API tests** - verify DTOs serialize correctly

---

## Future Improvements

### Address "not null" Discriminator

While JPA doesn't support multiple discriminator values per class, consider:

1. **Custom UserType** to map specific discriminator values
2. **Split into two classes** (PresetFeAnalysis, CustomFeAnalysis)
3. **Accept limitation** with clear documentation

### Consider DTO Alignment

Ensure DTOs mirror the entity structure:

```java
public abstract class FeAnalysisDTO {
    // Common fields
}

public class PresetFeAnalysisDTO extends FeAnalysisDTO {
    private PresetFeatureAnalysisDesign design;
}

public class PrevalenceFeAnalysisDTO extends FeAnalysisDTO {
    private CriteriaFeatureDesign design;
}
```

### Standardize Serialization

Create consistent JSON serialization strategy:
- Entities store raw JSON in `rawDesign`
- Lazy deserialization on `getDesign()` call
- External systems work with typed design objects

---

## Conclusion

The current `FeAnalysisEntity` hierarchy suffers from over-engineering through multiple competing abstraction mechanisms. The recommended refactoring (Option 1) eliminates generic type parameters at the entity level while maintaining single table inheritance and pushing interface implementation to subclasses. This approach:

- **Fixes type safety violations** with StandardizedAnalysisAPI
- **Eliminates raw type warnings** throughout the codebase
- **Simplifies reasoning** about the code structure
- **Requires minimal changes** to existing code and database
- **Provides clear migration path** through incremental updates

The key insight is that **persistence entities and domain contracts are separate concerns** that should not be conflated through generic type parameters. Let inheritance handle database polymorphism and interface implementation handle domain contracts.
