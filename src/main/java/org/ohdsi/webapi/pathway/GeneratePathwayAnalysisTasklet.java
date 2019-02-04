package org.ohdsi.webapi.pathway;

import org.ohdsi.sql.SqlSplit;
import org.ohdsi.webapi.cohortcharacterization.repository.AnalysisGenerationInfoEntityRepository;
import org.ohdsi.webapi.common.generation.AnalysisTasklet;
import org.ohdsi.webapi.pathway.converter.SerializedPathwayAnalysisToPathwayAnalysisConverter;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.SourceUtils;
import org.slf4j.LoggerFactory;
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
    private final SourceService sourceService;

    public GeneratePathwayAnalysisTasklet(
            CancelableJdbcTemplate jdbcTemplate,
            TransactionTemplate transactionTemplate,
            PathwayService pathwayService,
            AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository,
            UserRepository userRepository,
            SourceService sourceService
    ) {

        super(LoggerFactory.getLogger(GeneratePathwayAnalysisTasklet.class), jdbcTemplate, transactionTemplate, analysisGenerationInfoEntityRepository);
        this.pathwayService = pathwayService;
        this.userRepository = userRepository;
        this.sourceService = sourceService;
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
        Source source = sourceService.findBySourceId(sourceId);
        PathwayAnalysisEntity pathwayAnalysis = pathwayService.getById(Integer.parseInt(jobParams.get(PATHWAY_ANALYSIS_ID).toString()));
        Long jobId = chunkContext.getStepContext().getStepExecution().getJobExecution().getId();
        UserEntity user = userRepository.findByLogin(jobParams.get(JOB_AUTHOR).toString());
        String cohortTable = jobParams.get(TARGET_TABLE).toString();
        String sessionId = jobParams.get(SESSION_ID).toString();

        saveInfo(jobId, new SerializedPathwayAnalysisToPathwayAnalysisConverter().convertToDatabaseColumn(pathwayAnalysis), user);

        String analysisSql = pathwayService.buildAnalysisSql(jobId, pathwayAnalysis, sourceId, SourceUtils.getTempQualifier(source) + "." + cohortTable, sessionId);

        return SqlSplit.splitSql(analysisSql);
    }
}
