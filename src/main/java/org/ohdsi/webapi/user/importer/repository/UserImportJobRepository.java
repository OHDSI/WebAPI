package org.ohdsi.webapi.user.importer.repository;

import com.odysseusinc.scheduler.repository.ArachneJobRepository;
import org.ohdsi.webapi.user.importer.model.LdapProviderType;
import org.ohdsi.webapi.user.importer.model.UserImportJob;

import java.util.Optional;

public interface UserImportJobRepository extends ArachneJobRepository<UserImportJob> {

  UserImportJob findByProviderType(LdapProviderType providerType);
}
