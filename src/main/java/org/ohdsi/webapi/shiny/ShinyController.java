package org.ohdsi.webapi.shiny;

import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.ohdsi.webapi.service.ShinyService;
import org.ohdsi.webapi.shiro.annotations.DataSourceAccess;
import org.ohdsi.webapi.shiro.annotations.SourceKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;

@Component
@ConditionalOnProperty(name = "shiny.enabled", havingValue = "true")
@Path("/shiny")
public class ShinyController {

    @Autowired
    private ShinyService service;

    @GET
    @Path("/download/{type}/{id}/{sourceKey}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @DataSourceAccess
    public Response downloadShinyApp(
            @PathParam("type") String type,
            @PathParam("id") final int id,
            @PathParam("sourceKey") @SourceKey String sourceKey
    ) throws IOException {
        TemporaryFile data = service.packageShinyApp(type, id, sourceKey, PackagingStrategies.zip());
        ContentDisposition contentDisposition = ContentDisposition.type("attachment")
                .fileName(data.getFilename())
                .build();
        return Response
                .ok(Files.newInputStream(data.getFile()))
                .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .build();
    }

    @GET
    @Path("/publish/{type}/{id}/{sourceKey}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @DataSourceAccess
    @Transactional
    public Response publishShinyApp(
            @PathParam("type") String type,
            @PathParam("id") final int id,
            @PathParam("sourceKey") @SourceKey String sourceKey
    ) {
        service.publishApp(type, id, sourceKey);
        return Response.ok().build();
    }
}
