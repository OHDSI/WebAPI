package org.ohdsi.webapi.cdmresults;

import org.ohdsi.webapi.service.CDMResultsService;
import org.ohdsi.webapi.source.Source;
import org.springframework.batch.core.step.tasklet.Tasklet;

public abstract class BaseCDMResultsCacheTasklet implements Tasklet {

    protected final Source source;
    protected final CDMResultsService service;

    protected BaseCDMResultsCacheTasklet(Source source, CDMResultsService service) {
        this.source = source;
        this.service = service;
    }
}
