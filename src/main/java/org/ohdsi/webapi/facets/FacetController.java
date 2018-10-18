package org.ohdsi.webapi.facets;

import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/facets")
@Controller
public class FacetController {
    private final Collection<FacetProvider> providers;
    private final Map<String, FacetProvider> providersByFacet = new HashMap<>();

    @PostConstruct
    private void init() {
        providers.forEach(p -> providersByFacet.put(p.getName(), p));
    }


    public FacetController(Collection<FacetProvider> providers) {
        this.providers = providers;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<Object> getValues(@QueryParam("facet") String facet, @QueryParam("entityName") String entityName) {
        final FacetProvider facetProvider = providersByFacet.get(facet);
        if(facetProvider == null) {
            throw new IllegalArgumentException("unknown facet");
        }
        return facetProvider.getValues(entityName);
    }

}
