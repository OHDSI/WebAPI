package org.ohdsi.webapi.mvc.controller;

import org.ohdsi.webapi.info.Info;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ohdsi.webapi.info.InfoService;

/**
 * Spring MVC version of InfoService
 *
 * Migration Status: Replaces /info/InfoService.java (Jersey)
 * Endpoints: 1 GET endpoint
 * Complexity: Simple - read-only, no parameters
 *
 * Note: This controller delegates to the existing Jersey InfoService to get the Info object.
 * This allows us to avoid duplicate dependency injection issues with BuildProperties/BuildInfo
 * while still providing the same endpoint via Spring MVC.
 */
@RestController
@RequestMapping("/info")
public class InfoMvcController extends AbstractMvcController {

    @Autowired
    private InfoService infoService;

    /**
     * Get info about the WebAPI instance
     *
     * Jersey: GET /WebAPI/info/
     * Spring MVC: GET /WebAPI/v2/info/
     */
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Info> getInfo() {
        // Delegate to the existing InfoService which is already configured
        // with BuildProperties and BuildInfo
        Info info = infoService.getInfo();
        return ok(info);
    }
}
