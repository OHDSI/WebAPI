package org.ohdsi.webapi.mvc.controller;

import org.ohdsi.webapi.activity.Tracker;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring MVC version of ActivityService
 *
 * Migration Status: Replaces /service/ActivityService.java (Jersey)
 * Endpoints: 1 GET endpoint
 * Complexity: Simple - deprecated, read-only
 *
 * @deprecated Example REST service - will be deprecated in a future release
 */
@RestController
@RequestMapping("/activity")
@Deprecated
public class ActivityMvcController extends AbstractMvcController {

    /**
     * Get latest activity
     *
     * Jersey: GET /WebAPI/activity/latest
     * Spring MVC: GET /WebAPI/v2/activity/latest
     *
     * @deprecated DO NOT USE - will be removed in future release
     */
    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    public ResponseEntity<Object[]> getLatestActivity() {
        return ok(Tracker.getActivity());
    }
}
