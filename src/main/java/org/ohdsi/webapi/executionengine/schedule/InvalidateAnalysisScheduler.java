package org.ohdsi.webapi.executionengine.schedule;

import org.apache.commons.lang3.time.DateUtils;
import org.ohdsi.webapi.executionengine.entity.AnalysisExecution;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class InvalidateAnalysisScheduler {

  private final ScriptExecutionService executionService;

  @Value("${execution.invalidation.maxage}")
  private int invalidateHours;

  @Autowired
  public InvalidateAnalysisScheduler(ScriptExecutionService executionService) {

    this.executionService = executionService;
  }

  @Scheduled(fixedDelayString = "${execution.invalidation.period}")
  public void invalidateExecutions(){

    Date invalidate = DateUtils.addHours(new Date(), -invalidateHours);
    List<AnalysisExecution> executions = executionService.findOutdatedAnalyses(invalidate);
    executions.forEach(exec -> {
      executionService.updateAnalysisStatus(exec, AnalysisExecution.Status.FAILED);
    });
  }

}
