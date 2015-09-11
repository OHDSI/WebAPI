package org.ohdsi.webapi.service;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.ohdsi.webapi.shiro.Entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import waffle.windows.auth.IWindowsAccount;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by GMalikov on 20.08.2015.
 */
@Path("/test/")
@Component
public class TestService {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public RoleRepository roleRepository;

    @Autowired
    public PermissionRepository permissionRepository;

    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean getTestResponse() {
//        Subject currentUser = SecurityUtils.getSubject();
//        WindowsAuthProviderImpl waffle = new WindowsAuthProviderImpl();
//        IWindowsAccount test = waffle.lookupAccount(waffle.getCurrentComputer().getComputerName());
//        UsernamePasswordToken token = new UsernamePasswordToken("Login","Password");
//        currentUser.login(token);
//    return currentUser.isAuthenticated();
        Subject currentUser = SecurityUtils.getSubject();
        UserEntity secondEntity = userRepository.findByLogin("testLogin");
        RoleEntity roleEntity = roleRepository.findById(1L);
        PermissionEntity permissionEntity = permissionRepository.findById(1L);
        return currentUser.isAuthenticated();
    }

    @POST
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String shiroLogin(@FormParam("login") String login, @FormParam("password") String password) {
        Subject currentUser = SecurityUtils.getSubject();
        if (!currentUser.isAuthenticated()) {
            UsernamePasswordToken token = new UsernamePasswordToken(login, password);

            try {
                currentUser.login(token);
            } catch (UnknownAccountException uae) {
                return "Unknown account";
            } catch (IncorrectCredentialsException ice) {
                return "Incorrect credentials";
            } catch (AuthenticationException ae) {
                return "Authentication failed";
            }
            return "User successfully logged";
        }
        return "User already authenticated";
    }

    @GET
    @Path("roleCheck")
    @Produces(MediaType.APPLICATION_JSON)
    public String roleCheck() {
        Subject currentUser = SecurityUtils.getSubject();
        if(!currentUser.isAuthenticated()){
            return "Action forbidden for unauthenticated users.";
        } else if (currentUser.hasRole("Admin")){
            return "Success!";
        } else{
            return "This action allowed only for Admin";
        }
    }

    @GET
    @Path("permissionCheck")
    @Produces(MediaType.APPLICATION_JSON)
    public String permissionCheck() {
        Subject currentUser = SecurityUtils.getSubject();
        if(!currentUser.isAuthenticated()){
            return "Action forbidden for unauthenticated users.";
        } else if (currentUser.isPermitted("edit vocabulary")){
            return "Success!";
        } else {
            return "Action is not authorized for current account";
        }
    }

    @POST
    @Path("logout")
    @Produces(MediaType.APPLICATION_JSON)
    public String shiroLogout() {
        Subject currentUser = SecurityUtils.getSubject();
        if(currentUser.isAuthenticated()){
            currentUser.logout();
            return "User logged out.";
        }
        return "User is not authenticated. Unable to logout";
    }
}
