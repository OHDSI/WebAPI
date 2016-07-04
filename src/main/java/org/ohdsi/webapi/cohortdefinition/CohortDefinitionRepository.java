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
package org.ohdsi.webapi.cohortdefinition;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author cknoll1
 */
public interface CohortDefinitionRepository extends CrudRepository<CohortDefinition, Integer> {
  Page<CohortDefinition> findAll(Pageable pageable);
  
  // Bug in hibernate, findById should use @EntityGraph, but details are not being feched. Workaround: mark details Fetch.EAGER,
  // but means findAll() will eager load definitions (what the @EntityGraph was supposed to solve)
  @EntityGraph(value = "CohortDefinition.withDetail", type = EntityGraph.EntityGraphType.LOAD)
  @Query("select cd from CohortDefinition cd where id = ?1")
  CohortDefinition findOneWithDetail(Integer id);
  
  @Query("select cd from CohortDefinition AS cd JOIN FETCH cd.details as d")          
  List<CohortDefinition> list();
  
}
