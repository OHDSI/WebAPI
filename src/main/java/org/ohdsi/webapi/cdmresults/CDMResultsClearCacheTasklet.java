package org.ohdsi.webapi.cdmresults;

import org.ohdsi.webapi.cdmresults.service.CDMCacheService;
import org.ohdsi.webapi.source.Source;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class CDMResultsClearCacheTasklet implements Tasklet {
    private final Source source;
    private final CDMCacheService cdmCacheService;

    public CDMResultsClearCacheTasklet(Source source, CDMCacheService cdmCacheService) {
        this.source = source;
        this.cdmCacheService = cdmCacheService;
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) {
        cdmCacheService.clearCache(this.source);
        return RepeatStatus.FINISHED;
    }
}