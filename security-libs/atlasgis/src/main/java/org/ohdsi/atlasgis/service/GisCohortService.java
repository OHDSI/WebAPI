package org.ohdsi.atlasgis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.commons.utils.TemplateUtils;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.AnalysisSyncRequestDTO;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.ohdsi.atlasgis.converter.GeoBoundingBoxMapper;
import org.ohdsi.atlasgis.dto.GeoBoundingBox;
import org.ohdsi.atlasgis.utils.GisHttpClient;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class GisCohortService {
    private static final String GET_COHORT_BOUNDS_SQL_PATH = "/bounds/getCohortBounds.sql";
    private static final String GET_LOCATION_SQL_FILENAME = "getLocation.sql";
    private static final String GET_LOCATION_SQL_PATH = "/sql/" + GET_LOCATION_SQL_FILENAME;

    private static final String CALCULATE_CONTOURS_R_PATH = "/density/calculateContours.R";
    private static final String CALCULATE_CONTOURS_RESULT_FILE = "geo.json";

    private static final String CALCULATE_CLUSTERS_R_PATH = "/cluster/calculateClusters.R";
    private static final String CALCULATE_CLUSTERS_RESULT_FILE = "clusters.json";

    private static final String COHORT_ID_PARAM = "cohortId";

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Value("${executionengine.url}")
    private String executionEngineURL;
    @Value("${executionengine.token}")
    private String executionEngineToken;

    private final GisSourceService gisSourceService;
    private final ObjectMapper objectMapper;
    private final GeoBoundingBoxMapper geoBoundingBoxMapper;
    private final GisHttpClient client;

    public GeoBoundingBox getCohortBounds(Integer cohortId, String sourceKey) throws IOException, SQLException {
        DataSourceUnsecuredDTO source = gisSourceService.getDataSourceDTO(sourceKey);
        String sql;
        try (InputStream is = new ClassPathResource(GET_COHORT_BOUNDS_SQL_PATH).getInputStream()) {
            String sqlTmpl = IOUtils.toString(is, StandardCharsets.UTF_8);
            sqlTmpl = SqlRender.renderSql(sqlTmpl, new String[]{"cdmSchema", "resultSchema", "cohortId"},
                    new String[]{source.getCdmSchema(), source.getResultSchema(), cohortId.toString()});
            sql = SqlTranslate.translateSql(sqlTmpl, source.getType().getOhdsiDB());
        }

        GeoBoundingBox boundingBox = gisSourceService.executeOnSource(source, jdbcTemplate -> {
            GeoBoundingBox bbox = new GeoBoundingBox();
            jdbcTemplate.query(sql, rs -> {
                bbox.setNorthLatitude(rs.getDouble("max_latitude"));
                bbox.setWestLongitude(rs.getDouble("max_longitude"));
                bbox.setSouthLatitude(rs.getDouble("min_latitude"));
                bbox.setEastLongitude(rs.getDouble("min_longitude"));
            });
            return bbox;
        });

        return boundingBox;
    }

    public JsonNode getDensityMap(Integer cohortId, String dataSourceKey, GeoBoundingBox geoBoundingBox) throws IOException {

        return runGisAnalysis(cohortId, dataSourceKey, geoBoundingBox, CALCULATE_CONTOURS_R_PATH, CALCULATE_CONTOURS_RESULT_FILE);
    }

    public JsonNode getClusters(Integer cohortId, String dataSourceKey, GeoBoundingBox geoBoundingBox) throws IOException {

        return runGisAnalysis(cohortId, dataSourceKey, geoBoundingBox, CALCULATE_CLUSTERS_R_PATH, CALCULATE_CLUSTERS_RESULT_FILE);
    }

    private JsonNode runGisAnalysis(Integer cohortId, String dataSourceKey, GeoBoundingBox geoBoundingBox, String scriptPath, String resultFilename) throws IOException {

        String scriptFn = new File(scriptPath).getName();

        DataSourceUnsecuredDTO dataSourceDTO = gisSourceService.getDataSourceDTO(dataSourceKey);

        Map<String, Object> params = geoBoundingBoxMapper.toMap(geoBoundingBox);
        params.put(COHORT_ID_PARAM, cohortId);
        String script = TemplateUtils.loadTemplate(scriptPath).apply(params);
        String locationSql = TemplateUtils.loadTemplate(GET_LOCATION_SQL_PATH).apply(params);

        MultiPart multiPart = buildRequest(dataSourceDTO, scriptFn);
        try (ByteArrayInputStream baisScript = new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8));
             ByteArrayInputStream baisSql = new ByteArrayInputStream(locationSql.getBytes(StandardCharsets.UTF_8));) {
            StreamDataBodyPart filePartScript = new StreamDataBodyPart("file", baisScript, scriptFn);
            multiPart.bodyPart(filePartScript);

            StreamDataBodyPart filePartSql = new StreamDataBodyPart("file", baisSql, GET_LOCATION_SQL_FILENAME);
            multiPart.bodyPart(filePartSql);

            WebTarget webTarget = client.target(executionEngineURL + "analyze/sync");

            FormDataMultiPart formDataMultiPart = webTarget
                    .request(MediaType.MULTIPART_FORM_DATA_TYPE)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(AUTHORIZATION_HEADER, executionEngineToken)
                    .post(Entity.entity(multiPart, multiPart.getMediaType()),
                            FormDataMultiPart.class);

            return resolveJsonResult(formDataMultiPart, resultFilename);
        }
    }

    private MultiPart buildRequest(DataSourceUnsecuredDTO dataSourceDTO, String executableFilename) {
        AnalysisSyncRequestDTO analysisRequestDTO = getSyncRequestDTO(dataSourceDTO, executableFilename);

        MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

        multiPart.bodyPart(
                new FormDataBodyPart("analysisRequest", analysisRequestDTO,
                        MediaType.APPLICATION_JSON_TYPE));
        return multiPart;
    }

    private AnalysisSyncRequestDTO getSyncRequestDTO(DataSourceUnsecuredDTO dataSourceDTO, String executableFilename) {

        AnalysisSyncRequestDTO analysisRequest = new AnalysisSyncRequestDTO();
        analysisRequest.setId(0L);
        analysisRequest.setDataSource(dataSourceDTO);
        analysisRequest.setRequested(new Date());
        analysisRequest.setExecutableFileName(executableFilename);
        return analysisRequest;
    }

    private JsonNode resolveJsonResult(FormDataMultiPart multiPart, String filename) {
        Optional<FormDataBodyPart> resFile = multiPart.getFields("file").stream()
                .filter(mf -> Objects.equals(mf.getContentDisposition().getFileName(), filename))
                .findFirst();
        if (!resFile.isPresent()) {
            multiPart.getFields("stdout").stream()
                    .findFirst()
                    .ifPresent(f -> {
                        try {
                            BodyPartEntity bodyPartEntity = (BodyPartEntity) f.getEntity();
                            byte[] contents = IOUtils.toByteArray(bodyPartEntity.getInputStream());
                            log.error(new String(contents, StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            log.error("Cannot extract contents of stdout.txt");
                        }
                    });
            throw new RuntimeException("Cannot extract result file");
        } else {
            try {
                BodyPartEntity bodyPartEntity = (BodyPartEntity) resFile.get().getEntity();
                byte[] contents = IOUtils.toByteArray(bodyPartEntity.getInputStream());
                return objectMapper.readTree(new String(contents, StandardCharsets.UTF_8));
            } catch (IOException ex) {
                throw new UncheckedIOException("Cannot parse result file", ex);
            }
        }
    }
}
