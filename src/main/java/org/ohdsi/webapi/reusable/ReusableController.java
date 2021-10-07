package org.ohdsi.webapi.reusable;

import org.ohdsi.webapi.Pagination;
import org.ohdsi.webapi.reusable.dto.ReusableDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/reusable")
@Controller
public class ReusableController {
    private final ReusableService reusableService;

    @Autowired
    public ReusableController(ReusableService reusableService) {
        this.reusableService = reusableService;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ReusableDTO create(final ReusableDTO dto) {
        return reusableService.create(dto);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Page<ReusableDTO> page(@Pagination Pageable pageable) {
        return reusableService.page(pageable);
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ReusableDTO update(@PathParam("id") final Integer id, final ReusableDTO dto) {
        return reusableService.update(id, dto);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ReusableDTO get(@PathParam("id") final Integer id) {
        return reusableService.getDTOById(id);
    }

    @GET
    @Path("/{id}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean exists(@PathParam("id") @DefaultValue("0") final int id, @QueryParam("name") String name) {
        return reusableService.exists(name);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(@PathParam("id") final Integer id) {
        reusableService.delete(id);
    }

    /**
     * Assign tag to Reusable
     *
     * @param id
     * @param tagId
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/tag/")
    public void assignTag(@PathParam("id") final int id, final int tagId) {
        reusableService.assignTag(id, tagId, false);
    }

    /**
     * Unassign tag from Reusable
     *
     * @param id
     * @param tagId
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/tag/{tagId}")
    public void unassignTag(@PathParam("id") final int id, @PathParam("tagId") final int tagId) {
        reusableService.unassignTag(id, tagId, false);
    }

    /**
     * Assign protected tag to Reusable
     *
     * @param id
     * @param tagId
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/protectedtag/")
    public void assignPermissionProtectedTag(@PathParam("id") int id, final int tagId) {
        reusableService.assignTag(id, tagId, true);
    }

    /**
     * Unassign protected tag from Reusable
     *
     * @param id
     * @param tagId
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/protectedtag/{tagId}")
    public void unassignPermissionProtectedTag(@PathParam("id") final int id, @PathParam("tagId") final int tagId) {
        reusableService.unassignTag(id, tagId, true);
    }
}
