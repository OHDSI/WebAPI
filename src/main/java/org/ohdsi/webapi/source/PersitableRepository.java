package org.ohdsi.webapi.source;

import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;

public interface PersitableRepository<T, ID extends Serializable> {
    void persist(T value);
}
