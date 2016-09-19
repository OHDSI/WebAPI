/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.evidence;

import java.util.Collection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author asena5
 */
public interface NegativeControlRepository extends CrudRepository<NegativeControlRecord, Integer> {
  @Query("SELECT n FROM NegativeControlRecord n WHERE n.sourceId = :sourceId AND n.conceptSetId = :conceptSetId")
  Collection<NegativeControlRecord> findAllBySourceIdAndConceptId(@Param("sourceId") int sourceId, @Param("conceptSetId") int conceptSetId);
  
  @Modifying
  @Query("DELETE FROM NegativeControlRecord n WHERE n.conceptSetId = :conceptSetId")
  void deleteAllByConceptSetId(@Param("conceptSetId") int conceptSetId);
}
