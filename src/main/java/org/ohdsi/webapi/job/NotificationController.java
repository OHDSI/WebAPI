package org.ohdsi.webapi.job;

import org.springframework.batch.core.JobExecution;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/notifications")
@Controller
@Transactional
public class NotificationController {

    private final NotificationService service;
    private final GenericConversionService conversionService;

    NotificationController(final NotificationService service, GenericConversionService conversionService) {
        this.service = service;
        this.conversionService = conversionService;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<JobExecutionResource> list() {
        return service.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private JobExecutionResource toDTO(JobExecution entity) {
        return conversionService.convert(entity, JobExecutionResource.class);
    }
}
