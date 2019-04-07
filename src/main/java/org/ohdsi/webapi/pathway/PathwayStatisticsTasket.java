package org.ohdsi.webapi.pathway;

import com.fasterxml.jackson.core.type.TypeReference;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.common.generation.TransactionalTasklet;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.ohdsi.webapi.pathway.dto.internal.PathwayCode;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisGenerationRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class PathwayStatisticsTasket extends TransactionalTasklet {
    
    private final JdbcTemplate jdbcTemplate;
    private final Source source;
    private Long generationId;
    private final PathwayAnalysisGenerationRepository pathwayAnalysisGenerationRepository;
    private final PathwayService pathwayService;
    private final GenericConversionService genericConversionService;
    
    public PathwayStatisticsTasket(CancelableJdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate, Source source,
                         PathwayAnalysisGenerationRepository pathwayAnalysisGenerationRepository, PathwayService pathwayService, GenericConversionService genericConversionService) {
        super(LoggerFactory.getLogger(PathwayStatisticsTasket.class), transactionTemplate);
        this.jdbcTemplate = jdbcTemplate;
        this.source = source;
        this.pathwayAnalysisGenerationRepository = pathwayAnalysisGenerationRepository;
        this.pathwayService = pathwayService;
        this.genericConversionService = genericConversionService;
    }

    @Override
    protected void doBefore(ChunkContext chunkContext) {
        initTx();
        generationId = chunkContext.getStepContext().getStepExecution().getJobExecution().getId();
    }

    private void initTx() {
        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        txDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus initStatus = this.transactionTemplate.getTransactionManager().getTransaction(txDefinition);
        this.transactionTemplate.getTransactionManager().commit(initStatus);
    }

    @Override
    protected int[] doTask(ChunkContext chunkContext) {

			int[] rowsUpdated = new int[2]; // stores the rows updated from each batch.
			
			// roll up patient-level events into pathway counts and save to DB.
			rowsUpdated[0] = savePaths(source, generationId);

			// build comboId -> combo name map
			final PathwayAnalysisEntity design = genericConversionService
							.convert(Utils.deserialize(pathwayService.findDesignByGenerationId(generationId),
											new TypeReference<PathwayAnalysisExportDTO>() {}), PathwayAnalysisEntity.class);
			
			List<PathwayCode> pathwayCodes = buildPathwayCodes(design, source);

			// save combo lookup to DB
			rowsUpdated[1] = savePathwayCodes(pathwayCodes);
			
			return rowsUpdated;
    }

    private List<PathwayCode> buildPathwayCodes(PathwayAnalysisEntity design, Source source) {
			PreparedStatementRenderer pathwayStatsPsr = new PreparedStatementRenderer(
							source, "/resources/pathway/getDistinctCodes.sql", "target_database_schema", 
							source.getTableQualifier(SourceDaimon.DaimonType.Results),
							new String[] { "generation_id" },
							new Object[] { generationId }
			);

			return jdbcTemplate.query(pathwayStatsPsr.getSql(), pathwayStatsPsr.getSetter(), (rs, rowNum) -> {
				int code = rs.getInt("combo_id");
				List<PathwayEventCohort> eventCohorts = getEventCohortsByComboCode(design, code);
				String names = eventCohorts.stream()
								.map(PathwayEventCohort::getName)
								.collect(Collectors.joining(","));
				 return new PathwayCode(code, names, eventCohorts.size() > 1);
			});
    }
		
    private List<PathwayEventCohort> getEventCohortsByComboCode(PathwayAnalysisEntity pathwayAnalysis, int comboCode) {

        Map<Integer, Integer> eventCodes = pathwayService.getEventCohortCodes(pathwayAnalysis);
        return pathwayAnalysis.getEventCohorts()
                .stream()
                .filter(ec -> ((long) Math.pow(2, Double.valueOf(eventCodes.get(ec.getCohortDefinition().getId()))) & comboCode) > 0)
                .collect(Collectors.toList());
    }
    
    private int savePathwayCodes(List<PathwayCode> pathwayCodes) {
        String[] codeNames = new String[]{"generation_id", "code", "name", "is_combo"};
        pathwayCodes.forEach(code -> {
					Object[] values = new Object[]{generationId, code.getCode(), code.getName(), code.isCombo() ? 1 : 0};
					PreparedStatementRenderer psr = new PreparedStatementRenderer(source, "/resources/pathway/saveCodes.sql",
									"target_database_schema", source.getTableQualifier(SourceDaimon.DaimonType.Results), codeNames, values);
					jdbcTemplate.update(psr.getSql(), psr.getSetter());
        });
				return pathwayCodes.size();
    }

    private int savePaths(Source source, Long generationId) {

        PreparedStatementRenderer pathwayEventsPsr = new PreparedStatementRenderer(
                source,
                "/resources/pathway/savePaths.sql",
                new String[]{"target_database_schema"},
                new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results)},
                new String[] { "generation_id"},
                new Object[] { generationId}
        );

				return jdbcTemplate.update(pathwayEventsPsr.getSql(), pathwayEventsPsr.getSetter());
    }
}
