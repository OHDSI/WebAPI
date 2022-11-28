package org.ohdsi.webapi.conceptset.search;

import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.exception.AtlasException;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.vocabulary.Concept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.SOURCE_KEY;

@Service
public class ConceptSetReindexTasklet implements Tasklet {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private ConceptSetSearchService conceptSetSearchService;

    private ConceptSetService conceptSetService;

    private VocabularyService vocabService;

    public ConceptSetReindexTasklet(ConceptSetSearchService conceptSetSearchService,
                                    ConceptSetService conceptSetService,
                                    VocabularyService vocabService) {
        this.conceptSetSearchService = conceptSetSearchService;
        this.conceptSetService = conceptSetService;
        this.vocabService = vocabService;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        try {
            fullIndex(chunkContext);
            stepContribution.setExitStatus(ExitStatus.COMPLETED);
        } catch (final Throwable ex) {
            log.error(ex.getMessage(), ex);
            stepContribution.setExitStatus(new ExitStatus(Constants.FAILED, ex.getMessage()));
            throw new AtlasException(ex);
        }
        return RepeatStatus.FINISHED;
    }

    private void fullIndex(ChunkContext chunkContext) {
        Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
        String sourceKey = jobParams.get(SOURCE_KEY).toString();

        final Collection<ConceptSetSearchDocument> documents = new ArrayList<>();

        final Iterable<ConceptSet> conceptSets = conceptSetService.getConceptSetRepository().findAll();

        conceptSets.forEach(cs -> {
            final ConceptSetExpression csExpression = conceptSetService.getConceptSetExpression(cs.getId());
            final Collection<Concept> concepts = vocabService.executeMappedLookup(sourceKey, csExpression);

            documents.addAll(concepts.stream().map(item -> {
                final ConceptSetSearchDocument concept = new ConceptSetSearchDocument();
                concept.setConceptSetId(cs.getId());
                concept.setConceptId(item.conceptId);
                concept.setConceptName(item.conceptName);
                concept.setConceptCode(item.conceptCode);
                concept.setDomainName(item.domainId);
                return concept;
            }).collect(Collectors.toList()));

            log.info("Concept set {} added to reindex", cs.getId());
        });

        log.info("Full concept sets reindex start");
        conceptSetSearchService.indexConceptSetsFull(documents);
        log.info("Full concept sets reindex finish");
    }
}