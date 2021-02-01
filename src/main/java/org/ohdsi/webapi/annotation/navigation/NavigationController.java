package org.ohdsi.webapi.annotation.navigation;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ohdsi.webapi.cohortresults.ProfileSampleRecord;
import javax.ws.rs.QueryParam;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.cohortresults.*;
import javax.annotation.PostConstruct;
import org.ohdsi.webapi.annotation.annotation.AnnotationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("annotations/")
@Component
public class NavigationController extends AbstractDaoService {

  @Autowired
  private VisualizationDataRepository visualizationDataRepository;

  @Autowired
  private AnnotationService annotationService;
  

  @Autowired
  private NavigationService ns;

  private CohortResultsAnalysisRunner queryRunner = null;


  @GET
  @Path("navigation")
  @Produces(MediaType.APPLICATION_JSON)
  public Navigation getNavigation(
    @QueryParam("cohortId") final int cohortId,
    @QueryParam("subjectId") final Long subjectId,
    @QueryParam("sampleName") final String sampleName,
    @QueryParam("sourceKey") final String sourceKey
  ) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    Navigation nav = ns.navData(new Navigation(), subjectId, sampleName, cohortId, source);

    return nav;
  }

}
