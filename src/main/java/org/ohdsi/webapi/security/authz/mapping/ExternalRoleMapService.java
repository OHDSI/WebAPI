package org.ohdsi.webapi.security.authz.mapping;

import com.opencsv.CSVReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.ohdsi.webapi.security.authc.UserOrigin;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.authz.Role;
import org.ohdsi.webapi.security.authz.RoleEntity;
import org.ohdsi.webapi.security.authz.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for managing external role mappings.
 * Handles both administrative operations (CRUD) and runtime role resolution during login.
 */
@Service
@Transactional
public class ExternalRoleMapService {

  private final ExternalRoleMapRepository externalRoleMapRepository;
  private final RoleRepository roleRepository;
  private final AuthorizationService authorizationService;

  public ExternalRoleMapService(ExternalRoleMapRepository externalRoleMapRepository,
                                RoleRepository roleRepository,
                                AuthorizationService authorizationService) {
    this.externalRoleMapRepository = externalRoleMapRepository;
    this.roleRepository = roleRepository;
    this.authorizationService = authorizationService;
  }

  /**
   * Create a new external role mapping.
   *
   * @param origin the authentication origin (LDAP, OIDC, WINDOWS, etc.)
   * @param externalClaim the claim/group/identifier value from the external source
   * @param roleId the WebAPI role ID to map to
   * @param description optional description of the mapping
   * @return the created mapping
   * @throws IllegalArgumentException if the role doesn't exist or mapping already exists
   */
  public ExternalRoleMap createMapping(UserOrigin origin, String externalClaim, Long roleId, String description) {
    RoleEntity roleEntity = roleRepository.findById(roleId)
        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

    ExternalRoleMapEntity entity = new ExternalRoleMapEntity(origin, externalClaim, roleEntity, description);
    ExternalRoleMapEntity saved = externalRoleMapRepository.save(entity);
    return ExternalRoleMap.fromEntity(saved);
  }

  /**
   * Create a new external role mapping without description.
   *
   * @param origin the authentication origin
   * @param externalClaim the claim/group/identifier value
   * @param roleId the WebAPI role ID
   * @return the created mapping
   */
  public ExternalRoleMap createMapping(UserOrigin origin, String externalClaim, Long roleId) {
    return createMapping(origin, externalClaim, roleId, null);
  }

  /**
   * Remove an external role mapping.
   *
   * @param mappingId the mapping ID to remove
   */
  public void removeMapping(Integer mappingId) {
    externalRoleMapRepository.deleteById(mappingId);
  }

