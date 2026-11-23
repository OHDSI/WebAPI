package org.ohdsi.atlasgis.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.ohdsi.atlasgis.dto.GeoBoundingBox;
import org.ohdsi.atlasgis.service.GisCohortService;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.SQLException;

@Controller
@Path(value = "/gis/cohort")
public class GisCohortController {
    private final GisCohortService gisCohortService;

    public GisCohortController(GisCohortService gisCohortService) {
        this.gisCohortService = gisCohortService;
    }

    @GET
    @Path("/{cohortId}/bounds/{dataSourceKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public GeoBoundingBox getClusters(
        @PathParam("cohortId") Integer cohortId,
        @PathParam("dataSourceKey") String dataSourceKey
    ) throws IOException, SQLException {

        return gisCohortService.getCohortBounds(cohortId, dataSourceKey);
    }

    @GET
    @Path("/{cohortId}/clusters/{dataSourceKey}")
    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
    public JsonNode getClusters(
        @PathParam("cohortId") Integer cohortId,
        @PathParam("dataSourceKey") String dataSourceKey,
        @QueryParam("eastLongitude") Double eastLongitude,
        @QueryParam("northLatitude") Double northLatitude,
        @QueryParam("westLongitude") Double westLongitude,
        @QueryParam("southLatitude") Double southLatitude
    ) throws IOException {
        GeoBoundingBox geoBoundingBox = getGeoBoundingBox(eastLongitude, northLatitude, westLongitude, southLatitude);
        return gisCohortService.getClusters(cohortId, dataSourceKey, geoBoundingBox);
    }

    @GET
    @Path("/{cohortId}/density/{dataSourceKey}")
    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
    public JsonNode getDensityMap(
        @PathParam("cohortId") Integer cohortId,
        @PathParam("dataSourceKey") String dataSourceKey,
        @QueryParam("eastLongitude") Double eastLongitude,
        @QueryParam("northLatitude") Double northLatitude,
        @QueryParam("westLongitude") Double westLongitude,
        @QueryParam("southLatitude") Double southLatitude
    ) throws IOException {
        GeoBoundingBox geoBoundingBox = getGeoBoundingBox(eastLongitude, northLatitude, westLongitude, southLatitude);
        return gisCohortService.getDensityMap(cohortId, dataSourceKey, geoBoundingBox);
    }

    private GeoBoundingBox getGeoBoundingBox(@QueryParam("eastLongitude") Double eastLongitude, @QueryParam("northLatitude") Double northLatitude, @QueryParam("westLongitude") Double westLongitude, @QueryParam("southLatitude") Double southLatitude) {
        GeoBoundingBox geoBoundingBox = new GeoBoundingBox();
        geoBoundingBox.setEastLongitude(eastLongitude);
        geoBoundingBox.setNorthLatitude(northLatitude);
        geoBoundingBox.setWestLongitude(westLongitude);
        geoBoundingBox.setSouthLatitude(southLatitude);
        return geoBoundingBox;
    }
}
