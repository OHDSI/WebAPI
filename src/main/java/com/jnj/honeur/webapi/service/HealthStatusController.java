package com.jnj.honeur.webapi.service;

import com.jnj.honeur.webapi.hss.StorageServiceClient;
import com.jnj.honeur.webapi.liferay.LiferayApiClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;

@Component("healthStatusController")
@Path("/health-status")
@ConditionalOnProperty(name = "webapi.central", havingValue = "true")
public class HealthStatusController {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private LiferayApiClient liferayApiClient;
    @Autowired
    private StorageServiceClient storageServiceClient;

    @Value("${security.cas.login.url}")
    private String casLoginUrl;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHealthStatus(@DefaultValue("true") @QueryParam("transitive") boolean transitive) {
        HashMap<String, String> components = new HashMap<>();

        if (transitive) {
            components.put("CAS", String.valueOf(healthCheckCAS()));

            components.put("HONEUR Storage Service", String.valueOf(storageServiceClient.healthCheck()));

            components.put("Liferay", String.valueOf(liferayApiClient.healthCheck()));
        }

        if (components.values().stream().anyMatch(statusCode -> !statusCode.equals("200"))) {
            return Response.status(HttpStatus.SERVICE_UNAVAILABLE.value()).entity(components).build();
        }

        return Response.ok(components).build();
    }

    private int healthCheckCAS() {
        final RestTemplate restTemplate = new RestTemplate();
        String serviceUrl = casLoginUrl;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(serviceUrl, String.class);
            return response.getStatusCode().value();
        } catch (RestClientException e) {
            log.warn(e.getMessage(), e);
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
    }


    @PostConstruct
    public void initIt() throws Exception {
        System.out.println("HEALTH STATUS CONTROLLER CREATED");
    }
}
