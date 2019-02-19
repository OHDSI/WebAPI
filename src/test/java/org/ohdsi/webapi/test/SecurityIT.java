package org.ohdsi.webapi.test;


import static org.assertj.core.api.Java6Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.ImmutableMap;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.JerseyConfig;
import org.ohdsi.webapi.WebApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecurityIT {

    private Map<String, HttpStatus> EXPECTED_RESPONSE_CODES = ImmutableMap.<String, HttpStatus>builder()
            .put("/info/", HttpStatus.OK)
            .put("/ddl/results", HttpStatus.OK)
            .put("/ddl/cemresults", HttpStatus.OK)
            .build();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JerseyConfig jerseyConfig;

    @Autowired
    private Environment environment;

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @BeforeClass
    public static void prepareGroup() {
        TomcatURLStreamHandlerFactory.disable();
    }

    @Before
    public void prepareTest() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        converter.setSupportedMediaTypes(
                Arrays.asList(new MediaType[]{MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM}));

        restTemplate.getRestTemplate().setMessageConverters(Arrays.asList(converter, new FormHttpMessageConverter()));
    }

    @Test
    public void testServiceSecurity() throws Exception {
        if (isSecurityEnabled()) {
            String port = environment.getProperty("local.server.port");
            String contextPath = environment.getProperty("server.context-path") + "/";
            String serverPath = "http://localhost:" + port;

            Map<String, List<ServiceInfo>> serviceMap = getServiceMap();
            for(String servicePrefix: serviceMap.keySet()) {
                List<ServiceInfo> serviceInfos = serviceMap.get(servicePrefix);
                for(ServiceInfo serviceInfo: serviceInfos) {
                    String serviceRawUrl = contextPath + serviceInfo.pathPrefix;
                    String rawUrl = serverPath + serviceRawUrl;

                    try {
                        Map<String, String> parametersMap = prepareParameters(serviceInfo.parameters);

                        HttpEntity<?> entity = new HttpEntity<>(new HttpHeaders());
                        URI uri = UriComponentsBuilder.fromUriString(rawUrl)
                                .buildAndExpand(parametersMap).encode().toUri();

                        ResponseEntity<Object> response = this.restTemplate.exchange(uri,
                                serviceInfo.httpMethod, entity, Object.class);
                        HttpStatus expectedStatus = EXPECTED_RESPONSE_CODES.getOrDefault(serviceInfo.pathPrefix, HttpStatus.UNAUTHORIZED);
                        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
                    } catch (Throwable t) {
                        collector.addError(new ThrowableEx(t, serviceRawUrl));
                    }
                }
            }
        }
    }

    private Map<String, String> prepareParameters(List<Parameter> parameters) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Map<String, String> parametersMap = new HashMap<String, String>();
        if(parameters != null && !parameters.isEmpty()) {
            for (Parameter parameter : parameters) {
                String value = "0";
                // if parameter has classcloader then it is of object type, else it is primitive type
                if(parameter.getRawType().getClassLoader() != null) {
                    value = null;
                }
                parametersMap.put(parameter.getSourceName(), value);
            }
        }
        return parametersMap;
    }

    private boolean isSecurityEnabled() {
        String securityPropertyValue = environment.getProperty("security.provider");
        return true || securityPropertyValue != null && "AtlasRegularSecurity".equals(securityPropertyValue);
    }

    /*
     * Retrieve information about rest services (path prefixes, http methods, parameters)
     */
    private Map<String, List<ServiceInfo>> getServiceMap() throws Exception {
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

    private Map<String, List<ServiceInfo>> scan(Class baseClass) {
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
        List<Resource> resources = new ArrayList<>();
        resources.addAll(resource.getChildResources());
        if (resource.getPath() != null) {
            pathPrefix = pathPrefix + resource.getPath();
        }
        for (ResourceMethod method : resource.getAllMethods()) {
            List<ServiceInfo> serviceInfos = info.get(pathPrefix);
            if (null == serviceInfos) {
                serviceInfos = new ArrayList<>();
                info.put(pathPrefix, serviceInfos);
            }
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.pathPrefix = pathPrefix;
            serviceInfo.httpMethod = HttpMethod.resolve(method.getHttpMethod());
            serviceInfo.parameters = method.getInvocable().getParameters();
            serviceInfos.add(serviceInfo);
        }
        for (Resource childResource : resources) {
            process(pathPrefix, childResource, info);
        }
        return info;
    }

    private class ServiceInfo {
        public String pathPrefix;
        public HttpMethod httpMethod;
        public List<Parameter> parameters;
    }

    private class ThrowableEx extends Throwable {
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