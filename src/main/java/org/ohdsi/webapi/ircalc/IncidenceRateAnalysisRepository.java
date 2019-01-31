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
package org.ohdsi.webapi.ircalc;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public interface IncidenceRateAnalysisRepository extends CrudRepository<IncidenceRateAnalysis, Integer> {
  
  @Query("SELECT ira FROM IncidenceRateAnalysis AS ira LEFT JOIN FETCH ira.details AS d")          
  Iterable<IncidenceRateAnalysis> findAll();

  @Query("SELECT ira FROM IncidenceRateAnalysis AS ira LEFT JOIN FETCH ira.executionInfoList e WHERE ira.id = ?1 AND e.source.deletedDate = NULL")
  IncidenceRateAnalysis findOneWithExecutionsOnExistingSources(int id);

  @Query("SELECT ira FROM IncidenceRateAnalysis AS ira LEFT JOIN FETCH ira.executionInfoList e WHERE ira.id = ?1")
  IncidenceRateAnalysis findOneWithExecutionsOnAllSources(int id);
}