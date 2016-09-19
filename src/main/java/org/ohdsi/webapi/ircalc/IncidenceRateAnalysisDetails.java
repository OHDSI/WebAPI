/*
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
package org.ohdsi.webapi.ircalc;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

/**
 *
 * Stores the LOB/CLOB portion of the cohort definition expression.
 */
@Entity(name = "IncidenceRateAnalysisDetails")
@Table(name="ir_analysis_details")
public class IncidenceRateAnalysisDetails implements Serializable {

  private static final long serialVersionUID = 1L;
  
  @Id
  private Integer id;
  
  @MapsId
  @OneToOne
  @JoinColumn(name="id")
  private IncidenceRateAnalysis analysis;
 
  @Lob
  @Type(type = "org.hibernate.type.TextType")  
  private String expression;

  protected IncidenceRateAnalysisDetails() {}
  
  public IncidenceRateAnalysisDetails(IncidenceRateAnalysis analysis) {
    this.analysis = analysis;
  }
  
  public String getExpression() {
    return expression;
  }
  public IncidenceRateAnalysisDetails setExpression(String expression) {
    this.expression = expression;
    return this;
  }
    
  public IncidenceRateAnalysis getAnalysis() {
    return this.analysis;
  }
}
