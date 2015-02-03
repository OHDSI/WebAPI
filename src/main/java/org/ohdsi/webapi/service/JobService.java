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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@Path("/job/")
public class JobService extends AbstractDaoService {
    
    @Autowired
    private String batchTablePrefix;
    
    @Autowired
    private JobExplorer jobExplorer;
    
    @GET
    @Path("{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String status(@PathParam("jobId") final String jobId) {
        final JobInstance job = this.jobExplorer.getJobInstance(Long.valueOf(jobId));
        return job.getJobName();
    }
    
    @GET
    @Path("{jobId}/execution/{executionId}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public String status(@PathParam("jobId") final String jobId, @PathParam("executionId") final String executionId) {
        final JobExecution exec = this.jobExplorer.getJobExecution(Long.valueOf(executionId));
        return exec.getStatus().toString();
    }
}
