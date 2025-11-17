package org.ohdsi.webapi.reusable;

import org.ohdsi.webapi.Pagination;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.ohdsi.webapi.reusable.dto.ReusableDTO;
import org.ohdsi.webapi.reusable.dto.ReusableVersionFullDTO;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * Spring MVC version of ReusableController
 *
 * Migration Status: Replaces /reusable/ReusableController.java (Jersey)
 * Endpoints: 14 endpoints (POST, GET, PUT, DELETE)
 * Complexity: Medium - CRUD operations with versioning and tagging
 */
@RestController
@RequestMapping("/reusable")
public class ReusableMvcController extends AbstractMvcController {

    private final ReusableService reusableService;

    @Autowired
    public ReusableMvcController(ReusableService reusableService) {
        this.reusableService = reusableService;
    }

    /**
     * Create a new reusable
     *
     * Jersey: POST /WebAPI/reusable/
     * Spring MVC: POST /WebAPI/v2/reusable/
     *
     * @param dto the reusable DTO
     * @return created reusable
     */
    @PostMapping(
        value = "/",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ReusableDTO> create(@RequestBody ReusableDTO dto) {
        return ok(reusableService.create(dto));
    }

    /**
     * Get paginated list of reusables
     *
     * Jersey: GET /WebAPI/reusable/
     * Spring MVC: GET /WebAPI/v2/reusable/
     *
     * @param pageable pagination parameters
     * @return page of reusables
     */
    @GetMapping(
        value = "/",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Page<ReusableDTO>> page(@Pagination Pageable pageable) {
        return ok(reusableService.page(pageable));
    }

    /**
     * Update an existing reusable
     *
     * Jersey: PUT /WebAPI/reusable/{id}
     * Spring MVC: PUT /WebAPI/v2/reusable/{id}
     *
     * @param id the reusable ID
     * @param dto the reusable DTO
     * @return updated reusable
     */
    @PutMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ReusableDTO> update(@PathVariable("id") Integer id, @RequestBody ReusableDTO dto) {
        return ok(reusableService.update(id, dto));
    }

    /**
     * Copy a reusable
     *
     * Jersey: POST /WebAPI/reusable/{id}
     * Spring MVC: POST /WebAPI/v2/reusable/{id}
     *
     * @param id the reusable ID
     * @return copied reusable
     */
    @PostMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ReusableDTO> copy(@PathVariable("id") int id) {
        return ok(reusableService.copy(id));
    }

    /**
     * Get a reusable by ID
     *
     * Jersey: GET /WebAPI/reusable/{id}
     * Spring MVC: GET /WebAPI/v2/reusable/{id}
     *
     * @param id the reusable ID
     * @return the reusable
     */
    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ReusableDTO> get(@PathVariable("id") Integer id) {
        return ok(reusableService.getDTOById(id));
    }

    /**
     * Check if a reusable name exists
     *
     * Jersey: GET /WebAPI/reusable/{id}/exists
     * Spring MVC: GET /WebAPI/v2/reusable/{id}/exists
     *
     * @param id the reusable ID (default 0)
     * @param name the name to check
     * @return true if exists
     */
    @GetMapping(
        value = "/{id}/exists",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Boolean> exists(
            @PathVariable("id") int id,
            @RequestParam(value = "name", required = false) String name) {
        return ok(reusableService.exists(id, name));
    }

    /**
     * Delete a reusable
     *
     * Jersey: DELETE /WebAPI/reusable/{id}
     * Spring MVC: DELETE /WebAPI/v2/reusable/{id}
     *
     * @param id the reusable ID
     */
    @DeleteMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        reusableService.delete(id);
        return ok();
    }

    /**
     * Assign tag to Reusable
     *
     * Jersey: POST /WebAPI/reusable/{id}/tag/
     * Spring MVC: POST /WebAPI/v2/reusable/{id}/tag/
     *
     * @param id the reusable ID
     * @param tagId the tag ID
     */
    @PostMapping(
        value = "/{id}/tag/",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> assignTag(@PathVariable("id") int id, @RequestBody int tagId) {
        reusableService.assignTag(id, tagId);
        return ok();
    }

    /**
     * Unassign tag from Reusable
     *
     * Jersey: DELETE /WebAPI/reusable/{id}/tag/{tagId}
     * Spring MVC: DELETE /WebAPI/v2/reusable/{id}/tag/{tagId}
     *
     * @param id the reusable ID
     * @param tagId the tag ID
     */
    @DeleteMapping(
        value = "/{id}/tag/{tagId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> unassignTag(@PathVariable("id") int id, @PathVariable("tagId") int tagId) {
        reusableService.unassignTag(id, tagId);
        return ok();
    }

    /**
     * Assign protected tag to Reusable
     *
     * Jersey: POST /WebAPI/reusable/{id}/protectedtag/
     * Spring MVC: POST /WebAPI/v2/reusable/{id}/protectedtag/
     *
     * @param id the reusable ID
     * @param tagId the tag ID
     */
    @PostMapping(
        value = "/{id}/protectedtag/",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> assignPermissionProtectedTag(@PathVariable("id") int id, @RequestBody int tagId) {
        reusableService.assignTag(id, tagId);
        return ok();
    }

    /**
     * Unassign protected tag from Reusable
     *
     * Jersey: DELETE /WebAPI/reusable/{id}/protectedtag/{tagId}
     * Spring MVC: DELETE /WebAPI/v2/reusable/{id}/protectedtag/{tagId}
     *
     * @param id the reusable ID
     * @param tagId the tag ID
     */
    @DeleteMapping(
        value = "/{id}/protectedtag/{tagId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> unassignPermissionProtectedTag(@PathVariable("id") int id, @PathVariable("tagId") int tagId) {
        reusableService.unassignTag(id, tagId);
        return ok();
    }

    /**
     * Get list of versions of Reusable
     *
     * Jersey: GET /WebAPI/reusable/{id}/version/
     * Spring MVC: GET /WebAPI/v2/reusable/{id}/version/
     *
     * @param id the reusable ID
     * @return list of versions
     */
    @GetMapping(
        value = "/{id}/version/",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<VersionDTO>> getVersions(@PathVariable("id") long id) {
        return ok(reusableService.getVersions(id));
    }

    /**
     * Get version of Reusable
     *
     * Jersey: GET /WebAPI/reusable/{id}/version/{version}
     * Spring MVC: GET /WebAPI/v2/reusable/{id}/version/{version}
     *
     * @param id the reusable ID
     * @param version the version number
     * @return the version
     */
    @GetMapping(
        value = "/{id}/version/{version}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ReusableVersionFullDTO> getVersion(@PathVariable("id") int id, @PathVariable("version") int version) {
        return ok(reusableService.getVersion(id, version));
    }

    /**
     * Update version of Reusable
     *
     * Jersey: PUT /WebAPI/reusable/{id}/version/{version}
     * Spring MVC: PUT /WebAPI/v2/reusable/{id}/version/{version}
     *
     * @param id the reusable ID
     * @param version the version number
     * @param updateDTO the version update DTO
     * @return updated version
     */
    @PutMapping(
        value = "/{id}/version/{version}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<VersionDTO> updateVersion(
            @PathVariable("id") int id,
            @PathVariable("version") int version,
            @RequestBody VersionUpdateDTO updateDTO) {
        return ok(reusableService.updateVersion(id, version, updateDTO));
    }

    /**
     * Delete version of Reusable
     *
     * Jersey: DELETE /WebAPI/reusable/{id}/version/{version}
     * Spring MVC: DELETE /WebAPI/v2/reusable/{id}/version/{version}
     *
     * @param id the reusable ID
     * @param version the version number
     */
    @DeleteMapping(
        value = "/{id}/version/{version}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> deleteVersion(@PathVariable("id") int id, @PathVariable("version") int version) {
        reusableService.deleteVersion(id, version);
        return ok();
    }

    /**
     * Create a new asset from version of Reusable
     *
     * Jersey: PUT /WebAPI/reusable/{id}/version/{version}/createAsset
     * Spring MVC: PUT /WebAPI/v2/reusable/{id}/version/{version}/createAsset
     *
     * @param id the reusable ID
     * @param version the version number
     * @return new reusable created from version
     */
    @PutMapping(
        value = "/{id}/version/{version}/createAsset",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ReusableDTO> copyAssetFromVersion(@PathVariable("id") int id, @PathVariable("version") int version) {
        return ok(reusableService.copyAssetFromVersion(id, version));
    }

    /**
     * Get list of reusables with assigned tags
     *
     * Jersey: POST /WebAPI/reusable/byTags
     * Spring MVC: POST /WebAPI/v2/reusable/byTags
     *
     * @param requestDTO tag name list request
     * @return list of reusables
     */
    @PostMapping(
        value = "/byTags",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<ReusableDTO>> listByTags(@RequestBody TagNameListRequestDTO requestDTO) {
        if (requestDTO == null || requestDTO.getNames() == null || requestDTO.getNames().isEmpty()) {
            return ok(Collections.emptyList());
        }
        return ok(reusableService.listByTags(requestDTO));
    }
}
