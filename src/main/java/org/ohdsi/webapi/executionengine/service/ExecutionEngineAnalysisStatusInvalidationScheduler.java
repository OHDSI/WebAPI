package org.ohdsi.webapi.executionengine.service;


import java.util.Date;
import javax.transaction.Transactional;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


//Cannot use ConditionalOnProperty annotation here, because it checks only existence of the property, but not null/empty values
@ConditionalOnExpression(
        "!T(org.springframework.util.StringUtils).isEmpty('${execution.invalidation.maxage:}') && " +
        "!T(org.springframework.util.StringUtils).isEmpty('${execution.invalidation.period:}')")
@Service
@Transactional
public class ExecutionEngineAnalysisStatusInvalidationScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionEngineAnalysisStatusInvalidationScheduler.class);

    @Value("${execution.invalidation.maxage}")
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
