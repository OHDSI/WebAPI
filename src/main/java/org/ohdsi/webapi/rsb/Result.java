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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
  "jobId",
  "applicationName",
  "resultTime",
  "success",
  "type",
  "selfUri",
  "dataUri"
})
public class Result {
  @JsonProperty("jobId")
  private String jobId;
  @JsonProperty("applicationName")
  private String applicationName;
  @JsonProperty("resultTime")
  private String resultTime;
  @JsonProperty("success")
  private String success;
  @JsonProperty("type")
  private String type;
  @JsonProperty("selfUri")
  private String selfUri;
  @JsonProperty("dataUri")
  private String dataUri;

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
   * @return The resultTime
   */
  @JsonProperty("resultTime")
  public String getResultTime() {
    return resultTime;
  }

  /**
   *
   * @param resultTime The resultTime
   */
  @JsonProperty("resultTime")
  public void setResultTime(String resultTime) {
    this.resultTime = resultTime;
  }

  /**
   *
   * @return The success
   */
  @JsonProperty("success")
  public String getSuccess() {
    return success;
  }

  /**
   *
   * @param success The success
   */
  @JsonProperty("success")
  public void setSuccess(String success) {
    this.success = success;
  }

  /**
   *
   * @return The type
   */
  @JsonProperty("type")
  public String getType() {
    return type;
  }

  /**
   *
   * @param type The type
   */
  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  /**
   *
   * @return The selfUri
   */
  @JsonProperty("selfUri")
  public String getSelfUri() {
    return selfUri;
  }

  /**
   *
   * @param selfUri The selfUri
   */
  @JsonProperty("selfUri")
  public void setSelfUri(String selfUri) {
    this.selfUri = selfUri;
  }

  /**
   *
   * @return The dataUri
   */
  @JsonProperty("dataUri")
  public String getDataUri() {
    return dataUri;
  }

  /**
   *
   * @param dataUri The dataUri
   */
  @JsonProperty("dataUri")
  public void setDataUri(String dataUri) {
    this.dataUri = dataUri;
  }

}