  /**
   * Get all mappings for a specific authentication origin.
   *
   * @param origin the authentication origin
   * @return list of all mappings for that origin
   */
  public List<ExternalRoleMap> getMappingsForOrigin(UserOrigin origin) {
    return externalRoleMapRepository.findByOrigin(origin).stream()
        .map(ExternalRoleMap::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get all mappings for a specific authentication origin.
   *
   * @param origin the authentication origin as string
   * @return list of all mappings for that origin
   */
  public List<ExternalRoleMap> getMappingsForOrigin(String origin) {
    return getMappingsForOrigin(UserOrigin.valueOf(origin));
  }

  /**
   * Resolve external claims to WebAPI role names.
   * Used during login to map external identities (groups, claims) to roles.
   *
   * @param origin the authentication origin
   * @param externalClaims collection of claim values to resolve (group DNs, claim values, etc.)
   * @return set of WebAPI role names that the claims map to
   */
  @Transactional(readOnly = true)
  public Set<String> resolveRoleNames(UserOrigin origin, Collection<String> externalClaims) {
    if (externalClaims == null || externalClaims.isEmpty()) {
      return new HashSet<>();
    }

    return externalRoleMapRepository.findByOriginAndExternalClaimIn(origin, externalClaims)
        .stream()
        .map(mapping -> mapping.getRole().getName())
        .collect(Collectors.toSet());
  }

  /**
   * Sync user roles from a specific authentication origin.
   * Removes roles from that origin that user no longer has, adds new ones they do have.
   * Never modifies roles from other origins (e.g., SYSTEM origin remains untouched).
   *
   * @param login the WebAPI user login name
   * @param origin the authentication origin to sync
   * @param mappedRoleNames set of role names user should have from this origin
   */
  public void syncUserRoles(String login, UserOrigin origin, Set<String> mappedRoleNames) {
    if (mappedRoleNames == null) {
      mappedRoleNames = new HashSet<>();
    }

    // Get all current roles for this user from this specific origin
    List<String> currentRoleNames = authorizationService.getRolesByOrigin(login, origin);
    Set<String> currentRoles = new HashSet<>(currentRoleNames);

    // Determine which roles to remove (in current but not in mapped)
    Set<String> toRemove = new HashSet<>(currentRoles);
    toRemove.removeAll(mappedRoleNames);

    // Determine which roles to add (in mapped but not in current)
    Set<String> toAdd = new HashSet<>(mappedRoleNames);
    toAdd.removeAll(currentRoles);

    // Remove roles no longer in external source
    for (String roleName : toRemove) {
      authorizationService.removeUserFromRole(roleName, login, origin);
    }

    // Add new roles
    for (String roleName : toAdd) {
      authorizationService.addUserToRole(roleName, login, origin);
    }
  }

  /**
   * Add new mappings from CSV file.
   * CSV must have header row: claim, role_name, description
   * All mappings scoped to the provided origin.
   *
   * @param csvFile MultipartFile containing CSV content
   * @param origin the authentication origin these mappings apply to
   * @return import result with row details and summary
   * @throws InvalidFormatException if CSV structure is invalid
   * @throws ValidationException if validation fails (role not found, duplicate, etc.)
   */
  public ImportResult addMappings(MultipartFile csvFile, UserOrigin origin)
      throws InvalidFormatException, ValidationException, IOException {
    String csvContent = new String(csvFile.getBytes(), StandardCharsets.UTF_8);
    return addMappingsFromString(csvContent, origin);
  }

  /**
   * Internal method: Add new mappings from CSV string content.
   */
  private ImportResult addMappingsFromString(String csvContent, UserOrigin origin)
      throws InvalidFormatException, ValidationException {
    Map<String, RoleEntity> rolesByName = loadRolesForLookup();
    List<ParsedMapping> mappings = parseCsv(csvContent, rolesByName);
    validateNoDuplicatesForAdd(mappings, origin);

    return doAddMappings(mappings, origin);
  }

  /**
   * Delete mappings from CSV file.
   * CSV must have header row: claim, role_name, description
   * Finds and deletes matching mappings for the provided origin.
   *
   * @param csvFile MultipartFile containing CSV content
   * @param origin the authentication origin these mappings apply to
   * @return import result with row details and summary
   * @throws InvalidFormatException if CSV structure is invalid
   * @throws ValidationException if validation fails
   */
  public ImportResult deleteMappings(MultipartFile csvFile, UserOrigin origin)
      throws InvalidFormatException, ValidationException, IOException {
    String csvContent = new String(csvFile.getBytes(), StandardCharsets.UTF_8);
    return deleteMappingsFromString(csvContent, origin);
  }

  /**
   * Internal method: Delete mappings from CSV string content.
   */
  private ImportResult deleteMappingsFromString(String csvContent, UserOrigin origin)
      throws InvalidFormatException, ValidationException {
    Map<String, RoleEntity> rolesByName = loadRolesForLookup();
    List<ParsedMapping> mappings = parseCsv(csvContent, rolesByName);

    return doDeleteMappings(mappings, origin);
  }

  /**
   * Overwrite all mappings for an origin with CSV file.
   * Performs a diff: removes mappings not in CSV, adds mappings in CSV but not currently present.
   * CSV must have header row: claim, role_name, description
   *
   * @param csvFile MultipartFile containing CSV content
   * @param origin the authentication origin these mappings apply to
   * @return import result with row details and summary
   * @throws InvalidFormatException if CSV structure is invalid
   */
  public ImportResult overwriteMappings(MultipartFile csvFile, UserOrigin origin)
      throws InvalidFormatException, IOException {
    String csvContent = new String(csvFile.getBytes(), StandardCharsets.UTF_8);
    return overwriteMappingsFromString(csvContent, origin);
  }

  /**
   * Internal method: Overwrite all mappings from CSV string content.
   */
  private ImportResult overwriteMappingsFromString(String csvContent, UserOrigin origin)
      throws InvalidFormatException {
    try {
      Map<String, RoleEntity> rolesByName = loadRolesForLookup();
      List<ParsedMapping> mappings = parseCsv(csvContent, rolesByName);

      return doOverwriteMappings(mappings, origin);
    } catch (ValidationException e) {
      throw new InvalidFormatException(e.getMessage());
    }
  }

  // ============================================================================
  // PRIVATE HELPER METHODS
  // ============================================================================

  /**
   * Load all roles into a map keyed by role name for fast lookup during parsing.
   */
  private Map<String, RoleEntity> loadRolesForLookup() {
    Map<String, RoleEntity> map = new HashMap<>();
    for (RoleEntity role : roleRepository.findAll()) {
      map.put(role.getName(), role);
    }
    return map;
  }

  /**
   * Validate the CSV header row has exactly 3 columns in the correct order.
   */
  private void validateHeader(String[] headerRow) throws InvalidFormatException {
    if (headerRow.length != 3 || !headerRow[0].equalsIgnoreCase("claim")
        || !headerRow[1].equalsIgnoreCase("role_name")
        || !headerRow[2].equalsIgnoreCase("description")) {
      String got = String.join(", ", headerRow);
      throw new InvalidFormatException(
          "Expected header: claim, role_name, description; but got: " + got);
    }
  }

  /**
   * Validate a data row has correct structure and valid values.
   */
  private void validateRow(String[] row, int rowNum) throws InvalidFormatException {
    if (row.length != 3) {
      throw new InvalidFormatException("Row " + rowNum + ": Expected 3 columns, got " + row.length);
    }

    String claim = row[0].trim();
    if (claim.isEmpty()) {
      throw new InvalidFormatException("Row " + rowNum + ": 'claim' cannot be empty");
    }

    String roleName = row[1].trim();
    if (roleName.isEmpty()) {
      throw new InvalidFormatException("Row " + rowNum + ": 'role_name' cannot be empty");
    }

    String description = row[2].trim();
    if (description.length() > 500) {
      throw new InvalidFormatException("Row " + rowNum + ": description exceeds 500 characters");
    }
  }

  /**
   * Parse CSV content into structured mappings.
   * Resolves role names to role IDs via the provided rolesByName map.
   */
  private List<ParsedMapping> parseCsv(String csvContent, Map<String, RoleEntity> rolesByName)
      throws InvalidFormatException, ValidationException {
    try {
      CSVReader reader = new CSVReader(new StringReader(csvContent));
      List<String[]> rows = reader.readAll();

      if (rows.isEmpty()) {
        throw new InvalidFormatException("CSV is empty");
      }

      validateHeader(rows.get(0));

      List<ParsedMapping> mappings = new ArrayList<>();
      for (int i = 1; i < rows.size(); i++) {
        String[] row = rows.get(i);

        // Skip only rows with no data (length 0). Rows with empty values (,,) are validation errors.
        if (row.length == 0) {
          continue;
        }

        validateRow(row, i + 1);

        String claim = row[0].trim();
        String roleName = row[1].trim();
        String description = row[2].trim();

        // Resolve role name to role ID
        RoleEntity role = rolesByName.get(roleName);
        if (role == null) {
          throw new ValidationException(
              "Row " + (i + 1) + ": Role '" + roleName + "' not found in system");
        }

        mappings.add(new ParsedMapping(claim, roleName, role.getId(), description.isEmpty() ? null : description));
      }

      if (mappings.isEmpty()) {
        throw new InvalidFormatException("CSV contains no valid data rows");
      }

      return mappings;
    } catch (IOException e) {
      throw new InvalidFormatException("Failed to parse CSV: " + e.getMessage());
    }
  }

  /**
   * Validate no duplicate claims within CSV and no conflicts with existing DB mappings for this origin.
   */
  private void validateNoDuplicatesForAdd(List<ParsedMapping> mappings, UserOrigin origin)
      throws ValidationException {
    // Check for duplicates within CSV
    Set<String> claims = new HashSet<>();
    for (ParsedMapping m : mappings) {
      if (!claims.add(m.externalClaim())) {
        throw new ValidationException("Duplicate claim in CSV: " + m.externalClaim());
      }
    }

    // Check for existing mappings in DB for this origin
    List<String> claimsInCsv = mappings.stream()
        .map(ParsedMapping::externalClaim)
        .collect(Collectors.toList());

    long existingCount = externalRoleMapRepository.countByOriginAndExternalClaimIn(origin, claimsInCsv);
    if (existingCount > 0) {
      throw new ValidationException(
          "Some mappings already exist for origin " + origin
              + ". Use overwriteMappings() to replace them.");
    }
  }

  /**
   * Add mappings to database. Called after all validations pass.
   */
  private ImportResult doAddMappings(List<ParsedMapping> mappings, UserOrigin origin) {
    List<RowResult> rows = new ArrayList<>();
    int addedCount = 0;

    for (ParsedMapping m : mappings) {
      try {
        RoleEntity roleEntity = roleRepository.findById(m.roleId())
            .orElseThrow(() -> new IllegalStateException("Role disappeared: " + m.roleId()));

        ExternalRoleMapEntity entity = new ExternalRoleMapEntity(origin, m.externalClaim(),
            roleEntity, m.description());
        externalRoleMapRepository.save(entity);

        rows.add(new RowResult(m.externalClaim(), m.roleName(), RowResultStatus.ADDED, null));
        addedCount++;
      } catch (Exception e) {
        rows.add(new RowResult(m.externalClaim(), m.roleName(), RowResultStatus.ERRORED, e.getMessage()));
      }
    }

    ImportSummary summary = new ImportSummary(addedCount, 0, 0, rows.size() - addedCount);
    return new ImportResult(rows, summary);
  }

  /**
   * Delete mappings from database. Best-effort - reports success/failure per row.
   */
  private ImportResult doDeleteMappings(List<ParsedMapping> mappings, UserOrigin origin) {
    List<RowResult> rows = new ArrayList<>();
    int removedCount = 0;
    int skippedCount = 0;

    for (ParsedMapping m : mappings) {
      int deleted = externalRoleMapRepository.deleteByOriginAndExternalClaimAndRoleId(origin,
          m.externalClaim(), m.roleId());

      if (deleted > 0) {
        rows.add(new RowResult(m.externalClaim(), m.roleName(), RowResultStatus.REMOVED, null));
        removedCount++;
      } else {
        rows.add(new RowResult(m.externalClaim(), m.roleName(), RowResultStatus.SKIPPED, null));
        skippedCount++;
      }
    }

    ImportSummary summary = new ImportSummary(0, removedCount, skippedCount, 0);
    return new ImportResult(rows, summary);
  }

  /**
   * Overwrite all mappings for an origin.
   * Diffs: removes mappings not in CSV, adds new ones in CSV.
   * Best-effort - collects errors for invalid rows but continues processing.
   */
  private ImportResult doOverwriteMappings(List<ParsedMapping> csvMappings, UserOrigin origin) {
    List<RowResult> rows = new ArrayList<>();
    int addedCount = 0;
    int removedCount = 0;
    int skippedCount = 0;

    // Get current mappings for this origin
    List<ExternalRoleMapEntity> currentMappings = externalRoleMapRepository.findByOrigin(origin);
    Map<String, ExternalRoleMapEntity> currentByKey = new HashMap<>();
    for (ExternalRoleMapEntity entity : currentMappings) {
      String key = entity.getExternalClaim() + "|" + entity.getRole().getId();
      currentByKey.put(key, entity);
    }

    // Build a set of CSV mapping keys and track what we've seen
    Set<String> csvKeys = new HashSet<>();
    Map<String, ParsedMapping> csvByKey = new HashMap<>();
    for (ParsedMapping m : csvMappings) {
      String key = m.externalClaim() + "|" + m.roleId();
      csvKeys.add(key);
      csvByKey.put(key, m);
    }

    // Remove mappings not in CSV
    for (Map.Entry<String, ExternalRoleMapEntity> entry : currentByKey.entrySet()) {
      if (!csvKeys.contains(entry.getKey())) {
        externalRoleMapRepository.deleteById(entry.getValue().getId());
        rows.add(new RowResult(entry.getValue().getExternalClaim(), entry.getValue().getRole().getName(),
            RowResultStatus.REMOVED, null));
        removedCount++;
      }
    }

    // Add mappings from CSV that don't exist
    for (ParsedMapping m : csvMappings) {
      String key = m.externalClaim() + "|" + m.roleId();

      if (!currentByKey.containsKey(key)) {
        try {
          RoleEntity roleEntity = roleRepository.findById(m.roleId())
              .orElseThrow(() -> new IllegalStateException("Role not found: " + m.roleId()));

          ExternalRoleMapEntity entity = new ExternalRoleMapEntity(origin, m.externalClaim(),
              roleEntity, m.description());
          externalRoleMapRepository.save(entity);

          rows.add(new RowResult(m.externalClaim(), m.roleName(), RowResultStatus.ADDED, null));
          addedCount++;
        } catch (Exception e) {
          rows.add(new RowResult(m.externalClaim(), m.roleName(), RowResultStatus.ERRORED, e.getMessage()));
        }
      } else {
        // Mapping exists and is in CSV - skip it (no change)
        rows.add(new RowResult(m.externalClaim(), m.roleName(), RowResultStatus.SKIPPED, null));
        skippedCount++;
      }
    }

    ImportSummary summary = new ImportSummary(addedCount, removedCount, skippedCount, 0);
    return new ImportResult(rows, summary);
  }

  // ============================================================================
  // RECORD TYPES AND ENUMS
  // ============================================================================

  /**
   * Status of a row result from import operation.
   */
  public enum RowResultStatus {
    ADDED, REMOVED, SKIPPED, ERRORED
  }

  /**
   * Represents a parsed mapping from CSV before database persistence.
   */
  public record ParsedMapping(String externalClaim, String roleName, Long roleId, String description) {
  }

  /**
   * Result of processing a single mapping row during import.
   */
  public record RowResult(String externalClaim, String roleName, RowResultStatus status,
                          String errorMessage) {
  }

  /**
   * Summary of bulk import operation.
   */
  public record ImportSummary(int addedCount, int removedCount, int skippedCount, int erroredCount) {
  }

  /**
   * Complete result of import operation with row details and summary.
   */
  public record ImportResult(List<RowResult> rows, ImportSummary summary) {
  }

  /**
   * Thrown when CSV structure is invalid (header, column count, parsing error).
   */
  public static class InvalidFormatException extends Exception {
    public InvalidFormatException(String message) {
      super(message);
    }

    public InvalidFormatException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Thrown when CSV validation fails (role not found, duplicate, business rule violation).
   */
  public static class ValidationException extends Exception {
    public ValidationException(String message) {
      super(message);
    }

    public ValidationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
