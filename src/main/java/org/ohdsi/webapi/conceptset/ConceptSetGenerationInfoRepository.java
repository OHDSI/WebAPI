/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.conceptset;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author Anthony Sena <https://github.com/ohdsi>
 */
public interface ConceptSetGenerationInfoRepository extends CrudRepository<ConceptSetGenerationInfo, Integer> {

    @Query("from ConceptSetGenerationInfo where concept_set_id = ?1")    
    List<ConceptSetGenerationInfo> findAllByConceptSetId(Integer conceptSetId);

    void deleteByConceptSetId(Integer conceptSetId);
}
