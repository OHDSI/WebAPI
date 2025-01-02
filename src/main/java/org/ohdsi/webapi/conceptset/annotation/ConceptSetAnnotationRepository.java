package org.ohdsi.webapi.conceptset.annotation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConceptSetAnnotationRepository extends JpaRepository<ConceptSetAnnotation, Integer> {
    
    @Query("DELETE FROM ConceptSetAnnotation cc WHERE cc.conceptSetId = :conceptSetId and cc.conceptId in :conceptId")
    void deleteAnnotationByConceptSetIdAndInConceptId(int conceptSetId, List<Integer> conceptId);

    void deleteAnnotationByConceptSetIdAndConceptId(int conceptSetId, int conceptId);

    List<ConceptSetAnnotation> findByConceptSetId(int conceptSetId);
    ConceptSetAnnotation findById(int id);
    void deleteById(int id);
    Optional<ConceptSetAnnotation> findConceptSetAnnotationByConceptIdAndConceptId(int conceptSetId, int conceptId);
}
