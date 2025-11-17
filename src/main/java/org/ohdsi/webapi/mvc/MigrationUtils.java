package org.ohdsi.webapi.mvc;

import jakarta.ws.rs.core.MediaType;

/**
 * Utility methods to assist with Jersey to Spring MVC migration.
 */
public class MigrationUtils {

    /**
     * Convert JAX-RS MediaType constant to Spring media type string
     */
    public static String toSpringMediaType(String jaxrsMediaType) {
        // JAX-RS uses constants like MediaType.APPLICATION_JSON
        // Spring uses constants like MediaType.APPLICATION_JSON_VALUE
        // This is mainly for documentation/reference
        return switch (jaxrsMediaType) {
            case MediaType.APPLICATION_JSON -> org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
            case MediaType.APPLICATION_XML -> org.springframework.http.MediaType.APPLICATION_XML_VALUE;
            case MediaType.TEXT_PLAIN -> org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
            case MediaType.TEXT_HTML -> org.springframework.http.MediaType.TEXT_HTML_VALUE;
            case MediaType.MULTIPART_FORM_DATA -> org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
            case MediaType.APPLICATION_FORM_URLENCODED -> org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
            default -> jaxrsMediaType;
        };
    }

    /**
     * Annotation mapping reference for migration
     */
    public static class AnnotationMapping {
        /*
         * JAX-RS to Spring MVC Annotation Mapping Guide:
         *
         * @Path("/api")               → @RequestMapping("/WebAPI/api")
         * @GET                        → @GetMapping
         * @POST                       → @PostMapping
         * @PUT                        → @PutMapping
         * @DELETE                     → @DeleteMapping
         * @PathParam("id")            → @PathVariable("id")
         * @QueryParam("name")         → @RequestParam(value="name")
         * @FormParam("field")         → @RequestParam("field")
         * @FormDataParam("file")      → @RequestPart("file") // for MultipartFile
         * @Produces(APPLICATION_JSON) → produces = APPLICATION_JSON_VALUE
         * @Consumes(APPLICATION_JSON) → consumes = APPLICATION_JSON_VALUE
         * Response                    → ResponseEntity<T>
         * Response.ok(entity)         → ResponseEntity.ok(entity)
         * Response.status(404)        → ResponseEntity.status(404) or ResponseEntity.notFound()
         *
         * Provider Classes:
         * @Provider + ExceptionMapper     → @ControllerAdvice + @ExceptionHandler
         * @Provider + ContainerRequestFilter → HandlerInterceptor
         * @Provider + MessageBodyWriter   → HttpMessageConverter
         */
    }
}
