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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 *
 * @author fdefalco
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "jobId",
  "applicationName",
  "submissionTime",
  "applicationResultsUri",
  "resultUri"
})
public class JobToken {

  @JsonProperty("jobId")
  private String jobId;
  @JsonProperty("applicationName")
  private String applicationName;
  @JsonProperty("submissionTime")
  private String submissionTime;
  @JsonProperty("applicationResultsUri")
  private String applicationResultsUri;
  @JsonProperty("resultUri")
  private String resultUri;

  /**
   *
   * @return The jobId
   */
  @JsonProperty("jobId")
  public String getJobId() {
    return jobId;
  }

  /**
   *
   * @param jobId The jobId
   */
  @JsonProperty("jobId")
  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  /**
   *
   * @return The applicationName
   */
  @JsonProperty("applicationName")
  public String getApplicationName() {
    return applicationName;
  }

  /**
   *
   * @param applicationName The applicationName
   */
  @JsonProperty("applicationName")
  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  /**
   *
   * @return The submissionTime
   */
  @JsonProperty("submissionTime")
  public String getSubmissionTime() {
    return submissionTime;
  }

  /**
   *
   * @param submissionTime The submissionTime
   */
  @JsonProperty("submissionTime")
  public void setSubmissionTime(String submissionTime) {
    this.submissionTime = submissionTime;
  }

  /**
   *
   * @return The applicationResultsUri
   */
  @JsonProperty("applicationResultsUri")
  public String getApplicationResultsUri() {
    return applicationResultsUri;
  }

  /**
   *
   * @param applicationResultsUri The applicationResultsUri
   */
  @JsonProperty("applicationResultsUri")
  public void setApplicationResultsUri(String applicationResultsUri) {
    this.applicationResultsUri = applicationResultsUri;
  }

  /**
   *
   * @return The resultUri
   */
  @JsonProperty("resultUri")
  public String getResultUri() {
    return resultUri;
  }

  /**
   *
   * @param resultUri The resultUri
   */
  @JsonProperty("resultUri")
  public void setResultUri(String resultUri) {
    this.resultUri = resultUri;
  }

}
