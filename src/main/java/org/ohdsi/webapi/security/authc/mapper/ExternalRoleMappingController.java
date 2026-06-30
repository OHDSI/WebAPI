package org.ohdsi.webapi.security.authc.mapper;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.ohdsi.webapi.security.authc.UserOrigin;

/**
 * REST controller for managing external role mappings.
 * Provides endpoints for administrators to create, read, update, and delete
 * mappings between external identities and WebAPI roles.
 */
@RestController
@RequestMapping("/admin/external-role-mappings")
public class ExternalRoleMappingController {

  private final ExternalRoleMapService externalRoleMapService;

  public ExternalRoleMappingController(ExternalRoleMapService externalRoleMapService) {
    this.externalRoleMapService = externalRoleMapService;
  }

  /**
   * Create a new external role mapping.
   *
   * POST /admin/external-role-mappings
   *
   * @param request the mapping request with origin, externalClaim, roleId, and optional description
   * @return the created mapping
   */
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isPermitted('admin:security')")
  @ResponseStatus(HttpStatus.CREATED)
  public ExternalRoleMap createMapping(@RequestBody CreateMappingRequest request) {
    UserOrigin origin = UserOrigin.valueOf(request.getOrigin());
    return externalRoleMapService.createMapping(
        origin,
        request.getExternalClaim(),
        request.getRoleId(),
        request.getDescription()
    );
  }

  /**
   * Get all mappings for a specific authentication origin.
   *
   * GET /admin/external-role-mappings?origin=LDAP
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
   * GET /admin/external-role-mappings/all
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
   * DELETE /admin/external-role-mappings/{id}
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
   * Request DTO for creating a new external role mapping.
   */
  public static class CreateMappingRequest {
    private String origin;
    private String externalClaim;
    private Long roleId;
    private String description;

    public CreateMappingRequest() {
    }

    public CreateMappingRequest(String origin, String externalClaim, Long roleId, String description) {
      this.origin = origin;
      this.externalClaim = externalClaim;
      this.roleId = roleId;
      this.description = description;
    }

    public String getOrigin() {
      return origin;
    }

    public void setOrigin(String origin) {
      this.origin = origin;
    }

    public String getExternalClaim() {
      return externalClaim;
    }

    public void setExternalClaim(String externalClaim) {
      this.externalClaim = externalClaim;
    }

    public Long getRoleId() {
      return roleId;
    }

    public void setRoleId(Long roleId) {
      this.roleId = roleId;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }
  }
}
