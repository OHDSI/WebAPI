package org.ohdsi.webapi;

import org.ohdsi.webapi.i18n.mvc.LocaleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC Configuration.
 * Jersey has been removed - Spring MVC now serves all endpoints.
 *
 * Spring MVC endpoints are served at: /WebAPI/* (via server.context-path=/WebAPI)
 *
 * NOTE: We don't use @EnableWebMvc because:
 * - Spring Boot auto-configures Spring MVC by default
 * - @EnableWebMvc would disable Spring Boot's auto-configuration
 * - This would conflict with existing I18nConfig (duplicate localeResolver bean)
 * - We only need to customize specific aspects, not override everything
 *
 * NOTE: We don't need a custom ServletRegistrationBean because:
 * - Spring Boot's default DispatcherServlet already serves at context-path + /*
 * - With server.context-path=/WebAPI, it automatically serves /WebAPI/*
 * - @ComponentScan in WebApi.java finds controllers in org.ohdsi.webapi.mvc.controller
 *
 * @see org.ohdsi.webapi.I18nConfig
 * @see org.ohdsi.webapi.WebApi
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired(required = false)
    private LocaleInterceptor localeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Add locale interceptor if available (replaces Jersey LocaleFilter)
        if (localeInterceptor != null) {
            registry.addInterceptor(localeInterceptor)
                    .addPathPatterns("/**");
        }
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Add custom OutputStreamMessageConverter (replaces Jersey's OutputStreamWriter)
        // Spring Boot already configures Jackson converter, so we just extend the list
        converters.add(new org.ohdsi.webapi.mvc.OutputStreamMessageConverter());
    }
}
