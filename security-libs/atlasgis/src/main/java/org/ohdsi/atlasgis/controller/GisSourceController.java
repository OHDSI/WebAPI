package org.ohdsi.atlasgis.controller;

import org.ohdsi.atlasgis.service.GisSourceService;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Controller
@Path(value = "/gis/source")
public class GisSourceController {
    private final GisSourceService gisSourceService;

    public GisSourceController(final GisSourceService gisSourceService) {
        this.gisSourceService = gisSourceService;
    }

    @GET
    @Path("/check/{dataSourceKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response check(@PathParam("dataSourceKey") final String dataSourceKey) throws IOException {
        return gisSourceService.checkIfSourceHasGeodata(dataSourceKey)
                ? Response.ok().build()
                : Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }
}
