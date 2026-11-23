package org.ohdsi.atlasgis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.ohdsi.atlasgis.converter.GeoBoundingBoxMapper;
import org.ohdsi.atlasgis.dto.GeoBoundingBox;
import org.ohdsi.atlasgis.utils.GisHttpClient;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class GisTemplateRegressionTest {
    @Test
    public void densityMapSubmitsRenderedSqlAndUnchangedRScript() throws Exception {
        checkAnalysis(true, "/density/calculateContours.R", "geo.json");
    }

    @Test
    public void clusteringSubmitsRenderedSqlAndUnchangedRScript() throws Exception {
        checkAnalysis(false, "/cluster/calculateClusters.R", "clusters.json");
    }

    private void checkAnalysis(boolean density, String resource, String resultName) throws Exception {
        GisSourceService sources = mock(GisSourceService.class);
        when(sources.getDataSourceDTO("test")).thenReturn(new DataSourceUnsecuredDTO());
        GisHttpClient client = mock(GisHttpClient.class);
        WebTarget target = mock(WebTarget.class);
        Invocation.Builder request = mock(Invocation.Builder.class);
        when(client.target("https://engine.invalid/analyze/sync")).thenReturn(target);
        when(target.request(MediaType.MULTIPART_FORM_DATA_TYPE)).thenReturn(request);
        when(request.accept(MediaType.APPLICATION_JSON)).thenReturn(request);
        when(request.header(eq("Authorization"), any())).thenReturn(request);
        FormDataMultiPart response = mock(FormDataMultiPart.class);
        FormDataBodyPart result = mock(FormDataBodyPart.class);
        when(result.getContentDisposition()).thenReturn(FormDataContentDisposition.name("file").fileName(resultName).build());
        BodyPartEntity entity = mock(BodyPartEntity.class);
        when(entity.getInputStream()).thenReturn(new ByteArrayInputStream("{\"ok\":true}".getBytes(StandardCharsets.UTF_8)));
        when(result.getEntity()).thenReturn(entity);
        when(response.getFields("file")).thenReturn(Arrays.asList(result));
        when(request.post(any(Entity.class), eq(FormDataMultiPart.class))).thenReturn(response);
        GisCohortService service = new GisCohortService(sources, new ObjectMapper(), new GeoBoundingBoxMapper() {}, client);
        Field url = GisCohortService.class.getDeclaredField("executionEngineURL");
        url.setAccessible(true);
        url.set(service, "https://engine.invalid/");
        GeoBoundingBox box = new GeoBoundingBox();
        box.setWestLongitude(-10.5); box.setEastLongitude(20.25);
        box.setNorthLatitude(50.75); box.setSouthLatitude(-30.125);

        // Inspect streams inside post(), before the service closes them.
        Map<String, String> submitted = new HashMap<>();
        when(request.post(any(Entity.class), eq(FormDataMultiPart.class))).thenAnswer(invocation -> {
            MultiPart multipart = (MultiPart) ((Entity<?>) invocation.getArguments()[0]).getEntity();
            for (org.glassfish.jersey.media.multipart.BodyPart part : multipart.getBodyParts()) {
                if (part instanceof StreamDataBodyPart) {
                    StreamDataBodyPart file = (StreamDataBodyPart) part;
                    submitted.put(file.getContentDisposition().getFileName(), read((InputStream) file.getEntity()));
                }
            }
            return response;
        });
        JsonNode json = density ? service.getDensityMap(42, "test", box) : service.getClusters(42, "test", box);
        assertTrue(json.get("ok").asBoolean());
        assertEquals(read(getClass().getResourceAsStream(resource)), submitted.get(resource.substring(resource.lastIndexOf('/') + 1)));
        String sql = submitted.get("getLocation.sql");
        assertNotNull(sql);
        assertTrue(sql.contains("cohort_definition_id = 42"));
        for (String value : new String[] {"-10.5", "20.25", "50.75", "-30.125"}) assertTrue(sql.contains(value));
        assertTrue(sql.contains("@resultSchema")); // Subsequent engine-side SqlRender behavior is preserved.
        assertFalse(sql.contains("{{"));
    }

    private static String read(InputStream input) throws Exception {
        assertNotNull(input);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024]; int count;
        while ((count = input.read(bytes)) != -1) output.write(bytes, 0, count);
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}
