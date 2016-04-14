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
package org.ohdsi.webapi.service;

import java.util.HashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.rsb.ClusterCohortRequest;
import org.ohdsi.webapi.rsb.ProxyResult;
import org.springframework.stereotype.Component;
import org.ohdsi.webapi.rsb.RSBTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author fdefalco
 */
@Path("rsb/")
@Component
public class RSBProxyService extends AbstractDaoService {

  @Autowired
  private JobTemplate jobTemplate;

  @Autowired
  private JobBuilderFactory jobFactory;

  @Autowired
  private StepBuilderFactory stepFactory;

  @Path("test")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String test(@PathParam("sourceKey") String sourceKey) {
    return "pass";
  }

  @Path("clustercohort")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public ProxyResult clusterCohort(ClusterCohortRequest request) {
    ProxyResult result = new ProxyResult();

    String functionName = "clusterCohort";
    HashMap<String, Object> parameters = new HashMap();
    parameters.put("cohortDefinitionId", request.cohortDefinitionId);
    parameters.put("executionId", 100);
    parameters.put("sourceKey", request.sourceKey);

    RSBTasklet t = new RSBTasklet(functionName, parameters);

    Step executeRSBStep = stepFactory.get("rsbTask")
            .tasklet(t)
            .build();

    JobParametersBuilder builder = new JobParametersBuilder();
    builder.addString("jobName", "clustering cohort " + request.cohortDefinitionId + " on " + request.sourceKey);
    JobParameters jobParameters = builder.toJobParameters();
    Job generateCohortJob = jobFactory.get("executeRSB")
            .start(executeRSBStep)
            .build();

    JobExecutionResource jer = jobTemplate.launch(generateCohortJob, jobParameters);
    result.status = jer.getStatus();
    result.message = jer.getExecutionId().toString();

    return result;
  }
}
