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

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphCrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public interface IncidenceRateAnalysisRepository extends EntityGraphCrudRepository<IncidenceRateAnalysis, Integer> {
  
  @Query("SELECT ira FROM IncidenceRateAnalysis AS ira LEFT JOIN FETCH ira.details AS d")          
  Iterable<IncidenceRateAnalysis> findAll();

  @Query("SELECT ira FROM IncidenceRateAnalysis AS ira LEFT JOIN ira.executionInfoList e LEFT JOIN Source s ON s.id = e.source.id AND s.deletedDate = NULL WHERE ira.id = ?1")
  IncidenceRateAnalysis findOneWithExecutionsOnExistingSources(int id, EntityGraph entityGraph);

  @Query("SELECT COUNT(ira) FROM IncidenceRateAnalysis ira WHERE ira.name = :name and ira.id <> :id")
  int getCountIRWithSameName(@Param("id") Integer id, @Param("name") String name);

  @Query("SELECT ira FROM IncidenceRateAnalysis ira WHERE ira.name LIKE ?1 ESCAPE '\\'")
  List<IncidenceRateAnalysis> findAllByNameStartsWith(String pattern);
  
  Optional<IncidenceRateAnalysis> findByName(String name);

  @Query("SELECT DISTINCT ira FROM IncidenceRateAnalysis ira JOIN FETCH ira.tags t WHERE lower(t.name) in :tagNames")
  List<IncidenceRateAnalysis> findByTags(@Param("tagNames") List<String> tagNames);
}
