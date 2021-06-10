package org.ohdsi.webapi.security.listener;

import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.security.model.EntityPermissionSchema;
import org.ohdsi.webapi.security.model.EntityPermissionSchemaResolver;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

public class EntityDeleteEventListener implements PostDeleteEventListener {

    private final EntityPermissionSchemaResolver entityPermissionSchemaResolver;

    public EntityDeleteEventListener(EntityPermissionSchemaResolver entityPermissionSchemaResolver) {

        this.entityPermissionSchemaResolver = entityPermissionSchemaResolver;
    }

    @Override
    public void onPostDelete(PostDeleteEvent postDeleteEvent) {

        final Object entity = postDeleteEvent.getEntity();
        if (CommonEntity.class.isAssignableFrom(entity.getClass())) {
            CommonEntity commonEntity = (CommonEntity) entity;
            EntityPermissionSchema permissionSchema = entityPermissionSchemaResolver.getForClass(commonEntity.getClass());
            if (permissionSchema != null) {
                new SimpleAsyncTaskExecutor().execute(() -> permissionSchema.onDelete(commonEntity));
            }
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister entityPersister) {

        return false;
    }
}
