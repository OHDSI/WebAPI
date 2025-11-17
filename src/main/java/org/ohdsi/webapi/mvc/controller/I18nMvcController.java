package org.ohdsi.webapi.mvc.controller;

import com.google.common.collect.ImmutableList;
import org.ohdsi.webapi.i18n.I18nService;
import org.ohdsi.webapi.i18n.LocaleDTO;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Spring MVC version of I18nController
 *
 * Migration Status: Replaces /i18n/I18nController.java (Jersey)
 * Endpoints: 2 GET endpoints
 * Complexity: Simple - i18n resource handling
 *
 * Note: Original used @Controller with JAX-RS annotations (mixed framework)
 * This is pure Spring MVC implementation
 */
@RestController
@RequestMapping("/i18n")
public class I18nMvcController extends AbstractMvcController {

    @Value("${i18n.enabled}")
    private boolean i18nEnabled = true;

    @Value("${i18n.defaultLocale}")
    private String defaultLocale = "en";

    @Autowired
    private I18nService i18nService;

    /**
     * Get i18n resources for current locale
     *
     * Jersey: GET /WebAPI/i18n/
     * Spring MVC: GET /WebAPI/v2/i18n/
     *
     * Note: Locale is resolved by LocaleInterceptor and stored in LocaleContextHolder
     */
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getResources() {
        // Get locale from LocaleContextHolder (set by LocaleInterceptor)
        Locale locale = LocaleContextHolder.getLocale();

        if (!this.i18nEnabled || locale == null || !isLocaleSupported(locale.getLanguage())) {
            locale = Locale.forLanguageTag(defaultLocale);
        }

        String messages = i18nService.getLocaleResource(locale);
        return ok(messages);
    }

    /**
     * Get list of available locales
     *
     * Jersey: GET /WebAPI/i18n/locales
     * Spring MVC: GET /WebAPI/v2/i18n/locales
     */
    @GetMapping(value = "/locales", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LocaleDTO>> getAvailableLocales() {
        if (this.i18nEnabled) {
            return ok(i18nService.getAvailableLocales());
        }

        // if i18n is disabled, then return only default locale
        return ok(ImmutableList.of(new LocaleDTO(this.defaultLocale, null, true)));
    }

    private boolean isLocaleSupported(String code) {
        return i18nService.getAvailableLocales().stream().anyMatch(l -> Objects.equals(code, l.getCode()));
    }
}
