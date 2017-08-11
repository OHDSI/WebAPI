/*
 * Copyright 2016 Observational Health Data Sciences and Informatics [OHDSI.org].
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
package org.ohdsi.webapi.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 * @contributor Gowtham Rao <gowthamrao@gmail.com>
 * @contributor Tommy Huynh <tommyhuynh93@gmail.com>
 * @contributor Cedrick Hall <>
 */
public class EventEndDateStrategy extends EndStrategy {

  public enum DateField {
    EventMaxEndDate,
    EventEndDate
  }
  
  @JsonProperty("DateField")
  public DateField dateField = DateField.EventMaxEndDate;
  
  @Override
  public String accept(IGetEndStrategySqlDispatcher dispatcher, String eventTable) {
    return dispatcher.getStrategySql(this, eventTable);
  }
}
