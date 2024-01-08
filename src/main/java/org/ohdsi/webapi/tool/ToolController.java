package org.ohdsi.webapi.tool;

import org.ohdsi.webapi.tool.dto.ToolDTO;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Controller
@Path("/tool")
public class ToolController {
    private final ToolServiceImpl service;

    public ToolController(ToolServiceImpl service) {
        this.service = service;
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ToolDTO> getTools() {
        return service.getTools();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ToolDTO getToolById(@PathParam("id") Integer id) {
        return service.getById(id);
    }

    @POST
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ToolDTO createTool(ToolDTO dto) {
        return service.saveTool(dto);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void delete(@PathParam("id") Integer id) {
        service.delete(id);
    }

    @PUT
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ToolDTO updateTool(ToolDTO toolDTO) {
        return service.saveTool(toolDTO);
    }
}
