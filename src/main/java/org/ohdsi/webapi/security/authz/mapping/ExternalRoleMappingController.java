package org.ohdsi.webapi.security.authz.mapping;

import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.ohdsi.webapi.security.authc.UserOrigin;
import org.ohdsi.webapi.security.authz.mapping.ExternalRoleMapService.ImportResult;
import org.ohdsi.webapi.security.authz.mapping.ExternalRoleMapService.InvalidFormatException;
import org.ohdsi.webapi.security.authz.mapping.ExternalRoleMapService.ValidationException;

/**
 * REST controller for managing external role mappings.
 * Provides endpoints for administrators to create, read, update, and delete
 * mappings between external identities and WebAPI roles.
 */
@RestController
@RequestMapping("/role/mapping")
public class ExternalRoleMappingController {

  private final ExternalRoleMapService externalRoleMapService;

  public ExternalRoleMappingController(ExternalRoleMapService externalRoleMapService) {
    this.externalRoleMapService = externalRoleMapService;
  }

  /**
   * Create a new external role mapping.
   *
   * POST /role/mapping
   *
   * @param request the mapping request with origin, externalClaim, roleId, and optional description
   * @return the created mapping
   */
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isPermitted('admin:security')")
  @ResponseStatus(HttpStatus.CREATED)
  public ExternalRoleMap createMapping(@RequestBody MappingRequest request) {
    return externalRoleMapService.createMapping(
        request.origin(),
        request.externalClaim(),
        request.roleId(),
        request.description()
    );
  }

  /**
   * Get all mappings for a specific authentication origin.
   *
   * GET /role/mapping?origin=LDAP
   *
   * @param origin the authentication origin (LDAP, ACTIVE_DIRECTORY, WINDOWS, OIDC, etc.)
   * @return list of mappings for that origin
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isPermitted('admin:security')")
  public List<ExternalRoleMap> listMappings(@RequestParam String origin) {
    return externalRoleMapService.getMappingsForOrigin(origin);
  }

  /**
   * Get all mappings for all origins.
   *
   * GET /role/mapping/all
   *
   * @return list of all mappings across all origins
   */
  @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isPermitted('admin:security')")
  public List<ExternalRoleMap> listAllMappings() {
    List<ExternalRoleMap> allMappings = new java.util.ArrayList<>();
    for (UserOrigin origin : UserOrigin.values()) {
      allMappings.addAll(externalRoleMapService.getMappingsForOrigin(origin));
    }
    return allMappings;
  }

  /**
   * Delete an external role mapping by ID.
   *
   * DELETE /role/mapping/{id}
   *
   * @param id the mapping ID to delete
   */
  @DeleteMapping(value = "/{id}")
  @PreAuthorize("isPermitted('admin:security')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteMapping(@PathVariable Integer id) {
    externalRoleMapService.removeMapping(id);
  }

  /**
   * Add new external role mappings from a CSV file.
   * CSV must have header row: claim, role_name, description
   * All mappings will be added to the specified origin.
   *
   * POST /role/mapping/import?origin=LDAP
   *
   * @param csvFile multipart file containing CSV content
   * @param origin the authentication origin to add mappings for
   * @return import result with row details and summary
   */
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isPermitted('admin:security')")
  public ImportResult addMappingsFromCsv(
      @RequestPart("csvfile") MultipartFile csvFile,
      @RequestParam UserOrigin origin) throws IOException, InvalidFormatException, ValidationException {
    return externalRoleMapService.addMappings(csvFile, origin);
  }

  /**
   * Overwrite all external role mappings for an origin with a CSV file.
   * Performs a diff: removes mappings not in CSV, adds mappings in CSV but not currently present.
   * CSV must have header row: claim, role_name, description
   *
   * PUT /role/mapping/import?origin=LDAP
   *
   * @param csvFile multipart file containing CSV content
   * @param origin the authentication origin to overwrite mappings for
   * @return import result with row details and summary
   */
  @PutMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isPermitted('admin:security')")
  public ImportResult overwriteMappingsFromCsv(
      @RequestPart("csvfile") MultipartFile csvFile,
      @RequestParam UserOrigin origin) throws IOException, InvalidFormatException {
    return externalRoleMapService.overwriteMappings(csvFile, origin);
  }

  /**
   * Delete external role mappings from a CSV file.
   * Matches mappings based on claim and role_name, then removes them.
   * CSV must have header row: claim, role_name, description
   *
   * DELETE /role/mapping/import?origin=LDAP
   *
   * @param csvFile multipart file containing CSV content
   * @param origin the authentication origin to delete mappings for
   * @return import result with row details and summary
   */
  @DeleteMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isPermitted('admin:security')")
  public ImportResult deleteMappingsFromCsv(
      @RequestPart("csvfile") MultipartFile csvFile,
      @RequestParam UserOrigin origin) throws IOException, InvalidFormatException, ValidationException {
    return externalRoleMapService.deleteMappings(csvFile, origin);
  }

  /**
   * Request record for creating or managing external role mappings.
   */
  public record MappingRequest(
      UserOrigin origin,
      String externalClaim,
      Long roleId,
      String description
  ) {
  }
}
