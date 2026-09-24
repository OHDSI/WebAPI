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
package org.ohdsi.webapi.cohortdefinition;

import java.util.List;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class InclusionRuleReport {
  
  public static class Summary {
    public long baseCount;
    public long finalCount;
    public long lostCount;
    public String percentMatched;
  }
  
  public static class InclusionRuleStatistic
  {
      public int id;
      public String name;
      public String percentExcluded;
      public String percentSatisfying;
      public long countSatisfying;
  }
  
  public Summary summary;
  public List<InclusionRuleStatistic> inclusionRuleStats;
  public String treemapData;
  
}
