package org.ohdsi.webapi.cdmresults;

import org.ohdsi.webapi.achilles.service.AchillesCacheService;
import org.ohdsi.webapi.source.Source;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class AchillesClearCacheTasklet implements Tasklet {
    private final Source source;
    private final AchillesCacheService cacheService;

    public AchillesClearCacheTasklet(Source source, AchillesCacheService cacheService) {
        this.source = source;
        this.cacheService = cacheService;
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) {
        cacheService.clearCache(this.source);
        return RepeatStatus.FINISHED;
    }
}
