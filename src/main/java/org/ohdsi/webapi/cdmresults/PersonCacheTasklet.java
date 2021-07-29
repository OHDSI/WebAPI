package org.ohdsi.webapi.cdmresults;

import org.ohdsi.webapi.service.CDMResultsService;
import org.ohdsi.webapi.source.Source;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

public class PersonCacheTasklet extends BaseCDMResultsCacheTasklet {

    public PersonCacheTasklet(Source source, CDMResultsService service) {
        super(source, service);
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        service.getPerson(source.getSourceKey(), false);
        return RepeatStatus.FINISHED;
    }
}
