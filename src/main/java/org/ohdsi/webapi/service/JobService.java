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
package org.ohdsi.webapi.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.job.JobResource;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@Path("/job")
public class JobService {
    
    private static final Log log = LogFactory.getLog(JobService.class);
    
    @Autowired
    JobLauncher jobLauncher;
    
    @Autowired
    private org.springframework.batch.core.Job batchJob;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JobResource submitJob() throws Exception {
        // escape single quote for queries
        //schedule/launch with Spring Batch?
        //-Will have to add our Job to the context (spring will then serialize/deserialize)
        JobExecution exec = this.jobLauncher.run(batchJob, new JobParameters());
        log.info("Status: " + exec.getStatus());
        JobResource job = new JobResource(exec.getJobId());
        return job;
    }
}
