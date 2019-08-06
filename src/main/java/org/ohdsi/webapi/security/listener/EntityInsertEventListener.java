package org.ohdsi.webapi.security.listener;

import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.security.model.EntityPermissionSchema;
import org.ohdsi.webapi.security.model.EntityPermissionSchemaResolver;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class EntityInsertEventListener implements PreInsertEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(EntityInsertEventListener.class);

    private final EntityPermissionSchemaResolver entityPermissionSchemaResolver;
    private final PermissionManager permissionManager;

    public EntityInsertEventListener(
            EntityPermissionSchemaResolver entityPermissionSchemaResolver,
            PermissionManager permissionManager
    ) {

        this.entityPermissionSchemaResolver = entityPermissionSchemaResolver;
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean onPreInsert(PreInsertEvent event) {

        Object entity = event.getEntity();

        if (CommonEntity.class.isAssignableFrom(entity.getClass())) {
            CommonEntity commonEntity = (CommonEntity) entity;
            EntityPermissionSchema permissionSchema = entityPermissionSchemaResolver.getForClass(commonEntity.getClass());
            if (permissionSchema != null) {
                // NOTE:
                // Fails if executed within the same thread
                // http://anshuiitk.blogspot.com/2010/11/hibernate-pre-database-opertaion-event.html
                Future<Boolean> future = new SimpleAsyncTaskExecutor().submit(() -> {
                    try {
                        String login = permissionManager.getSubjectName();
                        RoleEntity role = permissionManager.getUserPersonalRole(login);
                        permissionManager.addPermissionsFromTemplate(role, permissionSchema.getAllPermissions(), commonEntity.getId().toString());
                        return true;
                    } catch (Exception ex) {
                        LOG.error(ex.getMessage(), ex);
                        return false;
                    }
                });
                try {
                    if (future.get()) {
                        return false;
                    }
                } catch (InterruptedException | ExecutionException e) {
                }
            }
            throw new RuntimeException();
        }
        return false;
    }
}
