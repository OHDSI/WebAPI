package com.jnj.honeur.webapi.shiro.filters;

import org.apache.shiro.web.servlet.AdviceFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

public class HoneurOriginFilter extends AdviceFilter {

    private Pattern honeurRequestOriginPattern;

    public HoneurOriginFilter(String honeurRequestOrigin) {
        this.honeurRequestOriginPattern = Pattern.compile(honeurRequestOrigin);
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) {
        if(request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest)request;
            return honeurRequestOriginPattern.matcher(httpServletRequest.getRequestURL()).matches();
        }
        return false;
    }

}
