package com.jnj.honeur.webapi.liferay;

import com.fasterxml.jackson.databind.JsonNode;
import com.jnj.honeur.webapi.liferay.model.Organization;
import org.apache.commons.codec.binary.Base64;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRoleEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnExpression("${datasource.honeur.enabled} and ${webapi.central}")
public class LiferayApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiferayApiClient.class);

    private static final String STANDARD_ROLE = "1";


    @Value("${datasource.liferay.company.web.id}")
    private String companyWebId;

    @Value("${datasource.liferay.url}")
    private String liferayBaseUrl;
    @Value("${datasource.liferay.user}")
    private String liferayServiceUser;
    @Value("${datasource.liferay.password}")
    private String liferayServicePassword;

    private String companyId;
    private String liferayWebApi;
    private RestTemplate restTemplate;

    public LiferayApiClient(){
        restTemplate = new RestTemplate();
    }

    public void setLiferayBaseUrl(String liferayBaseUrl) {
        this.liferayBaseUrl = liferayBaseUrl;
    }
    public void setLiferayServiceUser(String liferayServiceUser) {
        this.liferayServiceUser = liferayServiceUser;
    }
    public void setLiferayServicePassword(String liferayServicePassword) {
        this.liferayServicePassword = liferayServicePassword;
    }

    @PostConstruct
    void initializeLiferayUrlAndCompanyId(){
        liferayWebApi = liferayBaseUrl +"/api/jsonws";
        companyId = getCompanyId();
    }

    private String getCompanyId(){
        String companyEndPoint = "/company/get-companies";
        String companyId;
        try {
            List<LinkedHashMap<String, String>> companies = restTemplate.exchange(liferayWebApi + companyEndPoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    List.class).getBody();

            if (companies.size() == 1) {
                companyId = companies.get(0).get("companyId");
            } else {
                List<LinkedHashMap<String, String>> filteredCompanies = companies.stream().filter(company -> company.get("webId").equals(companyWebId)).collect(Collectors.toList());
                companyId = filteredCompanies.get(0).get("companyId");
            }
        } catch (HttpStatusCodeException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
        return companyId;
    }

    public UserEntity findUserByLogin(String login) {
        String userIdEndpoint = "/user/get-user-id-by-email-address/company-id/"+ companyId +"/email-address/"+login;
        String id;
        try {
            id = restTemplate.exchange(liferayWebApi + userIdEndpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody().asText();
        } catch (HttpStatusCodeException e) {
            LOGGER.error(e.getMessage());
            return null;
        }

        String endpoint = "/user/get-user-by-id/user-id/"+id;
        JsonNode response = restTemplate.exchange(liferayWebApi + endpoint,
                HttpMethod.GET, new HttpEntity<>(createHeaders()),
                JsonNode.class).getBody();
        UserEntity u = new UserEntity();
//        u.setPassword(response.path("password").asText());
        u.setName(response.path("firstName").asText());
        u.setLogin(response.path("emailAddress").asText());
        u.setId(response.path("userId").asLong());
        return u;
    }

    public UserEntity findUserById(Long id) {
        String endpoint = "/user/get-user-by-id/user-id/"+id;
        JsonNode response;

        try {
            response = restTemplate.exchange(liferayWebApi + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();
        } catch (HttpStatusCodeException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        UserEntity u = new UserEntity();
        u.setId(id);
        u.setName(response.path("firstName").asText());
        u.setLogin(response.path("emailAddress").asText());

        return u;
    }

    public boolean addRole(RoleEntity role){
        return addRole(role, true);
    }


    public boolean addRole(RoleEntity role, boolean prefix){
        String endpoint = "/role/add-role/class-name/com.liferay.portal.kernel.model.Role/class-pk/0/name/" +
                (prefix ? "Atlas " : "") + role.getName()+"/title-map/{}/description-map/{}/type/"+STANDARD_ROLE+"?subtype=";

        try {
            JsonNode responseBody = restTemplate.exchange(liferayWebApi + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();
            long roleId = responseBody.path("roleId").asLong();
            LOGGER.info("Role {} with id {} created", role.getName(), roleId);
            return true;
        } catch (HttpStatusCodeException e){
            LOGGER.error("Error in adding Atlas role {} to Liferay", role.getName());
            return false;
        }
    }

    public boolean updateRole(RoleEntity role) {
        String roleId = getRoleId(role);

        String endpoint = "/role/update-role/role-id/"+roleId+"/name/Atlas "+role.getName()+"/title-map/{}/description-map/{}?subtype=";

        try {
            restTemplate.exchange(liferayWebApi + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();

            return true;
        } catch (HttpStatusCodeException e){
            LOGGER.error("Error in updating Atlas role in Liferay", e);
            return false;
        }
    }

    public boolean deleteUserRole(UserEntity user, RoleEntity role) {
        String roleId = getRoleId(role);

        String endpoint = "/user/has-role-user/role-id/"+roleId+"/user-id/"+user.getId();
        Boolean hasRole = restTemplate.exchange(liferayWebApi + endpoint,
                HttpMethod.GET, new HttpEntity<>(createHeaders()),
                Boolean.class).getBody();
        if(hasRole){
            String deleteEndpoint = "/user/delete-role-user/role-id/"+roleId+"/user-id/"+user.getId();
            try {
                restTemplate.exchange(liferayWebApi + deleteEndpoint,
                        HttpMethod.GET, new HttpEntity<>(createHeaders()),
                        JsonNode.class);
                return true;
            } catch (HttpStatusCodeException e){
                LOGGER.error("Error in deleting user role link ("+user.getLogin()+","+role.getName()+") in Liferay");
                LOGGER.error(e.getMessage(), e);
                return false;
            }
        }
        return false;
    }

    public UserRoleEntity addUserRole(UserEntity user, RoleEntity role) {
        String roleId = getRoleId(role);

        String endpoint = "/role/add-user-roles/user-id/"+user.getId()+"/role-ids/"+roleId;

        try {
            restTemplate.exchange(liferayWebApi + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();

            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setRole(role);
            userRoleEntity.setUser(user);

            return userRoleEntity;

        } catch (HttpStatusCodeException e){
            LOGGER.error("Error in adding user role link ("+user.getLogin()+","+role.getName()+") in Liferay");
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get role names associated with the user. These role names are retrieved from Liferay.
     * @param user
     * @return
     */
    public Set<String> getRoleNamesOfUser(UserEntity user) {
        if(user == null) {
            return Collections.emptySet();
        }

        try {
            Set<String> roleNames = new HashSet<>();

            // Add all user's roles in liferay
            String endpoint = "/role/get-user-roles/user-id/"+user.getId();
            JsonNode response = restTemplate.exchange(liferayWebApi + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();

            for(JsonNode node: response){
                roleNames.add(node.path("name").asText());
            }

            // Add all user's organizations as role
            List<String> groupNames = getUserGroupNamesOfUser(user);
            roleNames.addAll(groupNames);

            // Add all roles related to user's organizations
            List<String> groupIds = getUserGroupIdsOfUser(user);
            for (String group: groupIds) {
                String getGroupRoleEndpoint = "/role/get-group-roles/group-id/"+group;

                JsonNode roleResponse = restTemplate.exchange(liferayWebApi + getGroupRoleEndpoint,
                        HttpMethod.GET, new HttpEntity<>(createHeaders()),
                        JsonNode.class).getBody();
                roleNames.addAll(roleResponse.findValuesAsText("name"));
            }
            return roleNames;
        } catch (HttpStatusCodeException e){
            LOGGER.error("Error in getting the names of the roles associated to the user ("+user.getLogin()+") in Liferay");
            LOGGER.error(e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    public List<String> getRoleNames(){
        String endpoint = "/role/get-roles/company-id/"+ companyId +"/types/"+STANDARD_ROLE;
        try {
            JsonNode response = restTemplate.exchange(liferayWebApi + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();
            return response.findValuesAsText("name");
        } catch (HttpStatusCodeException e){
            LOGGER.error("Error in getting all the roles in Liferay", e);
            return Collections.emptyList();
        }
    }

    public boolean deleteRole(RoleEntity role) {
        String roleId = getRoleId(role);
        String endpoint = "/role/delete-role/role-id/"+roleId;
        try {
           restTemplate.exchange(liferayWebApi + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();
           return true;
        } catch (HttpStatusCodeException e){
            LOGGER.error("Error in deleting role in Liferay", e);
            return false;
        }
    }

    public List<Organization> getOrganizations() {
        String endpoint = "/organization/get-organizations/company-id/" + companyId + "/parent-organization-id/-1";
        try {
            return Arrays.asList(restTemplate.exchange(liferayWebApi + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    Organization[].class).getBody());
        } catch (HttpStatusCodeException e){
            LOGGER.error("Error in getting organizations in Liferay", e);
            return Collections.emptyList();
        }
    }

    private List<String> getUserGroupIdsOfUser(UserEntity userEntity) {
        String endpoint = "/group/get-user-organizations-groups/user-id/"+userEntity.getId()+"?start=-1&end=-1";
        try {
            JsonNode response = restTemplate.exchange(liferayWebApi + endpoint,
            HttpMethod.GET, new HttpEntity<>(createHeaders()),
            JsonNode.class).getBody();

            List<String> listOfIds = new ArrayList<>();
            for(JsonNode node: response){
                listOfIds.add(node.path("groupId").asText());
            }
            return listOfIds;
        } catch (HttpStatusCodeException e){
            LOGGER.error("Error in retrieving groups in Liferay", e);
            return Collections.emptyList();
        }

    }

    private List<String> getUserGroupNamesOfUser(UserEntity userEntity) {
        String endpoint = "/group/get-user-organizations-groups/user-id/"+userEntity.getId()+"?start=-1&end=-1";
        try {
            JsonNode response = restTemplate.exchange(liferayWebApi + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();

            List<String> listOfIds = new ArrayList<>();
            for(JsonNode node: response){
                listOfIds.add(node.path("nameCurrentValue").asText());
            }
            return listOfIds;
        } catch (HttpStatusCodeException e){
            LOGGER.error("Error in retrieving groups in Liferay", e);
            return Collections.emptyList();
        }
    }

    String getRoleId(RoleEntity role) {
        String endpoint = "/role/get-role/company-id/"+ companyId +"/name/Atlas "+role.getName();
        try {
            JsonNode response = restTemplate.exchange(liferayWebApi + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();
            return response.path("roleId").asText();
        } catch (HttpStatusCodeException e){
            LOGGER.error("Error in getting roleId in Liferay", e);
            return "";
        }
    }

    private HttpHeaders createHeaders(){
        return new HttpHeaders() {{
            String auth = liferayServiceUser + ":" + liferayServicePassword;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
        }};
    }


    public int healthCheck() {
        final RestTemplate restTemplate = new RestTemplate();
        String serviceUrl = liferayWebApi;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(serviceUrl, String.class);
            return response.getStatusCode().value();
        } catch (RestClientException e) {
            LOGGER.warn(e.getMessage(), e);
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
    }
}
