package org.ohdsi.webapi.conceptset.search;

import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.exception.ConceptNotExistException;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.service.dto.ConceptSetReindexDTO;
import org.ohdsi.webapi.vocabulary.Concept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.JOB_NAME;

@Service
public class ConceptSetReindexJobService {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ConceptSetSearchService conceptSetSearchService;

    @Autowired
    private ConceptSetService conceptSetService;

    @Autowired
    private VocabularyService vocabService;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private JobTemplate jobTemplate;

    @Autowired
    private JobExplorer jobExplorer;

    private static final String REINDEX_JOB_NAME = "concept sets reindex: %s";

    private static final String REINDEX_TOTAL_DOCUMENTS = "REINDEX_TOTAL_DOCUMENTS";

    private static final String REINDEX_PROCESSED_DOCUMENTS = "REINDEX_PROCESSED_DOCUMENTS";

    public ConceptSetReindexDTO createIndex(String sourceKey) {
        if (!conceptSetSearchService.isSearchAvailable()) {
            return new ConceptSetReindexDTO(ConceptSetReindexStatus.UNAVAILABLE);
        }
        String jobName = String.format(REINDEX_JOB_NAME, sourceKey);
        Optional<JobExecution> jobExecutionOptional = jobExplorer.findRunningJobExecutions(jobName).stream().findFirst();
        if (jobExecutionOptional.isPresent()) {
            return new ConceptSetReindexDTO(ConceptSetReindexStatus.RUNNING, jobExecutionOptional.get().getId());
        } else {
            JobExecutionResource newJobExecutionResource = createReindexJob(sourceKey);
            return new ConceptSetReindexDTO(ConceptSetReindexStatus.CREATED, newJobExecutionResource.getExecutionId());
        }
    }

    public ConceptSetReindexDTO getIndexStatus(String sourceKey, Long executionId) {
        if (!conceptSetSearchService.isSearchAvailable()) {
            return new ConceptSetReindexDTO(ConceptSetReindexStatus.UNAVAILABLE);
        }
        String jobName = String.format(REINDEX_JOB_NAME, sourceKey);
        Optional<JobExecution> jobExecutionOptional = jobExplorer.findRunningJobExecutions(jobName).stream().findFirst();
        JobExecution jobExecution = jobExecutionOptional.orElseGet(() -> jobExplorer.getJobExecution(executionId));

        if (jobExecution != null) {
            ConceptSetReindexDTO conceptSetReindexDTO;
            if ("COMPLETED".equals(jobExecution.getStatus().name())) {
                conceptSetReindexDTO = new ConceptSetReindexDTO(ConceptSetReindexStatus.COMPLETED);
            } else if ("FAILED".equals(jobExecution.getStatus().name())) {
                conceptSetReindexDTO = new ConceptSetReindexDTO(ConceptSetReindexStatus.FAILED);
            } else {
                conceptSetReindexDTO = new ConceptSetReindexDTO(ConceptSetReindexStatus.RUNNING);
            }
            conceptSetReindexDTO.setExecutionId(jobExecution.getId());
            fillCounts(conceptSetReindexDTO, jobExecution);
            return conceptSetReindexDTO;
        } else {
            return new ConceptSetReindexDTO(ConceptSetReindexStatus.UNAVAILABLE);
        }
    }

    private JobExecutionResource createReindexJob(String sourceKey) {
        String jobName = String.format(REINDEX_JOB_NAME, sourceKey);
        final List<ConceptSet> conceptSets = conceptSetService.getConceptSetRepository().findAll();
        conceptSetSearchService.clearConceptSetIndex();
        Step step = stepBuilderFactory.get(jobName)
                .<ConceptDocuments, ConceptDocuments>chunk(1)
                .reader(new DocumentReader(sourceKey, conceptSets))
                .writer(new DocumentWriter())
                .listener(new JobStepExecutionListener())
                .listener(new JobChunkListener())
                .build();

        JobParametersBuilder parametersBuilder = new JobParametersBuilder();
        parametersBuilder.addString(JOB_NAME, String.format(REINDEX_JOB_NAME, sourceKey));
        parametersBuilder.addString(Constants.Params.SOURCE_KEY, sourceKey);
        parametersBuilder.addString(REINDEX_TOTAL_DOCUMENTS, String.valueOf(conceptSets.size()));

        Job reindexJob = jobBuilderFactory.get(jobName)
                .start(step)
                .build();

        return jobTemplate.launch(reindexJob, parametersBuilder.toJobParameters());
    }

