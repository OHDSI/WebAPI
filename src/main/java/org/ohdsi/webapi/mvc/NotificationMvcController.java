package org.ohdsi.webapi.mvc;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.job.JobExecutionInfo;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Services related to working with the system notifications
 *
 * @summary Notifications
 */
@RestController
@RequestMapping("/notifications")
@Transactional
public class NotificationMvcController extends AbstractMvcController {
    private static final Logger log = LoggerFactory.getLogger(NotificationMvcController.class);

    private final NotificationService service;
    private final GenericConversionService conversionService;

    public NotificationMvcController(final NotificationService service, @Qualifier("conversionService") GenericConversionService conversionService) {
        this.service = service;
        this.conversionService = conversionService;
    }

    /**
     * Get the list of notifications
     *
     * @summary Get all notifications
     * @param hideStatuses Used to filter statuses - passes as a comma-delimited
     * list
     * @param refreshJobs Boolean - when true, it will refresh the cache
     * of notifications
     * @return
     */
    @GetMapping("/")
    @Transactional(readOnly = true)
    public ResponseEntity<List<JobExecutionResource>> list(
            @RequestParam(value = "hide_statuses", required = false) String hideStatuses,
            @RequestParam(value = "refreshJobs", defaultValue = "FALSE") Boolean refreshJobs) {
        List<BatchStatus> statuses = new ArrayList<>();
        if (StringUtils.isNotEmpty(hideStatuses)) {
            for (String status : hideStatuses.split(",")) {
                try {
                    statuses.add(BatchStatus.valueOf(status));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid argument passed as batch status: {}", status);
                }
            }
        }
        List<JobExecutionInfo> executionInfos;
        if (refreshJobs) {
            executionInfos = service.findRefreshCacheLastJobs();
        } else {
            executionInfos = service.findLastJobs(statuses);
        }
        return ok(executionInfos.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    /**
     * Gets the date when notifications were last viewed
     *
     * @summary Get notification last viewed date
     * @return The date when notifications were last viewed
     */
    @GetMapping("/viewed")
    @Transactional(readOnly = true)
    public ResponseEntity<Date> getLastViewedTime() {
        try {
            return ok(service.getLastViewedTime());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the date when notifications were last viewed
     *
     * @summary Set notification last viewed date
     * @param stamp
     */
    @PostMapping("/viewed")
    public ResponseEntity<Void> setLastViewedTime(@RequestBody Date stamp) {
        try {
            service.setLastViewedTime(stamp);
            return ok();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JobExecutionResource toDTO(JobExecutionInfo entity) {
        return conversionService.convert(entity, JobExecutionResource.class);
    }
}
