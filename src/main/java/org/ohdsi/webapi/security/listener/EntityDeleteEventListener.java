package org.ohdsi.webapi.security.listener;

import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.security.model.EntityPermissionSchema;
import org.ohdsi.webapi.security.model.EntityPermissionSchemaResolver;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.Map;

public class EntityDeleteEventListener implements PostDeleteEventListener {

    private final EntityPermissionSchemaResolver entityPermissionSchemaResolver;
    private final PermissionManager permissionManager;

    public EntityDeleteEventListener(EntityPermissionSchemaResolver entityPermissionSchemaResolver, PermissionManager permissionManager) {

        this.entityPermissionSchemaResolver = entityPermissionSchemaResolver;
        this.permissionManager = permissionManager;
    }

    @Override
    public void onPostDelete(PostDeleteEvent postDeleteEvent) {

        final Object entity = postDeleteEvent.getEntity();
        if (CommonEntity.class.isAssignableFrom(entity.getClass())) {
            CommonEntity commonEntity = (CommonEntity) entity;
            EntityPermissionSchema permissionSchema = entityPermissionSchemaResolver.getForClass(commonEntity.getClass());
            if (permissionSchema != null) {
                new SimpleAsyncTaskExecutor().execute(() -> {
                    Map<String, String> permissionTemplates = permissionSchema.getAllPermissions();
                    permissionManager.removePermissionsFromTemplate(permissionTemplates, commonEntity.getId().toString());
                });
            }
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister entityPersister) {

        return false;
    }
}
