package org.ohdsi.atlasgis.service;

import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;
import org.apache.commons.io.IOUtils;
import org.ohdsi.atlasgis.dto.GeoBoundingBox;
import org.ohdsi.atlasgis.dto.PersonLocation;
import org.ohdsi.atlasgis.dto.PersonLocationHistory;
import org.ohdsi.atlasgis.utils.DateUtils;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@Transactional
public class GisPersonService {
    private static final String GET_PERSON_BOUNDS_SQL_PATH = "/bounds/getPersonLocations.sql";

    private final GisSourceService gisSourceService;

    public GisPersonService(GisSourceService gisSourceService) {

        this.gisSourceService = gisSourceService;
    }

    public PersonLocationHistory getBoundsHistory(Integer personId, String dataSourceKey,
                                                  Date startDate, Date endDate) throws IOException {
        DataSourceUnsecuredDTO source = gisSourceService.getDataSourceDTO(dataSourceKey);

        String sql;
        try (InputStream is = new ClassPathResource(GET_PERSON_BOUNDS_SQL_PATH).getInputStream()) {
            String sqlTmpl = IOUtils.toString(is, StandardCharsets.UTF_8);
            sqlTmpl = SqlRender.renderSql(sqlTmpl, new String[]{"cdmSchema", "personId", "startDate", "endDate"},
                    new String[]{ source.getCdmSchema(), personId.toString(), DateUtils.formatISO(startDate), DateUtils.formatISO(endDate) });
            sql = SqlTranslate.translateSql(sqlTmpl, source.getType().getOhdsiDB());
        }

        PersonLocationHistory locationHistory = gisSourceService.executeOnSource(source, jdbcTemplate -> {
            PersonLocationHistory lh = new PersonLocationHistory();
            jdbcTemplate.query(sql, rs -> {
                PersonLocation location = new PersonLocation();
                location.setLatitude(rs.getDouble("latitude"));
                location.setLongitude(rs.getDouble("longitude"));
                location.setStartDate(rs.getDate("start_date"));
                location.setEndDate(rs.getDate("end_date"));
                lh.getLocations().add(location);
            });
            return lh;
        });

        GeoBoundingBox bbox = new GeoBoundingBox();
        bbox.setNorthLatitude(locationHistory.getLocations().stream().map(PersonLocation::getLatitude).max(Double::compare).orElse(0.0));
        bbox.setWestLongitude(locationHistory.getLocations().stream().map(PersonLocation::getLongitude).max(Double::compare).orElse(0.0));
        bbox.setSouthLatitude(locationHistory.getLocations().stream().map(PersonLocation::getLatitude).min(Double::compare).orElse(0.0));
        bbox.setEastLongitude(locationHistory.getLocations().stream().map(PersonLocation::getLongitude).min(Double::compare).orElse(0.0));
        locationHistory.setBbox(bbox);

        return locationHistory;
    }
}
