package org.ohdsi.webapi.shiro.filters;

import io.buji.pac4j.subject.Pac4jPrincipal;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.util.UserUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.ohdsi.webapi.shiro.management.AtlasSecurity.PERMISSIONS_ATTRIBUTE;

public class UpdateAtlasRoleFromTokenFilter extends AdviceFilter {

    private final PermissionManager authorizer;

    public UpdateAtlasRoleFromTokenFilter(
            PermissionManager authorizer) {
        this.authorizer = authorizer;
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        if (!SecurityUtils.getSubject().isAuthenticated()) {
            WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String login;
        final PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
        Object principal = principals.getPrimaryPrincipal();

        if (principal instanceof Pac4jPrincipal) {
            login = ((Pac4jPrincipal) principal).getProfile().getUsername();
            Set<String> principalRoles = ((Pac4jPrincipal) principal).getProfile().getRoles();

            login = UserUtils.toLowerCase(login);
//            try {
//                this.authorizer.removeUserFromAllRole(login);
//                List<String> allRoleNames = StreamSupport.stream(this.authorizer.getRoles(false).spliterator(), false)
//                        .map(roleEntity -> roleEntity.getName())
//                        .collect(Collectors.toList());
//                for (String role : principalRoles) {
//                    if(!allRoleNames.contains(role))
//                        this.authorizer.addRole(role, true);
//                    this.authorizer.addUserToRole(role, login);
//                }
//            } catch (Exception e) {
//                WebUtils.toHttp(response).setHeader("x-auth-error", e.getMessage());
//                throw new Exception(e);
//            }
        } else {
            throw new Exception("Not principal of Pac4jPrincipal type");
        }

        Collection<String> permissions = this.authorizer.getAuthorizationInfo(login).getStringPermissions();
        request.setAttribute(PERMISSIONS_ATTRIBUTE, StringUtils.join(permissions, "|"));
        return true;
    }
}
