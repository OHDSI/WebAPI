/*
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
package org.ohdsi.webapi.cohortcomparison;

import java.util.ArrayList;

/**
 * @author fdefalco <fdefalco@ohdsi.org>
 */
public class PropensityScoreModelReport {
   
  private float auc;
  private ArrayList<PropensityScoreModelCovariate> covariates;

  public PropensityScoreModelReport() {
    covariates = new ArrayList<>();
  }
    
  public float getAuc() {
    return auc;
  }

  public void setAuc(float auc) {
    this.auc = auc;
  }

  public ArrayList<PropensityScoreModelCovariate> getCovariates() {
    return covariates;
  }

  public void setCovariates(ArrayList<PropensityScoreModelCovariate> covariates) {
    this.covariates = covariates;
  }
}
