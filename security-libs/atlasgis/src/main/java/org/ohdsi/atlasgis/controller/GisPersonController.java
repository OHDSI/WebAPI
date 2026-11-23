package org.ohdsi.atlasgis.controller;

import org.ohdsi.atlasgis.dto.PersonLocationHistory;
import org.ohdsi.atlasgis.service.GisPersonService;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Controller
@Path(value = "/gis/person")
public class GisPersonController {
    private final GisPersonService gisPersonService;

    public GisPersonController(GisPersonService gisPersonService) {
        this.gisPersonService = gisPersonService;
    }

    @GET
    @Path("/{personId}/bounds/{dataSourceKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public PersonLocationHistory getClusters(
            @PathParam("personId") Integer personId,
            @PathParam("dataSourceKey") String dataSourceKey,
            @QueryParam("startDate") Long startTimestamp,
            @QueryParam("endDate") Long endTimestamp
    ) throws IOException {
        Date startDate = Optional.ofNullable(startTimestamp).map(Date::new).orElse(null),
                endDate = Optional.ofNullable(endTimestamp).map(Date::new).orElse(null);
        return gisPersonService.getBoundsHistory(personId, dataSourceKey, startDate, endDate);
    }
}
