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
package org.ohdsi.webapi;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Converter(autoApply = true)
public class GenerationStatusConverter implements AttributeConverter<GenerationStatus, Integer>{

  @Override
  public Integer convertToDatabaseColumn(GenerationStatus status) {
    switch (status) {
      case ERROR:
        return -1;
      case PENDING:
        return 0;
      case RUNNING:
        return 1;
      case COMPLETE:
        return 2;
      default:
        throw new IllegalArgumentException("Unknown value: " + status);
    }
  }

  @Override
  public GenerationStatus convertToEntityAttribute(Integer statusValue) {
    switch (statusValue) {
      case -1: return GenerationStatus.ERROR;
      case 0: return GenerationStatus.PENDING;
      case 1: return GenerationStatus.RUNNING;
      case 2: return GenerationStatus.COMPLETE;
      default: throw new IllegalArgumentException("Unknown status value: " + statusValue);
    }
  }
  
}
