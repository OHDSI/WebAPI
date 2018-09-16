package org.ohdsi.webapi.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.ohdsi.hydra.Hydra;
import org.ohdsi.webapi.conceptset.ExportUtil;
import org.springframework.stereotype.Component;

@Path("/estimation/")
@Component
public class EstimationService extends AbstractDaoService {
    @GET
    @Path("info")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getInfo() throws IOException {
        String studySpecs = new String(Files.readAllBytes(Paths.get("C:\\Git\\anthonysena\\HydraTestApp\\src\\main\\java\\org\\anthonysena\\hydratestapp\\ExampleStudySpecs.json")));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        Hydra h = new Hydra(studySpecs);
        h.hydrate(baos);
        
        
        Response response = Response
                .ok(baos)
                .type(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", String.format("attachment; filename=\"estimation_%d_export.zip\"", 1))
                .build();

        return response;
    }
}
