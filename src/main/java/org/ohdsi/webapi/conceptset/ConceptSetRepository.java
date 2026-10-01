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

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author fdefalco
 */
public interface ConceptSetRepository extends CrudRepository<ConceptSet, Integer>, JpaSpecificationExecutor<ConceptSet> {
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

	@Query("SELECT cs FROM ConceptSet cs WHERE " +
		"(:createdFrom IS NULL OR cs.createdDate >= :createdFrom) AND " +
		"(:createdTo IS NULL OR cs.createdDate <= :createdTo) AND " +
		"(:updatedFrom IS NULL OR cs.modifiedDate >= :updatedFrom) AND " +
		"(:updatedTo IS NULL OR cs.modifiedDate <= :updatedTo)")
	List<ConceptSet> findByDateFilters(
		@Param("createdFrom") Date createdFrom,
		@Param("createdTo") Date createdTo,
		@Param("updatedFrom") Date updatedFrom,
		@Param("updatedTo") Date updatedTo
	);

	@Query("SELECT DISTINCT cs FROM ConceptSet cs " +
		"JOIN cs.tags t " +
		"WHERE t.id IN :tagIds AND " +
		"(:createdFrom IS NULL OR cs.createdDate >= :createdFrom) AND " +
		"(:createdTo IS NULL OR cs.createdDate <= :createdTo) AND " +
		"(:updatedFrom IS NULL OR cs.modifiedDate >= :updatedFrom) AND " +
		"(:updatedTo IS NULL OR cs.modifiedDate <= :updatedTo)")
	List<ConceptSet> findByTagsAndDateFilters(
		@Param("tagIds") List<Integer> tagIds,
		@Param("createdFrom") Date createdFrom,
		@Param("createdTo") Date createdTo,
		@Param("updatedFrom") Date updatedFrom,
		@Param("updatedTo") Date updatedTo
	);
}
