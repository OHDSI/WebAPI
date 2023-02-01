package org.ohdsi.webapi.test;


import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.google.common.collect.ImmutableMap;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.ohdsi.webapi.JerseyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Java6Assertions.assertThat;

@DatabaseTearDown(value = "/database/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class SecurityIT extends WebApiIT {

    private final Map<String, HttpStatus> EXPECTED_RESPONSE_CODES = ImmutableMap.<String, HttpStatus>builder()
            .put("/info/", HttpStatus.OK)
            .put("/i18n/", HttpStatus.OK)
            .put("/i18n/locales", HttpStatus.OK)
            .put("/ddl/results", HttpStatus.OK)
            .put("/ddl/cemresults", HttpStatus.OK)
            .put("/ddl/achilles", HttpStatus.OK)
            .put("/saml/saml-metadata", HttpStatus.OK)
            .put("/saml/slo", HttpStatus.TEMPORARY_REDIRECT)
            .build();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JerseyConfig jerseyConfig;

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    private final Logger LOG = LoggerFactory.getLogger(SecurityIT.class);

    @BeforeClass
    public static void prepare() {
        
        System.setProperty("security.provider", "AtlasRegularSecurity");
    }

    @AfterClass
    public static void disableSecurity() {

        System.setProperty("security.provider", "DisabledSecurity");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testServiceSecurity() {

        Map<String, List<ServiceInfo>> serviceMap = getServiceMap();
        for (String servicePrefix : serviceMap.keySet()) {
            List<ServiceInfo> serviceInfos = serviceMap.get(servicePrefix);
            for (ServiceInfo serviceInfo : serviceInfos) {
                if (!serviceInfo.pathPrefix.startsWith("/")) {
                    serviceInfo.pathPrefix = "/" + serviceInfo.pathPrefix;
                }
                serviceInfo.pathPrefix = serviceInfo.pathPrefix.replaceAll("//", "/");
                String rawUrl = getBaseUri() + serviceInfo.pathPrefix;
                URI uri = null;
                try {
                    Map<String, String> parametersMap = prepareParameters(serviceInfo.parameters);
                    HttpEntity<?> entity = new HttpEntity<>(new HttpHeaders());
                    uri = UriComponentsBuilder.fromUriString(rawUrl)
                            .buildAndExpand(parametersMap).encode().toUri();
                    LOG.info("testing service {}:{}", serviceInfo.httpMethod, uri);
                    ResponseEntity<?> response = this.restTemplate.exchange(uri, serviceInfo.httpMethod, entity,
                            getResponseType(serviceInfo));
                    LOG.info("tested service {}:{} with code {}", serviceInfo.httpMethod, uri, response.getStatusCode());
                    HttpStatus expectedStatus = EXPECTED_RESPONSE_CODES.getOrDefault(serviceInfo.pathPrefix, HttpStatus.UNAUTHORIZED);
                    assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
                } catch (Throwable t) {
                    LOG.info("failed service {}:{}", serviceInfo.httpMethod, uri);
                    collector.addError(new ThrowableEx(t, rawUrl));
                }
            }
        }
    }

    private Class<?> getResponseType(ServiceInfo serviceInfo) {

        if (serviceInfo.mediaTypes.contains(MediaType.TEXT_PLAIN_TYPE)) {
            return String.class;
        } else if (serviceInfo.pathPrefix.equalsIgnoreCase("/saml/saml-metadata")) {
            return Void.class;
        }
        return Object.class;
    }

    private Map<String, String> prepareParameters(List<Parameter> parameters) {

        Map<String, String> parametersMap = new HashMap<>();
        if (parameters != null && !parameters.isEmpty()) {
            for (Parameter parameter : parameters) {
                String value = "0";
                // if parameter has classloader then it is of object type, else it is primitive type
                if (parameter.getRawType().getClassLoader() != null) {
                    value = null;
                }
                parametersMap.put(parameter.getSourceName(), value);
            }
        }
        return parametersMap;
    }

    /*
     * Retrieve information about rest services (path prefixes, http methods, parameters)
     */
    private Map<String, List<ServiceInfo>> getServiceMap() {
        //
        Set<Class<?>> classes = this.jerseyConfig.getClasses();
        Map<String, List<ServiceInfo>> serviceMap = new HashMap<>();
        for (Class<?> clazz : classes) {
            Map<String, List<ServiceInfo>> map = scan(clazz);
            if (map != null) {
                serviceMap.putAll(map);
            }
        }

        return serviceMap;
    }

    private Map<String, List<ServiceInfo>> scan(Class<?> baseClass) {

        Resource.Builder builder = Resource.builder(baseClass);
        if (null == builder)
            return null;
        Resource resource = builder.build();
        String uriPrefix = "";
        Map<String, List<ServiceInfo>> info = new TreeMap<>();
        return process(uriPrefix, resource, info);
    }

    private Map<String, List<ServiceInfo>> process(String uriPrefix, Resource resource, Map<String, List<ServiceInfo>> info) {

        String pathPrefix = uriPrefix;
        List<Resource> resources = new ArrayList<>(resource.getChildResources());
        if (resource.getPath() != null) {
            pathPrefix = pathPrefix + resource.getPath();
        }
        for (ResourceMethod method : resource.getAllMethods()) {
            List<ServiceInfo> serviceInfos = info.computeIfAbsent(pathPrefix, k -> new ArrayList<>());
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.pathPrefix = pathPrefix;
            serviceInfo.httpMethod = HttpMethod.resolve(method.getHttpMethod());
            serviceInfo.parameters = method.getInvocable().getParameters();
            serviceInfo.mediaTypes = method.getProducedTypes();
            serviceInfos.add(serviceInfo);
        }
        for (Resource childResource : resources) {
            process(pathPrefix, childResource, info);
        }
        return info;
    }

    private static class ServiceInfo {
        public String pathPrefix;
        public HttpMethod httpMethod;
        public List<Parameter> parameters;
        public List<MediaType> mediaTypes;
    }

    private static class ThrowableEx extends Throwable {
        private final String serviceName;

        public ThrowableEx(Throwable throwable, String serviceName) {

            super(throwable);
            this.serviceName = serviceName;
        }

        @Override
        public String getMessage() {

            return serviceName + ": " + super.getMessage();
        }
    }
}