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
package org.ohdsi.webapi.feasibility;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@JsonFormat(shape=JsonFormat.Shape.OBJECT)
public enum TheraputicArea {
  // [{ id: 0, name: 'Cardiovascular & Metabolism' }, { id: 1, name: 'Immunology' }, { id: 2, name: 'Infectious Diseases & Vaccines' }, { id: 3, name: 'Neuroscience' }, { id: 4, name: 'Oncology' }]
  CARDIOVASCULAR_METABOLISM(0,"Cardiovascular & Metabolism"),
  IMMUNOLOGY(1,"Immunology"),
  INFECTIOUSDISEASE_VACCINES(2,"Infectious Diseases & Vaccines"),
  NEUROSCIENCE(3,"Neuroscience"),
  ONCOLOGY(4,"Oncology");
  
  
    private final int id;
    private final String name;
 
    private TheraputicArea(final int id, final String name) {
        this.id = id;
        this.name = name;
    }
    
    @JsonProperty("id")
    public int getId() {
        return id;
    }
 
    @JsonProperty("name")
    public String getName() {
        return name;
    }
   /**
     * Gets a MyEnumType from id or <tt>null</tt> if the requested type doesn't exist.
     * @param id String
     * @return MyEnumType
     */
    public static TheraputicArea fromId(final int id) {
      for (TheraputicArea type : TheraputicArea.values()) {
          if (id == type.id) {
              return type;
          }
      }
      return null;
    }    
}
