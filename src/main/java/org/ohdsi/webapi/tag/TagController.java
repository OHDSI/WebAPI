package org.ohdsi.webapi.tag;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.ohdsi.webapi.tag.dto.TagGroupSubscriptionDTO;
import org.ohdsi.webapi.tag.dto.AssignmentPermissionsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

@Path("/tag")
@Controller
public class TagController {
    private final TagService tagService;
    private final TagGroupService tagGroupService;

    @Autowired
    public TagController(TagService pathwayService,
                         TagGroupService tagGroupService) {
        this.tagService = pathwayService;
        this.tagGroupService = tagGroupService;
    }

    /**
     * Creates a tag.
     *
     * @param dto
     * @return
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TagDTO create(final TagDTO dto) {
        return tagService.create(dto);
    }

    /**
     * Returns list of tags, which names contain a provided substring.
     *
     * @summary Search tags by name part
     * @param namePart
     * @return
     */
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TagDTO> search(@QueryParam("namePart") String namePart) {
        if (StringUtils.isBlank(namePart)) {
            return Collections.emptyList();
        }
        return tagService.listInfoDTO(namePart);
    }

    /**
     * Returns list of all tags.
     *
     * @return
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TagDTO> list() {
        return tagService.listInfoDTO();
    }

    /**
     * Updates tag with ID={id}.
     *
     * @param id
     * @param dto
     * @return
     */
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TagDTO update(@PathParam("id") final Integer id, final TagDTO dto) {
        return tagService.update(id, dto);
    }

    /**
     * Return tag by ID.
     *
     * @param id
     * @return
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TagDTO get(@PathParam("id") final Integer id) {
        return tagService.getDTOById(id);
    }

    /**
     * Deletes tag with ID={id}.
     *
     * @param id
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(@PathParam("id") final Integer id) {
        tagService.delete(id);
    }

    /**
     * Assignes group of tags to groups of assets.
     *
     * @param dto
     * @return
     */
    @POST
    @Path("/multiAssign")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void assignGroup(final TagGroupSubscriptionDTO dto) {
        tagGroupService.assignGroup(dto);
    }

    /**
     * Unassignes group of tags from groups of assets.
     *
     * @param dto
     * @return
     */
    @POST
    @Path("/multiUnassign")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void unassignGroup(final TagGroupSubscriptionDTO dto) {
        tagGroupService.unassignGroup(dto);
    }

    /**
     * Tags assignment permissions for current user
     *
     * @return
     */
    @GET
    @Path("/assignmentPermissions")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public AssignmentPermissionsDTO assignmentPermissions() {
        return tagService.getAssignmentPermissions();
    }
}