    private void fillCounts(ConceptSetReindexDTO conceptSetReindexDTO, JobExecution jobExecution) {
        try {
            String jobTotalDocuments = jobExecution.getJobParameters().getString(REINDEX_TOTAL_DOCUMENTS);
            if (jobTotalDocuments != null) {
                conceptSetReindexDTO.setMaxCount(Integer.parseInt(jobTotalDocuments));
            }
            Object jobProcessedDocuments = jobExecution.getExecutionContext().get(REINDEX_PROCESSED_DOCUMENTS);
            if (jobProcessedDocuments != null) {
                conceptSetReindexDTO.setDoneCount((Integer) jobProcessedDocuments);
            } else {
                // If the job is still running we can get number of processed documents only from step parameters
                jobExecution.getStepExecutions().stream()
                        .filter(se -> se.getStepName().equals(jobExecution.getJobParameters().getString(JOB_NAME)))
                        .findFirst()
                        .ifPresent(se -> {
                            Object stepProcessedDocuments = se.getExecutionContext().get(REINDEX_PROCESSED_DOCUMENTS);
                            if (stepProcessedDocuments != null) {
                                conceptSetReindexDTO.setDoneCount((Integer) stepProcessedDocuments);
                            }
                        });
            }
        } catch (Exception e) {
            log.error("Failed to get count parameters for job with execution id = {}, {}", jobExecution.getId(), e);
        }
    }

    private static class ConceptDocuments {
        final Integer conceptSetId;

        final List<ConceptSetSearchDocument> documents;

        private ConceptDocuments() {
            this.conceptSetId = null;
            this.documents = Collections.emptyList();
        }

        private ConceptDocuments(Integer conceptSetId, List<ConceptSetSearchDocument> documents) {
            this.conceptSetId = conceptSetId;
            this.documents = documents;
        }

        public boolean hasDataToProcess() {
            return conceptSetId != null && documents != null && !documents.isEmpty();
        }
    }

    private class DocumentReader implements ItemReader<ConceptDocuments> {
        private final Iterator<ConceptSet> iterator;

        private final String sourceKey;

        public DocumentReader(String sourceKey, List<ConceptSet> conceptSets) {
            this.iterator = conceptSets.stream().iterator();
            this.sourceKey = sourceKey;
        }

        @Override
        public ConceptDocuments read() throws Exception {
            try {
                if (iterator.hasNext()) {
                    ConceptSet conceptSet = iterator.next();
                    final ConceptSetExpression csExpression;

                    try {
                        csExpression = conceptSetService.getConceptSetExpression(conceptSet.getId());
                    } catch (final ConceptNotExistException e) {
                        // data source does not contain required concepts, skip CS
                        return new ConceptDocuments();
                    }

                    final Collection<Concept> concepts = vocabService.executeMappedLookup(sourceKey, csExpression);

                    final List<ConceptSetSearchDocument> documents = concepts.stream().map(item -> {
                        final ConceptSetSearchDocument concept = new ConceptSetSearchDocument();
                        concept.setConceptSetId(conceptSet.getId());
                        concept.setConceptId(item.conceptId);
                        concept.setConceptName(item.conceptName);
                        concept.setConceptCode(item.conceptCode);
                        concept.setDomainName(item.domainId);
                        return concept;
                    }).collect(Collectors.toList());
                    return new ConceptDocuments(conceptSet.getId(), documents);
                } else {
                    return null;
                }
            } catch (Exception e) {
                log.error("Failed to get data for processing, {}", e);
                return new ConceptDocuments();
            }
        }
    }

    private class DocumentWriter implements ItemWriter<ConceptDocuments> {
        @Override
        public void write(List<? extends ConceptDocuments> list) throws Exception {
            list.stream()
                    .filter(ConceptDocuments::hasDataToProcess)
                    .forEach(cd -> conceptSetSearchService.reindexConceptSet(cd.conceptSetId, cd.documents));
        }
    }

    public class JobStepExecutionListener implements StepExecutionListener {
        @Override
        public void beforeStep(StepExecution stepExecution) {
        }

        @Override
        public ExitStatus afterStep(StepExecution stepExecution) {
            Object processedCount = stepExecution.getExecutionContext().get(REINDEX_PROCESSED_DOCUMENTS);
            if (processedCount != null) {
                if ((Integer) processedCount != 0) {
                    // Subtract 1 if the value is not equal to zero because "beforeChunk" method is called
                    // even if there's no element to process, so we get total number of processed documents plus one
                    stepExecution.getJobExecution().getExecutionContext()
                            .put(REINDEX_PROCESSED_DOCUMENTS, ((Integer) processedCount) - 1);
                }
            }
            return stepExecution.getExitStatus();
        }
    }

    public class JobChunkListener implements ChunkListener {
        private int counter = 0;

        @Override
        public void beforeChunk(ChunkContext context) {
            // Increment the number of processed documents before chunk because saving of step execution parameters
            // is made before "afterChunk" is called
            context.getStepContext().getStepExecution().getExecutionContext()
                    .put(REINDEX_PROCESSED_DOCUMENTS, ++counter);
        }

        @Override
        public void afterChunk(ChunkContext context) {
            // This method is called after saving of step parameters, so we can't use it
        }

        @Override
        public void afterChunkError(ChunkContext context) {
        }
    }
}