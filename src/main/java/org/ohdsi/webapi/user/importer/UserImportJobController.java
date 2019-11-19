package org.ohdsi.webapi.user.importer;

import com.odysseusinc.scheduler.exception.JobNotFoundException;
import org.ohdsi.webapi.user.importer.dto.JobHistoryItemDTO;
import org.ohdsi.webapi.user.importer.dto.UserImportJobDTO;
import org.ohdsi.webapi.user.importer.dto.UserImportJobMappingDTO;
import org.ohdsi.webapi.user.importer.exception.JobAlreadyExistException;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.ohdsi.webapi.user.importer.service.UserImportJobService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.JOB_IS_ALREADY_SCHEDULED;

@RestController
@Path("/user/import/job")
@Transactional
public class UserImportJobController {
  private final UserImportJobService jobService;
  private final GenericConversionService conversionService;

  public UserImportJobController(UserImportJobService jobService, GenericConversionService conversionService) {

    this.jobService = jobService;
    this.conversionService = conversionService;
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public UserImportJobDTO createJob(UserImportJobDTO jobDTO) {

    UserImportJob job = conversionService.convert(jobDTO, UserImportJob.class);
    try {
      UserImportJob created = jobService.createJob(job);
      return conversionService.convert(created, UserImportJobDTO.class);
    } catch(JobAlreadyExistException e) {
      throw new NotAcceptableException(String.format(JOB_IS_ALREADY_SCHEDULED, job.getProviderType()));
    }
  }

  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public UserImportJobMappingDTO updateJob(@PathParam("id") Long jobId, UserImportJobMappingDTO jobDTO) {

    UserImportJob job = conversionService.convert(jobDTO, UserImportJob.class);
    try {
      job.setId(jobId);
      UserImportJob updated = jobService.updateJob(job);
      return conversionService.convert(updated, UserImportJobMappingDTO.class);
    } catch (JobNotFoundException e) {
      throw new NotFoundException();
    }
  }

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public List<UserImportJobDTO> listJobs() {

    return jobService.getJobs().stream()
            .map(job -> conversionService.convert(job, UserImportJobDTO.class))
            .peek(job -> jobService.getLatestHistoryItem(job.getId())
                    .ifPresent(item -> job.setLastExecuted(item.getEndTime())))
            .collect(Collectors.toList());
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public UserImportJobMappingDTO getJob(@PathParam("id") Long id) {

    return jobService.getJob(id).map(job -> conversionService.convert(job, UserImportJobMappingDTO.class))
            .orElseThrow(NotFoundException::new);
  }

  @DELETE
  @Path("/{id}")
  public Response deleteJob(@PathParam("id") Long id) {
    UserImportJob job = jobService.getJob(id).orElseThrow(NotFoundException::new);
    jobService.delete(job);
    return Response.ok().build();
  }

  @GET
  @Path("/{id}/history")
  @Produces(MediaType.APPLICATION_JSON)
  public List<JobHistoryItemDTO> getImportHistory(@PathParam("id") Long id) {

    return jobService.getJobHistoryItems(id)
            .map(item -> conversionService.convert(item, JobHistoryItemDTO.class))
            .collect(Collectors.toList());
  }

}
