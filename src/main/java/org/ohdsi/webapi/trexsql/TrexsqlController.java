package org.ohdsi.webapi.trexsql;

import com.trex.Trexsql;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.ohdsi.webapi.source.SourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin proxy controller that forwards all /trexsql/* requests to the trexsql plugin.
 * All routing and business logic is handled by the plugin.
 */
@Path("trexsql")
@Component
public class TrexsqlController {

    private static final Logger log = LoggerFactory.getLogger(TrexsqlController.class);

    private final TrexsqlInstanceManager instanceManager;

    public TrexsqlController(TrexsqlInstanceManager instanceManager,
                             SourceRepository sourceRepository) {
        this.instanceManager = instanceManager;
        Trexsql.setSourceRepository(sourceRepository);
    }

    @Path("{path:.*}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleGet(@PathParam("path") String path,
                              @Context HttpHeaders headers,
                              @Context UriInfo uriInfo) {
        return forward("GET", path, null, headers, uriInfo);
    }

    @Path("{path:.*}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handlePost(@PathParam("path") String path,
                               String body,
                               @Context HttpHeaders headers,
                               @Context UriInfo uriInfo) {
        return forward("POST", path, body, headers, uriInfo);
    }

    @Path("{path:.*}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handlePut(@PathParam("path") String path,
                              String body,
                              @Context HttpHeaders headers,
                              @Context UriInfo uriInfo) {
        return forward("PUT", path, body, headers, uriInfo);
    }

    @Path("{path:.*}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleDelete(@PathParam("path") String path,
                                 @Context HttpHeaders headers,
                                 @Context UriInfo uriInfo) {
        return forward("DELETE", path, null, headers, uriInfo);
    }

    @Path("{path:.*}")
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handlePatch(@PathParam("path") String path,
                                String body,
                                @Context HttpHeaders headers,
                                @Context UriInfo uriInfo) {
        return forward("PATCH", path, body, headers, uriInfo);
    }

    @SuppressWarnings("unchecked")
    private Response forward(String method, String path, String body,
                             HttpHeaders headers, UriInfo uriInfo) {
        log.debug("{} /trexsql/{}", method, path);

        try {
            Object db = instanceManager.getInstance();

            Map<String, String> headerMap = new HashMap<>();
            headers.getRequestHeaders().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    headerMap.put(key, values.get(0));
                }
            });

            Map<String, String> queryParams = new HashMap<>();
            uriInfo.getQueryParameters().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    queryParams.put(key, values.get(0));
                }
            });

            Map<String, Object> result = Trexsql.handleRequest(db, method, path, body, headerMap, queryParams);

            int status = ((Number) result.getOrDefault("status", 200)).intValue();
            Object responseBody = result.get("body");

            Response.ResponseBuilder responseBuilder = Response.status(status);

            Map<String, String> responseHeaders = (Map<String, String>) result.get("headers");
            if (responseHeaders != null) {
                responseHeaders.forEach(responseBuilder::header);
            }

            if (responseBody != null) {
                responseBuilder.entity(responseBody);
            }

            return responseBuilder.build();

        } catch (IllegalStateException e) {
            log.warn("Trexsql not available: {}", e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of("error", "TREXSQL_UNAVAILABLE", "message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            log.error("Error forwarding request to trexsql: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "INTERNAL_ERROR", "message", e.getMessage()))
                    .build();
        }
    }
}
