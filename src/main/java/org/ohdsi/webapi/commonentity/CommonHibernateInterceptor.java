package org.ohdsi.webapi.commonentity;

import java.io.Serializable;
import java.util.Optional;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.ohdsi.webapi.cohortcomparison.CCAExecutionExtension;
import org.ohdsi.webapi.cohortcomparison.CCAExecutionExtensionRepository;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisExecution;
import org.ohdsi.webapi.commonentity.model.CommonEntity;
import org.ohdsi.webapi.util.GUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class CommonHibernateInterceptor extends EmptyInterceptor implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonHibernateInterceptor.class);

    private CommonEntityRepository commonEntityRepository;
    private CCAExecutionExtensionRepository ccaExecutionExtensionRepository;
    private ApplicationContext applicationContext;

    private CommonEntityRepository getCommonEntityRepository() {

        if (commonEntityRepository == null) {
            commonEntityRepository = applicationContext.getBean(CommonEntityRepository.class);
        }
        return commonEntityRepository;
    }

    private CCAExecutionExtensionRepository getCCAExecutionRepository() {

        if (ccaExecutionExtensionRepository == null) {
            ccaExecutionExtensionRepository = applicationContext.getBean(CCAExecutionExtensionRepository.class);
        }
        return ccaExecutionExtensionRepository;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {

        if (!isEntityExcluded(entity)) {
            LOGGER.info("Saved: {} with id: {}", entity, id);
            CommonEntity commonEntity = new CommonEntity();
            commonEntity.setGuid(GUIDUtil.newGuid());
            commonEntity.setLocalId(getLocalId(id));
            commonEntity.setTargetEntity(entity.getClass().getName());
            getCommonEntityRepository().save(commonEntity);
        }
        if (entity instanceof ComparativeCohortAnalysisExecution) {
            LOGGER.info("Creating CCA Execution Extension for {}", id);
            CCAExecutionExtension ccaExt = new CCAExecutionExtension();
            ccaExt.setId((Integer) id);
            getCCAExecutionRepository().save(ccaExt);
        }
        return false;
    }

    private Integer getLocalId(Serializable id) {

        if (id instanceof Integer) {
            return (Integer) id;
        } else if (id instanceof Long) {
            return ((Long) id).intValue();
        } else {
            return 0;
        }
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {

        if (!isEntityExcluded(entity)) {
            LOGGER.info("Removed: {} with id: {}", entity, id);
            Optional<CommonEntity> commonEntity = commonEntityRepository
                    .findByLocalIdAndTargetEntity((Integer) id, entity.getClass().getName());
            commonEntity.ifPresent(getCommonEntityRepository()::delete);
            if (entity instanceof ComparativeCohortAnalysisExecution) {
                getCCAExecutionRepository().delete((Integer) id);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }

    private boolean isEntityExcluded(Object entity) {

        return entity instanceof CommonEntity || entity instanceof CCAExecutionExtension;
    }
}
