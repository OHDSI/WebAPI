package com.jnj.honeur.webapi.liferay;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.binary.Base64;
import com.jnj.honeur.webapi.liferay.model.Organization;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRoleEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class LiferayApiClient extends RestTemplate {

    @Value("${datasource.liferay.url}")
    private String liferayBaseUrl;
    @Value("${datasource.liferay.user}")
    private String liferayServiceUser;
    @Value("${datasource.liferay.password}")
    private String liferayServicePassword;

    private String LIFERAY_WEB_API;
    private static final String STANDARD_ROLE = "1";
    private static final String COMPANY_ID = "20116";

    private static RestTemplate restTemplate;

    Logger logger = Logger.getLogger(LiferayApiClient.class.getName());

    public LiferayApiClient(){
        restTemplate = new RestTemplate();
    }

    @PostConstruct
    private void initializeLiferayUrl(){
        LIFERAY_WEB_API = liferayBaseUrl +"/api/jsonws";
    }

    public UserEntity findUserByLogin(String login) {
        String userIdEndpoint = "/user/get-user-id-by-email-address/company-id/"+COMPANY_ID+"/email-address/"+login;
        String id;
        try {
            id = restTemplate.exchange(LIFERAY_WEB_API + userIdEndpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody().asText();
        } catch (HttpStatusCodeException e){
            return null;
        }

        String endpoint = "/user/get-user-by-id/user-id/"+id;
        JsonNode response = restTemplate.exchange(LIFERAY_WEB_API + endpoint,
                HttpMethod.GET, new HttpEntity<>(createHeaders()),
                JsonNode.class).getBody();
        UserEntity u = new UserEntity();
        u.setPassword(response.path("password").asText());
        u.setName(response.path("firstName").asText());
        u.setLogin(response.path("emailAddress").asText());
        u.setId(response.path("userId").asLong());
        return u;
    }

    public UserEntity findUserById(Long id) {
        String endpoint = "/user/get-user-by-id/user-id/"+id;
        JsonNode response;

        try {
            response = restTemplate.exchange(LIFERAY_WEB_API + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();
        } catch (HttpStatusCodeException e){
            return null;
        }
        UserEntity u = new UserEntity();
        u.setName(response.path("firstName").asText());
        u.setLogin(response.path("emailAdress").asText());
        u.setId(id);
        return u;
    }

    public RoleEntity addRole(RoleEntity role){
        return addRole(role, true);
    }


    public RoleEntity addRole(RoleEntity role, boolean prefix){
        String endpoint = "/role/add-role/class-name/com.liferay.portal.kernel.model.Role/class-pk/0/name/" +
                (prefix ? "Atlas " : "") + role.getName()+"/title-map/{}/description-map/{}/type/"+STANDARD_ROLE+"?subtype=";

        try {
            restTemplate.exchange(LIFERAY_WEB_API + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();
        } catch (HttpStatusCodeException e){
            logger.log(Level.INFO, "Error in adding Atlas role to Liferay: " + e.toString());
        }

        return role;
    }

    public RoleEntity updateRole(RoleEntity role) {
        String roleId = getRoleId(role);

        String endpoint = "/role/update-role/role-id/"+roleId+"/name/Atlas "+role.getName()+"/title-map/{}/description-map/{}?subtype=";

        try {
            restTemplate.exchange(LIFERAY_WEB_API + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();
        } catch (HttpStatusCodeException e){
            logger.log(Level.INFO, "Error in updating Atlas role in Liferay: " + e.toString());
        }

        return role;
    }

    public void deleteUserRole(UserEntity user, RoleEntity role) {
        String roleId = getRoleId(role);

        String endpoint = "/user/has-role-user/role-id/"+roleId+"/user-id/"+user.getId();
        Boolean hasRole = restTemplate.exchange(LIFERAY_WEB_API + endpoint,
                HttpMethod.GET, new HttpEntity<>(createHeaders()),
                Boolean.class).getBody();
        if(hasRole){
            String deleteEndpoint = "/user/delete-role-user/role-id/"+roleId+"/user-id/"+user.getId();
            try {
                restTemplate.exchange(LIFERAY_WEB_API + deleteEndpoint,
                        HttpMethod.GET, new HttpEntity<>(createHeaders()),
                        JsonNode.class);
            } catch (HttpStatusCodeException e){
                logger.log(Level.INFO, "Error in deleting user role link ("+user.getLogin()+","+role.getName()+") in Liferay: " + e.toString());
            }
        }
    }
    public UserRoleEntity addUserRole(UserEntity user, RoleEntity role) {
        String roleId = getRoleId(role);

        String endpoint = "/role/add-user-roles/user-id/"+user.getId()+"/role-ids/"+roleId;

        try {
            restTemplate.exchange(LIFERAY_WEB_API + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();
        } catch (HttpStatusCodeException e){
            logger.log(Level.INFO, "Error in adding user role link ("+user.getLogin()+","+role.getName()+") in Liferay: " + e.toString());
        }

        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setRole(role);
        userRoleEntity.setUser(user);
        return userRoleEntity;
    }

    public Set<String> getRoleNamesOfUser(UserEntity user) {
        if(user == null) {
            return Collections.emptySet();
        }

        Set<String> roleNames = new HashSet<>();
        try {
            String endpoint = "/role/get-user-roles/user-id/"+user.getId();
            JsonNode response = restTemplate.exchange(LIFERAY_WEB_API + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();

            for(JsonNode node: response){
                String roleId = node.path("roleId").asText();
                String getRoleEndpoint = "/role/get-role/role-id/"+roleId;

                JsonNode roleResponse = restTemplate.exchange(LIFERAY_WEB_API + getRoleEndpoint,
                        HttpMethod.GET, new HttpEntity<>(createHeaders()),
                        JsonNode.class).getBody();

                roleNames.add(roleResponse.path("name").asText());
            }

            //TODO: Not yet working
            List<String> groups = getUserGroupIdsOfUser(user);
            for (String group: groups) {
                String getGroupRoleEndpoint = "/role/get-group-roles/group-id/"+group;

                JsonNode roleResponse = restTemplate.exchange(LIFERAY_WEB_API + getGroupRoleEndpoint,
                        HttpMethod.GET, new HttpEntity<>(createHeaders()),
                        JsonNode.class).getBody();
                roleNames.addAll(roleResponse.findValuesAsText("name"));
            }
        } catch (HttpStatusCodeException e){
            logger.log(Level.INFO, "Error in getting the names of the roles associated to the user ("+user.getLogin()+") in Liferay: " + e.toString());
        }
        return roleNames;
    }

    public List<String> getRoleNames(){
        String endpoint = "/role/get-roles/company-id/"+COMPANY_ID+"/types/"+STANDARD_ROLE;
        try {
            JsonNode response = restTemplate.exchange(LIFERAY_WEB_API + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();
            return response.findValuesAsText("name");
        } catch (HttpStatusCodeException e){
            logger.log(Level.INFO, "Error in getting all the roles in Liferay: " + e.toString());
        }
        return new ArrayList<>();
    }

    public void deleteRole(RoleEntity role) {
        String roleId = getRoleId(role);
        String endpoint = "/role/delete-role/role-id/"+roleId;
        try {
           restTemplate.exchange(LIFERAY_WEB_API + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();
        } catch (HttpStatusCodeException e){
            logger.log(Level.INFO, "Error in deleting role in Liferay: " + e.toString());
        }
    }

    public List<Organization> getOrganizations() {
        String endpoint = "/organization/get-organizations/company-id/20116/parent-organization-id/-1";
        List<Organization> organizations = new ArrayList<>();
        try {
            organizations = Arrays.asList(restTemplate.exchange(LIFERAY_WEB_API + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    Organization[].class).getBody());
        } catch (HttpStatusCodeException e){
            logger.log(Level.INFO, "Error in getting organizations in Liferay: " + e.toString());
        }
        return organizations;

    }

    private List<String> getUserGroupIdsOfUser(UserEntity userEntity) {
        String endpoint = "/group/get-user-organizations-groups/user-id/"+userEntity.getId()+"?start=-1&end=-1";
        JsonNode response;
        try {
            response = restTemplate.exchange(LIFERAY_WEB_API + endpoint,
            HttpMethod.GET, new HttpEntity<>(createHeaders()),
            JsonNode.class).getBody();
        } catch (HttpStatusCodeException e){
            logger.log(Level.INFO, "Error in retrieving groups in Liferay: " + e.toString());
            return new ArrayList<>();
        }
        List<String> listOfIds = new ArrayList<>();
        for(JsonNode node: response){
            listOfIds.add(node.path("groupId").asText());
        }
        return listOfIds;
    }

    private String getRoleId(RoleEntity role) {
        String endpoint = "/role/get-role/company-id/"+COMPANY_ID+"/name/Atlas "+role.getName();
        try {
            JsonNode response = restTemplate.exchange(LIFERAY_WEB_API + endpoint,
                    HttpMethod.GET, new HttpEntity<>(createHeaders()),
                    JsonNode.class).getBody();
            return response.path("roleId").asText();
        } catch (HttpStatusCodeException e){
            logger.log(Level.INFO, "Error in getting roleId in Liferay: " + e.toString());
        }
        return "";
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
}
