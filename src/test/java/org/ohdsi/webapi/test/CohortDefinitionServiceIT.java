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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ohdsi.webapi.model.CohortDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 */
public class CohortDefinitionServiceIT extends WebApiIT {
    
    @Value("${cohortdefinitionservice.endpoint.cohortdefinitions}")
    private String endpoint;
    
    @Test
    public void cohortDefinitions() {
        log.info("Testing cohortDefinition endpoint");
        final ResponseEntity<String> entity = getRestTemplate().getForEntity(this.endpoint, String.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        log.debug("Body:" + entity.getBody());
        //or
        CohortDefinition[] cohortDefinitions = getRestTemplate().getForObject(this.endpoint, CohortDefinition[].class);
        for (CohortDefinition c : cohortDefinitions) {
            log.debug("CohortDefinition:" + c.getCohortDefinitionName());
        }
    }
}
