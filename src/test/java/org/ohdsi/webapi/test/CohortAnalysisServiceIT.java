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
package org.ohdsi.webapi.test;

import java.util.Arrays;
import org.junit.Test;
import org.ohdsi.webapi.util.SecurityUtils;
import org.ohdsi.webapi.cohortanalysis.CohortAnalysisTask;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

/**
 *
 */
public class CohortAnalysisServiceIT extends WebApiIT {
    
    @Value("${cohortanalysis.endpoint.job}")
    private String endpointCohortAnalysis;
    
    @Test //may not want to always run analyses. TODO inject criteria from properties
    public void createAnalysis() {
        CohortAnalysisTask task = new CohortAnalysisTask();
        //set attributes
        task.setAnalysisIds(Arrays.asList("0"));
        task.setCohortDefinitionIds(Arrays.asList("1"));
        final ResponseEntity<JobExecutionResource> postEntity = getRestTemplate().postForEntity(this.endpointCohortAnalysis,
            task, JobExecutionResource.class);//TODO 409 or other errors prevent deserialization...
        assertOk(postEntity);
        SecurityUtils.sleep(10000);
        postEntity.getBody();
    }
    
    private void assertOk(final ResponseEntity<?> entity) {
        Assert.state(entity.getStatusCode() == HttpStatus.OK);
    }
    
    private void assertJobExecution(final JobExecutionResource execution) {
        Assert.state(execution != null);
        Assert.state(execution.getExecutionId() != null);
        Assert.state(execution.getJobInstanceResource().getInstanceId() != null);
    }
}
