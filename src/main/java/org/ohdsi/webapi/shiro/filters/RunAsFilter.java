package org.ohdsi.webapi.shiro.filters;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

public class RunAsFilter extends AdviceFilter {

  private UserRepository userRepository;

  public RunAsFilter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {

    final String runAs = request.getParameter("login");
    if (StringUtils.isNotBlank(runAs)) {
      UserEntity userEntity = userRepository.findByLogin(runAs);
      if (Objects.isNull(userEntity)) {
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return false;
      }
      AuthenticationInfo authInfo = new SimpleAuthenticationInfo(runAs, null, "runAs");
      SecurityUtils.getSubject().runAs(authInfo.getPrincipals());
    }
    return true;
  }
}
