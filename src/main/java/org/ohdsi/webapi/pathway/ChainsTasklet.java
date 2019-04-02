package org.ohdsi.webapi.pathway;

import com.fasterxml.jackson.core.type.TypeReference;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.common.generation.TransactionalTasklet;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGenerationEntity;
import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.domain.PathwayTargetCohort;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.ohdsi.webapi.pathway.dto.internal.CohortPathways;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;
import org.ohdsi.webapi.pathway.dto.internal.PathwayCode;
import org.ohdsi.webapi.pathway.dto.internal.PathwayGenerationStats;
import org.ohdsi.webapi.pathway.dto.internal.PersonPathwayEvent;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisGenerationRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.EntityUtils;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.summingInt;

public class ChainsTasklet extends TransactionalTasklet {
    
    private final JdbcTemplate jdbcTemplate;
    private final Source source;
    private Long generationId;
    private final PathwayAnalysisGenerationRepository pathwayAnalysisGenerationRepository;
    private final PathwayService pathwayService;
    private final GenericConversionService genericConversionService;
    
    public ChainsTasklet(CancelableJdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate, Source source,
                         PathwayAnalysisGenerationRepository pathwayAnalysisGenerationRepository, PathwayService pathwayService, GenericConversionService genericConversionService) {
        super(LoggerFactory.getLogger(ChainsTasklet.class), transactionTemplate);
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
    protected Object doTask(ChunkContext chunkContext) {

        Set<Integer> comboCodes = new HashSet<>();
        PathwayAnalysisResult result = new PathwayAnalysisResult();

        List<PathwayGenerationStats> targetCohortStatsList = queryGenerationStats(source, generationId);

        final PathwayAnalysisEntity design = genericConversionService
                .convert(Utils.deserialize(pathwayService.findDesignByGenerationId(generationId),
                        new TypeReference<PathwayAnalysisExportDTO>() {}), PathwayAnalysisEntity.class);
        design.getTargetCohorts().forEach(tc -> {
            CohortPathways cohortPathways = new CohortPathways();
            cohortPathways.setCohortId(tc.getCohortDefinition().getId());

            targetCohortStatsList.stream()
                    .filter(s -> Objects.equals(s.getTargetCohortId(), cohortPathways.getCohortId()))
                    .findFirst()
                    .ifPresent(targetCohortStats -> {
                        cohortPathways.setTargetCohortCount(targetCohortStats.getTargetCohortCount());
                        cohortPathways.setTotalPathwaysCount(targetCohortStats.getPathwaysCount());
                    });

            List<PersonPathwayEvent> events = queryPathwayEvents(source, generationId, tc.getCohortDefinition().getId());
            events.forEach(e -> comboCodes.add(e.getComboId()));

            Map<String, Integer> chains = events.stream()
                    .collect(Collectors.groupingBy(PersonPathwayEvent::getSubjectId))
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        entry.getValue().sort(Comparator.comparing(PersonPathwayEvent::getStartDate));
                        return entry.getValue().stream().map(e -> e.getComboId().toString()).collect(Collectors.joining("-"));
                    })
                    .collect(Collectors.groupingBy(Function.identity(), summingInt(x -> 1)))
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() > design.getMinCellCount())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            cohortPathways.setPathwaysCounts(chains);
            result.getCohortPathwaysList().add(cohortPathways);
        });

        comboCodes.forEach(code -> {
            List<PathwayEventCohort> eventCohorts = getEventCohortsByComboCode(design, code);
            String names = eventCohorts.stream()
                    .map(PathwayEventCohort::getName)
                    .collect(Collectors.joining(","));
            result.getCodes().add(new PathwayCode(code, names, eventCohorts.size() > 1));
        });
        
        saveResult(result);
        return result;
    }

    private List<PathwayEventCohort> getEventCohortsByComboCode(PathwayAnalysisEntity pathwayAnalysis, Integer comboCode) {

        Map<Integer, Integer> eventCodes = pathwayService.getEventCohortCodes(pathwayAnalysis);
        return pathwayAnalysis.getEventCohorts()
                .stream()
                .filter(ec -> ((int) Math.pow(2, Double.valueOf(eventCodes.get(ec.getCohortDefinition().getId()))) & comboCode) > 0)
                .collect(Collectors.toList());
    }
    
    private void saveResult(PathwayAnalysisResult result) {
        String[] codeNames = new String[]{"pathway_generation_id", "code", "name", "is_combo"};
        String[] cohortPathwaysNames = new String[]{"pathway_generation_id", "cohort_id", "target_cohort_count", "target_pathways_count"};
        String[] pathwaysCountsNames = new String[]{"cohort_id", "chain_name", "chain_amount"};
        result.getCodes().forEach(code -> {
            Object[] values = new Object[]{generationId, code.getCode(), code.getName(), code.isCombo() + ""};
            PreparedStatementRenderer psr = new PreparedStatementRenderer(source, "/resources/pathway/saveCodes.sql",
                    "target_database_schema", source.getTableQualifier(SourceDaimon.DaimonType.Results), codeNames, values);
            jdbcTemplate.update(psr.getSql(), psr.getSetter());
        });
        
        result.getCohortPathwaysList().forEach(cp -> {
            Object[] cohortPathwaysValues = new Object[]{generationId, cp.getCohortId(), cp.getTargetCohortCount(), cp.getTotalPathwaysCount()};
            PreparedStatementRenderer psr = new PreparedStatementRenderer(source, "/resources/pathway/saveCohortPathways.sql",
                    "target_database_schema", source.getTableQualifier(SourceDaimon.DaimonType.Results), cohortPathwaysNames, cohortPathwaysValues);
            jdbcTemplate.update(psr.getSql(), psr.getSetter());
            
            cp.getPathwaysCounts().forEach((key, value) -> {
                Object[] countValues = new Object[]{cp.getCohortId(), key, value};
                PreparedStatementRenderer countPsr = new PreparedStatementRenderer(source, "/resources/pathway/savePathwaysCount.sql",
                        "target_database_schema", source.getTableQualifier(SourceDaimon.DaimonType.Results), pathwaysCountsNames, countValues);
                jdbcTemplate.update(countPsr.getSql(), countPsr.getSetter());
            });
        });        
    }

    private List<PathwayGenerationStats> queryGenerationStats(Source source, Long generationId) {

        PreparedStatementRenderer pathwayStatsPsr = new PreparedStatementRenderer(
                source, "/resources/pathway/getStats.sql", "target_database_schema", 
                source.getTableQualifier(SourceDaimon.DaimonType.Results),
                new String[] { "generation_id" },
                new Object[] { generationId }
        );

        return jdbcTemplate.query(pathwayStatsPsr.getSql(), pathwayStatsPsr.getSetter(), (rs, rowNum) -> {
            PathwayGenerationStats stats = new PathwayGenerationStats();
            stats.setTargetCohortId(rs.getInt("target_cohort_id"));
            stats.setTargetCohortCount(rs.getInt("target_cohort_count"));
            stats.setPathwaysCount(rs.getInt("pathways_count"));
            return stats;
        });
    }

    private List<PersonPathwayEvent> queryPathwayEvents(Source source, Long generationId, Integer targetCohortId) {

        PreparedStatementRenderer pathwayEventsPsr = new PreparedStatementRenderer(
                source,
                "/resources/pathway/getPersonLevelPathwayResults.sql",
                new String[]{"target_database_schema"},
                new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results)},
                new String[] { "pathway_analysis_generation_id", "target_cohort_id" },
                new Object[] { generationId, targetCohortId }
        );

        return jdbcTemplate.query(pathwayEventsPsr.getSql(), pathwayEventsPsr.getSetter(), (rs, rowNum) -> {
            PersonPathwayEvent event = new PersonPathwayEvent();
            event.setComboId(rs.getInt("combo_id"));
            event.setSubjectId(rs.getInt("subject_id"));
            event.setStartDate(rs.getDate("cohort_start_date"));
            event.setEndDate(rs.getDate("cohort_end_date"));
            return event;
        });
    }
}
