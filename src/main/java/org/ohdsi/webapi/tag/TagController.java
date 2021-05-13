package org.ohdsi.webapi.tag;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.i18n.I18nService;
import org.ohdsi.webapi.tag.dto.TagDTO;
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

    @Autowired
    public TagController(TagService pathwayService) {
        this.tagService = pathwayService;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TagDTO create(final TagDTO dto) {
        return tagService.create(dto);
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TagDTO> search(@QueryParam("namePart") String namePart) {
        if (StringUtils.isBlank(namePart)) {
            return Collections.emptyList();
        }
        return tagService.listInfoDTO(namePart);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TagDTO> list() {
        return tagService.listInfoDTO();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TagDTO update(@PathParam("id") final Integer id, final TagDTO dto) {
        return tagService.update(id, dto);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TagDTO get(@PathParam("id") final Integer id) {
        return tagService.getDTOById(id);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void delete(@PathParam("id") final Integer id) {
        tagService.delete(id);
    }
}
