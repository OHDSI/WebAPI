/**
 * The contents of this file are subject to the Regenstrief Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance with the License.
 * Please contact Regenstrief Institute if you would like to obtain a copy of the license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) Regenstrief Institute.  All Rights Reserved.
 */
package org.ohdsi.webapi.exampleapplication;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.job.JobResource;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@Path("/example")
public class ExampleApplicationWithJobService extends AbstractDaoService {
    
    private static final Log log = LogFactory.getLog(ExampleApplicationWithJobService.class);
    
    @Autowired
    JobLauncher jobLauncher;
    
    @Autowired
    private org.springframework.batch.core.Job batchJob;
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public JobResource queueJob() throws Exception {
        JobExecution exec = this.jobLauncher.run(batchJob, new JobParameters());
        log.info("Status: " + exec.getStatus());
        JobResource job = new JobResource(exec.getJobId(), exec.getId());
        return job;
    }
    
}
