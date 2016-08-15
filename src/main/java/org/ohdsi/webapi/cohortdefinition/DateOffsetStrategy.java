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
 */
public class DateOffsetStrategy extends EndStrategy {

  public enum DateField {
    StartDate,
    EndDate
  }
  
  @JsonProperty("DateField")
  public DateField dateField = DateField.StartDate;

  @JsonProperty("Offset")
  public int offset = 0;
  
  @Override
  public String accept(IGetEndStrategySqlDispatcher dispatcher, String eventTable) {
    return dispatcher.getStrategySql(this, eventTable);
  }
}
