package org.ohdsi.webapi.facets;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/facets")
@Controller
@Transactional(readOnly = true)
public class FacetController {
    private final FacetedSearchService searchService;

    public FacetController(FacetedSearchService searchService) {
        this.searchService = searchService;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<FacetItem> getValues(@QueryParam("facet") String facet, @QueryParam("entityName") String entityName) {
        return searchService.getValues(facet,  entityName);
     }
}
