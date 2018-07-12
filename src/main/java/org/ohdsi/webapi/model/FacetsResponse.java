package org.ohdsi.webapi.model;

import java.util.List;
import java.util.Map;

public class FacetsResponse {

  private Map<String, List<FacetValue>> facets;

  public Map<String, List<FacetValue>> getFacets() {
    return facets;
  }

  public void setFacets(Map<String, List<FacetValue>> facets) {
    this.facets = facets;
  }

}
