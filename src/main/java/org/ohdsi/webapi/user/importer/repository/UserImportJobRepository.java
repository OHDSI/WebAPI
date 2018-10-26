package org.ohdsi.webapi.user.importer.repository;

import com.odysseusinc.scheduler.repository.ArachneJobRepository;
import org.ohdsi.webapi.user.importer.model.LdapProviderType;
import org.ohdsi.webapi.user.importer.model.UserImportJob;

import java.util.stream.Stream;

public interface UserImportJobRepository extends ArachneJobRepository<UserImportJob> {

  UserImportJob findByProviderType(LdapProviderType providerType);

  Stream<UserImportJob> findUserImportJobsBy();
}
