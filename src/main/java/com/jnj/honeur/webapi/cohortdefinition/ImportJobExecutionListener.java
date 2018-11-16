package com.jnj.honeur.webapi.cohortdefinition;

import com.jnj.honeur.webapi.SourceDaimonContextHolder;
import com.jnj.honeur.webapi.shiro.LiferayPermissionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfoRepository;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;

public class ImportJobExecutionListener implements JobExecutionListener {

    protected final Log log = LogFactory.getLog(getClass());

    public static final String COHORT_GENERATION_RESULTS = "cohortGenerationResults";

    private CohortGenerationResults cohortGenerationResults;
    private TransactionTemplate transactionTemplate;
    private CohortGenerationInfoRepository cohortGenerationInfoRepository;
    private CohortDefinitionRepository cohortDefinitionRepository;
    private CohortDefinitionService cohortDefinitionService;

    private LiferayPermissionManager authorizer;


    public ImportJobExecutionListener(CohortGenerationResults cohortGenerationResults,
                                      TransactionTemplate transactionTemplate,
                                      CohortGenerationInfoRepository cohortGenerationInfoRepository,
                                      CohortDefinitionRepository cohortDefinitionRepository,
                                      CohortDefinitionService cohortDefinitionService,
                                      LiferayPermissionManager authorizer) {
        this.transactionTemplate = transactionTemplate;
        this.cohortGenerationResults = cohortGenerationResults;
        this.cohortGenerationInfoRepository = cohortGenerationInfoRepository;
        this.cohortDefinitionRepository = cohortDefinitionRepository;
        this.cohortDefinitionService = cohortDefinitionService;
        this.authorizer = authorizer;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        JobParameters jobParams = jobExecution.getJobParameters();
        Integer defId = Integer.valueOf(jobParams.getString(Constants.Params.COHORT_DEFINITION_ID));
        String sourceKey = jobParams.getString(Constants.Params.SOURCE_KEY);

        DefaultTransactionDefinition initTx = new DefaultTransactionDefinition();
        initTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(initTx);

        CohortGenerationInfo copyInfo = new CohortGenerationInfo(cohortDefinitionRepository.findOne(defId),
                cohortDefinitionService.getSourceRepository().findBySourceKey(sourceKey).getSourceId());

        copyInfo.setStatus(GenerationStatus.IMPORTING);
        cohortGenerationInfoRepository.save(copyInfo);

        this.transactionTemplate.getTransactionManager().commit(initStatus);
        jobExecution.getExecutionContext().put(COHORT_GENERATION_RESULTS, cohortGenerationResults);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        //To be sure context is switched back
        SourceDaimonContextHolder.clear();

        JobParameters jobParams = jobExecution.getJobParameters();
        DefaultTransactionDefinition completeTx = new DefaultTransactionDefinition();
        completeTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus completeStatus = this.transactionTemplate.getTransactionManager().getTransaction(completeTx);

        Integer defId = Integer.valueOf(jobParams.getString(Constants.Params.COHORT_DEFINITION_ID));
        String sourceKey = jobParams.getString(Constants.Params.SOURCE_KEY);

        addViewPermissions(defId, sourceKey);

        if (jobExecution.getStatus() == BatchStatus.FAILED || jobExecution.getStatus() == BatchStatus.STOPPED) {
            cohortGenerationResults.getCohortGenerationInfo().setStatus(GenerationStatus.ERROR);
        } else {
            cohortGenerationResults.getCohortGenerationInfo().setStatus(GenerationStatus.COMPLETE);
        }

        importCohortGenerationInfo(defId, sourceKey, cohortGenerationResults.getCohortGenerationInfo());

        this.transactionTemplate.getTransactionManager().commit(completeStatus);
    }

    private CohortGenerationInfo importCohortGenerationInfo(int id, String sourceKey,
                                                            CohortGenerationInfo cohortGenerationInfo) {
        CohortGenerationInfo cohortGenerationInfoAdapted =
                cohortGenerationInfoRepository.findGenerationInfoByIdAndSourceId(id,
                        cohortDefinitionService.getSourceRepository().findBySourceKey(sourceKey).getSourceId());
        cohortGenerationInfoAdapted.setStatus(cohortGenerationInfo.getStatus());
        cohortGenerationInfoAdapted.setExecutionDuration(cohortGenerationInfo.getExecutionDuration());
        cohortGenerationInfoAdapted.setIsValid(cohortGenerationInfo.isIsValid());
        cohortGenerationInfoAdapted.setStartTime(cohortGenerationInfo.getStartTime());
        cohortGenerationInfoAdapted.setFailMessage(cohortGenerationInfo.getFailMessage());
        cohortGenerationInfoAdapted.setPersonCount(cohortGenerationInfo.getPersonCount());
        cohortGenerationInfoAdapted.setRecordCount(cohortGenerationInfo.getRecordCount());
        cohortGenerationInfoAdapted.setIncludeFeatures(cohortGenerationInfo.isIncludeFeatures());

        return cohortGenerationInfoRepository.save(cohortGenerationInfoAdapted);
    }

    private void addViewPermissions(int id, String sourceKey) {
        //TODO make more central (code duplication in HoneurCohortService.java
        HashMap<String, String> map = new HashMap<>();
        map.put("cohortdefinition:%s:report:" + sourceKey + ":get",
                "View Cohort Definition generation results for defintion with ID = %s for source " + sourceKey);


        String permissionPattern = String.format("cohortdefinition:%s:get", id);
        Iterable<RoleEntity> roleEntities = authorizer.getRoles(true);

        for (RoleEntity role : roleEntities) {
            try {
                if (authorizer.getRolePermissions(role.getId())
                        .contains(authorizer.getPermissionByValue(permissionPattern))) {
                    authorizer.addPermissionsFromTemplate(role, map,
                            String.valueOf(id));
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
