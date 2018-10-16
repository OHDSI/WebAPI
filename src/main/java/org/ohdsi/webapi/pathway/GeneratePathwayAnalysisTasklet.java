package org.ohdsi.webapi.pathway;

import org.apache.commons.logging.LogFactory;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.webapi.cohortcharacterization.repository.AnalysisGenerationInfoEntityRepository;
import org.ohdsi.webapi.common.generation.AnalysisTasklet;
import org.ohdsi.webapi.pathway.converter.SerializedPathwayAnalysisToPathwayAnalysisConverter;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

import static org.ohdsi.webapi.Constants.Params.*;

public class GeneratePathwayAnalysisTasklet extends AnalysisTasklet {

    private final PathwayService pathwayService;
    private final UserRepository userRepository;

    public GeneratePathwayAnalysisTasklet(
            CancelableJdbcTemplate jdbcTemplate,
            TransactionTemplate transactionTemplate,
            PathwayService pathwayService,
            AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository,
            UserRepository userRepository
    ) {

        super(LogFactory.getLog(GeneratePathwayAnalysisTasklet.class), jdbcTemplate, transactionTemplate, analysisGenerationInfoEntityRepository);
        this.pathwayService = pathwayService;
        this.userRepository = userRepository;
    }

    private void initTx() {
        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        txDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(txDefinition);
        this.transactionTemplate.getTransactionManager().commit(initStatus);
    }

    protected String[] prepareQueries(ChunkContext chunkContext, CancelableJdbcTemplate jdbcTemplate) {
        initTx();

        Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();

        Integer sourceId = Integer.parseInt(jobParams.get(SOURCE_ID).toString());
        PathwayAnalysisEntity pathwayAnalysis = pathwayService.getById(Integer.parseInt(jobParams.get(PATHWAY_ANALYSIS_ID).toString()));
        Long jobId = chunkContext.getStepContext().getStepExecution().getJobExecution().getId();
        UserEntity user = userRepository.findByLogin(jobParams.get(JOB_AUTHOR).toString());
        String cohortTable = jobParams.get(TARGET_TABLE).toString();

        saveInfo(jobId, new SerializedPathwayAnalysisToPathwayAnalysisConverter().convertToDatabaseColumn(pathwayAnalysis), user);

        String analysisSql = pathwayService.buildAnalysisSql(jobId, pathwayAnalysis, sourceId, cohortTable);

        return SqlSplit.splitSql(analysisSql);
    }
}
