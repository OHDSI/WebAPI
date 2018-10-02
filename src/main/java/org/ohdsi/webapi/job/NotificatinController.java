package org.ohdsi.webapi.job;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/notifications")
@Controller
@Transactional
public class NotificatinController {

    private final NotificationService service;

    NotificatinController(final NotificationService service) {
        this.service = service;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<JobExecutionResource> list() {
        return service.findAll();
    }
}
