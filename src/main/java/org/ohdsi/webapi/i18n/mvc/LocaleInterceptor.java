package org.ohdsi.webapi.i18n.mvc;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.Constants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Spring MVC HandlerInterceptor replacement for Jersey's LocaleFilter.
 * Extracts locale from request headers and sets it in LocaleContextHolder.
 *
 * Migration Status: Replaces /i18n/LocaleFilter.java (JAX-RS ContainerRequestFilter)
 */
@Component
public class LocaleInterceptor implements HandlerInterceptor {

    private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    private static final String LANG_PARAM = "lang";
    private static final String DEFAULT_LOCALE = "en";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Locale locale = resolveLocale(request);
        LocaleContextHolder.setLocale(locale);
        // Store in request attribute for compatibility with existing code
        request.setAttribute("language", locale);
        return true;
    }

    private Locale resolveLocale(HttpServletRequest request) {
        // Priority 1: User-specific language header
        String userHeader = request.getHeader(Constants.Headers.USER_LANGAUGE);
        if (StringUtils.isNotBlank(userHeader)) {
            return Locale.forLanguageTag(userHeader);
        }

        // Priority 2: Query parameter 'lang'
        String langParam = request.getParameter(LANG_PARAM);
        if (StringUtils.isNotBlank(langParam)) {
            return Locale.forLanguageTag(langParam);
        }

        // Priority 3: Accept-Language header
        String acceptLanguage = request.getHeader(ACCEPT_LANGUAGE_HEADER);
        if (StringUtils.isNotBlank(acceptLanguage)) {
            return Locale.forLanguageTag(acceptLanguage);
        }

        // Default
        return Locale.forLanguageTag(DEFAULT_LOCALE);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        // Clean up locale context
        LocaleContextHolder.resetLocaleContext();
    }
}
