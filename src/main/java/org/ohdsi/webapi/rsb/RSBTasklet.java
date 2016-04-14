/*
 * Copyright 2016 fdefalco.
 *
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

import java.util.HashMap;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 *
 * @author fdefalco
 */
public class RSBTasklet implements Tasklet {

  private HashMap parameters;
  private String functionName;
  private String rsbEndpoint;
  
  public RSBTasklet(String functionName, HashMap parameters) {
    rsbEndpoint = "http://hixbeta.jnj.com:8999/rsb/api/rest/jobs";
    this.parameters = parameters;
    this.functionName = functionName;
  }

  @Override
  public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
    Client client = ClientBuilder.newClient();

    // TODO - Configure the RSB API URI
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
        Thread.sleep(3000);
      }
    }

    if (jobStatusResponse == null) {
      return RepeatStatus.FINISHED;
    } else {
      ResultResponse resultResponse = jobStatusResponse.readEntity(ResultResponse.class);
      return RepeatStatus.FINISHED;
    }
  }
};
