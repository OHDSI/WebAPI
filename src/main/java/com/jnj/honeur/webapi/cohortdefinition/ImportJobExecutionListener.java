package com.jnj.honeur.webapi.cohortdefinition;

import com.jnj.honeur.webapi.SourceDaimonContextHolder;
import com.jnj.honeur.webapi.shiro.LiferayPermissionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

import static org.ohdsi.webapi.Constants.Params.COHORT_DEFINITION_ID;
import static org.ohdsi.webapi.Constants.Params.SOURCE_KEY;

public class ImportJobExecutionListener implements JobExecutionListener {

    protected final Log log = LogFactory.getLog(getClass());

    public static final String COHORT_GENERATION_RESULTS = "cohortGenerationResults";

    private CohortGenerationResults cohortGenerationResults;

    @Autowired(required = false)
    private LiferayPermissionManager authorizer;


    public ImportJobExecutionListener(CohortGenerationResults cohortGenerationResults) {
        this.cohortGenerationResults = cohortGenerationResults;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        jobExecution.getExecutionContext().put(COHORT_GENERATION_RESULTS, cohortGenerationResults);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        JobParameters jobParams = jobExecution.getJobParameters();
        addViewPermissions(Integer.valueOf(jobParams.getString(COHORT_DEFINITION_ID)), jobParams.getString(SOURCE_KEY));

        //To be sure context is switched back
        SourceDaimonContextHolder.clear();
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
