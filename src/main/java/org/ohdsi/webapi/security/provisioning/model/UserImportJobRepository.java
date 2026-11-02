package org.ohdsi.webapi.security.provisioning.model;

import org.ohdsi.webapi.arachne.scheduler.repository.ArachneJobRepository;

import java.util.stream.Stream;

public interface UserImportJobRepository extends ArachneJobRepository<UserImportJob> {

  UserImportJob findByProviderType(LdapProviderType providerType);

  Stream<UserImportJob> findUserImportJobsBy();
}
