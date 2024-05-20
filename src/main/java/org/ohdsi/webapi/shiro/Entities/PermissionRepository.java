package org.ohdsi.webapi.shiro.Entities;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by GMalikov on 24.08.2015.
 */
public interface PermissionRepository extends CrudRepository<PermissionEntity, Long> {

  /* public PermissionEntity findById(Long id);   MDACA Spring Boot 3 migration compilation issue */
  public Optional<PermissionEntity> findById(Long id);

  public PermissionEntity findByValueIgnoreCase(String permission);

  List<PermissionEntity> findByValueLike(String permissionTemplate, EntityGraph entityGraph);
}
