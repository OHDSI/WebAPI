package org.ohdsi.webapi.commonentity;

import java.util.List;
import java.util.Optional;
import org.ohdsi.webapi.commonentity.model.CommonEntity;
import org.springframework.data.repository.CrudRepository;

public interface CommonEntityRepository extends CrudRepository<CommonEntity, Long> {
    Optional<CommonEntity> findByGuid(String guid);

    Optional<CommonEntity> findByLocalIdAndTargetEntity(Integer localId, String targetEntity);

    List<CommonEntity> findAllByLocalIdInAndTargetEntity(List<Integer> localId, String targetEntity);
}
