/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.rsb;

import java.util.Date;
import java.util.HashMap;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.ohdsi.webapi.util.SecurityUtils;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisExecution;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisExecutionRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * @author fdefalco - <fdefalco@ohdsi.org>
 */

public class RSBTasklet implements Tasklet {

  private HashMap parameters;
  private String functionName;
  private String rServiceHost;
  private int executionId;
  private final ComparativeCohortAnalysisExecutionRepository ccaeRepository;

  public RSBTasklet(ComparativeCohortAnalysisExecutionRepository repository) {
    ccaeRepository = repository;
  }

  public String getRServiceHost() {
    return rServiceHost;
  }

  public void setRServiceHost(String rServiceHost) {
    this.rServiceHost = rServiceHost;
  }

  public HashMap getParameters() {
    return parameters;
  }

  public void setParameters(HashMap parameters) {
    this.parameters = parameters;
  }

  public String getFunctionName() {
    return functionName;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  public int getExecutionId() {
    return executionId;
  }

  public void setExecutionId(int executionId) {
    this.executionId = executionId;
  }

  @Override
  public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) {
    String rsbEndpoint = rServiceHost + "rsb/api/rest/jobs";
    
    Client client = ClientBuilder.newClient();

    WebTarget jobTarget = client.target(rsbEndpoint);
    org.ohdsi.webapi.rsb.Job rsbJob = new Job(this.functionName);
    rsbJob.parameters.putAll(parameters);

    Response rsbJobResponse = jobTarget
            .request(MediaType.APPLICATION_JSON_TYPE)
            .accept(MediaType.WILDCARD_TYPE)
            .accept("application/vnd.rsb+json")
            .header("X-RSB-Application-Name", "testing")
            .post(Entity.json(rsbJob.parameters));

    JobResponse jobResponse = rsbJobResponse.readEntity(JobResponse.class);
    boolean jobCompleted = false;
    Response jobStatusResponse = null;
    WebTarget jobStatusTarget = client.target(jobResponse.getJobToken().getApplicationResultsUri())
            .path(jobResponse.getJobToken().getJobId());

    ComparativeCohortAnalysisExecution ccae = ccaeRepository.findOne(executionId);
    ccae.setExecutionStatus(ComparativeCohortAnalysisExecution.status.RUNNING);
    ccaeRepository.save(ccae);
    
    while (!jobCompleted) {
      jobStatusResponse = jobStatusTarget
              .request()
              .accept("application/vnd.rsb+json")
              .get();

      int status = jobStatusResponse.getStatus();
      
      if (status != 404) {
        jobCompleted = true;
      } else {
        // repeat polling delay
        SecurityUtils.sleep(5000);
      }
    }
    
    ccae.setExecutionStatus(ComparativeCohortAnalysisExecution.status.COMPLETED);
    Date timestamp = new Date();
    int seconds = (int) ((timestamp.getTime() - ccae.getExecuted().getTime()) / 1000);
    ccae.setDuration(seconds);
    
    ccaeRepository.save(ccae);

    if (jobStatusResponse == null) {
      return RepeatStatus.FINISHED;
    } else {
      ResultResponse resultResponse = jobStatusResponse.readEntity(ResultResponse.class);
      return RepeatStatus.FINISHED;
    }
  }
}
