package org.ohdsi.webapi.commonentity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.ohdsi.webapi.cohortcomparison.CCAExecutionExtension;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisExecution;
import org.ohdsi.webapi.commonentity.model.CommonEntity;
import org.springframework.data.repository.CrudRepository;

public interface CommonEntityService {
    <T> Optional<T> findByGuid(String guid);

    <T> Optional<T> findWithRepository(String guid, BiFunction<CrudRepository<T, Integer>, CommonEntity, T> callback);

    <T> Map<Integer, String> getGuidMap(Class<T> entityClass, List<T> entities, Function<T, Integer> identityMapper);

    CCAExecutionExtension getCCAExecutionExtension(ComparativeCohortAnalysisExecution ccaExecution);
}
