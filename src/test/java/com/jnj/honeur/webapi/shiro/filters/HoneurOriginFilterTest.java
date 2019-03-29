package com.jnj.honeur.webapi.shiro.filters;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HoneurOriginFilterTest {

    private HoneurOriginFilter filter = new HoneurOriginFilter("^(https|http)://webapi:.*/webapi.*");;
    private HttpServletRequest request;
    private ServletResponse response = mock(ServletResponse.class);

    @Before
    public void setup() {
        request = mock(HttpServletRequest.class);
    }

    @Test
    public void preHandleMatch() {
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://webapi:8080/webapi/hss/token"));
        assertTrue(filter.preHandle(request, response));
    }

    @Test
    public void preHandleNoMatch() {
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://zeppelin-dev:8080/webapi/hss/token"));
        assertFalse(filter.preHandle(request, response));
    }


    @Test
    public void preHandleMatchAll() {
        HoneurOriginFilter filter = new HoneurOriginFilter(".*");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://zeppelin-dev:8080/webapi/hss/token"));
        assertTrue(filter.preHandle(request, response));
    }

    @Test
    public void preHandleNoHttpServletRequest() {
        ServletRequest request = mock(ServletRequest.class);
        assertFalse(filter.preHandle(request, response));
    }
}