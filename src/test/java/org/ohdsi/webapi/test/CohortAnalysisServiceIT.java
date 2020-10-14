/*
 * The contents of this file are subject to the Regenstrief Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance with the License.
 * Please contact Regenstrief Institute if you would like to obtain a copy of the license.
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) Regenstrief Institute.  All Rights Reserved.
 */
package org.ohdsi.webapi.test;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import java.util.Collections;
import org.junit.Test;
import org.ohdsi.webapi.cohortanalysis.CohortAnalysisTask;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

@DatabaseTearDown(value = "/database/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class CohortAnalysisServiceIT extends WebApiIT {

    @Value("${cohortanalysis.endpoint}")
    private String endpointCohortAnalysis;
    private static final String SOURCE_KEY = "110k";

    @Test
    @DatabaseSetup("/database/source.xml")
    public void canCreateAnalysis() {

        //Arrange
        CohortAnalysisTask task = new CohortAnalysisTask();
        task.setAnalysisIds(Collections.singletonList("0"));
        task.setCohortDefinitionIds(Collections.singletonList("1"));
        task.setSourceKey(SOURCE_KEY);
        
        //Action
        final ResponseEntity<JobExecutionResource> entity = getRestTemplate().postForEntity(this.endpointCohortAnalysis,
                task, JobExecutionResource.class);
        
        //Assert
        assertOK(entity);
    }
}
