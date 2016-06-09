package org.ohdsi.webapi.service;

import javax.annotation.PostConstruct;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.ohdsi.webapi.characterization.*;
import org.ohdsi.webapi.source.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Services related to characterization/summarization (Achilles)
 */
@Path("{sourceKey}/characterization/")
@Component
public class CharacterizationService extends AbstractDaoService {

    public static final String MIN_COVARIATE_PERSON_COUNT = "10";
    public static final String MIN_INTERVAL_PERSON_COUNT = "10";

    public static final String BASE_SQL_PATH = "/resources/characterization/sql";

    @Autowired
    private VisualizationDataRepository visualizationDataRepository;

    private ObjectMapper mapper = new ObjectMapper();
    private CharacterizationRunner queryRunner = null;

    @PostConstruct
    public void init() {
        queryRunner = new CharacterizationRunner(this.getSourceDialect(), this.visualizationDataRepository);
    }

    /**
     * Queries for datasource characterization (achilles) dashboard for the datasource id
     *
     * @return SourceDashboard
     */
    @GET
    @Path("/dashboard")
    @Produces(MediaType.APPLICATION_JSON)
    public SourceDashboard getDashboard(/*@PathParam("id") final int id,*/
                                        @PathParam("sourceKey") final String sourceKey,
                                        @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

        final String key = CharacterizationRunner.DASHBOARD;
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        VisualizationData data = refresh ? null : this.visualizationDataRepository.findBySourceIdAndVisualizationKey(source.getSourceId(), key);

        SourceDashboard dashboard = null;

        if (refresh || data == null) {
            dashboard = queryRunner.getDashboard(getSourceJdbcTemplate(source), id, source,
                    null, null, false, true);
        } else {
            try {
                dashboard = mapper.readValue(data.getData(), SourceDashboard.class);
            } catch (Exception e) {
                log.error(e);
            }
        }

        return dashboard;

    }

    /**
     * Queries for datasources
     *
     * @return Datasources
     */
    @GET
    @Path("/datasources")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Datasource> getDatasources(
            @PathParam("sourceKey") final String sourceKey,
            @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

        final String key = CharacterizationRunner.DATASOURCES;
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        VisualizationData data = refresh ? null : this.visualizationDataRepository.findBySourceIdAndVisualizationKey(source.getSourceId(), key);

        List<Datasource> datasources = null;

        if (refresh || data == null) {
            datasources = queryRunner.getDatasources(getSourceJdbcTemplate(source), source,
                    null, null, false, true);
        } else {
            try {
//                TODO fix List<DataSource> = mapper.readValue() assignment
//                datasources = mapper.readValue(data.getData(), Datasource.class);
//            } catch (Exception e) {
                log.error(e);
            }
        }
        return dashboard;
    }

}
