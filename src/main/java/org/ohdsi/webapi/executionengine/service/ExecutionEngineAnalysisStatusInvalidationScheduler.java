package org.ohdsi.webapi.executionengine.service;


import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


//Cannot use ConditionalOnProperty annotation here, because it checks only existence of the property, but not null/empty values
@ConditionalOnExpression(
        "!T(org.springframework.util.StringUtils).isEmpty('${execution.invalidation.maxage.hours:}') && " +
        "!T(org.springframework.util.StringUtils).isEmpty('${execution.invalidation.period:}')")
@Service
public class ExecutionEngineAnalysisStatusInvalidationScheduler {

    @Value("${execution.invalidation.maxage.hours}")
    private int invalidateHours;

    private ScriptExecutionService scriptExecutionService;

    @Autowired
    public ExecutionEngineAnalysisStatusInvalidationScheduler(ScriptExecutionService scriptExecutionService) {

        this.scriptExecutionService = scriptExecutionService;
    }

    @Scheduled(fixedDelayString = "${execution.invalidation.period}")
    public void invalidateExecutions() {

        Date invalidate = DateUtils.addHours(new Date(), -invalidateHours);
        scriptExecutionService.invalidateExecutions(invalidate);
    }

}
