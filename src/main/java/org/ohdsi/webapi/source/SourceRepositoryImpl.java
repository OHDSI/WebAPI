package org.ohdsi.webapi.source;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class SourceRepositoryImpl implements PersitableRepository<Source, Integer> {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void persist(Source s) {
        Session session = (Session) em.getDelegate();
        if (session.contains(s)) {
            session.evict(s);
        }
        session.update(s);
    }
}
