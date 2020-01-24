package org.ohdsi.webapi.job;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Path("/notifications")
@Controller
@Transactional
public class NotificationController {
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService service;
    private final GenericConversionService conversionService;

    NotificationController(final NotificationService service, GenericConversionService conversionService) {
        this.service = service;
        this.conversionService = conversionService;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(readOnly = true)
    public List<JobExecutionResource> list(
            @QueryParam("hide_statuses") String hideStatuses) {
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
        return service.findLast10(statuses).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @GET
    @Path("/viewed")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(readOnly = true)
    public Date getLastViewedTime() {
        try {
            return service.getLastViewedTime();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/viewed")
    @Produces(MediaType.APPLICATION_JSON)
    public void setLastViewedTime(Date stamp) {
        try {
            service.setLastViewedTime(stamp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JobExecutionResource toDTO(JobExecution entity) {
        return conversionService.convert(entity, JobExecutionResource.class);
    }
}
