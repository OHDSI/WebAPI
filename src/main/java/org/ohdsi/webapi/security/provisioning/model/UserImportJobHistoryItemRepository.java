package org.ohdsi.webapi.security.provisioning.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface UserImportJobHistoryItemRepository extends JpaRepository<UserImportJobHistoryItem, Long> {

    Stream<UserImportJobHistoryItem> findByUserImportId(Long userImportId);
    Optional<UserImportJobHistoryItem> findFirstByUserImportIdOrderByEndTimeDesc(Long userImportId);
}
