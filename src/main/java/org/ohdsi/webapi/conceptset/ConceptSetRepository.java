/*
 * Copyright 2015 fdefalco.
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
package org.ohdsi.webapi.conceptset;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author fdefalco
 */
public interface ConceptSetRepository extends CrudRepository<ConceptSet, Integer> {
  ConceptSet findById(Integer conceptSetId);
  
  @Deprecated
  @Query("SELECT cs FROM ConceptSet cs WHERE cs.name = :conceptSetName and cs.id <> :conceptSetId")
  Collection<ConceptSet> conceptSetExists(@Param("conceptSetId") Integer conceptSetId, @Param("conceptSetName") String conceptSetName);
  
  @Query("SELECT COUNT(cs) FROM ConceptSet cs WHERE cs.name = :conceptSetName and cs.id <> :conceptSetId")
  int getCountCSetWithSameName(@Param("conceptSetId") Integer conceptSetId, @Param("conceptSetName") String conceptSetName);

  @Query("SELECT cs FROM ConceptSet cs WHERE cs.name LIKE ?1 ESCAPE '\\'")
  List<ConceptSet> findAllByNameStartsWith(String pattern);
  
  Optional<ConceptSet> findByName(String name);
  
  @Query("SELECT DISTINCT cs FROM ConceptSet cs JOIN FETCH cs.tags t WHERE lower(t.name) in :tagNames")
  List<ConceptSet> findByTags(@Param("tagNames") List<String> tagNames);
}
