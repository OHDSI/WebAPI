package org.ohdsi.webapi.evidence;
import java.util.Collection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author fdefalco
 */
public interface DrugLabelRepository extends CrudRepository<DrugLabel, Integer> {
  Collection<DrugLabel> findAllBySetid(String setid);
  Collection<DrugLabel> findAllBySearchName(String searchName);
  Collection<DrugLabel> findAllByIngredientConceptId(int ingredientConceptId);
  @Query("SELECT d FROM DrugLabel d WHERE d.searchName LIKE %:searchTerm%")
  Collection<DrugLabel> searchNameContainsTerm(@Param("searchTerm") String searchTerm);
}