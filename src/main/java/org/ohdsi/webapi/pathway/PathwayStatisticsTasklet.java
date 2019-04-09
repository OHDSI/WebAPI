package org.ohdsi.webapi.pathway;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Arrays;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.ohdsi.webapi.pathway.dto.internal.PathwayCode;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import org.ohdsi.webapi.common.generation.CancelableTasklet;
import org.ohdsi.webapi.util.PreparedStatementRendererCreator;
import org.springframework.jdbc.core.PreparedStatementCreator;


public class PathwayStatisticsTasklet extends CancelableTasklet {
    
    private final CancelableJdbcTemplate jdbcTemplate;
    private final Source source;
    private Long generationId;
    private final PathwayService pathwayService;
    private final GenericConversionService genericConversionService;
    
    public PathwayStatisticsTasklet(CancelableJdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate, Source source,
						PathwayService pathwayService, GenericConversionService genericConversionService) {
			super(LoggerFactory.getLogger(PathwayStatisticsTasklet.class), jdbcTemplate, transactionTemplate);
			this.jdbcTemplate = jdbcTemplate;
			this.source = source;
			this.pathwayService = pathwayService;
			this.genericConversionService = genericConversionService;
    }
		
		private List<Integer> intArrayToList(int[] intArray) {
			return Arrays.stream(intArray).boxed().collect(Collectors.toList());
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
			Callable<int[]> execution;
			List<Integer> rowsUpdated = new ArrayList<>(); // stores the rows updated from each batch.
			
			// roll up patient-level events into pathway counts and save to DB.
			execution = () -> savePaths(source, generationId);
			FutureTask<int[]> savePathsTask = new FutureTask<>(execution);
			taskExecutor.execute(savePathsTask);
			rowsUpdated.addAll(intArrayToList(waitForFuture(savePathsTask)));
			
			if (isStopped()) return null;
			
			// build comboId -> combo name map
			final PathwayAnalysisEntity design = genericConversionService
							.convert(Utils.deserialize(pathwayService.findDesignByGenerationId(generationId),
											new TypeReference<PathwayAnalysisExportDTO>() {}), PathwayAnalysisEntity.class);
			
			List<PathwayCode> pathwayCodes = buildPathwayCodes(design, source);

			// save combo lookup to DB
			execution = () -> savePathwayCodes(pathwayCodes);
			FutureTask<int[]> saveCodesTask = new FutureTask<>(execution);
			taskExecutor.execute(saveCodesTask);			
			rowsUpdated.addAll(intArrayToList(waitForFuture(saveCodesTask)));

			return rowsUpdated.stream().mapToInt(Integer::intValue).toArray();
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
    
    private int[] savePathwayCodes(List<PathwayCode> pathwayCodes) {
        String[] codeNames = new String[]{"generation_id", "code", "name", "is_combo"};
				List<PreparedStatementCreator> creators = new ArrayList<>();
        pathwayCodes.forEach(code -> {
					Object[] values = new Object[]{generationId, code.getCode(), code.getName(), code.isCombo() ? 1 : 0};
					PreparedStatementRenderer psr = new PreparedStatementRenderer(source, "/resources/pathway/saveCodes.sql",
									"target_database_schema", source.getTableQualifier(SourceDaimon.DaimonType.Results), codeNames, values);
					creators.add(new PreparedStatementRendererCreator(psr));
        });
				
				return jdbcTemplate.batchUpdate(stmtCancel, creators);
    }

    private int[] savePaths(Source source, Long generationId) {

        PreparedStatementRenderer pathwayEventsPsr = new PreparedStatementRenderer(
                source,
                "/resources/pathway/savePaths.sql",
                new String[]{"target_database_schema"},
                new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results)},
                new String[] { "generation_id"},
                new Object[] { generationId}
        );

				return new int[] {jdbcTemplate.update(pathwayEventsPsr.getSql(), pathwayEventsPsr.getSetter())};
    }
}
