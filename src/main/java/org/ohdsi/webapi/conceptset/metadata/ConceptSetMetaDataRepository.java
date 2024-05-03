package org.ohdsi.webapi.conceptset.metadata;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConceptSetMetaDataRepository extends JpaRepository<ConceptSetMetaData, Integer> {
    
    @Query("DELETE FROM ConceptSetMetaData cc WHERE cc.conceptSetId = :conceptSetId and cc.conceptId in :conceptId")
    void deleteMetadataByConceptSetIdAndInConceptId(int conceptSetId, List<Integer> conceptId);

    void deleteMetadataByConceptSetIdAndConceptId(int conceptSetId, int conceptId);

    List<ConceptSetMetaData> findByConceptSetId(int conceptSetId);
    ConceptSetMetaData findById(int id);
    void deleteById(int id);
    Optional<ConceptSetMetaData> findConceptSetMetaDataByConceptIdAndConceptId(int conceptSetId, int conceptId);
}
