package org.ohdsi.webapi.mvc;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.tag.TagGroupService;
import org.ohdsi.webapi.tag.TagService;
import org.ohdsi.webapi.tag.dto.AssignmentPermissionsDTO;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.ohdsi.webapi.tag.dto.TagGroupSubscriptionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagMvcController extends AbstractMvcController {
    private final TagService tagService;
    private final TagGroupService tagGroupService;

    @Autowired
    public TagMvcController(TagService tagService,
                            TagGroupService tagGroupService) {
        this.tagService = tagService;
        this.tagGroupService = tagGroupService;
    }

    /**
     * Creates a tag.
     *
     * @param dto
     * @return
     */
    @PostMapping("/")
    public ResponseEntity<TagDTO> create(@RequestBody final TagDTO dto) {
        return ok(tagService.create(dto));
    }

    /**
     * Returns list of tags, which names contain a provided substring.
     *
     * @summary Search tags by name part
     * @param namePart
     * @return
     */
    @GetMapping("/search")
    public ResponseEntity<List<TagDTO>> search(@RequestParam("namePart") String namePart) {
        if (StringUtils.isBlank(namePart)) {
            return ok(Collections.emptyList());
        }
        return ok(tagService.listInfoDTO(namePart));
    }

    /**
     * Returns list of all tags.
     *
     * @return
     */
    @GetMapping("/")
    public ResponseEntity<List<TagDTO>> list() {
        return ok(tagService.listInfoDTO());
    }

    /**
     * Updates tag with ID={id}.
     *
     * @param id
     * @param dto
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<TagDTO> update(@PathVariable("id") final Integer id, @RequestBody final TagDTO dto) {
        return ok(tagService.update(id, dto));
    }

    /**
     * Return tag by ID.
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> get(@PathVariable("id") final Integer id) {
        return ok(tagService.getDTOById(id));
    }

    /**
     * Deletes tag with ID={id}.
     *
     * @param id
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final Integer id) {
        tagService.delete(id);
        return ok();
    }

    /**
     * Assignes group of tags to groups of assets.
     *
     * @param dto
     * @return
     */
    @PostMapping("/multiAssign")
    public ResponseEntity<Void> assignGroup(@RequestBody final TagGroupSubscriptionDTO dto) {
        tagGroupService.assignGroup(dto);
        return ok();
    }

    /**
     * Unassignes group of tags from groups of assets.
     *
     * @param dto
     * @return
     */
    @PostMapping("/multiUnassign")
    public ResponseEntity<Void> unassignGroup(@RequestBody final TagGroupSubscriptionDTO dto) {
        tagGroupService.unassignGroup(dto);
        return ok();
    }

    /**
     * Tags assignment permissions for current user
     *
     * @return
     */
    @GetMapping("/assignmentPermissions")
    public ResponseEntity<AssignmentPermissionsDTO> assignmentPermissions() {
        return ok(tagService.getAssignmentPermissions());
    }
}
