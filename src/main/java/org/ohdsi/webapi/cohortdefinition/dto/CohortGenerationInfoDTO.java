/*
 * Copyright 2015 Observational Health Data Sciences and Informatics [OHDSI.org].
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
package org.ohdsi.webapi.cohortdefinition.dto;

import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfoId;
import org.ohdsi.webapi.user.dto.UserDTO;

import java.util.Date;

/**
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class CohortGenerationInfoDTO {
    private CohortGenerationInfoId id;

    private Date startTime;

    private Integer executionDuration;

    private GenerationStatus status;

    private boolean isValid;

    private boolean isCanceled;

    private String failMessage;

    private Long personCount;

    private Long recordCount;

    private UserDTO createdBy;

    public CohortGenerationInfoId getId() {
        return id;
    }

    public void setId(CohortGenerationInfoId id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Integer getExecutionDuration() {
        return executionDuration;
    }

    public void setExecutionDuration(Integer executionDuration) {
        this.executionDuration = executionDuration;
    }

    public GenerationStatus getStatus() {
        return status;
    }

    public void setStatus(GenerationStatus status) {
        this.status = status;
    }

    public boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    public boolean getIsCanceled() {
        return isCanceled;
    }

    public void setIsCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

    public Long getPersonCount() {
        return personCount;
    }

    public void setPersonCount(Long personCount) {
        this.personCount = personCount;
    }

    public Long getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Long recordCount) {
        this.recordCount = recordCount;
    }

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }
}
