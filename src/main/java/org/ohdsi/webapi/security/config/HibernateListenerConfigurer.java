package org.ohdsi.webapi.security.config;

import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.ohdsi.webapi.security.listener.EntityDeleteEventListener;
import org.ohdsi.webapi.security.listener.EntityInsertEventListener;
import org.ohdsi.webapi.security.model.EntityPermissionSchemaResolver;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

@Configuration
public class HibernateListenerConfigurer {

    @PersistenceUnit
    private EntityManagerFactory emf;

    private final EntityPermissionSchemaResolver entityPermissionSchemaResolver;
    private final PermissionManager permissionManager;

    public HibernateListenerConfigurer(EntityPermissionSchemaResolver entityPermissionSchemaResolver, PermissionManager permissionManager) {

        this.entityPermissionSchemaResolver = entityPermissionSchemaResolver;
        this.permissionManager = permissionManager;
    }

    @PostConstruct
    protected void init() {

        SessionFactoryImpl sessionFactory = emf.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
        registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(new EntityInsertEventListener(entityPermissionSchemaResolver, permissionManager));
        registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(new EntityDeleteEventListener(entityPermissionSchemaResolver, permissionManager));

    }
}
