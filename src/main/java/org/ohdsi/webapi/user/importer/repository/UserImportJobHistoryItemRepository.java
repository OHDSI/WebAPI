package org.ohdsi.webapi.user.importer.repository;

import org.ohdsi.webapi.user.importer.model.LdapProviderType;
import org.ohdsi.webapi.user.importer.model.UserImportJobHistoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface UserImportJobHistoryItemRepository extends JpaRepository<UserImportJobHistoryItem, Long> {

  Stream<UserImportJobHistoryItem> findByProviderType(LdapProviderType providerType);
  Optional<UserImportJobHistoryItem> findFirstByProviderTypeOrderByEndTimeDesc(LdapProviderType providerType);
}
