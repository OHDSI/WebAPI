package org.ohdsi.webapi.evidence;
import java.util.Collection;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author fdefalco
 */
public interface DrugLabelRepository extends CrudRepository<DrugLabel, Integer> {
  Collection<DrugLabel> findAllBySetid(String setid);
  Collection<DrugLabel> findAllBySearchName(String searchName);
  Collection<DrugLabel> findAllByIngredientConceptId(int ingredientConceptId);
}