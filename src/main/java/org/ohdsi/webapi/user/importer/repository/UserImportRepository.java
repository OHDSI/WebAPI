package org.ohdsi.webapi.user.importer.repository;

import org.ohdsi.webapi.user.importer.model.UserImport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserImportRepository extends JpaRepository<UserImport, Integer> {
}
