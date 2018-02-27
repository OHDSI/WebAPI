package org.ohdsi.webapi.commonentity.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.ohdsi.webapi.cohortcomparison.CCAExecutionExtension;
import org.ohdsi.webapi.cohortcomparison.CCAExecutionExtensionRepository;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisExecution;
import org.ohdsi.webapi.commonentity.CommonEntityRepository;
import org.ohdsi.webapi.commonentity.CommonEntityService;
import org.ohdsi.webapi.commonentity.model.CommonEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;

@Service
public class CommonEntityServiceImpl implements CommonEntityService,
        ApplicationListener<ContextRefreshedEvent>,
        ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonEntityService.class);
    private final CommonEntityRepository commonEntityRepository;
    private final CCAExecutionExtensionRepository ccaExecutionExtensionRepository;
    private Repositories repositories;
    private ApplicationContext applicationContext;

    @Autowired
    public CommonEntityServiceImpl(CommonEntityRepository commonEntityRepository,
                                   CCAExecutionExtensionRepository ccaExecutionExtensionRepository) {

        this.commonEntityRepository = commonEntityRepository;
        this.ccaExecutionExtensionRepository = ccaExecutionExtensionRepository;
    }

    @Override
    public <T> Optional<T> findByGuid(String guid) {

        return this.findWithRepository(guid,
                (r, commonEntity) -> r.findOne(commonEntity.getLocalId()));
    }

    @Override
    public <T> Optional<T> findWithRepository(String guid, BiFunction<CrudRepository<T, Integer>, CommonEntity, T> callback) {

        Optional<CommonEntity> entity = commonEntityRepository.findByGuid(guid);
        return entity.map(c -> {
            try {
                Class<T> entityClass = (Class<T>) Class.forName(c.getTargetEntity());
                CrudRepository<T, Integer> repository = (CrudRepository<T, Integer>) repositories.getRepositoryFor(entityClass);
                return callback.apply(repository, c);
            } catch (ClassNotFoundException e) {
                LOGGER.error("Failed to get entity", e);
                return null;
            }
        });
    }

    @Override
    public <T> Map<Integer, String> getGuidMap(Class<T> entityClass, List<T> entities, Function<T, Integer> identityMapper) {

        List<Integer> identities = entities.stream().map(identityMapper).collect(Collectors.toList());
        List<CommonEntity> commonEntities = commonEntityRepository
                .findAllByLocalIdInAndTargetEntity(identities, entityClass.getName());
        return commonEntities.stream().collect(Collectors.toMap(CommonEntity::getLocalId, CommonEntity::getGuid));
    }

    @Override
    public CCAExecutionExtension getCCAExecutionExtension(ComparativeCohortAnalysisExecution ccaExecution) {

        return ccaExecutionExtensionRepository.findOne(ccaExecution.getExecutionId());
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

        repositories = new Repositories(applicationContext);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }
}
